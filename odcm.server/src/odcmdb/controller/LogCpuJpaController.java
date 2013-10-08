/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package odcmdb.controller;

import odcmdb.controller.exceptions.NonexistentEntityException;
import odcmdb.controller.exceptions.PreexistingEntityException;
import odcmdb.LogCpu;
import odcmdb.LogCpuPK;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import odcmdb.Usage;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author zantaz
 */
public class LogCpuJpaController implements Serializable {

    public LogCpuJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(LogCpu logCpu) throws PreexistingEntityException, Exception {
        if (logCpu.getLogCpuPK() == null) {
            logCpu.setLogCpuPK(new LogCpuPK());
        }
        logCpu.getLogCpuPK().setHostid(logCpu.getUsage().getHostid());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Usage usage = logCpu.getUsage();
            if (usage != null) {
                usage = em.getReference(usage.getClass(), usage.getHostid());
                logCpu.setUsage(usage);
            }
            em.persist(logCpu);
            if (usage != null) {
                usage.getLogCpuList().add(logCpu);
                usage = em.merge(usage);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findLogCpu(logCpu.getLogCpuPK()) != null) {
                throw new PreexistingEntityException("LogCpu " + logCpu + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(LogCpu logCpu) throws NonexistentEntityException, Exception {
        logCpu.getLogCpuPK().setHostid(logCpu.getUsage().getHostid());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            LogCpu persistentLogCpu = em.find(LogCpu.class, logCpu.getLogCpuPK());
            Usage usageOld = persistentLogCpu.getUsage();
            Usage usageNew = logCpu.getUsage();
            if (usageNew != null) {
                usageNew = em.getReference(usageNew.getClass(), usageNew.getHostid());
                logCpu.setUsage(usageNew);
            }
            logCpu = em.merge(logCpu);
            if (usageOld != null && !usageOld.equals(usageNew)) {
                usageOld.getLogCpuList().remove(logCpu);
                usageOld = em.merge(usageOld);
            }
            if (usageNew != null && !usageNew.equals(usageOld)) {
                usageNew.getLogCpuList().add(logCpu);
                usageNew = em.merge(usageNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                LogCpuPK id = logCpu.getLogCpuPK();
                if (findLogCpu(id) == null) {
                    throw new NonexistentEntityException("The logCpu with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(LogCpuPK id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            LogCpu logCpu;
            try {
                logCpu = em.getReference(LogCpu.class, id);
                logCpu.getLogCpuPK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The logCpu with id " + id + " no longer exists.", enfe);
            }
            Usage usage = logCpu.getUsage();
            if (usage != null) {
                usage.getLogCpuList().remove(logCpu);
                usage = em.merge(usage);
            }
            em.remove(logCpu);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<LogCpu> findLogCpuEntities() {
        return findLogCpuEntities(true, -1, -1);
    }

    public List<LogCpu> findLogCpuEntities(int maxResults, int firstResult) {
        return findLogCpuEntities(false, maxResults, firstResult);
    }

    private List<LogCpu> findLogCpuEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(LogCpu.class));
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

    public LogCpu findLogCpu(LogCpuPK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(LogCpu.class, id);
        } finally {
            em.close();
        }
    }

    public int getLogCpuCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<LogCpu> rt = cq.from(LogCpu.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
