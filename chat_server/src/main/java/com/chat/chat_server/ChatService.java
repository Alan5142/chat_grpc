package com.chat.chat_server;

import com.chat.grpc.ChatGrpc;
import com.chat.grpc.ChatServer;
import com.chat.grpc.Uuid;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@GRpcService
public class ChatService extends ChatGrpc.ChatImplBase {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void onNewMessage(Uuid.UUID request, StreamObserver<ChatServer.ChatMessage> responseObserver) {
    }

    @Override
    public void sendMessage(ChatServer.SendMessageRequest request, StreamObserver<ChatServer.ChatMessage> responseObserver) {
        responseObserver.onNext(ChatServer.ChatMessage.newBuilder().build());
        responseObserver.onCompleted();
    }
}
