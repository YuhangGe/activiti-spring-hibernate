package me.xiaoge.prelog.autorun;

/**
 * Created by abraham on 14/8/27.
 */
public class RhoExpressionExclusiveHolder extends RhoExpressionHolder {

    private int idx;

    public RhoExpressionExclusiveHolder() {
        super();
        idx = 0;
    }


    @Override
    public void reset() {
        idx = 0;
    }

    @Override
    public boolean hasNext() {
        return idx != conditionList.size() - 1;
    }

    @Override
    public void next() {
        if(idx>=conditionList.size()-1) {
            return;
        }
        /**
         * 每次迭代，把其中的一个condition置为true，其它的置为false.
         */
        for(RhoExpressionCondition condition: conditionList) {
            condition.setValue(false);
        }
        conditionList.get(idx).setValue(true);
        idx++;
    }

}
