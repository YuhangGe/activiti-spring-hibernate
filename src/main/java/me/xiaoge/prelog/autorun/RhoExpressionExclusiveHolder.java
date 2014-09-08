package me.xiaoge.prelog.autorun;

/**
 * Created by abraham on 14/8/27.
 */
public class RhoExpressionExclusiveHolder extends RhoExpressionHolder {

    private int idx;
    private int loopIdx;
    private int loopCount;
    public RhoExpressionExclusiveHolder() {
        super();
        idx = 0;
    }

    @Override
    public void first() {
        reset();
        next();
    }


    @Override
    public void reset() {
        idx = 0;
    }

    @Override
    public boolean hasNext() {
        return idx < conditionList.size();
    }

    @Override
    public void next() {
        if(!this.hasNext()) {
            return;
        }
        /**
         * 每次迭代，把其中的一个condition置为true，其它的置为false.
         */
        for(RhoExpressionCondition condition: conditionList) {
            condition.setValue(false);
        }
        conditionList.get(idx).setValue(true);
        loopIdx = idx;
        loopCount = 0;
        idx++;
    }

    @Override
    public void loop() {
        if(loopCount % conditionList.size() != 0) {
            loopCount++;
            return;
        }
        loopCount++;
        for(RhoExpressionCondition condition: conditionList) {
            condition.setLoopValue(false);
        }
        loopIdx++;
        if(loopIdx == conditionList.size()) {
            loopIdx = 0;
        }
        conditionList.get(loopIdx).setLoopValue(true);
        System.out.println("loop");

    }

}
