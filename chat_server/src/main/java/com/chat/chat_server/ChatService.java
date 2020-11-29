package com.chat.chat_server;

import com.chat.chat_server.data.*;
import com.chat.grpc.ChatGrpc;
import com.chat.grpc.ChatServer;
import com.chat.grpc.ChatServer.Group;
import com.chat.grpc.Uuid;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.UploadObjectArgs;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.UUID;
import java.util.stream.Collectors;

@GRpcService
public class ChatService extends ChatGrpc.ChatImplBase {

    private ChatHandlerService chatHandlerService;
    private MessageDBHandler messageDBHandler;

    @Autowired
    public ChatService(ChatHandlerService service, MessageDBHandler messageDBHandler) {
        this.chatHandlerService = service;
        this.messageDBHandler = messageDBHandler;
    }

    @PersistenceContext
    private EntityManager em;


    @Override
    public void joinChat(ChatServer.JoinChatRequest request, StreamObserver<ChatServer.ChatMessage> responseObserver) {
        UUID chatId = UUID.fromString(request.getId().getUuid());
        chatHandlerService.joinChat(chatId, responseObserver);
    }

    @Override
    public void createUser(ChatServer.RegisterUserRequest request, StreamObserver<ChatServer.User> responseObserver) {
        User user = messageDBHandler.findUserByAuth0Id(request.getAuth0Id());
        if (user == null) {
            user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setAuth0Id(request.getAuth0Id());
            messageDBHandler.create(user);
        }
        responseObserver.onNext(user.toGrpcUser());
        responseObserver.onCompleted();
    }

    @Override
    public void getUserChats(ChatServer.GetUserChatsRequest request, StreamObserver<ChatServer.GetUserChatsResponse> responseObserver) {
        User user = messageDBHandler.find(User.class, UUID.fromString(request.getUserId().getUuid()));
        if (user == null) {
            responseObserver.onError(new Exception("Error en el ID"));
            responseObserver.onCompleted();
            return;
        }

        ChatServer.GetUserChatsResponse response = ChatServer.GetUserChatsResponse.newBuilder()
                .addAllChats(user.getChats().stream().map(Chat::toGrpc).collect(Collectors.toList()))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void createChat(ChatServer.CreateChatRequest request, StreamObserver<Group> responseObserver) {
        Chat newChat;
        User creator = messageDBHandler
                .find(User.class, UUID.fromString(request.getUserId().getUuid()));
        switch (request.getChatType()) {
            case PairChat:
                newChat = new PairChat();
                break;
            case GroupChat:
                newChat = new GroupChat();
                break;
            default:
                responseObserver.onError(new Exception("No se bla bla"));
                responseObserver.onCompleted();
                return;
        }
        newChat.setName(request.getName());
        newChat.setAdmin(creator);
        newChat.getMembers().add(creator);
        creator.getChats().add(newChat);
        messageDBHandler.create(newChat);

        responseObserver.onNext(newChat.toGrpc());
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void sendMessage(ChatServer.SendMessageRequest request, StreamObserver<ChatServer.ChatMessage> responseObserver) {
        UUID chatId = UUID.fromString(request.getGroup().getUuid());
        UUID userId = UUID.fromString(request.getUserId().getUuid());
        Chat chat = this.messageDBHandler.find(Chat.class, chatId);
        User yuser = this.messageDBHandler.find(User.class, userId);

        if (chat != null) {
            MessageBase message;
            if (request.hasImageMessage()) {
                message = new ImageMessage();
                message.setContent(request.getImageMessage().getUrl());
            } else {
                message = new TextMessage();
                message.setContent(request.getTextMessage().getContent());
            }
            message.setBelongsTo(chat);
            message.setSender(yuser);
            message.setId(UUID.randomUUID());
            messageDBHandler.create(message);

            responseObserver.onNext(message.toChatMessage());
            chatHandlerService.broadcastMessage(chat.getId(), message);
        } else {
            responseObserver.onError(new Exception("Chat no encontrado"));
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getChat(ChatServer.GetChatRequest request, StreamObserver<Group> responseObserver) {
        UUID chatId = UUID.fromString(request.getId().getUuid());
        Chat chat = this.messageDBHandler.find(Chat.class, chatId);
        if (chat != null) {
            responseObserver.onNext(chat.toGrpc());
        } else {
            responseObserver.onError(new Exception("Chat no encontrado"));
        }
        responseObserver.onCompleted();
    }

    @Transactional
    @Override
    public void addToChat(ChatServer.AddToChatRequest request, StreamObserver<Empty> responseObserver) {
        User user = messageDBHandler.findUserByEmail(request.getUserToAddEmail());
        Chat chat = this.messageDBHandler.find(Chat.class, UUID.fromString(request.getChatId().getUuid()));
        try {
            if (!chat.getMembers().contains(user)) {
                chat.getMembers().add(user);
            }
            responseObserver.onNext(Empty.newBuilder().build());
        } catch (Exception e) {
            responseObserver.onError(e);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void uploadImage(ChatServer.UploadImageRequest request, StreamObserver<ChatServer.UploadImageResponse> responseObserver) {
        try {
            MinioClient minioClient =
                    MinioClient.builder()
                            .endpoint(System.getenv("MINIO_ENDPOINT"))
                            .credentials(System.getenv("MINIO_ACCESS_KEY"), System.getenv("MINIO_SECRET_KEY"))
                            .build();

            byte[] bytes = request.getImage().toByteArray();

            String name = String.format("%s%s", UUID.randomUUID().toString().replace("-", ""), request.getName());

            ObjectWriteResponse response = minioClient.putObject(PutObjectArgs.builder()
                    .contentType(URLConnection.guessContentTypeFromName(request.getName()))
                    .object(name)
                    .bucket("images")
                    .stream(new ByteArrayInputStream(bytes), bytes.length, 1024 * 1024 * 10)
                    .build());

            String url = String.format("%s/%s/%s", System.getenv("MINIO_ENDPOINT"), "images", name);

            responseObserver.onNext(ChatServer.UploadImageResponse.newBuilder()
                    .setUrl(url)
                    .build());
        } catch (Exception e) {
            responseObserver.onError(e);
        }
        responseObserver.onCompleted();
    }
}
