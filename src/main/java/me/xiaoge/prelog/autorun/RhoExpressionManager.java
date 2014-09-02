package me.xiaoge.prelog.autorun;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * Created by abraham on 14/8/27.
 */
public class RhoExpressionManager {

    private List<RhoExpressionHolder> expressionHolderList;
    private boolean running = false;
    private int finishCount = 0;

    public RhoExpressionManager() {
        expressionHolderList = new ArrayList<>();
    }

    public void addExpressionHolder(RhoExpressionHolder expressionHolder) throws Exception {
        if(running) {
            throw new Exception("can not add holder while running.");
        }
        expressionHolderList.add(expressionHolder);
    }

    private void doRun() {
//        int finish_idx = -1;
//        for(int i=expressionHolderList.size()-1;i>=0;i--) {
//            if(!expressionHolderList.get(i).hasNext()) {
//                finish_idx = i;
//                break;
//            }
//        }
//
//
//        for(int i=finish_idx+1;i<expressionHolderList.size();i++) {
//            expressionHolderList.get(i).first();
//        }
//
//        for(int i=expressionHolderList.size()-1;i>=0;i--) {
//            RhoExpressionHolder holder = expressionHolderList.get(i);
//            if(!holder.hasNext()) {
//                continue;
//            }
//            holder.next();
//            break;
//        }
    }

    public void run() {
        if(!running) {
            running = true;
            finishCount = 0;
            for(RhoExpressionHolder holder : expressionHolderList) {
                holder.reset();
            }
        }
        doRun();
    }
    public void printTo(PrintStream ps) {
        List<StringBuilder> stringList = new ArrayList<>();
        for(RhoExpressionHolder holder: expressionHolderList) {
            for(int i=0;i<holder.conditionList.size();i++) {
//                RhoExpressionCondition condition = holder.conditionList.get(i);
                if(stringList.size()<i+1) {
                    stringList.add(new StringBuilder());
                }
//                StringBuilder sb = stringList.get(i);
//                sb.append(condition.getValue()).append('\t');
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
        finishCount = 0;
    }

    public boolean isFinish() {
        return finishCount == expressionHolderList.size();
    }

}
