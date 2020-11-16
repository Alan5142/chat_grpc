package com.chat.chat_server;

import com.chat.chat_server.data.MessageBase;
import com.chat.grpc.ChatServer;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

@Service
@ApplicationScope
public class ChatHandlerService {
    private HashMap<UUID, ArrayList<StreamObserver<ChatServer.ChatMessage>>> sessions = new HashMap<>();

    public ChatHandlerService() {
    }

    public void joinChat(UUID group, StreamObserver<ChatServer.ChatMessage> observer) {
        if (!sessions.containsKey(group)) {
            sessions.put(group, new ArrayList<>());
        }
        sessions.get(group).add(observer);
    }

    public void broadcastMessage(UUID groupId, MessageBase message) {
        ArrayList<StreamObserver<ChatServer.ChatMessage>> list = sessions.get(groupId);
        ArrayList<StreamObserver<ChatServer.ChatMessage>> toDelete = new ArrayList<>();
        list.forEach(s -> {
            try {
                ChatServer.ChatMessage m = message.toChatMessage();
                s.onNext(m);
            } catch (Exception e) {
                toDelete.add(s);
            }
        });

        list.removeAll(toDelete);
    }
}
