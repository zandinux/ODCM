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
@Table(name = "host", catalog = "ODCM_DB", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Host.findAll", query = "SELECT h FROM Host h"),
    @NamedQuery(name = "Host.findByHostid", query = "SELECT h FROM Host h WHERE h.hostid = :hostid"),
    @NamedQuery(name = "Host.findByIp", query = "SELECT h FROM Host h WHERE h.ip = :ip"),
    @NamedQuery(name = "Host.findByMacAddress", query = "SELECT h FROM Host h WHERE h.macAddress = :macAddress"),
    @NamedQuery(name = "Host.findByName", query = "SELECT h FROM Host h WHERE h.name = :name"),
    @NamedQuery(name = "Host.findByCpuGhz", query = "SELECT h FROM Host h WHERE h.cpuGhz = :cpuGhz"),
    @NamedQuery(name = "Host.findByMaxCores", query = "SELECT h FROM Host h WHERE h.maxCores = :maxCores"),
    @NamedQuery(name = "Host.findByMaxRam", query = "SELECT h FROM Host h WHERE h.maxRam = :maxRam")})
public class Host implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "Host_id", nullable = false)
    private Long hostid;
    @Basic(optional = false)
    @Column(name = "IP", nullable = false, length = 45)
    private String ip;
    @Basic(optional = false)
    @Column(name = "MacAddress", nullable = false, length = 45)
    private String macAddress;
    @Basic(optional = false)
    @Column(name = "Name", nullable = false, length = 45)
    private String name;
    @Basic(optional = false)
    @Column(name = "Cpu_Ghz", nullable = false)
    private float cpuGhz;
    @Basic(optional = false)
    @Column(name = "MaxCores", nullable = false)
    private int maxCores;
    @Basic(optional = false)
    @Column(name = "MaxRam", nullable = false)
    private float maxRam;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "host")
    private Usage usage;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "host")
    private List<Vms> vmsList;

    public Host() {
    }

    public Host(Long hostid) {
        this.hostid = hostid;
    }

    public Host(Long hostid, String ip, String macAddress, String name, float cpuGhz, int maxCores, float maxRam) {
        this.hostid = hostid;
        this.ip = ip;
        this.macAddress = macAddress;
        this.name = name;
        this.cpuGhz = cpuGhz;
        this.maxCores = maxCores;
        this.maxRam = maxRam;
    }

    public Long getHostid() {
        return hostid;
    }

    public void setHostid(Long hostid) {
        this.hostid = hostid;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getCpuGhz() {
        return cpuGhz;
    }

    public void setCpuGhz(float cpuGhz) {
        this.cpuGhz = cpuGhz;
    }

    public int getMaxCores() {
        return maxCores;
    }

    public void setMaxCores(int maxCores) {
        this.maxCores = maxCores;
    }

    public float getMaxRam() {
        return maxRam;
    }

    public void setMaxRam(float maxRam) {
        this.maxRam = maxRam;
    }

    public Usage getUsage() {
        return usage;
    }

    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    @XmlTransient
    public List<Vms> getVmsList() {
        return vmsList;
    }

    public void setVmsList(List<Vms> vmsList) {
        this.vmsList = vmsList;
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
        if (!(object instanceof Host)) {
            return false;
        }
        Host other = (Host) object;
        if ((this.hostid == null && other.hostid != null) || (this.hostid != null && !this.hostid.equals(other.hostid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DataBase.Host[ hostid=" + hostid + " ]";
    }
    
}
