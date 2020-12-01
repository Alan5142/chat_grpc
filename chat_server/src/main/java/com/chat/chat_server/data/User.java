package com.chat.chat_server.data;

import com.chat.grpc.ChatServer;
import com.chat.grpc.Uuid;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;

/**
 * Representa un usuario en la BD, las contraseñas son gestionadas de manera externa
 */
@Entity
@Table(name = "ChatUsers")
@NamedQueries({
        @NamedQuery(name = "User.findByAuth0Id",
                query = "select a from User a where a.auth0Id=:id"),
        @NamedQuery(name = "User.findByEmail",
                query = "select u from User u where u.email=:email")
})
public class User implements DatabaseObject {

    /**
     * Id del usuario
     */
    @Id
    @Column(updatable = false, nullable = false)
    private UUID id = UUID.randomUUID();

    /**
     * Email del usuario
     */
    @Column(nullable = false)
    private String email = "";

    /**
     * Nombre del usuario
     */
    @Column(nullable = false)
    private String name = "";

    /**
     * ID de auth0
     */
    @Column(nullable = false)
    private String auth0Id = "";

    /**
     * Lista con los grupos a los que pertenece
     */
    @JoinTable(
            name = "chats_members",
            joinColumns = @JoinColumn(name = "fk_user_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "fk_chat_id", nullable = false)
    )
    @ManyToMany(fetch = FetchType.EAGER, targetEntity = Chat.class)
    private List<Chat> memberOf = new ArrayList<>();

    /**
     * Fecha de creación del usuario
     */
    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date creationDate = new Date();

    /**
     * Lista con los mensajes envíados por este usuario
     */
    @OneToMany(targetEntity = MessageBase.class)
    private List<MessageBase> messages = new ArrayList<>();

    /**
     * Obtiene la fecha de creación del usuario
     * @return fecha de creación
     */
    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Obtiene el id del usuario
     * @return id del usuario
     */
    @Override
    public UUID getId() {
        return id;
    }

    /**
     * Obtiene el nombre del usuario
     * @return nombre del usuario
     */
    public String getName() {
        return name;
    }

    /**
     * Establece el nombre del usuario
     * @param name nombre del usuario
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Obtiene la lista de mensajes que ha hecho el usuario
     * @return lista de mensajes del usuario
     */
    public List<MessageBase> getMessages() {
        return messages;
    }

    /**
     * Establece el email del usuario
     * @param email email del usuario
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtine el email del usuario
     * @return email del usuario
     */
    public String getEmail() {
        return email;
    }

    /**
     * Establece el id del usuario
     * @param id id del usuario
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Obtiene el id de auth0 del usuario
     * @return id de auth0 del usuario
     */
    public String getAuth0Id() {
        return auth0Id;
    }

    /**
     * Establece el id de auth0 del usuario
     * @param auth0Id id de auth0 del usuario
     */
    public void setAuth0Id(String auth0Id) {
        this.auth0Id = auth0Id;
    }

    /**
     * Obtiene los chats a los que pertenece el usuario
     * @return Lista de chats a los que pertenece el usuario
     */
    public List<Chat> getChats() {
        return memberOf;
    }

    /**
     * Devuelve la representación gRPC del usuario
     * @return representación gRPC
     */
    public ChatServer.User toGrpcUser() {
        return ChatServer.User.newBuilder()
                .setId(Uuid.UUID.newBuilder().setUuid(id.toString()).build())
                .setName(name)
                .setEmail(email)
                .build();
    }
}
