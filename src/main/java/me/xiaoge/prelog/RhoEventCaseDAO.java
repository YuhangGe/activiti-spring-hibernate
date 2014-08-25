package me.xiaoge.prelog;

/**
 * Created by xiaoge on 2014/8/25.
 */
public interface RhoEventCaseDAO {
    public RhoEventCaseEntity get(long id);
    public RhoEventCaseEntity getByProcessInstanceId(String processInstanceId);
    public void save(RhoEventCaseEntity rhoEventCaseEntity);
}
