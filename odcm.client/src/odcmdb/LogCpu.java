/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package odcmdb;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author zantaz
 */
@Entity
@Table(name = "log_cpu", catalog = "ODCM_DB", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "LogCpu.findAll", query = "SELECT l FROM LogCpu l"),
    @NamedQuery(name = "LogCpu.findByIdValue", query = "SELECT l FROM LogCpu l WHERE l.logCpuPK.idValue = :idValue"),
    @NamedQuery(name = "LogCpu.findByCpu", query = "SELECT l FROM LogCpu l WHERE l.cpu = :cpu"),
    @NamedQuery(name = "LogCpu.findByCpuTime", query = "SELECT l FROM LogCpu l WHERE l.cpuTime = :cpuTime"),
    @NamedQuery(name = "LogCpu.findByHostid", query = "SELECT l FROM LogCpu l WHERE l.logCpuPK.hostid = :hostid")})
public class LogCpu implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected LogCpuPK logCpuPK;
    @Basic(optional = false)
    @Column(name = "cpu", nullable = false)
    private float cpu;
    @Basic(optional = false)
    @Column(name = "cpu_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date cpuTime;
    @JoinColumn(name = "Host_id", referencedColumnName = "Host_id", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Usage usage;

    public LogCpu() {
    }

    public LogCpu(LogCpuPK logCpuPK) {
        this.logCpuPK = logCpuPK;
    }

    public LogCpu(LogCpuPK logCpuPK, float cpu, Date cpuTime) {
        this.logCpuPK = logCpuPK;
        this.cpu = cpu;
        this.cpuTime = cpuTime;
    }

    public LogCpu(int idValue, long hostid) {
        this.logCpuPK = new LogCpuPK(idValue, hostid);
    }

    public LogCpuPK getLogCpuPK() {
        return logCpuPK;
    }

    public void setLogCpuPK(LogCpuPK logCpuPK) {
        this.logCpuPK = logCpuPK;
    }

    public float getCpu() {
        return cpu;
    }

    public void setCpu(float cpu) {
        this.cpu = cpu;
    }

    public Date getCpuTime() {
        return cpuTime;
    }

    public void setCpuTime(Date cpuTime) {
        this.cpuTime = cpuTime;
    }

    public Usage getUsage() {
        return usage;
    }

    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (logCpuPK != null ? logCpuPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof LogCpu)) {
            return false;
        }
        LogCpu other = (LogCpu) object;
        if ((this.logCpuPK == null && other.logCpuPK != null) || (this.logCpuPK != null && !this.logCpuPK.equals(other.logCpuPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DataBase.LogCpu[ logCpuPK=" + logCpuPK + " ]";
    }
    
}
