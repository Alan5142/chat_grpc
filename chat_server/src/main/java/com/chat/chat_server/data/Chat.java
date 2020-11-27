package com.chat.chat_server.data;

import com.chat.grpc.ChatServer;
import com.chat.grpc.Uuid;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "Chats")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "ChatType")
public abstract class Chat implements DatabaseObject {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = true)
    private String name;

    @ManyToOne(targetEntity = User.class)
    private User admin;

    @JoinTable(
            name = "chats_members",
            joinColumns = @JoinColumn(name="fk_chat_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "fk_user_id", nullable = false)
    )
    @ManyToMany(fetch = FetchType.EAGER, targetEntity = User.class)
    private List<User> members = new ArrayList<>();

    @OneToMany(targetEntity = MessageBase.class)
    @JoinColumn(name = "belongs_to_id")
    private List<MessageBase> messages = new ArrayList<>();


    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date creationDate = new Date();

    public Chat() {
        this.id = UUID.randomUUID();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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

    public void addMessage(MessageBase message) {
        this.messages.add(message);
    }

    public void setAdmin(User user) {
        admin = user;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getAdmin() {
        return admin;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    public void setMessages(List<MessageBase> messages) {
        this.messages = messages;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public ChatServer.Group toGrpc() {
        return toGrpcBuilder().build();
    }

    protected ChatServer.Group.Builder toGrpcBuilder() {
        ChatServer.Group.Builder groupGrpcBuilder = ChatServer.Group.newBuilder()
                .setId(Uuid.UUID.newBuilder().setUuid(getId().toString()).build())
                .setAdmin(admin.toGrpcUser())
                .setName(name);

        groupGrpcBuilder.addAllMembers(getMembers()
                .stream()
                .map(User::toGrpcUser)
                .collect(Collectors.toList()));
        groupGrpcBuilder.addAllMessages(getMessages()
                .stream()
                .map(MessageBase::toChatMessage)
                .collect(Collectors.toList()));
        return groupGrpcBuilder;
    }
}
