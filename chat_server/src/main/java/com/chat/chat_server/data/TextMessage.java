package com.chat.chat_server.data;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Mensaje de tipo texto
 */
@Entity
public class TextMessage extends MessageBase {

    /**
     * Contenido del mensaje
     */
    @Column
    private String content = "";

    /**
     * Establece el contenido del mensaje
     * @param content contenido del mensaje
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Obtiene el contenido del mensaje
     * @return contenido del mensaje
     */
    @Override
    public String getContent() {
        return content;
    }
}
