package me.xiaoge.prelog.autorun;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.Condition;

/**
 * Created by abraham on 14/8/27.
 */
public class RhoExpressionCondition implements Condition {

    private boolean value = false;

    @Override
    public boolean evaluate(DelegateExecution delegateExecution) {
//        System.out.println("evalute : " + this.value);
        return this.value;
    }

    public RhoExpressionCondition() {

    }
    public RhoExpressionCondition(boolean value) {
        this.value = value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public boolean getValue() { return this.value; }
}
