 /****************************************\
  *       JDBC Queries for ODCM_DB.      *
  *                                      *
  *      Created by Giorgos Perreas      *
  *                                      *
  * Technological Educational Institute  *
  *  		     of Lamia                *
  *                                      *
 \****************************************/

package odcmdb.manager;

import odcmdb.Host;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.persistence.EntityManager;

public class JDBC
{
    
    //Get Cpu value from DataBase via JDBC Method
    public float Cpu( String CreteriaDecision, long id ) throws SQLException
    {
        //na ginei etc oste na krataei 2 dekadika psifia
        float cpu = 0;
        
        String query = null;
        String column = null;
        
        if( CreteriaDecision.equals("HOST") )
        {
            query = "SELECT Cpu FROM `Usage` WHERE Host_id = " + id;
            
            column = "Cpu";
        }
        else if( CreteriaDecision.equals("VM") )
        {     
            query = "SELECT VCpu FROM `VMUsage` WHERE VMs_id = " + id;
            
            column = "VCpu";
        }
            
        EntityManager entitymanager = DBM.emf.createEntityManager();

        Statement stmt = null;

        entitymanager.getTransaction().begin();
        java.sql.Connection conn = entitymanager.unwrap(java.sql.Connection.class);

        stmt = conn.createStatement();
        
            
        ResultSet rs = stmt.executeQuery(query);
        
        rs.next();
        cpu = rs.getFloat(column);

        stmt.close();

        entitymanager.getTransaction().commit();
        
        return cpu;        
        
    }

    
    //Check to Host by ID if MaxCores = FreeCores for OrderVMs()
    public boolean checkMaxCores_equal_FreeCores( long id ) throws SQLException
    {
        
        boolean result;
        
        String query = "SELECT IF( h.MaxCores = u.Freecores, 'true', 'false' )"
                      +"FROM `Host` h, `Usage` u "
                      +"WHERE h.id = u.Host_id "
                      +"AND h.id = " + id;
        
        EntityManager entitymanager = DBM.emf.createEntityManager();

        Statement stmt = null;

        entitymanager.getTransaction().begin();
        java.sql.Connection conn = entitymanager.unwrap(java.sql.Connection.class);

        stmt = conn.createStatement();
        
            
        ResultSet rs = stmt.executeQuery(query);
        
        rs.next();
        // 1 : First field because we get 1 result
        result = rs.getBoolean(1);

        stmt.close();

        entitymanager.getTransaction().commit();
        
        return result;
    }


}