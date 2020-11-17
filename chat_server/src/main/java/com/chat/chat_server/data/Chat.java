package com.chat.chat_server.data;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "Chats")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "ChatType")
public abstract class Chat implements DatabaseObject {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(targetEntity = User.class)
    private User admin;

    @ManyToMany(targetEntity = User.class)
    private List<User> members = new ArrayList<>();

    @ManyToOne(targetEntity = MessageBase.class)
    private List<MessageBase> messages = new ArrayList<>();

    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date creationDate;

    public Chat() {
        this.id = UUID.randomUUID();
    }

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public abstract int getUserLimit();

    public List<User> getMembers() {
        return members;
    }

    public List<MessageBase> getMessages() {
        return messages;
    }
}
