package me.xiaoge.prelog.autorun;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by abraham on 14/8/27.
 */
public abstract class RhoExpressionHolder{

    protected List<RhoExpressionCondition> conditionList;

    public RhoExpressionHolder() {
        conditionList = new ArrayList<>();
    }

    public void addCondition(RhoExpressionCondition expressionCondition) {
        conditionList.add(expressionCondition);
    }

    public abstract void first();

    public abstract void reset();

    public abstract boolean hasNext();

    public abstract void next();

}
