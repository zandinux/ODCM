 /****************************************\
  *       Open Data Center Manager       *
  *         ( O.D.C.M - Client).         *
  *                                      *
  *      Created by Giorgos Perreas      *
  *                                      *
  * Technological Educational Institute  *
  *  		     of Lamia                *
  *                                      *
 \****************************************/


package odcm;

import odcmdb.controller.exceptions.IllegalOrphanException;
import odcmdb.controller.exceptions.NonexistentEntityException;
import odcmdb.manager.DBM;
import odcmdb.Usage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import odcm.connect.udp.server.EchoServer;

import odcm.driver.Xen;
import java.io.File;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import org.libvirt.*;
import org.libvirt.jna.*;


public class Main
{

    public static void main(String[] args) throws LibvirtException, IOException, InterruptedException
    {
        //Check or Create Directory
        /*String  directory = "/root/XenClient/XmlData";
        File folder = new File( directory );

        boolean create;

        if( !folder.exists() ) 
        {
            create = folder.mkdirs();
        }*/
        
        
        //Create Connection to listen Server for Migration
        final EchoServer udp = new EchoServer();

        final Xen driver = new Xen();
        
        /*Its help to sychonize very easy 2 threads: 
         *thread1 decide about thread2 if can run
         */
        final AtomicBoolean send_data_to_DB = new AtomicBoolean( true );

        
        Thread ServerRequest = new Thread( new Runnable() {

            @Override
            public void run()
            {
                try {
                    
                    while(true) 
                    {
                        send_data_to_DB.set( udp.receive() );
                    }
                    
                    
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }catch (Exception ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
                throw new UnsupportedOperationException("Not supported yet.");
            }
        } );
        
        
        Thread CollectData = new Thread( new Runnable() {

            @Override
            public void run()
            {               
                
                try
                {
                    while( send_data_to_DB.get() )
                    {
                        driver.exec();       
                    }
                    
                    if( send_data_to_DB.get() == false )
                    {
                        driver.setDEFAULT_VALUEStoDB( udp.getMessage() );
                    }
    
                    
                } catch (LibvirtException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
                throw new UnsupportedOperationException("Not supported yet.");
            }
        } );
           
        
        //Enable Threads
        CollectData.start();
        
        ServerRequest.start();
        

    }

}