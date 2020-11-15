package com.chat.chat_server.data;

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
    private UUID id;

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
}
