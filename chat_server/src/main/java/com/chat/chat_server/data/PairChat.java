package com.chat.chat_server.data;

import com.chat.grpc.ChatServer;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Clase que representa un chat entre dos personas,
 * solo puede tener dos personas y el nombre no es útil en este
 * chat
 */
@Entity
@Table(name = "pair_chats")
public class PairChat extends Chat {
    /**
     * Cantidad máxima de usuarios
     */
    public final static int MAX_USERS = 2;

    /**
     * Obtiene la cantidad máxima de usuarios
     * @return cantidad máxima de usuarios
     */
    @Override
    public int getUserLimit() {
        return MAX_USERS;
    }

    /**
     * Obtiene el builder gRPC del mensaje
     * @return builder para la representación gRPC del mensaje
     */
    @Override
    protected ChatServer.Group.Builder toGrpcBuilder() {
        ChatServer.Group.Builder builder = super.toGrpcBuilder();
        builder.setGroupType(ChatServer.GroupType.PAIR);
        return builder;
    }
}
