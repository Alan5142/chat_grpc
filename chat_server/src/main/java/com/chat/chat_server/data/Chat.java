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

/**
 * Representa un chat en la base de datos, hay
 */
@Entity
@Table(name = "Chats")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "ChatType")
public abstract class Chat implements DatabaseObject {

    /**
     * Identificador del chat
     */
    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * Nombre del chat, no se utiliza en un chat de pares
     */
    @Column(nullable = true)
    private String name;

    /**
     * Lista de todos los usuarios del chat
     */
    @JoinTable(
            name = "chats_members",
            joinColumns = @JoinColumn(name="fk_chat_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "fk_user_id", nullable = false)
    )
    @ManyToMany(fetch = FetchType.EAGER, targetEntity = User.class)
    private List<User> members = new ArrayList<>();

    /**
     * Lista de mensajes del chat
     */
    @OneToMany(targetEntity = MessageBase.class)
    @JoinColumn(name = "belongs_to_id")
    private List<MessageBase> messages = new ArrayList<>();

    /**
     * Fecha de creación del chat
     */
    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date creationDate = new Date();

    /**
     * Constructor del chat, inicializa el campo id
     */
    public Chat() {
        this.id = UUID.randomUUID();
    }

    /**
     * Establece el nombre del chat
     * @param name nombre del chat
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Obtiene el nombre del chat
     * @return nombre del chat
     */
    public String getName() {
        return name;
    }

    /**
     * Obtiene la fecha de creación del chat
     * @return fecha de creación del chat
     */
    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Obtien el id del chat
     * @return id del chat
     */
    @Override
    public UUID getId() {
        return id;
    }

    /**
     * Obtiene la cantidad máxima de usuarios del chat
     * @return cantidad máxima de usuarios
     */
    public abstract int getUserLimit();

    /**
     * Obtiene los miembros del chat
     * @return miembros del chat
     */
    public List<User> getMembers() {
        return members;
    }

    /**
     * Obtiene los mensajes del chat
     * @return mensajes del chat
     */
    public List<MessageBase> getMessages() {
        return messages;
    }

    /**
     * Añade un mensaje al chat
     * @param message mensaje a añadir
     */
    public void addMessage(MessageBase message) {
        this.messages.add(message);
    }

    /**
     * Establece el id del chat
     * @param id nuevo id
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Añade un usuario al chat
     * @param user usuario a añadir
     * @return true si
     */
    public boolean addUser(User user) {
        if (getMembers().contains(user) || getMembers().size() >= getUserLimit()) return false;
        return getMembers().add(user);
    }

    /**
     * Convierte el chat a la representación para gRPC
     * @return chat gRPC
     */
    public ChatServer.Group toGrpc() {
        return toGrpcBuilder().build();
    }

    /**
     * Builder para el chat gRPC, pensado para ser sobreescrito por las clases hijas para que añadan características
     * @return builder gRPC
     */
    protected ChatServer.Group.Builder toGrpcBuilder() {
        ChatServer.Group.Builder groupGrpcBuilder = ChatServer.Group.newBuilder()
                .setId(Uuid.UUID.newBuilder().setUuid(getId().toString()).build())
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
