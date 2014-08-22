package me.xiaoge.prelog;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by xiaoge on 2014/8/22.
 */
public class RhoEventLoggerDAOImpl implements RhoEventLoggerDAO {

    @Autowired
    private SessionFactory sessionFactory;

    public RhoEventLoggerEntity get(int id) {
        return (RhoEventLoggerEntity) sessionFactory.getCurrentSession().get(RhoEventLoggerEntity.class, id);
    }

    public void delete(RhoEventLoggerEntity event) {
        sessionFactory.getCurrentSession().delete(event);
    }

    @SuppressWarnings("unchecked")
    public List<RhoEventLoggerEntity> findAll() {
        return sessionFactory.getCurrentSession().createQuery(
                "FROM RhoEventLoggerEntity ORDER BY id")
                .list();
    }

    public void save(RhoEventLoggerEntity event) {
        sessionFactory.getCurrentSession().merge(event);

    }

    @Override
    public RhoEventLoggerEntity findByUserName(String username) {
        return (RhoEventLoggerEntity) sessionFactory.getCurrentSession().createQuery(
                "FROM RhoEventLoggerEntity u WHERE u.name = :username ")
                .setString("username", username).uniqueResult();
    }
}
