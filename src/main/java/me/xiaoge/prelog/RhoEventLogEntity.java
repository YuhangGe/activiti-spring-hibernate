package me.xiaoge.prelog;

import javax.persistence.*;

/**
 * Created by xiaoge on 2014/8/24.
 */
@Entity
@Table(name = "rho_log", indexes = {@Index(name="index1", columnList = "case_id")})
public class RhoEventLogEntity {

    private long id;
    private long caseId;
    private String preTask;
    private String curTask;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "case_id", nullable = false)
    public long getCaseId() {
        return caseId;
    }

    public void setCaseId(long caseId) {
        this.caseId = caseId;
    }

    @Column(name = "pre_task", nullable = false)
    public String getPreTask() {
        return preTask;
    }

    public void setPreTask(String preTask) {
        this.preTask = preTask;
    }

    @Column(name = "cur_task", nullable = false)
    public String getCurTask() {
        return curTask;
    }

    public void setCurTask(String curTask) {
        this.curTask = curTask;
    }
}
