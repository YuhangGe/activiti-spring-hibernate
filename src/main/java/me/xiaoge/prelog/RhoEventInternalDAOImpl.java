package me.xiaoge.prelog;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by xiaoge on 2014/8/22.
 */
public class RhoEventInternalDAOImpl implements RhoEventInternalDAO {

    public RhoEventInternalDAOImpl() {

    }
    public RhoEventInternalDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Autowired
    private SessionFactory sessionFactory;

    public RhoEventInternalEntity get(long id) {
        return (RhoEventInternalEntity) sessionFactory.getCurrentSession().get(RhoEventInternalEntity.class, id);
    }

    public void delete(RhoEventInternalEntity event) {
        sessionFactory.getCurrentSession().delete(event);
    }

    @SuppressWarnings("unchecked")
    public List<RhoEventInternalEntity> findAll() {
        return sessionFactory.getCurrentSession().createQuery(
                "FROM RhoEventInternalEntity ORDER BY id")
                .list();
    }

    @Override
    public List<RhoEventInternalEntity> findByP(String processInstanceId, String[] preTaskList) {
        return sessionFactory.getCurrentSession().createQuery("from RhoEventInternalEntity where process_instance_id = :pid and task_def_id in(:tid)")
                .setString("pid", processInstanceId).setParameterList("tid", preTaskList).list();
    }

    @Override
    public void deleteByProcessInstanceId(String processInstanceId) {
        sessionFactory.getCurrentSession().createQuery("delete RhoEventInternalEntity where process_instance_id = :pid")
                .setString("pid", processInstanceId).executeUpdate();
    }

    public void save(RhoEventInternalEntity event) {
        sessionFactory.getCurrentSession().save(event);
    }

}
