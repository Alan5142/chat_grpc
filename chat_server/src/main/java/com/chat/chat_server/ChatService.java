package com.chat.chat_server;

import com.chat.chat_server.data.User;
import com.chat.grpc.ChatGrpc;
import com.chat.grpc.ChatServer;
import com.chat.grpc.Uuid;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.UUID;

@GRpcService
public class ChatService extends ChatGrpc.ChatImplBase {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void onNewMessage(Uuid.UUID request, StreamObserver<ChatServer.ChatMessage> responseObserver) {
    }

    @Override
    @Transactional
    public void sendMessage(ChatServer.SendMessageRequest request, StreamObserver<ChatServer.ChatMessage> responseObserver) {
        responseObserver.onNext(ChatServer.ChatMessage.newBuilder().build());
        responseObserver.onCompleted();
    }
}
