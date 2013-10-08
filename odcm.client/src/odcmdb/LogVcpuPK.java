/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package odcmdb;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author zantaz
 */
@Embeddable
public class LogVcpuPK implements Serializable {
    @Basic(optional = false)
    @Column(name = "id_value", nullable = false)
    private int idValue;
    @Basic(optional = false)
    @Column(name = "Vm_id", nullable = false)
    private long vmid;
    @Basic(optional = false)
    @Column(name = "Host_id", nullable = false)
    private long hostid;

    public LogVcpuPK() {
    }

    public LogVcpuPK(int idValue, long vmid, long hostid) {
        this.idValue = idValue;
        this.vmid = vmid;
        this.hostid = hostid;
    }

    public int getIdValue() {
        return idValue;
    }

    public void setIdValue(int idValue) {
        this.idValue = idValue;
    }

    public long getVmid() {
        return vmid;
    }

    public void setVmid(long vmid) {
        this.vmid = vmid;
    }

    public long getHostid() {
        return hostid;
    }

    public void setHostid(long hostid) {
        this.hostid = hostid;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) idValue;
        hash += (int) vmid;
        hash += (int) hostid;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof LogVcpuPK)) {
            return false;
        }
        LogVcpuPK other = (LogVcpuPK) object;
        if (this.idValue != other.idValue) {
            return false;
        }
        if (this.vmid != other.vmid) {
            return false;
        }
        if (this.hostid != other.hostid) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DataBase.LogVcpuPK[ idValue=" + idValue + ", vmid=" + vmid + ", hostid=" + hostid + " ]";
    }
    
}
