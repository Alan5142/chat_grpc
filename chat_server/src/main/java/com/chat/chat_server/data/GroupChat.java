package com.chat.chat_server.data;

import com.chat.grpc.ChatServer;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Chat grupal
 */
@Entity
@Table(name = "group_chats")
public class GroupChat extends Chat {
    /**
     * Cantidad máxima de usuarios en el chat
     */
    private final static int MAX_USERS = 500;

    @Override
    public int getUserLimit() {
        return MAX_USERS;
    }

    @Override
    protected ChatServer.Group.Builder toGrpcBuilder() {
        ChatServer.Group.Builder builder = super.toGrpcBuilder();
        builder.setGroupType(ChatServer.GroupType.GROUP);
        return builder;
    }
}
