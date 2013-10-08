/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package odcmdb.controller;

import odcmdb.controller.exceptions.NonexistentEntityException;
import odcmdb.controller.exceptions.PreexistingEntityException;
import odcmdb.LogVcpu;
import odcmdb.LogVcpuPK;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import odcmdb.Vmusage;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author zantaz
 */
public class LogVcpuJpaController implements Serializable {

    public LogVcpuJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(LogVcpu logVcpu) throws PreexistingEntityException, Exception {
        if (logVcpu.getLogVcpuPK() == null) {
            logVcpu.setLogVcpuPK(new LogVcpuPK());
        }
        logVcpu.getLogVcpuPK().setHostid(logVcpu.getVmusage().getVmusagePK().getHostid());
        logVcpu.getLogVcpuPK().setVmid(logVcpu.getVmusage().getVmusagePK().getVmid());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Vmusage vmusage = logVcpu.getVmusage();
            if (vmusage != null) {
                vmusage = em.getReference(vmusage.getClass(), vmusage.getVmusagePK());
                logVcpu.setVmusage(vmusage);
            }
            em.persist(logVcpu);
            if (vmusage != null) {
                vmusage.getLogVcpuList().add(logVcpu);
                vmusage = em.merge(vmusage);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findLogVcpu(logVcpu.getLogVcpuPK()) != null) {
                throw new PreexistingEntityException("LogVcpu " + logVcpu + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(LogVcpu logVcpu) throws NonexistentEntityException, Exception {
        logVcpu.getLogVcpuPK().setHostid(logVcpu.getVmusage().getVmusagePK().getHostid());
        logVcpu.getLogVcpuPK().setVmid(logVcpu.getVmusage().getVmusagePK().getVmid());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            LogVcpu persistentLogVcpu = em.find(LogVcpu.class, logVcpu.getLogVcpuPK());
            Vmusage vmusageOld = persistentLogVcpu.getVmusage();
            Vmusage vmusageNew = logVcpu.getVmusage();
            if (vmusageNew != null) {
                vmusageNew = em.getReference(vmusageNew.getClass(), vmusageNew.getVmusagePK());
                logVcpu.setVmusage(vmusageNew);
            }
            logVcpu = em.merge(logVcpu);
            if (vmusageOld != null && !vmusageOld.equals(vmusageNew)) {
                vmusageOld.getLogVcpuList().remove(logVcpu);
                vmusageOld = em.merge(vmusageOld);
            }
            if (vmusageNew != null && !vmusageNew.equals(vmusageOld)) {
                vmusageNew.getLogVcpuList().add(logVcpu);
                vmusageNew = em.merge(vmusageNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                LogVcpuPK id = logVcpu.getLogVcpuPK();
                if (findLogVcpu(id) == null) {
                    throw new NonexistentEntityException("The logVcpu with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(LogVcpuPK id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            LogVcpu logVcpu;
            try {
                logVcpu = em.getReference(LogVcpu.class, id);
                logVcpu.getLogVcpuPK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The logVcpu with id " + id + " no longer exists.", enfe);
            }
            Vmusage vmusage = logVcpu.getVmusage();
            if (vmusage != null) {
                vmusage.getLogVcpuList().remove(logVcpu);
                vmusage = em.merge(vmusage);
            }
            em.remove(logVcpu);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<LogVcpu> findLogVcpuEntities() {
        return findLogVcpuEntities(true, -1, -1);
    }

    public List<LogVcpu> findLogVcpuEntities(int maxResults, int firstResult) {
        return findLogVcpuEntities(false, maxResults, firstResult);
    }

    private List<LogVcpu> findLogVcpuEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(LogVcpu.class));
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

    public LogVcpu findLogVcpu(LogVcpuPK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(LogVcpu.class, id);
        } finally {
            em.close();
        }
    }

    public int getLogVcpuCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<LogVcpu> rt = cq.from(LogVcpu.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
