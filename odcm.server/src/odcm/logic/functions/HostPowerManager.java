 /****************************************\
  *           Host Power Manager         *    
  *                                      *
  *      Created by Giorgos Perreas      *
  *                                      *
  * Technological Educational Institute  *
  *  		     of Lamia                *
  *                                      *
 \****************************************/

/*  
 *  -------------------    
 *  |   TODO LIST     |
 *  -------------------
 * 
 *  NA VRETHEI MIA SXESI-PRAKSI POSA WATT GLITONOUME SE KATHE MODE 
 * 
 *  MODE:
 *      1) POWER OFF
 *      2) SUSPEND
 *      3) HYBERNATE
 *      4) SUSPEND+HYPERNATE
 * 
 */

package odcm.logic.functions;

import odcmdb.Host;
import odcmdb.manager.DBM;
import odcmdb.manager.ReadDB;

import odcm.connect.udp.host.ConnectToHost;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;


public class HostPowerManager 
{
    
    ConnectToHost udp;
    
    ReadDB read;
    
    
    public HostPowerManager()
    {
        this.udp = new ConnectToHost();
        
        this.read = new ReadDB();
    }
    
    //Enable Host with WOL: Wake_on_Lan via UDP
    public void enableHost( Host host, String mode ) throws SocketException, IOException
    {
        
        udp.wake_on_lan( host.getMacAddress() );

    }
    
    
    //Disable host in 4 ways Power State
    public void disableHost( long id ) throws SocketException, IOException
    {
       //long id, host.getIp(),  DBM.getHost_ctrl().findHost(id).getName()
       //AN XREIAZETAI Host host = DBM.getHost_ctrl().findHost( id );
        
        
        /* Send Message: 1) suspend
         *               2) hibernate
         *               3) suspend-hybrid : SUSPEND+HYBERNATE
         *               4) shutoff
         */ 

        udp.run( DBM.getHost_ctrl().findHost(id).getIp(), "shutoff" );
        
        
        System.out.println( "\tShutdown DONE"
                          + "\n!!!!THANK YOU GOD!!!!" );
        
    }

}