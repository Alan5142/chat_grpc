package com.chat.chat_server.data;

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
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToMany(targetEntity = Chat.class)
    private List<Chat> memberOf = new ArrayList<>();

    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date creationDate;

    @OneToMany(targetEntity = Chat.class)
    private List<Chat> adminOf = new ArrayList<>();

    @OneToMany(targetEntity = MessageBase.class)
    private List<MessageBase> messages = new ArrayList<>();

    public User() {
        this.creationDate = new Date();
    }

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
}
