 /****************************************\
  *     Queries with JPA for ODCM_DB.    *
  *                                      *
  *      Created by Giorgos Perreas      *
  *                                      *
  * Technological Educational Institute  *
  *  		     of Lamia                *
  *                                      *
 \****************************************/


package odcmdb.manager;

import odcmdb.Host;
import java.util.List;
import javax.persistence.Query;




public class ReadDB
{

    public Host Hosts( long id )
    {
        String query = "SELECT h "
                     + "FROM Host h "
                     + "WHERE h.hostid = :hostid";
        
        
        
        Query q = DBM.getEm_host().createQuery( query )
                                  .setParameter( "hostid", id );


        Host host = (Host) q.getSingleResult();
        
        //Clear Cache
        DBM.getEm_host().clear();
        
        DBM.getEm_host().getEntityManagerFactory().getCache().evictAll();
        
        
        return host;

    }
    
    //Get Host List by State
    public List<Host> Hosts( String state )
    {

        String query = "SELECT h "
                     + "FROM Host h "
                     + "WHERE h.hostid IN "
                     + "(SELECT u.hostid "
                     + "FROM Usage u "
                     + "WHERE u.state = :state)";
        
        
        Query q = DBM.getEm_host().createQuery( query )
                                  .setParameter( "state", state );


        List<Host> list = q.getResultList();
        
        //Clear Cache
        DBM.getEm_host().clear();
        
        DBM.getEm_host().getEntityManagerFactory().getCache().evictAll();

        
        return list;
    }
    
    
    //Get Host List by State AND NOT include specific ID
    public List<Host> Hosts( String s, long not_id )
    {

        String query = "SELECT h "
                     + "FROM Host h "
                     + "WHERE h.id IN "
                     + "(SELECT u.hostid "
                     + "FROM Usage u "
                     + "WHERE u.state = :state "
                     + "AND u.hostid != :hostid)";
        
        
        Query q = DBM.getEm_host().createQuery( query )
                                  .setParameter( "state", s )
                                  .setParameter( "hostid", not_id );


        List<Host> list = q.getResultList();
        
        //Clear Cache
        DBM.getEm_host().clear();
        
        DBM.getEm_host().getEntityManagerFactory().getCache().evictAll();

        
        return list;
    }
    
    
    //Get Host List by State AND NOT include specific ID
    public List<Host> Hosts( String s, long not_id, long not_id2 )
    {

        String query = "SELECT h "
                     + "FROM Host h "
                     + "WHERE h.id IN "
                     + "(SELECT u.hostid "
                     + "FROM Usage u "
                     + "WHERE u.state = :state "
                     + "AND u.hostid != :hostid"
                     + "AND u.hostid != :hostid)";
        
        
        Query q = DBM.getEm_host().createQuery( query )
                                  .setParameter( "state", s )
                                  .setParameter( "hostid", not_id )
                                  .setParameter( "hostid", not_id2 );


        List<Host> list = q.getResultList();
        
        //Clear Cache
        DBM.getEm_host().clear();
        
        DBM.getEm_host().getEntityManagerFactory().getCache().evictAll();

        
        return list;
    }
    
    
    //Get Sender Hosts List
    public List<Host> Hosts( String state, String inequality )
    {
        
        //Inequality not be < because MaxCores cannot be smaller than FreeCores
        
        String query =  "SELECT h "
                      + "FROM Host h "
                      + "WHERE h.hostid IN "
                      + "(SELECT u.hostid "
                      + "FROM Usage u "
                      + "WHERE u.state = :state "
                      + "AND h.maxCores " + inequality + " u.freeCores)";
        
        
        Query q = DBM.getEm_host().createQuery( query )
                                  .setParameter( "state", state );
        
        
        List<Host> list = q.getResultList();
       
        //Clear Cache
        DBM.getEm_host().clear();
        
        DBM.getEm_host().getEntityManagerFactory().getCache().evictAll();
      
        
        
        return list;
    }
     
    //Get Receiver Hosts List
    public List<Host> Hosts( String state, long not_id, long not_id2, String inequality, int cores )
    {

        String query =  "SELECT h "
                      + "FROM Host h "
                      + "WHERE h.hostid IN "
                      + "(SELECT u.hostid "
                      + "FROM Usage u "
                      + "WHERE u.state = :state "
                      + "AND u.hostid != :hostid "
                      + "AND u.hostid != :hostid "
                      + "AND u.freeCores" + inequality + " :freeCores)";
        
        
        Query q = DBM.getEm_host().createQuery( query )
                                  .setParameter( "state", state )
                                  .setParameter("hostid", not_id)
                                  .setParameter("hostid", not_id2)
                                  .setParameter("freeCores", cores);
        
        List<Host> list = q.getResultList();
        
        //Clear Cache
        DBM.getEm_host().clear();
        
        DBM.getEm_host().getEntityManagerFactory().getCache().evictAll();

        
        
        return list;
    }
        
    //Get Receiver Hosts List
    public List<Host> Hosts( String state, long not_id, String inequality, int cores )
    {

        String query =  "SELECT h "
                      + "FROM Host h "
                      + "WHERE h.hostid IN "
                      + "(SELECT u.hostid "
                      + "FROM Usage u "
                      + "WHERE u.state = :state "
                      + "AND u.hostid != :hostid "
                      + "AND u.freeCores" + inequality + " :freeCores)";
        
        
        Query q = DBM.getEm_host().createQuery( query )
                                  .setParameter( "state", state )
                                  .setParameter("hostid", not_id)
                                  .setParameter("freeCores", cores);
        
        List<Host> list = q.getResultList();
        
        //Clear Cache
        DBM.getEm_host().clear();
        
        DBM.getEm_host().getEntityManagerFactory().getCache().evictAll();

        
        
        return list;
    }
    
    //Na ftiaxtei
    
    public float Cpu( String CreteriaDecision, long id )
    {
        float cpu = 0;
        
        String query = null;
        String column = null;
        
        if( CreteriaDecision.equals("HOST") )
        {
            query = "SELECT cpu FROM `Usage` WHERE hostid = :hostid";
            
            column = "Cpu";
        }
        else if( CreteriaDecision.equals("VM") )
        {     
            query = "SELECT VCpu FROM `Vmusage` WHERE vmid = " + id;
            
            column = "VCpu";
        }
        
                
        return cpu;
    }
    
    
    
}