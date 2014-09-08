package me.xiaoge.prelog.autorun;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by abraham on 14/8/27.
 */
public class RhoExpressionManager {

    private List<RhoExpressionHolder> expressionHolderList;
    private boolean running = false;
    private int finishCount = -1;

    public RhoExpressionManager() {
        expressionHolderList = new ArrayList<>();
    }

    public void addExpressionHolder(RhoExpressionHolder expressionHolder) throws Exception {
        if(running) {
            throw new Exception("can not add holder while running.");
        }
        expressionHolderList.add(expressionHolder);
    }

    private void initHolder(int idx) {
        for(int i = expressionHolderList.size()-1;i>=idx;i--) {
            expressionHolderList.get(i).first();
        }
    }
    private void doRun() {
        int total_size = expressionHolderList.size();
        for(int i= total_size - 1;i>=0;i--) {
            RhoExpressionHolder holder = expressionHolderList.get(i);
            if(holder.hasNext()) {
                holder.next();
                initHolder(i+1);
                break;
            }
        }
        finishCount = 0;
        for(RhoExpressionHolder holder: expressionHolderList) {
            holder.loopReset();
            if(!holder.hasNext()) {
                finishCount++;
            }
        }
    }

    public void run() throws Exception {
        if(this.isFinish()) {
            return;
        }

        if(!running) {
            running = true;
            finishCount = 0;
            if(expressionHolderList.size() == 0) {
                return;
            }
            initHolder(0);
        } else {

            doRun();
        }
    }
    public void printTo(PrintStream ps) throws Exception {
        List<StringBuilder> stringList = new ArrayList<>();
        for(RhoExpressionHolder holder: expressionHolderList) {
            for(int i=0;i<holder.conditionList.size();i++) {
                if(stringList.size()<i+1) {
                    stringList.add(new StringBuilder());
                }
            }
        }
        for(RhoExpressionHolder holder: expressionHolderList) {
            int s = holder.conditionList.size();
            for (int i = 0; i < s; i++) {
                RhoExpressionCondition condition = holder.conditionList.get(i);
                StringBuilder sb = stringList.get(i);
                sb.append(condition.getValue()).append('\t');
            }
            if(s<stringList.size()) {
                for(int i=s;i<stringList.size();i++) {
                    StringBuilder sb = stringList.get(i);
                    sb.append("    \t");
                }
            }
        }
        for(StringBuilder sb : stringList) {
            ps.println(sb.toString());
        }
        ps.println("---------");
    }

    public void stop() {
        running = false;
        finishCount = -1;
    }

    public boolean isFinish() {
        return finishCount >= expressionHolderList.size();
    }

    public void reset() {
        this.stop();
    }

}
