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

    public static final String BEFORE_STORE_LOG = "BEFORE_STORE_LOG";

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
    protected Map<String, BufferedWriter> logFileWriterMap = new HashMap<>();
    protected String logFileExtension = "plog";
    protected boolean saveLogFile = false;

    // Listeners for new events
    protected List<RhoEventLoggerListener> listeners;


    protected static HashMap<String, HashMap<String, PT>> processPreTaskCache = new HashMap<>();

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

    public RhoEventLogger(Clock clock, SessionFactory sessionFactory) {
        this(clock);
        this.rhoEventInternalDAO = new RhoEventInternalDAOImpl(sessionFactory);
        this.rhoEventCaseDAO = new RhoEventCaseDAOImpl(sessionFactory);
        this.rhoEventLogDAO = new RhoEventLogDAOImpl(sessionFactory);
    }


    private static void getActivityPreTasks(List<PvmTransition> pvmTransitionList, List<String> preTaskList, String myId) {
        for(PvmTransition pvmTransition:pvmTransitionList) {
            PvmActivity pvmActivity = pvmTransition.getSource();
            String actType = (String)pvmActivity.getProperty("type");
            if(actType.endsWith("Task")) {
                //userTask, seriviceTask, ...
                if(!pvmActivity.getId().equals(myId)) {
                    preTaskList.add(pvmActivity.getId());
                }
            } else if(actType.endsWith("Gateway")) {
                //gateway
                getActivityPreTasks(pvmActivity.getIncomingTransitions(), preTaskList, myId);
            }
        }
    }

    private synchronized static PT getCacheSortedPreTasks(String processDefId, ActivityImpl ai) throws Exception {
        HashMap<String, PT> tm;
        if(!processPreTaskCache.containsKey(processDefId)) {
            tm = new HashMap<>();
            processPreTaskCache.put(processDefId, tm);
        } else {
            tm = processPreTaskCache.get(processDefId);
        }

        String taskDefId = ai.getId();
        PT apt;
        if(!tm.containsKey(taskDefId)) {
            apt = new PT();
            List<String> tmpPreTaskList = new ArrayList<>();
            List<PvmTransition> incomingTList = ai.getIncomingTransitions();
            String myId = ai.getId();

            getActivityPreTasks(incomingTList, tmpPreTaskList, myId);
            apt.choice = false;
//            if(incomingTList.size() != 1) {
//                throw new Exception("RhoEventLogger: do not support such kind of process");
//            }
//            PvmActivity preTaskAct = incomingTList.get(0).getSource();
//            String preTaskType = (String) preTaskAct.getProperty("type");
//            if(preTaskType.endsWith("Task")) {
//                tmpPreTaskList.add(preTaskAct.getId());
//                apt.choice = false;
//            } else if(preTaskType.equals("parallelGateway")) {
//                getActivityPreTasks(preTaskAct.getIncomingTransitions(), tmpPreTaskList, myId);
//                apt.choice = false;
//            } else if(preTaskType.equals("exclusiveGateway")) {
//                getActivityPreTasks(preTaskAct.getIncomingTransitions(), tmpPreTaskList, myId);
//                apt.choice = true;
//            } else {
//                throw new Exception("RhoEventLogger: do not support such kind of process");
//            }
            apt.preTaskList = new String[tmpPreTaskList.size()];
            tmpPreTaskList.toArray(apt.preTaskList);
//            println("---pre task : " + taskDefId + " ---");
//            println(preTaskList);

            tm.put(taskDefId, apt);
        } else {
            apt = tm.get(taskDefId);
        }
        return apt;
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
        } else if(!activityType.endsWith("Task")) {
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
                 * 同时将日志存入日志文件。
                 */
                if(this.saveLogFile) {
                    storeLogToFile(caseId, activitiActivityEvent.getProcessDefinitionId());
                }
//                this.rhoEventInternalDAO.deleteByProcessInstanceId(processInstanceId);
            } else {
                dealEvent(activitiActivityEvent, caseId);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }

    }

    protected void storeLogToFile(long caseId, String processDefId) throws IOException{
        BufferedWriter bw;
        if(!this.logFileWriterMap.containsKey(processDefId)) {
            File dir = new File(this.logFilePath);
            if(!dir.exists()) {
                dir.mkdir();
            } else if(!dir.isDirectory()) {
                throw new IOException("RhoEventLogger: logFilePath is not a directory");
            }
//            println(dir.getAbsolutePath());
            bw = new BufferedWriter(new FileWriter(dir.getAbsolutePath() + "/" + processDefId + "." + logFileExtension, true));
            this.logFileWriterMap.put(processDefId, bw);
        } else {
            bw = this.logFileWriterMap.get(processDefId);
        }

        List<RhoEventLogEntity> rhoEventLogList = rhoEventLogDAO.findByCaseId(caseId);
        if(rhoListenerMap.containsKey(BEFORE_STORE_LOG)) {
            for(RhoEventListener listener: rhoListenerMap.get(BEFORE_STORE_LOG)) {
                listener.onBeforeStoreLog(rhoEventLogList, this);
            }
        }
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
        logLine(bw, sb.toString());
    }

    protected synchronized void logLine(BufferedWriter bw, String line) throws IOException{
        println(line);
        bw.append(line);
        bw.flush();
    }

    protected void dealEvent(ActivitiActivityEvent activitiActivityEvent, long caseId) throws Exception {
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

        println("acti impl：" + ai.getProperty("name"));
        /*
         * 取到preTaskList
         */
        PT apt = getCacheSortedPreTasks(processDefId, ai);
        List<String> actualPreTaskList = new ArrayList<>();

        if(apt.preTaskList.length> 0) {
            List<RhoEventInternalEntity> rhoEventInternalList = rhoEventInternalDAO.findByP(processInstanceId, apt.preTaskList);
            if(apt.choice && rhoEventInternalList.size()>0) {
                actualPreTaskList.add(rhoEventInternalList.get(0).getTaskName());
            } else if(!apt.choice) {
                for (RhoEventInternalEntity re:rhoEventInternalList) {
                    if(!actualPreTaskList.contains(re.getTaskName())) {
                        actualPreTaskList.add(re.getTaskName());
                    }
                }
            }
        }


//        println("case id: " + caseId);
        log(caseId, actualPreTaskList, activitiActivityEvent.getActivityName());

        RhoEventInternalEntity curRhoEntity = new RhoEventInternalEntity();
        curRhoEntity.setProcessInstanceId(processInstanceId);
        curRhoEntity.setTaskDefId(ai.getId());
        curRhoEntity.setTaskName(activitiActivityEvent.getActivityName());
        curRhoEntity.setExecutionId(activitiActivityEvent.getExecutionId());
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

    public String getLogFileExtension() {
        return logFileExtension;
    }

    public void setLogFileExtension(String logFileExtension) {
        this.logFileExtension = logFileExtension;
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public boolean isSaveLogFile() {
        return saveLogFile;
    }

    public void setSaveLogFile(boolean saveLogFile) {
        this.saveLogFile = saveLogFile;
    }

    private Map<String, List<RhoEventListener>> rhoListenerMap = new HashMap<>();
    public void addRhoListener(String eventName, RhoEventListener listener) {
        List<RhoEventListener> listenerList;
        if(rhoListenerMap.containsKey(eventName)) {
            listenerList = rhoListenerMap.get(eventName);
        } else {
            listenerList = new ArrayList<>();
            rhoListenerMap.put(eventName, listenerList);
        }
        listenerList.add(listener);
    }

    public BufferedWriter getStoreWriterByProcessDefId(String processDefId) {
        if(logFileWriterMap.containsKey(processDefId)) {
            return logFileWriterMap.get(processDefId);
        } else {
            return null;
        }
    }
}

class PT {
    public String[] preTaskList;
    public boolean choice = false;
}

