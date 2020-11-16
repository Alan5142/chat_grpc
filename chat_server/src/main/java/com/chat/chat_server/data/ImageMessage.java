package com.chat.chat_server.data;

import com.chat.grpc.ChatServer;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class ImageMessage extends MessageBase {

    @Column(nullable = false)
    private String contentUrl;

    @Override
    public String getContent() {
        return contentUrl;
    }

    public ImageMessage(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public ChatServer.ImageMessage toGrpc() {
        return ChatServer.ImageMessage.newBuilder()
                .setUrl(contentUrl)
                .build();
    }
}
