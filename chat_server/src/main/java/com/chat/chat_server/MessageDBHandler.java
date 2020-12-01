package com.chat.chat_server;

import com.chat.chat_server.data.User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

/**
 * Maneja la conexión a la bd
 */
@Service
@ApplicationScope
public class MessageDBHandler {

    /**
     * Entity manager
     */
    @PersistenceContext
    private EntityManager em;


    // Poderosisima klass con k

    /***
     * Obtiene una entidad de tipo klass identificada por id
     * @param klass clase de la entidad
     * @param id id de la entidad
     * @param <T> Tipo de la entidad
     * @return entidad o null si no se encuentra
     */
    @Transactional
    public <T> T find(Class<T> klass, java.util.UUID id) {
        return em.find(klass, id);
    }

    /**
     * Obtiene un usuario a través de su id de auth0
     * @param id id del usuario de auth0
     * @return usuario si se encontró o null
     */
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

    /**
     * Obtiene un usuario a través de su email
     * @param email email del usuario
     * @return usuario si se encontró o null
     */
    @Transactional
    public User findUserByEmail(String email) {
        TypedQuery<User> queryByEmail = em.createNamedQuery(
                "User.findByEmail", User.class
        );
        queryByEmail.setParameter("email", email);
        try {
            return queryByEmail.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Crea una entidad en la base de datos
     * @param object entidad a crearse
     * @param <T> tipo de la entidad
     * @return true si se creo, false si no se creo
     */
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

    /**
     * Guarda una entidad en la base de datos
     * @param object entidad a crearse
     * @param <T> tipo de la entidad
     * @return true si se creo, false si no se creo
     */
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
