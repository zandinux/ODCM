 /****************************************\
  *     DataBase Manager for ODCM_DB.    *
  *                                      *
  *      Created by Giorgos Perreas      *
  *                                      *
  *         TEI LAMIAS AM:1880           *
  *                                      *
 \****************************************/

//DIAXEIRISTIS TWN ENTITIES CLASSEWN GIA SYNDESI-EGRAFI-ANANEOSI STIN BASI DEDOMENON



package odcmdb.manager;

import odcmdb.controller.LogCpuJpaController;
import odcmdb.controller.HostJpaController;
import odcmdb.controller.LogVcpuJpaController;
import odcmdb.controller.UsageJpaController;

import odcmdb.controller.VmsJpaController;
import odcmdb.controller.VmusageJpaController;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

public class DBM
{

    public static EntityManagerFactory emf = Persistence.createEntityManagerFactory("ODCM_ClientPU");


    private static HostJpaController host_ctrl = new HostJpaController( emf );

    private static UsageJpaController usage_ctrl = new UsageJpaController( emf );
    
    private static LogCpuJpaController log_cpu_ctrl = new LogCpuJpaController( emf );


    private static VmsJpaController vms_ctrl = new VmsJpaController( emf );

    private static VmusageJpaController vmusage_ctrl = new VmusageJpaController( emf );
    
    private static LogVcpuJpaController log_vcpu_ctrl = new LogVcpuJpaController( emf );
    

    private static EntityManager em_host = host_ctrl.getEntityManager();

    private static EntityManager em_usage = usage_ctrl.getEntityManager();

    private static EntityManager em_log_cpu = log_cpu_ctrl.getEntityManager();
    

    private static EntityManager em_vms = vms_ctrl.getEntityManager();

    private static EntityManager em_vmusage = vmusage_ctrl.getEntityManager();
    
    private static EntityManager em_log_vcpu = log_vcpu_ctrl.getEntityManager();





    /**
     * @return the host_ctrl
     */
    public static HostJpaController getHost_ctrl() {
        return host_ctrl;
    }

    /**
     * @return the em_host
     */
    public static EntityManager getEm_host() {
        return em_host;
    }

    /**
     * @return the usage_ctrl
     */
    public static UsageJpaController getUsage_ctrl() {
        return usage_ctrl;
    }

    /**
     * @return the em_usage
     */
    public static EntityManager getEm_usage() {
        return em_usage;
    }
    
    /**
     * @return the log_cpu_ctrl
     */
    public static LogCpuJpaController getLogCpu_ctrl() {
        return log_cpu_ctrl;
    }

    /**
     * @return the em_log_cpu
     */
    public static EntityManager getEm_log_cpu() {
        return em_log_cpu;
    }

    /**
     * @return the vms_ctrl
     */
    public static VmsJpaController getVms_ctrl() {
        return vms_ctrl;
    }

    /**
     * @return the em_vms
     */
    public static EntityManager getEm_vms() {
        return em_vms;
    }

    /**
     * @return the vmusage_ctrl
     */
    public static VmusageJpaController getVmusage_ctrl() {
        return vmusage_ctrl;
    }

    /**
     * @return the em_vmusage
     */
    public static EntityManager getEm_vmusage() {
        return em_vmusage;
    }
    
    /**
     * @return the log_vcpu_ctrl
     */
    public static LogVcpuJpaController getLogVcpu_ctrl() {
        return log_vcpu_ctrl;
    }

    /**
     * @return the em_log_vcpu
     */
    public static EntityManager getEm_log_vcpu() {
        return em_log_vcpu;
    }

}