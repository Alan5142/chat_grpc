package com.chat.chat_server.data;

import com.chat.grpc.ChatServer;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "pair_chats")
public class PairChat extends Chat {
    public final static int MAX_USERS = 2;

    @Override
    public int getUserLimit() {
        return MAX_USERS;
    }

    @Override
    public ChatServer.Group toGrpc() {
        return ChatServer.Group.newBuilder()
                .build();
    }
}
