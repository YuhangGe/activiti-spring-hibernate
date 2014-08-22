package me.xiaoge.prelog;

import java.util.List;

/**
 * Created by xiaoge on 2014/8/22.
 */
public interface RhoEventLoggerDAO {

    RhoEventLoggerEntity get(int id);
    void save(RhoEventLoggerEntity user);
    void delete(RhoEventLoggerEntity user);
    List<RhoEventLoggerEntity> findAll();
    RhoEventLoggerEntity findByUserName(String username);

}
