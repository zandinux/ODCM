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
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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
@Table(name = "usage", catalog = "ODCM_DB", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Usage.findAll", query = "SELECT u FROM Usage u"),
    @NamedQuery(name = "Usage.findByState", query = "SELECT u FROM Usage u WHERE u.state = :state"),
    @NamedQuery(name = "Usage.findByCpu", query = "SELECT u FROM Usage u WHERE u.cpu = :cpu"),
    @NamedQuery(name = "Usage.findByRam", query = "SELECT u FROM Usage u WHERE u.ram = :ram"),
    @NamedQuery(name = "Usage.findByFreeCores", query = "SELECT u FROM Usage u WHERE u.freeCores = :freeCores"),
    @NamedQuery(name = "Usage.findByHostid", query = "SELECT u FROM Usage u WHERE u.hostid = :hostid")})
public class Usage implements Serializable {
    private static final long serialVersionUID = 1L;
    @Basic(optional = false)
    @Column(name = "State", nullable = false, length = 45)
    private String state;
    @Basic(optional = false)
    @Column(name = "Cpu", nullable = false)
    private float cpu;
    @Basic(optional = false)
    @Column(name = "Ram", nullable = false)
    private float ram;
    @Basic(optional = false)
    @Column(name = "FreeCores", nullable = false)
    private int freeCores;
    @Id
    @Basic(optional = false)
    @Column(name = "Host_id", nullable = false)
    private Long hostid;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "usage")
    private List<LogCpu> logCpuList;
    @JoinColumn(name = "Host_id", referencedColumnName = "Host_id", nullable = false, insertable = false, updatable = false)
    @OneToOne(optional = false)
    private Host host;

    public Usage() {
    }

    public Usage(Long hostid) {
        this.hostid = hostid;
    }

    public Usage(Long hostid, String state, float cpu, float ram, int freeCores) {
        this.hostid = hostid;
        this.state = state;
        this.cpu = cpu;
        this.ram = ram;
        this.freeCores = freeCores;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public float getCpu() {
        return cpu;
    }

    public void setCpu(float cpu) {
        this.cpu = cpu;
    }

    public float getRam() {
        return ram;
    }

    public void setRam(float ram) {
        this.ram = ram;
    }

    public int getFreeCores() {
        return freeCores;
    }

    public void setFreeCores(int freeCores) {
        this.freeCores = freeCores;
    }

    public Long getHostid() {
        return hostid;
    }

    public void setHostid(Long hostid) {
        this.hostid = hostid;
    }

    @XmlTransient
    public List<LogCpu> getLogCpuList() {
        return logCpuList;
    }

    public void setLogCpuList(List<LogCpu> logCpuList) {
        this.logCpuList = logCpuList;
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
        hash += (hostid != null ? hostid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Usage)) {
            return false;
        }
        Usage other = (Usage) object;
        if ((this.hostid == null && other.hostid != null) || (this.hostid != null && !this.hostid.equals(other.hostid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DataBase.Usage[ hostid=" + hostid + " ]";
    }
    
}
