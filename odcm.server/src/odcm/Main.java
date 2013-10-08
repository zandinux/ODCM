 /****************************************\
  *       Open Data Center Manager       *
  *         ( O.D.C.M - Server).         *
  *                                      *
  *      Created by Giorgos Perreas      *
  *                                      *
  * Technological Educational Institute  *
  *  		     of Lamia                *
  *                                      *
 \****************************************/

/*  -------------------    
 *  |   TODO LIST     |
 *  -------------------
 * 
 * - READ FROM DB
 * - MIGRATOR - EXPERIMENT - ginetai
 * - NA FTIAXTEI KALYTERA
 * - NA XRISIMOPOIOUME LISTES KAI PINAKES NA XRISIMOPOIOUNTAI TOPIKA AN XREIAZETAI - egine
 * - NA PROSTHETHEI PERIORISMOS PRIORITY ANALOGA KAI ME TON FORTO POY MAS LEEI OTI PREPEI NA KLISEI O HOST
 *
 */


package odcm;

import odcmdb.Host;
import odcmdb.manager.DBM;
import odcmdb.manager.JDBC;
import odcmdb.manager.ReadDB;

import odcm.Functions.Migrator;
import odcm.Functions.HostPowerManager;
import odcm.UDP.ConnectToHost;

import java.io.IOException;
import java.math.BigInteger;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;



public class Main
{

    public static void main(String[] args) throws InterruptedException, SQLException, SocketException, IOException
    {

        ReadDB read = new ReadDB();
        
        JDBC jdbc = new JDBC();
        
        

        
        //TEST READING OF DB
        /*long id = 1;
        
        Host test = read.Hosts(id);
        
        while(true)
        {
            System.out.println("\n\nMaxCores:  " + test.getMaxCores() 
                              +"\nFreeCores: " + test.getUsage().getFreeCores() 
                              +"\nVm:  " + test.getVMsList().get(0).getVCores() );

                
            test = read.Hosts(id);   
        }*/

        final Migrator migrator = new Migrator();
        
        Thread operation = new Thread( new Runnable() 
        {

            @Override
            public void run() 
            {
                try 
                {
                    System.out.println( "\n******Migrator Started******\n" );
                    
                    migrator.OrderVMs();
                    //while(true) migrator.LoadBalanceHosts();
                    
                } catch (InterruptedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SQLException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SocketException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }, "operation" ) ;
        
        operation.start();

    }

}