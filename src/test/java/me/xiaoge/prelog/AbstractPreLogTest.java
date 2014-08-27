package me.xiaoge.prelog;

import org.activiti.engine.ManagementService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.util.HashMap;
import java.util.List;

/**
 * Created by xiaoge on 2014/8/22.
 */
@ContextConfiguration("classpath:testContext.xml")
public class AbstractPreLogTest  extends AbstractJUnit4SpringContextTests {

    @Autowired
    RuntimeService runtimeService;
    @Autowired
    TaskService taskService;
    @Autowired
    ManagementService managementService;

    protected static void print(int i) {
        System.out.println(i);
    }
    protected static void print(String s) {
        System.out.println(s);
    }
    protected static void print(List<Task> taskList) {
        print("----- tasks -----");
        for (int i = 0; i < taskList.size(); i++) {
            print(taskList.get(i).getName());
        }
    }
    protected static void print(boolean v) {
        System.out.println(v);
    }
    protected void completeTaskList(List<Task> taskList) {
        for(Task t : taskList) {
            taskService.complete(t.getId());
        }
    }
    protected void completeTaskList(List<Task> taskList, List<HashMap<String, Object>> varMapList) {
        for (int i = 0; i < taskList.size(); i++) {
            taskService.complete(taskList.get(i).getId(), varMapList.get(i));
        }
    }

}
