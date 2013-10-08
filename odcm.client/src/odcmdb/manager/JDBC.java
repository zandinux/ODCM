 /****************************************\
  *       JDBC Queries for ODCM_DB.      *
  *                                      *
  *      Created by Giorgos Perreas      *
  *                                      *
  *         TEI LAMIAS AM:1880           *
  *                                      *
 \****************************************/

/*
 * NA FTIAXTEI EISODOS TIMWN GIA TO SYNDESI STHN VASI     DEDOMENON
 *
 * - NA PROSTHETHEI DELETE BY STATUS(SHUT OFF OR PAUSED BY USER)
 *
 *
 */
package odcmdb.manager;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.persistence.EntityManager;

public class JDBC
{

    public void DeleteRecords( String [] ListInActiveDomains, long host_id ) throws SQLException
    {
        EntityManager entitymanager = DBM.emf.createEntityManager();

        Statement stmt = null;

        entitymanager.getTransaction().begin();
        java.sql.Connection conn = entitymanager.unwrap(java.sql.Connection.class);

        stmt = conn.createStatement();


        String query ;
        int i=0;

        while ( i < ListInActiveDomains.length )
        {
            query = "DELETE a.*, b.* "
                  + "FROM VMUsage a "
                  + "INNER JOIN VMs b "
                  + "ON a.VMs_id = b.id "
                  + "WHERE b.VName = " + "'" + ListInActiveDomains[i] + "' "
                  + "AND a.VMs_Host_id = " + host_id;

            stmt.executeUpdate(query);
            i++;
        }

        stmt.close();

        entitymanager.getTransaction().commit();

    }

}
