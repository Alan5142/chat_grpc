package com.chat.chat_server;

import com.chat.chat_server.data.*;
import com.chat.grpc.ChatGrpc;
import com.chat.grpc.ChatServer;
import com.chat.grpc.ChatServer.Group;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.net.URLConnection;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio que gestiona las peticiones gRPC
 */
@GRpcService
public class ChatService extends ChatGrpc.ChatImplBase {

    /**
     * Gestor de comunicación en tiempo real de los chats
     */
    private ChatHandlerService chatHandlerService;

    /**
     * Gestor de la bd
     */
    private MessageDBHandler messageDBHandler;

    /**
     * Ctor chatservice
     * @param service servicio de comunicación en tiempo real
     * @param messageDBHandler gestor de la bd
     */
    @Autowired
    public ChatService(ChatHandlerService service, MessageDBHandler messageDBHandler) {
        this.chatHandlerService = service;
        this.messageDBHandler = messageDBHandler;
    }


    /**
     * Agrega la conexión responseObserver a la lista de clientes en espera de un chat
     * @param request datos de la petición (id del chat)
     * @param responseObserver stream de mensajes
     */
    @Override
    public void joinChat(ChatServer.JoinChatRequest request, StreamObserver<ChatServer.ChatMessage> responseObserver) {
        UUID chatId = UUID.fromString(request.getId().getUuid());
        chatHandlerService.joinChat(chatId, responseObserver);
    }

    /**
     * Crea un usuario nuevo en la bd
     * @param request datos de la petición (email, nombre, id auth0)
     * @param responseObserver canal para envíar la response
     */
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

    /**
     * Obtiene la lista de chats de un usuario
     * @param request id del usuario
     * @param responseObserver canal para envíar la lista de chats
     */
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

    /**
     * Crea un chat nuevo
     * @param request información del nuevo chat
     * @param responseObserver canal para envíar la información del nuevo chat
     */
    @Override
    public void createChat(ChatServer.CreateChatRequest request, StreamObserver<Group> responseObserver) {
        try {

            Chat newChat;
            User creator = messageDBHandler
                    .find(User.class, UUID.fromString(request.getUserId().getUuid()));
            switch (request.getChatType()) {
                case PairChat:
                    newChat = new PairChat();
                    User partner = messageDBHandler.findUserByEmail(request.getPartnerEmail());
                    if (partner == null) {
                        throw new Exception("No se pudo crear");
                    }
                    newChat.getMembers().add(partner);
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
            newChat.getMembers().add(creator);
            creator.getChats().add(newChat);
            messageDBHandler.create(newChat);

            responseObserver.onNext(newChat.toGrpc());
        } catch (Exception e) {
            responseObserver.onError(e);
        }
        responseObserver.onCompleted();
    }

    /**
     * Envía un mensaje en un chat y hace un broadcast a todos los usuarios que escuchen en ese chat
     * @param request información del nuevo mensaje (contenido, quien lo envío, id del chat)
     * @param responseObserver canal por el que se enviará la información del nuevo mensaje
     */
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

    /**
     * Obtiene la información de un chat
     * @param request información de la petición (id del chat)
     * @param responseObserver canal para envíar la información del chat
     */
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

    /**
     * Agrega un usuario a un chat
     * @param request email del usuario a agregar
     * @param responseObserver canal para enviar la response (empty o error)
     */
    @Transactional
    @Override
    public void addToChat(ChatServer.AddToChatRequest request, StreamObserver<Empty> responseObserver) {
        User user = messageDBHandler.findUserByEmail(request.getUserToAddEmail());
        Chat chat = this.messageDBHandler.find(Chat.class, UUID.fromString(request.getChatId().getUuid()));
        try {
            if (user == null) {
                throw new Exception("Usuario no valido");
            }
            if (chat.getMembers().size() >= chat.getUserLimit()) {
                throw new Exception("XD");
            }
            if (!chat.addUser(user)) {
                throw new Exception("XD");
            }
            responseObserver.onNext(Empty.newBuilder().build());
        } catch (Exception e) {
                responseObserver.onError(e);
        }
        responseObserver.onCompleted();
    }

    /**
     * Sube una imagen a MinIO
     * @param request información de la imagen a subir
     * @param responseObserver canal para envíar la URL del mensaje
     */
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
