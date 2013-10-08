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
public class LogCpuPK implements Serializable {
    @Basic(optional = false)
    @Column(name = "id_value", nullable = false)
    private int idValue;
    @Basic(optional = false)
    @Column(name = "Host_id", nullable = false)
    private long hostid;

    public LogCpuPK() {
    }

    public LogCpuPK(int idValue, long hostid) {
        this.idValue = idValue;
        this.hostid = hostid;
    }

    public int getIdValue() {
        return idValue;
    }

    public void setIdValue(int idValue) {
        this.idValue = idValue;
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
        hash += (int) hostid;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof LogCpuPK)) {
            return false;
        }
        LogCpuPK other = (LogCpuPK) object;
        if (this.idValue != other.idValue) {
            return false;
        }
        if (this.hostid != other.hostid) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DataBase.LogCpuPK[ idValue=" + idValue + ", hostid=" + hostid + " ]";
    }
    
}
