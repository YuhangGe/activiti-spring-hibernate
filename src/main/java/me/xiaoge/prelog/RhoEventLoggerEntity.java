package me.xiaoge.prelog;

import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by xiaoge on 2014/8/22.
 */
@Entity
@Table(name = "rho_event_logger")
public class RhoEventLoggerEntity {

    private int id;
    private String taskName;
    private String taskId;
    private String taskDef;
    private String processInstanceId;
    private String executionId;

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "task_name")
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    @Column(name = "task_id")
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Column(name = "task_def")
    public String getTaskDef() {
        return taskDef;
    }

    public void setTaskDef(String taskDef) {
        this.taskDef = taskDef;
    }

    @Column(name = "process_instance_id")
    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Column(name = "execution_id")
    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }



}
