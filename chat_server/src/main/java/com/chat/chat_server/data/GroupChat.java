package com.chat.chat_server.data;

import com.chat.grpc.ChatServer;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "group_chats")
public class GroupChat extends Chat {
    private final static int MAX_USERS = 500;

    boolean removeUser(User user) {
        return getMembers().remove(user);
    }

    boolean addUser(User user) {
        if (getMembers().size() >= MAX_USERS) return false;
        return getMembers().add(user);
    }

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
