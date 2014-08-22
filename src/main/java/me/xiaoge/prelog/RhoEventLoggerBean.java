package me.xiaoge.prelog;


import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Created by xiaoge on 2014/8/22.
 */
public class RhoEventLoggerBean implements InitializingBean{
    private String logFilePath;
    private RuntimeService runtimeService;


    private ProcessEngineConfiguration processEngineConfiguration;

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

    public RhoEventLoggerBean() {
        System.out.println("new el bean");

    }


    public ProcessEngineConfiguration getProcessEngineConfiguration() {
        return processEngineConfiguration;
    }

    public void setProcessEngineConfiguration(ProcessEngineConfiguration processEngineConfiguration) {
        this.processEngineConfiguration = processEngineConfiguration;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        RhoEventLogger databaseEventLogger = new RhoEventLogger(processEngineConfiguration.getClock(), this.logFilePath);
        runtimeService.addEventListener(databaseEventLogger);
        System.out.println("rho bean init");
    }
}
