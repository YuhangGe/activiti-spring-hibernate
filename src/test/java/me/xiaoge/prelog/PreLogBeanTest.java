package me.xiaoge.prelog;

import org.junit.Test;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by xiaoge on 2014/8/22.
 */
public class PreLogBeanTest  extends AbstractPreLogTest {

    @Test
    public void test() {
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            int c = random.nextInt(2) + 1;
            HashMap<String, Object> varMap = new HashMap<>();
            varMap.put("chooice", c);
            runtimeService.startProcessInstanceByKey("autoTaskProcess", varMap);
        }


    }
}
