package me.xiaoge.prelog;

import org.activiti.engine.task.Task;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.util.List;

/**
 * Created by xiaoge on 2014/8/22.
 */
public class PreLogBeanTest  extends AbstractPreLogTest {

    @Test
    public void test() {
        runtimeService.startProcessInstanceByKey("autoTaskProcess");
//        List<Task> taskList = taskService.createTaskQuery().list();
//        print(taskList.size());
//        Assert.assertEquals(taskList.size(), 1);
//        print(taskList);
//        completeTaskList(taskList);
    }
}
