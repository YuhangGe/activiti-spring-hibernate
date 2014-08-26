package me.xiaoge.prelog;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by xiaoge on 2014/8/25.
 */
public class RhoEventLogDAOImpl implements RhoEventLogDAO {
    @Autowired
    private SessionFactory sessionFactory;

    public RhoEventLogDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public RhoEventLogEntity get(long id) {
        return (RhoEventLogEntity) sessionFactory.getCurrentSession().get(RhoEventLogEntity.class, id);
    }

    @Override
    public List<RhoEventLogEntity> findAll() {
        return sessionFactory.getCurrentSession().createQuery(
                "FROM RhoEventLogEntity")
                .list();
    }

    @Override
    public List<RhoEventLogEntity> findByCaseId(long caseId) {
        return sessionFactory.getCurrentSession().createQuery(
                "FROM RhoEventLogEntity WHERE case_id = :cid").setLong("cid", caseId)
                .list();
    }

    @Override
    public void save(RhoEventLogEntity rhoEventLogEntity) {
        sessionFactory.getCurrentSession().save(rhoEventLogEntity);
    }

    @Override
    public void delete(RhoEventLogEntity rhoEventLogEntity) {
        sessionFactory.getCurrentSession().delete(rhoEventLogEntity);
    }
}
