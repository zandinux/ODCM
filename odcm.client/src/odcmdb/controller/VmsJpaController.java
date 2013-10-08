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
import odcmdb.Vmusage;
import odcmdb.Host;
import odcmdb.Vms;
import odcmdb.VmsPK;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author zantaz
 */
public class VmsJpaController implements Serializable {

    public VmsJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Vms vms) throws PreexistingEntityException, Exception {
        if (vms.getVmsPK() == null) {
            vms.setVmsPK(new VmsPK());
        }
        vms.getVmsPK().setHostid(vms.getHost().getHostid());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Vmusage vmusage = vms.getVmusage();
            if (vmusage != null) {
                vmusage = em.getReference(vmusage.getClass(), vmusage.getVmusagePK());
                vms.setVmusage(vmusage);
            }
            Host host = vms.getHost();
            if (host != null) {
                host = em.getReference(host.getClass(), host.getHostid());
                vms.setHost(host);
            }
            em.persist(vms);
            if (vmusage != null) {
                Vms oldVmsOfVmusage = vmusage.getVms();
                if (oldVmsOfVmusage != null) {
                    oldVmsOfVmusage.setVmusage(null);
                    oldVmsOfVmusage = em.merge(oldVmsOfVmusage);
                }
                vmusage.setVms(vms);
                vmusage = em.merge(vmusage);
            }
            if (host != null) {
                host.getVmsList().add(vms);
                host = em.merge(host);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findVms(vms.getVmsPK()) != null) {
                throw new PreexistingEntityException("Vms " + vms + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Vms vms) throws IllegalOrphanException, NonexistentEntityException, Exception {
        vms.getVmsPK().setHostid(vms.getHost().getHostid());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Vms persistentVms = em.find(Vms.class, vms.getVmsPK());
            Vmusage vmusageOld = persistentVms.getVmusage();
            Vmusage vmusageNew = vms.getVmusage();
            Host hostOld = persistentVms.getHost();
            Host hostNew = vms.getHost();
            List<String> illegalOrphanMessages = null;
            if (vmusageOld != null && !vmusageOld.equals(vmusageNew)) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("You must retain Vmusage " + vmusageOld + " since its vms field is not nullable.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (vmusageNew != null) {
                vmusageNew = em.getReference(vmusageNew.getClass(), vmusageNew.getVmusagePK());
                vms.setVmusage(vmusageNew);
            }
            if (hostNew != null) {
                hostNew = em.getReference(hostNew.getClass(), hostNew.getHostid());
                vms.setHost(hostNew);
            }
            vms = em.merge(vms);
            if (vmusageNew != null && !vmusageNew.equals(vmusageOld)) {
                Vms oldVmsOfVmusage = vmusageNew.getVms();
                if (oldVmsOfVmusage != null) {
                    oldVmsOfVmusage.setVmusage(null);
                    oldVmsOfVmusage = em.merge(oldVmsOfVmusage);
                }
                vmusageNew.setVms(vms);
                vmusageNew = em.merge(vmusageNew);
            }
            if (hostOld != null && !hostOld.equals(hostNew)) {
                hostOld.getVmsList().remove(vms);
                hostOld = em.merge(hostOld);
            }
            if (hostNew != null && !hostNew.equals(hostOld)) {
                hostNew.getVmsList().add(vms);
                hostNew = em.merge(hostNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                VmsPK id = vms.getVmsPK();
                if (findVms(id) == null) {
                    throw new NonexistentEntityException("The vms with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(VmsPK id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Vms vms;
            try {
                vms = em.getReference(Vms.class, id);
                vms.getVmsPK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The vms with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Vmusage vmusageOrphanCheck = vms.getVmusage();
            if (vmusageOrphanCheck != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Vms (" + vms + ") cannot be destroyed since the Vmusage " + vmusageOrphanCheck + " in its vmusage field has a non-nullable vms field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Host host = vms.getHost();
            if (host != null) {
                host.getVmsList().remove(vms);
                host = em.merge(host);
            }
            em.remove(vms);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Vms> findVmsEntities() {
        return findVmsEntities(true, -1, -1);
    }

    public List<Vms> findVmsEntities(int maxResults, int firstResult) {
        return findVmsEntities(false, maxResults, firstResult);
    }

    private List<Vms> findVmsEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Vms.class));
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

    public Vms findVms(VmsPK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Vms.class, id);
        } finally {
            em.close();
        }
    }

    public int getVmsCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Vms> rt = cq.from(Vms.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
