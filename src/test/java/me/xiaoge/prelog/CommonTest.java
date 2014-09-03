package me.xiaoge.prelog;

import me.xiaoge.prelog.autorun.RhoExpressionCondition;
import me.xiaoge.prelog.autorun.RhoExpressionExclusiveHolder;
import me.xiaoge.prelog.autorun.RhoExpressionHolder;
import me.xiaoge.prelog.autorun.RhoExpressionManager;
import org.junit.Test;

import java.util.Random;

/**
 * Created by xiaoge on 2014/8/24.
 */
public class CommonTest {
    protected static void print(int i) {
        System.out.println(i);
    }
    protected static void print(String s) {
        System.out.println(s);
    }

    @Test
    public void test() throws Exception {
        RhoExpressionHolder h1 = new RhoExpressionExclusiveHolder();
        RhoExpressionHolder h2 = new RhoExpressionExclusiveHolder();
        RhoExpressionHolder h3 = new RhoExpressionExclusiveHolder();

        h3.addCondition(new RhoExpressionCondition());
        h3.addCondition(new RhoExpressionCondition());
        h3.addCondition(new RhoExpressionCondition());
        h3.addCondition(new RhoExpressionCondition());

        h1.addCondition(new RhoExpressionCondition());
        h1.addCondition(new RhoExpressionCondition());

        h2.addCondition(new RhoExpressionCondition());
        h2.addCondition(new RhoExpressionCondition());
        h2.addCondition(new RhoExpressionCondition());

        RhoExpressionManager manager = new RhoExpressionManager();
        manager.addExpressionHolder(h3);
        manager.addExpressionHolder(h1);
        manager.addExpressionHolder(h2);

        int count = 0;
        while(count<1000 && !manager.isFinish()) {
            manager.run();
            manager.printTo(System.out);
            count++;
        }
        System.out.println("count:" + count);
    }
}
