package me.xiaoge.prelog.autorun;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.Condition;

/**
 * Created by abraham on 14/8/27.
 */
public class RhoExpressionCondition implements Condition {

    private boolean value = false;
    private int loop = 0;

    @Override
    public boolean evaluate(DelegateExecution delegateExecution) {
//        System.out.println("evalute : " + this.value);
        if(this.loop == 0) {
            this.loop++;
        } else {
            holder.loop();
            this.loop++;
        }
        if(this.value) {
            holder.loopNext();
        }
        return this.value;
    }

    private RhoExpressionHolder holder;
    public RhoExpressionCondition(RhoExpressionHolder holder) {
        this.holder = holder;
    }
    public RhoExpressionCondition(RhoExpressionHolder holder, boolean value) {
        this(holder);
        this.value = value;
    }

    public void setValue(boolean value) {
        this.loop = 0;
        this.value = value;
    }

    public void setLoopValue(boolean value) {
        this.value = value;
    }

    public boolean getValue() throws Exception {

        return this.value;
    }
}
