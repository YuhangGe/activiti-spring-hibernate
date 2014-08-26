package me.xiaoge.prelog;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.*;
import org.activiti.engine.delegate.event.impl.ActivitiActivityEventImpl;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.event.logger.EventFlusher;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.hibernate.SessionFactory;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.runtime.Clock;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.*;


/**
 * @author Joram Barrez
 */
public class RhoEventLogger implements ActivitiEventListener {


    private static void println(String msg) {
        System.out.println(msg);
    }
    private static void println(long n) {System.out.println(n);}

    private static void println(int n) {
        System.out.println(n);
    }
    private static void println(String[] strArr) {
        for(String s: strArr) {
            println(s);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(RhoEventLogger.class);

    protected Clock clock;
    protected ObjectMapper objectMapper;
    protected String logFilePath = "";
    protected BufferedWriter logFileWriter = null;

    // Listeners for new events
    protected List<RhoEventLoggerListener> listeners;


    protected static HashMap<String, HashMap<String, String[]>> processPreTaskCache = new HashMap<>();

    private RepositoryService repositoryService = null;
    private RuntimeService runtimeService = null;
    private ProcessDefinitionEntity processDefinition = null;
    private String processDefId = "";
    private RhoEventInternalDAO rhoEventInternalDAO;
    private RhoEventCaseDAO rhoEventCaseDAO;
    private RhoEventLogDAO rhoEventLogDAO;

    public RhoEventLogger(Clock clock) {
        this.clock = clock;
        this.objectMapper = new ObjectMapper();
    }

    public RhoEventLogger(Clock clock, SessionFactory sessionFactory, String logFilePath) {
        this(clock);
        this.logFilePath = logFilePath;
        this.rhoEventInternalDAO = new RhoEventInternalDAOImpl(sessionFactory);
        this.rhoEventCaseDAO = new RhoEventCaseDAOImpl(sessionFactory);
        this.rhoEventLogDAO = new RhoEventLogDAOImpl(sessionFactory);
    }


    private static void getActivityPreTasks(List<PvmTransition> pvmTransitionList, List<String> preTaskList) {
        for(PvmTransition pvmTransition:pvmTransitionList) {
            PvmActivity pvmActivity = pvmTransition.getSource();
            String actType = (String)pvmActivity.getProperty("type");
            if(actType.endsWith("Task")) {
                //userTask, seriviceTask, ...
                preTaskList.add(pvmActivity.getId());
            } else if(actType.endsWith("Gateway")) {
                //gateway
                getActivityPreTasks(pvmActivity.getIncomingTransitions(), preTaskList);
            }
        }
    }

    private synchronized static String[] getCacheSortedPreTasks(String processDefId, ActivityImpl ai) {
        HashMap<String, String[]> tm;
        if(!processPreTaskCache.containsKey(processDefId)) {
            tm = new HashMap<>();
            processPreTaskCache.put(processDefId, tm);
        } else {
            tm = processPreTaskCache.get(processDefId);
        }

        String taskDefId = ai.getId();
        String[] preTaskList;
        if(!tm.containsKey(taskDefId)) {
            List<String> tmpPreTaskList = new ArrayList<>();
            getActivityPreTasks(ai.getIncomingTransitions(), tmpPreTaskList);
            preTaskList = new String[tmpPreTaskList.size()];
            tmpPreTaskList.toArray(preTaskList);
//            println("---pre task : " + taskDefId + " ---");
//            println(preTaskList);

            tm.put(taskDefId, preTaskList);
        } else {
            preTaskList = tm.get(taskDefId);
        }
        return preTaskList;
    }

    @Override
    public void onEvent(ActivitiEvent event) {

        if(!(event instanceof ActivitiActivityEventImpl)) {
           return;
        }
        ActivitiActivityEventImpl activitiActivityEvent = (ActivitiActivityEventImpl) event;
        String activityType = activitiActivityEvent.getActivityType();
        Boolean endProcessInstance = false;
        if(activityType.equals("endEvent")) {
            endProcessInstance = true;
        } else if(!(activityType.equals("userTask")
                || activityType.equals("scriptTask")
                || activityType.equals("mailTask")
                || activityType.equals("serviceTask")
                || activityType.equals("receiveTask")
                || activityType.equals("businessRuleTask")
        )) {
            return;
        }

        ActivitiEventType eventType = activitiActivityEvent.getType();

        if(!eventType.equals(ActivitiEventType.ACTIVITY_COMPLETED)) {
            return;
        }



        try{
            String processInstanceId = activitiActivityEvent.getProcessInstanceId();
            RhoEventCaseEntity rhoEventCaseEntity = rhoEventCaseDAO.getByProcessInstanceId(processInstanceId);
            if(rhoEventCaseEntity == null) {
                rhoEventCaseEntity = new RhoEventCaseEntity();
                rhoEventCaseEntity.setProcesssInstanceId(processInstanceId);
                rhoEventCaseDAO.save(rhoEventCaseEntity);
            }

            long caseId = rhoEventCaseEntity.getId();
            if(endProcessInstance) {
                /**
                 * 如果processInstance已经结束，则删除中间表里面的数据，以节省空间。
                 */
                rhoEventInternalDAO.deleteByProcessInstanceId(processInstanceId);
                if(this.logFilePath != null && !this.logFilePath.isEmpty()) {
                    storeLogToFile(caseId);
                }
            } else {
                dealEvent(activitiActivityEvent, caseId);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }

    }

    protected void storeLogToFile(long caseId) throws IOException{
        if(this.logFileWriter == null) {
            this.logFileWriter = new BufferedWriter(new FileWriter(this.logFilePath, true));
        }
        List<RhoEventLogEntity> rhoEventLogList = rhoEventLogDAO.findByCaseId(caseId);
//        println(caseId);
        StringBuilder sb = new StringBuilder();
        Boolean first = true;
        for(RhoEventLogEntity r : rhoEventLogList) {
            if(!first) {
                sb.append(',');
            } else {
                first = false;
            }
            sb.append('[').append(r.getPreTask()).append(']').append(r.getCurTask());
        }
        sb.append("\r\n");
        logLine(sb.toString());
    }

    protected synchronized void logLine(String line) throws IOException{
        this.logFileWriter.write(line);
        this.logFileWriter.flush();
    }

    protected void dealEvent(ActivitiActivityEvent activitiActivityEvent, long caseId) {
        String processInstanceId = activitiActivityEvent.getProcessInstanceId();

        if(repositoryService == null) {
//            println("init service");
            ProcessEngineImpl processEngine =(ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine();
            repositoryService = processEngine.getRepositoryService();
            runtimeService  = processEngine.getRuntimeService();
        }
        if(!activitiActivityEvent.getProcessDefinitionId().equals(processDefId)) {
            processDefId = activitiActivityEvent.getProcessDefinitionId();
            processDefinition =(ProcessDefinitionEntity) repositoryService.getProcessDefinition(processDefId);
//            println("get def: " + processDefId);
        }

        ActivityImpl ai = processDefinition.findActivity(activitiActivityEvent.getActivityId());

//        println("acti impl：" + ai.getId());
        /*
         * 取到已经排序的preTaskList
         */
        String[] preTaskList = getCacheSortedPreTasks(processDefId, ai);
        List<String> actualPreTaskList = new ArrayList<>();

        if(preTaskList.length> 0) {
            List<RhoEventInternalEntity> rhoEventInternalList = rhoEventInternalDAO.findByP(processInstanceId, preTaskList);
            for (RhoEventInternalEntity re:rhoEventInternalList) {
                actualPreTaskList.add(re.getTaskName());
            }
        }


//        println("case id: " + caseId);
        log(caseId, actualPreTaskList, activitiActivityEvent.getActivityName());

        RhoEventInternalEntity curRhoEntity = new RhoEventInternalEntity();
        curRhoEntity.setProcessInstanceId(processInstanceId);
        curRhoEntity.setTaskDefId(ai.getId());
        curRhoEntity.setTaskName(activitiActivityEvent.getActivityName());
        rhoEventInternalDAO.save(curRhoEntity);
    }
    protected void log(long caseId, List<String> preTaskList, String curTask) {
        RhoEventLogEntity rhoEventLogEntity = new RhoEventLogEntity();
        rhoEventLogEntity.setCaseId(caseId);
        rhoEventLogEntity.setCurTask(curTask);
        rhoEventLogEntity.setPreTask(StringUtils.join(preTaskList, ","));
        rhoEventLogDAO.save(rhoEventLogEntity);
//        println("" + caseId + ": [" + StringUtils.join(preTaskList, ",") + "]" + curTask);
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

