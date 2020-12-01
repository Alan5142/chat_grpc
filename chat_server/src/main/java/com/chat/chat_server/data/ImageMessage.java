package com.chat.chat_server.data;

import com.chat.grpc.ChatServer;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Mensaje de tipo imagen
 */
@Entity
public class ImageMessage extends MessageBase {

    /**
     * Dirección de la imagen
     */
    @Column
    private String contentUrl = "";

    /**
     * Devuelve la dirección de la imagen
     * @return dirección de la imagen
     */
    @Override
    public String getContent() {
        return contentUrl;
    }

    /**
     * Establece la dirección que contiene la imagen
     * @param contentUrl nueva dirección
     */
    public void setContent(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    /**
     * Construye una representación gRPC de este mensaje
     * @return representación gRPC
     */
    public ChatServer.ImageMessage toGrpc() {
        return ChatServer.ImageMessage.newBuilder()
                .setUrl(contentUrl)
                .build();
    }
}
