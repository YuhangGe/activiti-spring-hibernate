package me.xiaoge.prelog;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by xiaoge on 2014/8/25.
 */
public class RhoEventCaseDAOImpl implements RhoEventCaseDAO {
    @Autowired
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public RhoEventCaseDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    @Override
    public RhoEventCaseEntity get(long id) {
        return (RhoEventCaseEntity) sessionFactory.getCurrentSession().get(RhoEventCaseEntity.class, id);
    }

    @Override
    public RhoEventCaseEntity getByProcessInstanceId(String processInstanceId) {
        return (RhoEventCaseEntity) sessionFactory.getCurrentSession().createQuery("FROM RhoEventCaseEntity WHERE process_instance_id = :pid").setString("pid", processInstanceId).uniqueResult();
    }

    @Override
    public void save(RhoEventCaseEntity rhoEventCaseEntity) {
        sessionFactory.getCurrentSession().save(rhoEventCaseEntity);
    }
}
