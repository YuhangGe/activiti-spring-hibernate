package me.xiaoge.prelog;


import org.activiti.engine.ProcessEngineConfiguration;
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
        RhoEventLogger databaseEventLogger = new RhoEventLogger(processEngineConfiguration.getClock(), sessionFactory);
        databaseEventLogger.setLogFilePath(logFilePath);
        databaseEventLogger.setLogFileExtension(logFileExtension);
        databaseEventLogger.setSaveLogFile(saveLogFile);
        runtimeService.addEventListener(databaseEventLogger);
//        System.out.println("rho bean init");
    }
}
