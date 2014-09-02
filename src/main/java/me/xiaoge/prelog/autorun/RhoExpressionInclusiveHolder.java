package me.xiaoge.prelog.autorun;

/**
 * Created by abraham on 14/8/27.
 */
public class RhoExpressionInclusiveHolder extends RhoExpressionHolder {

    public RhoExpressionInclusiveHolder() throws Exception {
        throw new Exception("inclusive gateway is not support.");
    }

    @Override
    public void reset() {

    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public void next() {

    }

}
