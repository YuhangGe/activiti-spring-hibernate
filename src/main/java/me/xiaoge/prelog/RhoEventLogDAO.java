package me.xiaoge.prelog;

import java.util.List;

/**
 * Created by xiaoge on 2014/8/25.
 */
public interface RhoEventLogDAO {
    public RhoEventLogEntity get(long id);
    public List<RhoEventLogEntity> findAll();
    public List<RhoEventLogEntity> findByCaseId(long caseId);
    public void save(RhoEventLogEntity rhoEventLogEntity);
    public void delete(RhoEventLogEntity rhoEventLogEntity);

}
