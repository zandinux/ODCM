/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package odcmdb;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author zantaz
 */
@Entity
@Table(name = "vmusage", catalog = "ODCM_DB", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Vmusage.findAll", query = "SELECT v FROM Vmusage v"),
    @NamedQuery(name = "Vmusage.findByVState", query = "SELECT v FROM Vmusage v WHERE v.vState = :vState"),
    @NamedQuery(name = "Vmusage.findByVCpu", query = "SELECT v FROM Vmusage v WHERE v.vCpu = :vCpu"),
    @NamedQuery(name = "Vmusage.findByVmid", query = "SELECT v FROM Vmusage v WHERE v.vmusagePK.vmid = :vmid"),
    @NamedQuery(name = "Vmusage.findByHostid", query = "SELECT v FROM Vmusage v WHERE v.vmusagePK.hostid = :hostid")})
public class Vmusage implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected VmusagePK vmusagePK;
    @Basic(optional = false)
    @Column(name = "VState", nullable = false, length = 45)
    private String vState;
    @Basic(optional = false)
    @Column(name = "VCpu", nullable = false)
    private float vCpu;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "vmusage")
    private List<LogVcpu> logVcpuList;
    @JoinColumns({
        @JoinColumn(name = "Vm_id", referencedColumnName = "Vm_id", nullable = false, insertable = false, updatable = false),
        @JoinColumn(name = "Host_id", referencedColumnName = "Host_id", nullable = false, insertable = false, updatable = false)})
    @OneToOne(optional = false)
    private Vms vms;

    public Vmusage() {
    }

    public Vmusage(VmusagePK vmusagePK) {
        this.vmusagePK = vmusagePK;
    }

    public Vmusage(VmusagePK vmusagePK, String vState, float vCpu) {
        this.vmusagePK = vmusagePK;
        this.vState = vState;
        this.vCpu = vCpu;
    }

    public Vmusage(long vmid, long hostid) {
        this.vmusagePK = new VmusagePK(vmid, hostid);
    }

    public VmusagePK getVmusagePK() {
        return vmusagePK;
    }

    public void setVmusagePK(VmusagePK vmusagePK) {
        this.vmusagePK = vmusagePK;
    }

    public String getVState() {
        return vState;
    }

    public void setVState(String vState) {
        this.vState = vState;
    }

    public float getVCpu() {
        return vCpu;
    }

    public void setVCpu(float vCpu) {
        this.vCpu = vCpu;
    }

    @XmlTransient
    public List<LogVcpu> getLogVcpuList() {
        return logVcpuList;
    }

    public void setLogVcpuList(List<LogVcpu> logVcpuList) {
        this.logVcpuList = logVcpuList;
    }

    public Vms getVms() {
        return vms;
    }

    public void setVms(Vms vms) {
        this.vms = vms;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (vmusagePK != null ? vmusagePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Vmusage)) {
            return false;
        }
        Vmusage other = (Vmusage) object;
        if ((this.vmusagePK == null && other.vmusagePK != null) || (this.vmusagePK != null && !this.vmusagePK.equals(other.vmusagePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DataBase.Vmusage[ vmusagePK=" + vmusagePK + " ]";
    }
    
}
