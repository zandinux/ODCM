/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package odcmdb.controller;

import odcmdb.controller.exceptions.IllegalOrphanException;
import odcmdb.controller.exceptions.NonexistentEntityException;
import odcmdb.controller.exceptions.PreexistingEntityException;
import odcmdb.Host;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import odcmdb.Usage;
import odcmdb.Vms;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author zantaz
 */
public class HostJpaController implements Serializable {

    public HostJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Host host) throws PreexistingEntityException, Exception {
        if (host.getVmsList() == null) {
            host.setVmsList(new ArrayList<Vms>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Usage usage = host.getUsage();
            if (usage != null) {
                usage = em.getReference(usage.getClass(), usage.getHostid());
                host.setUsage(usage);
            }
            List<Vms> attachedVmsList = new ArrayList<Vms>();
            for (Vms vmsListVmsToAttach : host.getVmsList()) {
                vmsListVmsToAttach = em.getReference(vmsListVmsToAttach.getClass(), vmsListVmsToAttach.getVmsPK());
                attachedVmsList.add(vmsListVmsToAttach);
            }
            host.setVmsList(attachedVmsList);
            em.persist(host);
            if (usage != null) {
                Host oldHostOfUsage = usage.getHost();
                if (oldHostOfUsage != null) {
                    oldHostOfUsage.setUsage(null);
                    oldHostOfUsage = em.merge(oldHostOfUsage);
                }
                usage.setHost(host);
                usage = em.merge(usage);
            }
            for (Vms vmsListVms : host.getVmsList()) {
                Host oldHostOfVmsListVms = vmsListVms.getHost();
                vmsListVms.setHost(host);
                vmsListVms = em.merge(vmsListVms);
                if (oldHostOfVmsListVms != null) {
                    oldHostOfVmsListVms.getVmsList().remove(vmsListVms);
                    oldHostOfVmsListVms = em.merge(oldHostOfVmsListVms);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findHost(host.getHostid()) != null) {
                throw new PreexistingEntityException("Host " + host + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Host host) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Host persistentHost = em.find(Host.class, host.getHostid());
            Usage usageOld = persistentHost.getUsage();
            Usage usageNew = host.getUsage();
            List<Vms> vmsListOld = persistentHost.getVmsList();
            List<Vms> vmsListNew = host.getVmsList();
            List<String> illegalOrphanMessages = null;
            if (usageOld != null && !usageOld.equals(usageNew)) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("You must retain Usage " + usageOld + " since its host field is not nullable.");
            }
            for (Vms vmsListOldVms : vmsListOld) {
                if (!vmsListNew.contains(vmsListOldVms)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Vms " + vmsListOldVms + " since its host field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (usageNew != null) {
                usageNew = em.getReference(usageNew.getClass(), usageNew.getHostid());
                host.setUsage(usageNew);
            }
            List<Vms> attachedVmsListNew = new ArrayList<Vms>();
            for (Vms vmsListNewVmsToAttach : vmsListNew) {
                vmsListNewVmsToAttach = em.getReference(vmsListNewVmsToAttach.getClass(), vmsListNewVmsToAttach.getVmsPK());
                attachedVmsListNew.add(vmsListNewVmsToAttach);
            }
            vmsListNew = attachedVmsListNew;
            host.setVmsList(vmsListNew);
            host = em.merge(host);
            if (usageNew != null && !usageNew.equals(usageOld)) {
                Host oldHostOfUsage = usageNew.getHost();
                if (oldHostOfUsage != null) {
                    oldHostOfUsage.setUsage(null);
                    oldHostOfUsage = em.merge(oldHostOfUsage);
                }
                usageNew.setHost(host);
                usageNew = em.merge(usageNew);
            }
            for (Vms vmsListNewVms : vmsListNew) {
                if (!vmsListOld.contains(vmsListNewVms)) {
                    Host oldHostOfVmsListNewVms = vmsListNewVms.getHost();
                    vmsListNewVms.setHost(host);
                    vmsListNewVms = em.merge(vmsListNewVms);
                    if (oldHostOfVmsListNewVms != null && !oldHostOfVmsListNewVms.equals(host)) {
                        oldHostOfVmsListNewVms.getVmsList().remove(vmsListNewVms);
                        oldHostOfVmsListNewVms = em.merge(oldHostOfVmsListNewVms);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = host.getHostid();
                if (findHost(id) == null) {
                    throw new NonexistentEntityException("The host with id " + id + " no longer exists.");
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
            Host host;
            try {
                host = em.getReference(Host.class, id);
                host.getHostid();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The host with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Usage usageOrphanCheck = host.getUsage();
            if (usageOrphanCheck != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Host (" + host + ") cannot be destroyed since the Usage " + usageOrphanCheck + " in its usage field has a non-nullable host field.");
            }
            List<Vms> vmsListOrphanCheck = host.getVmsList();
            for (Vms vmsListOrphanCheckVms : vmsListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Host (" + host + ") cannot be destroyed since the Vms " + vmsListOrphanCheckVms + " in its vmsList field has a non-nullable host field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(host);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Host> findHostEntities() {
        return findHostEntities(true, -1, -1);
    }

    public List<Host> findHostEntities(int maxResults, int firstResult) {
        return findHostEntities(false, maxResults, firstResult);
    }

    private List<Host> findHostEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Host.class));
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

    public Host findHost(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Host.class, id);
        } finally {
            em.close();
        }
    }

    public int getHostCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Host> rt = cq.from(Host.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
