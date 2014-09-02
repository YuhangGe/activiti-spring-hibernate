package me.xiaoge.prelog.autorun;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * Created by abraham on 14/8/27.
 */
public class RhoExpressionManager {

    private Stack<RhoExpressionHolder> expressionHolderStack;
    private List<RhoExpressionHolder> expressionHolderList;
    private int idx;

    public RhoExpressionManager() {
        expressionHolderList = new ArrayList<>();
        expressionHolderStack = new Stack<>();
        idx = 0;
    }

    public void addExpressionHolder(RhoExpressionHolder expressionHolder) {
        expressionHolderList.add(expressionHolder);
    }

    public void run() {
        idx = 0;
        //todo
    }

    public boolean isFinish() {
        //todo
        return true;
    }

}
