package com.chat.chat_server.data;

import com.chat.grpc.ChatServer;
import com.chat.grpc.Uuid;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "ChatUsers")
public class User implements DatabaseObject {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(updatable = false, nullable = false)
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private String name = "";

    @ManyToMany(targetEntity = Chat.class)
    private List<Chat> memberOf = new ArrayList<>();

    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date creationDate = new Date();

    @OneToMany(targetEntity = Chat.class)
    private List<Chat> adminOf = new ArrayList<>();

    @OneToMany(targetEntity = MessageBase.class)
    private List<MessageBase> messages = new ArrayList<>();

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public List<Chat> getChats() {
        return memberOf;
    }

    public ChatServer.User toGrpcUser() {
        return ChatServer.User.newBuilder()
                .setId(Uuid.UUID.newBuilder().setUuid(id.toString()).build())
                .setName(name)
                .build();
    }
}
