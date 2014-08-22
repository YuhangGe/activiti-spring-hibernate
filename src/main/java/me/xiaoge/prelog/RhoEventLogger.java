package me.xiaoge.prelog;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.*;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.event.logger.DatabaseEventFlusher;
import org.activiti.engine.impl.event.logger.EventFlusher;
import org.activiti.engine.impl.event.logger.handler.EventLoggerEventHandler;
import org.activiti.engine.impl.event.logger.handler.ProcessInstanceEndedEventHandler;
import org.activiti.engine.impl.event.logger.handler.ProcessInstanceStartedEventHandler;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandContextCloseListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.runtime.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Joram Barrez
 */
public class RhoEventLogger implements ActivitiEventListener {


    private static void println(String msg) {
        System.out.println(msg);
    }

    private static void println(int n) {
        System.out.println(n);
    }

    private static final Logger logger = LoggerFactory.getLogger(RhoEventLogger.class);

    protected Clock clock;
    protected ObjectMapper objectMapper;
    protected String logFilePath;

    // Listeners for new events
    protected List<RhoEventLoggerListener> listeners;


    private static RepositoryService repositoryService = null;
    private static RuntimeService runtimeService = null;
    private static ProcessDefinitionEntity processDefinition = null;
    private static String processDefId = "";


    public RhoEventLogger(Clock clock) {
        this.clock = clock;
        this.objectMapper = new ObjectMapper();
    }

    public RhoEventLogger(Clock clock, String logFilePath) {
        this(clock);
        this.logFilePath = logFilePath;
    }

    @Override
    public void onEvent(ActivitiEvent event) {

        if(!event.getType().equals(ActivitiEventType.TASK_COMPLETED)) {
            return;
        }

        if(repositoryService == null) {
            println("init service");
            ProcessEngineImpl processEngine =(ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine();
            repositoryService = processEngine.getRepositoryService();
            runtimeService  = processEngine.getRuntimeService();
        }

        ActivitiEntityWithVariablesEvent eventWithVariables = (ActivitiEntityWithVariablesEvent) event;
        TaskEntity task = (TaskEntity) eventWithVariables.getEntity();
//        Map<String, Object> data = handleCommonTaskFields(task);
        println("complete: " + task.getId());

        if(!task.getProcessDefinitionId().equals(processDefId)) {
            processDefId = task.getProcessDefinitionId();
            processDefinition =(ProcessDefinitionEntity) repositoryService.getProcessDefinition(processDefId);
            println("get def: " + processDefId);
        }
        ActivityImpl ai = processDefinition.findActivity(task.getTaskDefinitionKey());

        println("acti impl：" + ai.getId());

    }


    @Override
    public boolean isFailOnException() {
        return false;
    }

    public void addEventLoggerListener(RhoEventLoggerListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<RhoEventLoggerListener>(1);
        }
        listeners.add(listener);
    }

    /**
     * Subclasses that want something else than the database flusher should override this method
     */
    protected EventFlusher createEventFlusher() {
        return null;
    }

    public Clock getClock() {
        return clock;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<RhoEventLoggerListener> getListeners() {
        return listeners;
    }

    public void setListeners(List<RhoEventLoggerListener> listeners) {
        this.listeners = listeners;
    }

}

