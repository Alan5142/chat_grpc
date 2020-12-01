package com.chat.chat_server;

import com.chat.chat_server.data.MessageBase;
import com.chat.grpc.ChatServer;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Maneja la comunicación en tiempo real en los chats
 */
@Service
@ApplicationScope
public class ChatHandlerService {
    /**
     * HashMap con asociación grupo -> usuarios que esperan nuevos mensajes
     */
    private HashMap<UUID, ArrayList<StreamObserver<ChatServer.ChatMessage>>> sessions = new HashMap<>();

    /**
     * Ctor vacío
     */
    public ChatHandlerService() {
    }

    /**
     * Une una conexión de un cliente a una entrada den la tabla hash, estableciendolo como "cliente en escucha"
     * @param group id del grupo
     * @param observer conexión Stream del usuario
     */
    public void joinChat(UUID group, StreamObserver<ChatServer.ChatMessage> observer) {
        if (!sessions.containsKey(group)) {
            sessions.put(group, new ArrayList<>());
        }
        sessions.get(group).add(observer);
    }

    /**
     * Envía un mensaje a todos los usuarios que esten esperando por un mensaje en un chat
     * @param groupId chat en el que se enviará el mensaje
     * @param message mensaje a enviar
     */
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
