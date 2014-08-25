package me.xiaoge.prelog;

import javax.persistence.*;

/**
 * Created by xiaoge on 2014/8/25.
 */
@Entity
@Table(name="rho_case", indexes = {@Index(name="index1", columnList = "process_instance_id")})
public class RhoEventCaseEntity {

    private long id;
    private String processsInstanceId;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="case_id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "process_instance_id", unique = true, nullable = false)
    public String getProcesssInstanceId() {
        return processsInstanceId;
    }

    public void setProcesssInstanceId(String processsInstanceId) {
        this.processsInstanceId = processsInstanceId;
    }
}
