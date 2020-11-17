package com.chat.chat_server.data;

import com.chat.grpc.ChatServer;
import com.chat.grpc.Uuid;
import com.google.protobuf.Timestamp;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "Messages")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "MessageType")
public abstract class MessageBase {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(updatable = false, nullable = false)
    private UUID id = UUID.randomUUID();

    @ManyToOne(targetEntity = Chat.class)
    private Chat belongsTo;

    @ManyToOne(targetEntity = Chat.class)
    private User sender;

    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date creationDate;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Chat getBelongsTo() {
        return belongsTo;
    }

    public void setBelongsTo(Chat belongsTo) {
        this.belongsTo = belongsTo;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public abstract String getContent();

    public ChatServer.ChatMessage toChatMessage() {
        ChatServer.ChatMessage.Builder builder = ChatServer.ChatMessage.newBuilder()
                .setGroup(Uuid.UUID.newBuilder().setUuid(belongsTo.getId().toString()).build())
                .setSender(sender.toGrpcUser())
                .setTime(Timestamp.newBuilder()
                        .setSeconds(creationDate.getTime())
                        .build());

        if (this instanceof ImageMessage) {
            builder.setImageMessage(ChatServer.ImageMessage.newBuilder().setUrl(getContent()).build());
        } else {
            builder.setTextMessage(ChatServer.TextMessage.newBuilder().setContent(getContent()).build());
        }

        return builder.build();
    }
}
