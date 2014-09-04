package me.xiaoge.prelog;


import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Created by xiaoge on 2014/8/22.
 */
public class RhoEventLoggerBean implements InitializingBean{
    private String logFilePath = "./";
    private String logFileExtension = ".plog";
    private boolean saveLogFile = false;

    private RuntimeService runtimeService;
    private RepositoryService repositoryService;
    private ProcessEngine processEngine;

    private RhoEventLogger rhoEventLogger;
    private SessionFactory sessionFactory;

    private ProcessEngineConfiguration processEngineConfiguration;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public RuntimeService getRuntimeService() {
        return runtimeService;
    }

    public void setRuntimeService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    public RepositoryService getRepositoryService() {
        return repositoryService;
    }

    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public ProcessEngine getProcessEngine() {
        return processEngine;
    }

    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    public RhoEventLogger getRhoEventLogger() {
        return rhoEventLogger;
    }

    public void setRhoEventLogger(RhoEventLogger rhoEventLogger) {
        this.rhoEventLogger = rhoEventLogger;
    }

    public void setLogFileExtension(String logFileExtension) {
        this.logFileExtension = logFileExtension;
    }

    public void setSaveLogFile(boolean saveLogFile) {
        this.saveLogFile = saveLogFile;
    }

    public String getLogFileExtension() {
        return logFileExtension;
    }

    public boolean isSaveLogFile() {
        return saveLogFile;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public RhoEventLoggerBean() {

    }


    public ProcessEngineConfiguration getProcessEngineConfiguration() {
        return processEngineConfiguration;
    }

    public void setProcessEngineConfiguration(ProcessEngineConfiguration processEngineConfiguration) {
        this.processEngineConfiguration = processEngineConfiguration;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        if(processEngine != null) {
            runtimeService = processEngine.getRuntimeService();
            repositoryService = processEngine.getRepositoryService();
            processEngineConfiguration = processEngine.getProcessEngineConfiguration();
        }

        rhoEventLogger = new RhoEventLogger(processEngineConfiguration.getClock(), sessionFactory);
        rhoEventLogger.setLogFilePath(logFilePath);
        rhoEventLogger.setLogFileExtension(logFileExtension);
        rhoEventLogger.setSaveLogFile(saveLogFile);
        runtimeService.addEventListener(rhoEventLogger);
//        System.out.println("rho bean init");
    }

    public void addRhoListener(String eventName, RhoEventListener listener) {
        rhoEventLogger.addRhoListener(eventName, listener);
    }
}
