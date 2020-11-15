package com.chat.chat_server.data;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class TextMessage extends MessageBase {
    @Column(nullable = false)
    private String content;
    @Override
    public String getContent() {
        return content;
    }
}
