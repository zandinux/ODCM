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
import odcmdb.Host;
import odcmdb.LogCpu;
import odcmdb.Usage;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author zantaz
 */
public class UsageJpaController implements Serializable {

    public UsageJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Usage usage) throws IllegalOrphanException, PreexistingEntityException, Exception {
        if (usage.getLogCpuList() == null) {
            usage.setLogCpuList(new ArrayList<LogCpu>());
        }
        List<String> illegalOrphanMessages = null;
        Host hostOrphanCheck = usage.getHost();
        if (hostOrphanCheck != null) {
            Usage oldUsageOfHost = hostOrphanCheck.getUsage();
            if (oldUsageOfHost != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("The Host " + hostOrphanCheck + " already has an item of type Usage whose host column cannot be null. Please make another selection for the host field.");
            }
        }
        if (illegalOrphanMessages != null) {
            throw new IllegalOrphanException(illegalOrphanMessages);
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Host host = usage.getHost();
            if (host != null) {
                host = em.getReference(host.getClass(), host.getHostid());
                usage.setHost(host);
            }
            List<LogCpu> attachedLogCpuList = new ArrayList<LogCpu>();
            for (LogCpu logCpuListLogCpuToAttach : usage.getLogCpuList()) {
                logCpuListLogCpuToAttach = em.getReference(logCpuListLogCpuToAttach.getClass(), logCpuListLogCpuToAttach.getLogCpuPK());
                attachedLogCpuList.add(logCpuListLogCpuToAttach);
            }
            usage.setLogCpuList(attachedLogCpuList);
            em.persist(usage);
            if (host != null) {
                host.setUsage(usage);
                host = em.merge(host);
            }
            for (LogCpu logCpuListLogCpu : usage.getLogCpuList()) {
                Usage oldUsageOfLogCpuListLogCpu = logCpuListLogCpu.getUsage();
                logCpuListLogCpu.setUsage(usage);
                logCpuListLogCpu = em.merge(logCpuListLogCpu);
                if (oldUsageOfLogCpuListLogCpu != null) {
                    oldUsageOfLogCpuListLogCpu.getLogCpuList().remove(logCpuListLogCpu);
                    oldUsageOfLogCpuListLogCpu = em.merge(oldUsageOfLogCpuListLogCpu);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findUsage(usage.getHostid()) != null) {
                throw new PreexistingEntityException("Usage " + usage + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Usage usage) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Usage persistentUsage = em.find(Usage.class, usage.getHostid());
            Host hostOld = persistentUsage.getHost();
            Host hostNew = usage.getHost();
            List<LogCpu> logCpuListOld = persistentUsage.getLogCpuList();
            List<LogCpu> logCpuListNew = usage.getLogCpuList();
            List<String> illegalOrphanMessages = null;
            if (hostNew != null && !hostNew.equals(hostOld)) {
                Usage oldUsageOfHost = hostNew.getUsage();
                if (oldUsageOfHost != null) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("The Host " + hostNew + " already has an item of type Usage whose host column cannot be null. Please make another selection for the host field.");
                }
            }
            for (LogCpu logCpuListOldLogCpu : logCpuListOld) {
                if (!logCpuListNew.contains(logCpuListOldLogCpu)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain LogCpu " + logCpuListOldLogCpu + " since its usage field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (hostNew != null) {
                hostNew = em.getReference(hostNew.getClass(), hostNew.getHostid());
                usage.setHost(hostNew);
            }
            List<LogCpu> attachedLogCpuListNew = new ArrayList<LogCpu>();
            for (LogCpu logCpuListNewLogCpuToAttach : logCpuListNew) {
                logCpuListNewLogCpuToAttach = em.getReference(logCpuListNewLogCpuToAttach.getClass(), logCpuListNewLogCpuToAttach.getLogCpuPK());
                attachedLogCpuListNew.add(logCpuListNewLogCpuToAttach);
            }
            logCpuListNew = attachedLogCpuListNew;
            usage.setLogCpuList(logCpuListNew);
            usage = em.merge(usage);
            if (hostOld != null && !hostOld.equals(hostNew)) {
                hostOld.setUsage(null);
                hostOld = em.merge(hostOld);
            }
            if (hostNew != null && !hostNew.equals(hostOld)) {
                hostNew.setUsage(usage);
                hostNew = em.merge(hostNew);
            }
            for (LogCpu logCpuListNewLogCpu : logCpuListNew) {
                if (!logCpuListOld.contains(logCpuListNewLogCpu)) {
                    Usage oldUsageOfLogCpuListNewLogCpu = logCpuListNewLogCpu.getUsage();
                    logCpuListNewLogCpu.setUsage(usage);
                    logCpuListNewLogCpu = em.merge(logCpuListNewLogCpu);
                    if (oldUsageOfLogCpuListNewLogCpu != null && !oldUsageOfLogCpuListNewLogCpu.equals(usage)) {
                        oldUsageOfLogCpuListNewLogCpu.getLogCpuList().remove(logCpuListNewLogCpu);
                        oldUsageOfLogCpuListNewLogCpu = em.merge(oldUsageOfLogCpuListNewLogCpu);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = usage.getHostid();
                if (findUsage(id) == null) {
                    throw new NonexistentEntityException("The usage with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Long id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Usage usage;
            try {
                usage = em.getReference(Usage.class, id);
                usage.getHostid();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The usage with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<LogCpu> logCpuListOrphanCheck = usage.getLogCpuList();
            for (LogCpu logCpuListOrphanCheckLogCpu : logCpuListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Usage (" + usage + ") cannot be destroyed since the LogCpu " + logCpuListOrphanCheckLogCpu + " in its logCpuList field has a non-nullable usage field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Host host = usage.getHost();
            if (host != null) {
                host.setUsage(null);
                host = em.merge(host);
            }
            em.remove(usage);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Usage> findUsageEntities() {
        return findUsageEntities(true, -1, -1);
    }

    public List<Usage> findUsageEntities(int maxResults, int firstResult) {
        return findUsageEntities(false, maxResults, firstResult);
    }

    private List<Usage> findUsageEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Usage.class));
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

    public Usage findUsage(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Usage.class, id);
        } finally {
            em.close();
        }
    }

    public int getUsageCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Usage> rt = cq.from(Usage.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
