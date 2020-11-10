package com.chat.chat_server;

import com.chat.grpc.ChatGrpc;
import com.chat.grpc.ChatServer;
import com.chat.grpc.Uuid;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

@GRpcService
public class ChatService extends ChatGrpc.ChatImplBase {

    @Override
    public void onNewMessage(Uuid.UUID request, StreamObserver<ChatServer.ChatMessage> responseObserver) {
        super.onNewMessage(request, responseObserver);
    }

    @Override
    public void sendMessage(ChatServer.SendMessageRequest request, StreamObserver<ChatServer.ChatMessage> responseObserver) {
        super.sendMessage(request, responseObserver);
    }
}
