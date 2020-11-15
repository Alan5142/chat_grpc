package com.chat.chat_server.data;

import java.util.Date;
import java.util.UUID;

public interface DatabaseObject {
    Date getCreationDate();
    UUID getId();
}
