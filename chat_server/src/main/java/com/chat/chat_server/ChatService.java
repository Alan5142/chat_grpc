package com.chat.chat_server;

import com.chat.chat_server.data.*;
import com.chat.grpc.ChatGrpc;
import com.chat.grpc.ChatServer;
import com.chat.grpc.Uuid;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.UUID;

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
    public void joinChat(Uuid.UUID request, StreamObserver<ChatServer.ChatMessage> responseObserver) {
        UUID chatId = UUID.fromString(request.getUuid());
        chatHandlerService.joinChat(chatId, responseObserver);
    }

    @Override
    public void getUserChats(ChatServer.GetUserChatsRequest request, StreamObserver<ChatServer.GetUserChatsRequest> responseObserver) {
        super.getUserChats(request, responseObserver);
    }

    @Override
    public void createChat(ChatServer.CreateChatRequest request, StreamObserver<ChatServer.Group> responseObserver) {
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
                return;
        }
        newChat.setAdmin(creator);
        newChat.getMembers().add(creator);
        messageDBHandler.create(newChat);

        responseObserver.onNext(newChat.toGrpc());
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
        } else {
            responseObserver.onError(new Exception("Chat no encontrado"));
        }
        responseObserver.onCompleted();
    }
}
