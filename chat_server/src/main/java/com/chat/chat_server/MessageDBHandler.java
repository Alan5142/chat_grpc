package com.chat.chat_server;

import com.chat.chat_server.data.Chat;
import com.chat.chat_server.data.MessageBase;
import com.chat.chat_server.data.User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.context.annotation.SessionScope;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.List;

@Service
@ApplicationScope
public class MessageDBHandler {

    @PersistenceContext
    private EntityManager em;

    // Poderosisima klass con k
    @Transactional
    public <T> T find(Class<T> klass, java.util.UUID id) {
        return em.find(klass, id);
    }

    @Transactional
    public User findUserByAuth0Id(String id) {
        TypedQuery<User> queryByAuth0Id = em.createNamedQuery(
                "User.findByAuth0Id", User.class
        );
        queryByAuth0Id.setParameter("id", id);
        try {
            return queryByAuth0Id.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Transactional
    public <T> boolean create(T object) {
        try {
            em.merge(object);
            em.flush();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public <T> boolean save(T object) {
        try {
            em.merge(object);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}
