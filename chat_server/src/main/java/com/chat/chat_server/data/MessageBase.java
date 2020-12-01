package com.chat.chat_server.data;

import com.chat.grpc.ChatServer;
import com.chat.grpc.Uuid;
import com.google.protobuf.Timestamp;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

/**
 * Clase abstracta de mensajes, de aquí se derivan los tipos de mensajes
 */
@Entity
@Table(name = "Messages")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "MessageType")
public abstract class MessageBase {

    /**
     * Identificador del chat
     */
    @Id
    @Column(updatable = false, nullable = false)
    private UUID id = UUID.randomUUID();

    /**
     * Chat al que pertenece el mensaje
     */
    @ManyToOne(targetEntity = Chat.class)
    private Chat belongsTo;

    /**
     * Quien envío el mensaje
     */
    @ManyToOne(targetEntity = User.class)
    private User sender;

    /**
     * Fecha de creación del mensaje
     */
    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date creationDate = new Date();

    /**
     * getter del id del mensaje
     * @return id del mensaje
     */
    public UUID getId() {
        return id;
    }

    /**
     * Establece el id del chat
     * @param id nuevo id del chat
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Establece el chat al que pertenece el mensaje
     * @param belongsTo establece a quien pertenece el mensaje
     */
    public void setBelongsTo(Chat belongsTo) {
        this.belongsTo = belongsTo;
    }

    /**
     * Obtiene quien envío el mensaje
     * @return usuario que envío el mensaje
     */
    public User getSender() {
        return sender;
    }

    /**
     * Establece el usuario que envío el mensaje
     * @param sender usuario que envío el mensaje
     */
    public void setSender(User sender) {
        this.sender = sender;
    }

    /**
     * Obtiene la fecha de creación
     * @return fecha de creación
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Establece la fecha de creación del proyecto
     * @param creationDate fecha de creación del proyecto
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Obtiene el contenido del mensaje
     * @return contenido del mensaje
     */
    public abstract String getContent();

    /**
     * Obtiene la representación gRPC del mensaje
     * @return representación gRPC del mensaje
     */
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

    public abstract void setContent(String content);
}
