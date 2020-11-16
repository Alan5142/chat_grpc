package com.chat.chat_server;

import com.chat.chat_server.data.*;
import com.chat.grpc.ChatGrpc;
import com.chat.grpc.ChatServer;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.UUID;

@GRpcService
public class ChatService extends ChatGrpc.ChatImplBase {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void onNewMessage(ChatServer.User request, StreamObserver<ChatServer.ChatMessage> responseObserver) {
        super.onNewMessage(request, responseObserver);
    }

    @Override
    public void createChat(ChatServer.User request, StreamObserver<ChatServer.Group> responseObserver) {
        super.createChat(request, responseObserver);
    }

    @Override
    @Transactional
    public void sendMessage(ChatServer.SendMessageRequest request, StreamObserver<ChatServer.ChatMessage> responseObserver) {

        UUID chatId = UUID.fromString(request.getGroup().getUuid());
        UUID userId = UUID.fromString(request.getUserId().getUuid());

        Chat chat = em.find(Chat.class, chatId);
        User user = em.find(User.class, userId);

        if (chat != null) {
            MessageBase message;
            if (request.hasImageMessage()) {
                message = new ImageMessage(request.getImageMessage().getUrl());
            } else {
                message = new TextMessage(request.getTextMessage().getContent());
            }

            message.setBelongsTo(chat);
            message.setCreationDate(new Date());
            message.setSender(user);
            message.setId(UUID.randomUUID());

            chat.getMessages().add(message);

            em.persist(chat);

            responseObserver.onNext(message.toChatMessage());
            // Send to all listening clients
        } else {
            responseObserver.onError(new Exception("Chat no encontrado"));
        }

        responseObserver.onCompleted();
    }
}
