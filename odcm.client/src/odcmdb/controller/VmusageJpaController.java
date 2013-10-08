/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package odcmdb.controller;

import odcmdb.controller.exceptions.IllegalOrphanException;
import odcmdb.controller.exceptions.NonexistentEntityException;
import odcmdb.controller.exceptions.PreexistingEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import odcmdb.Vms;
import odcmdb.LogVcpu;
import odcmdb.Vmusage;
import odcmdb.VmusagePK;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author zantaz
 */
public class VmusageJpaController implements Serializable {

    public VmusageJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Vmusage vmusage) throws IllegalOrphanException, PreexistingEntityException, Exception {
        if (vmusage.getVmusagePK() == null) {
            vmusage.setVmusagePK(new VmusagePK());
        }
        if (vmusage.getLogVcpuList() == null) {
            vmusage.setLogVcpuList(new ArrayList<LogVcpu>());
        }
        vmusage.getVmusagePK().setVmid(vmusage.getVms().getVmsPK().getVmid());
        vmusage.getVmusagePK().setHostid(vmusage.getVms().getVmsPK().getHostid());
        List<String> illegalOrphanMessages = null;
        Vms vmsOrphanCheck = vmusage.getVms();
        if (vmsOrphanCheck != null) {
            Vmusage oldVmusageOfVms = vmsOrphanCheck.getVmusage();
            if (oldVmusageOfVms != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("The Vms " + vmsOrphanCheck + " already has an item of type Vmusage whose vms column cannot be null. Please make another selection for the vms field.");
            }
        }
        if (illegalOrphanMessages != null) {
            throw new IllegalOrphanException(illegalOrphanMessages);
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Vms vms = vmusage.getVms();
            if (vms != null) {
                vms = em.getReference(vms.getClass(), vms.getVmsPK());
                vmusage.setVms(vms);
            }
            List<LogVcpu> attachedLogVcpuList = new ArrayList<LogVcpu>();
            for (LogVcpu logVcpuListLogVcpuToAttach : vmusage.getLogVcpuList()) {
                logVcpuListLogVcpuToAttach = em.getReference(logVcpuListLogVcpuToAttach.getClass(), logVcpuListLogVcpuToAttach.getLogVcpuPK());
                attachedLogVcpuList.add(logVcpuListLogVcpuToAttach);
            }
            vmusage.setLogVcpuList(attachedLogVcpuList);
            em.persist(vmusage);
            if (vms != null) {
                vms.setVmusage(vmusage);
                vms = em.merge(vms);
            }
            for (LogVcpu logVcpuListLogVcpu : vmusage.getLogVcpuList()) {
                Vmusage oldVmusageOfLogVcpuListLogVcpu = logVcpuListLogVcpu.getVmusage();
                logVcpuListLogVcpu.setVmusage(vmusage);
                logVcpuListLogVcpu = em.merge(logVcpuListLogVcpu);
                if (oldVmusageOfLogVcpuListLogVcpu != null) {
                    oldVmusageOfLogVcpuListLogVcpu.getLogVcpuList().remove(logVcpuListLogVcpu);
                    oldVmusageOfLogVcpuListLogVcpu = em.merge(oldVmusageOfLogVcpuListLogVcpu);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findVmusage(vmusage.getVmusagePK()) != null) {
                throw new PreexistingEntityException("Vmusage " + vmusage + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Vmusage vmusage) throws IllegalOrphanException, NonexistentEntityException, Exception {
        vmusage.getVmusagePK().setVmid(vmusage.getVms().getVmsPK().getVmid());
        vmusage.getVmusagePK().setHostid(vmusage.getVms().getVmsPK().getHostid());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Vmusage persistentVmusage = em.find(Vmusage.class, vmusage.getVmusagePK());
            Vms vmsOld = persistentVmusage.getVms();
            Vms vmsNew = vmusage.getVms();
            List<LogVcpu> logVcpuListOld = persistentVmusage.getLogVcpuList();
            List<LogVcpu> logVcpuListNew = vmusage.getLogVcpuList();
            List<String> illegalOrphanMessages = null;
            if (vmsNew != null && !vmsNew.equals(vmsOld)) {
                Vmusage oldVmusageOfVms = vmsNew.getVmusage();
                if (oldVmusageOfVms != null) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("The Vms " + vmsNew + " already has an item of type Vmusage whose vms column cannot be null. Please make another selection for the vms field.");
                }
            }
            for (LogVcpu logVcpuListOldLogVcpu : logVcpuListOld) {
                if (!logVcpuListNew.contains(logVcpuListOldLogVcpu)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain LogVcpu " + logVcpuListOldLogVcpu + " since its vmusage field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (vmsNew != null) {
                vmsNew = em.getReference(vmsNew.getClass(), vmsNew.getVmsPK());
                vmusage.setVms(vmsNew);
            }
            List<LogVcpu> attachedLogVcpuListNew = new ArrayList<LogVcpu>();
            for (LogVcpu logVcpuListNewLogVcpuToAttach : logVcpuListNew) {
                logVcpuListNewLogVcpuToAttach = em.getReference(logVcpuListNewLogVcpuToAttach.getClass(), logVcpuListNewLogVcpuToAttach.getLogVcpuPK());
                attachedLogVcpuListNew.add(logVcpuListNewLogVcpuToAttach);
            }
            logVcpuListNew = attachedLogVcpuListNew;
            vmusage.setLogVcpuList(logVcpuListNew);
            vmusage = em.merge(vmusage);
            if (vmsOld != null && !vmsOld.equals(vmsNew)) {
                vmsOld.setVmusage(null);
                vmsOld = em.merge(vmsOld);
            }
            if (vmsNew != null && !vmsNew.equals(vmsOld)) {
                vmsNew.setVmusage(vmusage);
                vmsNew = em.merge(vmsNew);
            }
            for (LogVcpu logVcpuListNewLogVcpu : logVcpuListNew) {
                if (!logVcpuListOld.contains(logVcpuListNewLogVcpu)) {
                    Vmusage oldVmusageOfLogVcpuListNewLogVcpu = logVcpuListNewLogVcpu.getVmusage();
                    logVcpuListNewLogVcpu.setVmusage(vmusage);
                    logVcpuListNewLogVcpu = em.merge(logVcpuListNewLogVcpu);
                    if (oldVmusageOfLogVcpuListNewLogVcpu != null && !oldVmusageOfLogVcpuListNewLogVcpu.equals(vmusage)) {
                        oldVmusageOfLogVcpuListNewLogVcpu.getLogVcpuList().remove(logVcpuListNewLogVcpu);
                        oldVmusageOfLogVcpuListNewLogVcpu = em.merge(oldVmusageOfLogVcpuListNewLogVcpu);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                VmusagePK id = vmusage.getVmusagePK();
                if (findVmusage(id) == null) {
                    throw new NonexistentEntityException("The vmusage with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(VmusagePK id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Vmusage vmusage;
            try {
                vmusage = em.getReference(Vmusage.class, id);
                vmusage.getVmusagePK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The vmusage with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<LogVcpu> logVcpuListOrphanCheck = vmusage.getLogVcpuList();
            for (LogVcpu logVcpuListOrphanCheckLogVcpu : logVcpuListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Vmusage (" + vmusage + ") cannot be destroyed since the LogVcpu " + logVcpuListOrphanCheckLogVcpu + " in its logVcpuList field has a non-nullable vmusage field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Vms vms = vmusage.getVms();
            if (vms != null) {
                vms.setVmusage(null);
                vms = em.merge(vms);
            }
            em.remove(vmusage);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Vmusage> findVmusageEntities() {
        return findVmusageEntities(true, -1, -1);
    }

    public List<Vmusage> findVmusageEntities(int maxResults, int firstResult) {
        return findVmusageEntities(false, maxResults, firstResult);
    }

    private List<Vmusage> findVmusageEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Vmusage.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Vmusage findVmusage(VmusagePK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Vmusage.class, id);
        } finally {
            em.close();
        }
    }

    public int getVmusageCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Vmusage> rt = cq.from(Vmusage.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
