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
public class VmusagePK implements Serializable {
    @Basic(optional = false)
    @Column(name = "Vm_id", nullable = false)
    private long vmid;
    @Basic(optional = false)
    @Column(name = "Host_id", nullable = false)
    private long hostid;

    public VmusagePK() {
    }

    public VmusagePK(long vmid, long hostid) {
        this.vmid = vmid;
        this.hostid = hostid;
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
        hash += (int) vmid;
        hash += (int) hostid;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof VmusagePK)) {
            return false;
        }
        VmusagePK other = (VmusagePK) object;
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
        return "DataBase.VmusagePK[ vmid=" + vmid + ", hostid=" + hostid + " ]";
    }
    
}
