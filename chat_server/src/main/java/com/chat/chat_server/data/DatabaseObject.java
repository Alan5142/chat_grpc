package com.chat.chat_server.data;

import java.util.Date;
import java.util.UUID;

/**
 * Interfaz que representa un objeto en la BD
 */
public interface DatabaseObject {
    /**
     * Fecha de creación de la entidad
     * @return fecha de creación
     */
    Date getCreationDate();

    /**
     * Obtiene el id de la entidad
     * @return id de la entidad
     */
    UUID getId();
}
