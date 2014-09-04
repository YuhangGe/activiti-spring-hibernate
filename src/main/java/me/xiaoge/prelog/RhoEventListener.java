package me.xiaoge.prelog;

import java.util.List;

/**
 * Created by abraham on 14/9/4.
 */
public interface RhoEventListener {
    public void onBeforeStoreLog(List<RhoEventLogEntity> rhoEventLogEntityList, RhoEventLogger rhoEventLogger);
}
