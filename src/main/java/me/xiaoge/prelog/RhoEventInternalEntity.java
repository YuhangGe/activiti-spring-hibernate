package me.xiaoge.prelog;

import javax.persistence.*;

/**
 * Created by xiaoge on 2014/8/22.
 */
@Entity
@Table(name = "rho_internal", indexes = {@Index(name = "index1", columnList = "process_instance_id, task_def_id")})
public class RhoEventInternalEntity {

    private long id;
    private String taskName;
    private String taskDefId;
    private String processInstanceId;
    private String executionId;

    public void setId(long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    public long getId() {
        return id;
    }

    @Column(name = "task_name")
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    @Column(name = "task_def_id")
    public String getTaskDefId() {
        return taskDefId;
    }

    public void setTaskDefId(String taskDefId) {
        this.taskDefId = taskDefId;
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
