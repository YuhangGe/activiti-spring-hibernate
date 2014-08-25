package me.xiaoge.prelog;

import java.util.List;

/**
 * Created by xiaoge on 2014/8/22.
 */
public interface RhoEventInternalDAO {

    RhoEventInternalEntity get(long id);
    void save(RhoEventInternalEntity rhoEventInternalEntity);
    void delete(RhoEventInternalEntity rhoEventInternalEntity);
    List<RhoEventInternalEntity> findAll();
    List<RhoEventInternalEntity> findByP(String processInstanceId, String[] preTaskList);
    void deleteByProcessInstanceId(String processInstanceId);
}
