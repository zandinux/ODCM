/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package odcmdb;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author zantaz
 */
@Entity
@Table(name = "vms", catalog = "ODCM_DB", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Vms.findAll", query = "SELECT v FROM Vms v"),
    @NamedQuery(name = "Vms.findByVmid", query = "SELECT v FROM Vms v WHERE v.vmsPK.vmid = :vmid"),
    @NamedQuery(name = "Vms.findByListid", query = "SELECT v FROM Vms v WHERE v.listid = :listid"),
    @NamedQuery(name = "Vms.findByVName", query = "SELECT v FROM Vms v WHERE v.vName = :vName"),
    @NamedQuery(name = "Vms.findByVCores", query = "SELECT v FROM Vms v WHERE v.vCores = :vCores"),
    @NamedQuery(name = "Vms.findByVRam", query = "SELECT v FROM Vms v WHERE v.vRam = :vRam"),
    @NamedQuery(name = "Vms.findByHostid", query = "SELECT v FROM Vms v WHERE v.vmsPK.hostid = :hostid")})
public class Vms implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected VmsPK vmsPK;
    @Basic(optional = false)
    @Column(name = "List_id", nullable = false)
    private int listid;
    @Basic(optional = false)
    @Column(name = "VName", nullable = false, length = 45)
    private String vName;
    @Basic(optional = false)
    @Column(name = "VCores", nullable = false)
    private int vCores;
    @Basic(optional = false)
    @Column(name = "VRam", nullable = false)
    private long vRam;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "vms")
    private Vmusage vmusage;
    @JoinColumn(name = "Host_id", referencedColumnName = "Host_id", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Host host;

    public Vms() {
    }

    public Vms(VmsPK vmsPK) {
        this.vmsPK = vmsPK;
    }

    public Vms(VmsPK vmsPK, int listid, String vName, int vCores, long vRam) {
        this.vmsPK = vmsPK;
        this.listid = listid;
        this.vName = vName;
        this.vCores = vCores;
        this.vRam = vRam;
    }

    public Vms(long vmid, long hostid) {
        this.vmsPK = new VmsPK(vmid, hostid);
    }

    public VmsPK getVmsPK() {
        return vmsPK;
    }

    public void setVmsPK(VmsPK vmsPK) {
        this.vmsPK = vmsPK;
    }

    public int getListid() {
        return listid;
    }

    public void setListid(int listid) {
        this.listid = listid;
    }

    public String getVName() {
        return vName;
    }

    public void setVName(String vName) {
        this.vName = vName;
    }

    public int getVCores() {
        return vCores;
    }

    public void setVCores(int vCores) {
        this.vCores = vCores;
    }

    public long getVRam() {
        return vRam;
    }

    public void setVRam(long vRam) {
        this.vRam = vRam;
    }

    public Vmusage getVmusage() {
        return vmusage;
    }

    public void setVmusage(Vmusage vmusage) {
        this.vmusage = vmusage;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (vmsPK != null ? vmsPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Vms)) {
            return false;
        }
        Vms other = (Vms) object;
        if ((this.vmsPK == null && other.vmsPK != null) || (this.vmsPK != null && !this.vmsPK.equals(other.vmsPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DataBase.Vms[ vmsPK=" + vmsPK + " ]";
    }
    
}
