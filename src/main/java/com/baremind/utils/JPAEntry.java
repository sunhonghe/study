package com.baremind.utils;

import com.baremind.data.Account;
import com.baremind.data.Scheduler;

import javax.persistence.*;

import org.junit.Test;

import java.util.*;

/**
 * Created by fixopen on 18/8/15.
 */
public class JPAEntry {
    public interface TouchFunction {
        void touch(Account a);
    }

    private static final String PERSISTENCE_UNIT_NAME = "supportData";
    private static EntityManagerFactory factory;
    private static EntityManager entityManager;


    
    
    
    
    public static EntityManager getEntityManager() {
        if (entityManager == null) {
            try {
                factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
                entityManager = factory.createEntityManager();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return entityManager;
    }

    public static <T> T getObject(Class<T> type, String fieldName, Object fieldValue) {
        T result = null;
        EntityManager em = getEntityManager();
        String jpql = "SELECT a FROM " + type.getSimpleName() + " a WHERE a." + fieldName + " = :variable";
        try {
            result = em.createQuery(jpql, type)
                .setParameter("variable", fieldValue)
                .getSingleResult();
        } catch (NoResultException e) {
            //do noting
        } catch (NonUniqueResultException e) {
            List<T> t = em.createQuery(jpql, type)
                    .setParameter("variable", fieldValue)
                    .getResultList();
            result = t.get(0);
        }
        return result;
    }

    public static <T> List<T> getList(Class<T> type, String fieldName, Object fieldValue) {
        HashMap<String, Object> condition = new HashMap<>(1);
        condition.put(fieldName, fieldValue);
        return getList(type, condition);
    }

    public static <T> List<T> getList(Class<T> type, Map<String, Object> conditions) {
        String jpql = "SELECT o FROM " + type.getSimpleName() + " o WHERE 1 = 1";
        if (conditions != null) {
            for (Map.Entry<String, Object> item : conditions.entrySet()) {
                jpql += " AND o." + item.getKey() + " = :" + item.getKey();
            }
        }
        EntityManager em = getEntityManager();
        TypedQuery<T> q = em.createQuery(jpql, type);
        if (conditions != null) {
            //conditions.forEach((key, value) -> {
            //    q.setParameter(key, value);
            //});
            for (Map.Entry<String, Object> item : conditions.entrySet()) {
                q.setParameter(item.getKey(), item.getValue());
            }
        }
        return q.getResultList();
    }

    public static Account getAccount(String sessionId) {
        Account result = null;
        List<Account> accounts = getList(Account.class, "sessionId", sessionId);
        int count = accounts.size();
        switch (count) {
            case 1: //ok
                result = accounts.get(0);
                break;
        }
        return result;
    }

    public static boolean isLogining(Account a) {
        return isLogining(a, (Account account) -> {
        });
    }

    public static boolean isLogining(Account account, TouchFunction touchFunction) {
        boolean result = false;
        if (account.getActive() == 1) {
            Date now = new Date();
            Date lastOperationTime = account.getLastOpereationTime();
            if (now.getTime() - lastOperationTime.getTime() < 30 * 60 * 1000) {
                EntityManager em = getEntityManager();
                em.getTransaction().begin();
                account.setLastOpereationTime(now);
                touchFunction.touch(account);
                em.merge(account);
                em.getTransaction().commit();
                result = true;
            }
        }
        return result;
    }

    public static boolean isLogining(String sessionId) {
		return true;
    	
        //return isLogining(sessionId, (Account a) -> {
        //});
    }

    public static boolean isLogining(String sessionId, TouchFunction touchFunction) {
        boolean result = false;
        Account account = getAccount(sessionId);
        if (account != null) {
            result = isLogining(account, touchFunction);
        }
        return result;
    }

    public static boolean hasKickOtherPermission(String sessionId) {
        return false;
    }
}
