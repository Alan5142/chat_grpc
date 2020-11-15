package com.chat.chat_server.data;

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
}
