package me.xiaoge.prelog;

import org.activiti.engine.*;
import org.activiti.engine.event.EventLogEntry;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.task.Task;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.util.HashMap;
import java.util.List;

/**
 * Created by xiaoge on 2014/8/22.
 */
public class PreLogTest extends AbstractPreLogTest {

    @Test
    public void test1() {

        ProcessEngineImpl processEngine = (ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine();
        ProcessEngineConfiguration processEngineConfiguration = processEngine.getProcessEngineConfiguration();
        RhoEventLogger databaseEventLogger = new RhoEventLogger(processEngineConfiguration.getClock(), "d:\\rho-log.txt");
        runtimeService.addEventListener(databaseEventLogger);

        runtimeService.startProcessInstanceByKey("singleTaskProcess");
        List<Task> taskList = taskService.createTaskQuery().list();
        print(taskList.size());
        Assert.assertEquals(taskList.size(), 1);
        print(taskList);
        completeTaskList(taskList);

//        List<EventLogEntry> eventLogEntries = managementService.getEventLogEntries(null, null);
//        print(eventLogEntries.size());

//        EventLogEntry ev = eventLogEntries.get(0);
//        print(ev.getType());


//        Task t = taskList.get(0);

//        print(t.getName());
//        List<ProcessInstance> processInstanceList = runtimeService.createProcessInstanceQuery().list();
//        print(processInstanceList.size());
//        ProcessInstance processInstance = processInstanceList.get(0);
//        HashMap<String, Object> varMap = new HashMap<>();
//        varMap.put("chooice", 1);
//        taskService.complete(t.getId(), varMap);

//        taskList = taskService.createTaskQuery().list();
//        print(taskList.size());
//        Assert.assertNotEquals(taskList.size(), 0);
//        print(taskList);


    }

}
