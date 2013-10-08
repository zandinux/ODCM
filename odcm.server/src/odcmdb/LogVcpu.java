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
import javax.persistence.JoinColumns;
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
@Table(name = "log_vcpu", catalog = "ODCM_DB", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "LogVcpu.findAll", query = "SELECT l FROM LogVcpu l"),
    @NamedQuery(name = "LogVcpu.findByIdValue", query = "SELECT l FROM LogVcpu l WHERE l.logVcpuPK.idValue = :idValue"),
    @NamedQuery(name = "LogVcpu.findByVcpu", query = "SELECT l FROM LogVcpu l WHERE l.vcpu = :vcpu"),
    @NamedQuery(name = "LogVcpu.findByVcpuTime", query = "SELECT l FROM LogVcpu l WHERE l.vcpuTime = :vcpuTime"),
    @NamedQuery(name = "LogVcpu.findByVmid", query = "SELECT l FROM LogVcpu l WHERE l.logVcpuPK.vmid = :vmid"),
    @NamedQuery(name = "LogVcpu.findByHostid", query = "SELECT l FROM LogVcpu l WHERE l.logVcpuPK.hostid = :hostid")})
public class LogVcpu implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected LogVcpuPK logVcpuPK;
    @Basic(optional = false)
    @Column(name = "vcpu", nullable = false)
    private float vcpu;
    @Basic(optional = false)
    @Column(name = "vcpu_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date vcpuTime;
    @JoinColumns({
        @JoinColumn(name = "Vm_id", referencedColumnName = "Vm_id", nullable = false, insertable = false, updatable = false),
        @JoinColumn(name = "Host_id", referencedColumnName = "Host_id", nullable = false, insertable = false, updatable = false)})
    @ManyToOne(optional = false)
    private Vmusage vmusage;

    public LogVcpu() {
    }

    public LogVcpu(LogVcpuPK logVcpuPK) {
        this.logVcpuPK = logVcpuPK;
    }

    public LogVcpu(LogVcpuPK logVcpuPK, float vcpu, Date vcpuTime) {
        this.logVcpuPK = logVcpuPK;
        this.vcpu = vcpu;
        this.vcpuTime = vcpuTime;
    }

    public LogVcpu(int idValue, long vmid, long hostid) {
        this.logVcpuPK = new LogVcpuPK(idValue, vmid, hostid);
    }

    public LogVcpuPK getLogVcpuPK() {
        return logVcpuPK;
    }

    public void setLogVcpuPK(LogVcpuPK logVcpuPK) {
        this.logVcpuPK = logVcpuPK;
    }

    public float getVcpu() {
        return vcpu;
    }

    public void setVcpu(float vcpu) {
        this.vcpu = vcpu;
    }

    public Date getVcpuTime() {
        return vcpuTime;
    }

    public void setVcpuTime(Date vcpuTime) {
        this.vcpuTime = vcpuTime;
    }

    public Vmusage getVmusage() {
        return vmusage;
    }

    public void setVmusage(Vmusage vmusage) {
        this.vmusage = vmusage;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (logVcpuPK != null ? logVcpuPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof LogVcpu)) {
            return false;
        }
        LogVcpu other = (LogVcpu) object;
        if ((this.logVcpuPK == null && other.logVcpuPK != null) || (this.logVcpuPK != null && !this.logVcpuPK.equals(other.logVcpuPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DataBase.LogVcpu[ logVcpuPK=" + logVcpuPK + " ]";
    }
    
}
