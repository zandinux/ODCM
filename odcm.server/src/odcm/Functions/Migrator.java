 /****************************************\
  *         Automatic Migration          * 
  *           Works with XEN             *
  *          (LIVE MIGRATION).           *
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
 * - O MIGRATOR NA ELEGXEI AN MPOREI NA SYMAZEVEI VMS KAI AN GINETAI NA KOITAEI AN MPOREI NA KLINEI
 *   KAPOION HOST THN PROHPOTHESI OTI MPOREI NA LEITOURGISEI TO CLUSTER ME ENAN LIGOTERO
 * 
 * 
 * - NA PROSTHETHEI LEITOYRGIA POU NA EPILEGEI O XRHSTHS TO VM 
 *   KAI NA STELNETAI STO KALYTERO HOST-RECEIVER
 * 
 * - NA PROSTHETHEI LEITOYRGIA POU NA EPILEGEIS HOST POU THES NA KLISEIS 
 *   KAI NA STELNONTE TA VMs SE HOSTs-RECEIVERs 
 * 
 */


package odcm.Functions;

import odcm.UDP.ConnectToHost;

import odcmdb.*;
import odcmdb.manager.DBM;
import odcmdb.manager.JDBC;
import odcmdb.manager.ReadDB;

import java.io.IOException;
import java.net.SocketException;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;


public class Migrator
{
    boolean Order_not_finish;
   
    ReadDB JPAread;
    
    JDBC jdbc;
    
    Topsis TOPSIS;
    
    
    HostPowerManager power;

    
    ConnectToHost udp;
    
    List<String> be_migrating;
    
    List<Host> Host_SenderList;
    List<Host> Host_SenderList_ordered;
    
    List<Vms> VmList;
    List<Vms> VmList_ordered;
    
    List<Host> Host_ReceiverList;
    List<Host> Host_ReceiverList_ordered;
    
    List<Host> Host_ReceiverForSpaceList;
    List<Host> Host_ReceiverForSpaceList_ordered;
    
    //Initialize Migrator with some Parameters and Methods
    public Migrator()
    {
        this.Order_not_finish = true;
        
        //Tools
        this.JPAread = new ReadDB();
        
        this.jdbc = new JDBC();
                
        this.TOPSIS = new Topsis();
        
        this.power = new HostPowerManager();
        
        this.udp = new ConnectToHost();
        
        //List
        this.be_migrating = new ArrayList();
        
        this.Host_SenderList = new ArrayList();
        this.Host_ReceiverForSpaceList_ordered = new ArrayList();
        
        this.VmList = new ArrayList();
        this.VmList_ordered = new ArrayList();
        
        this.Host_ReceiverList = new ArrayList();
        this.Host_ReceiverList_ordered = new ArrayList();
        
        this.Host_ReceiverForSpaceList = new ArrayList();
        this.Host_ReceiverForSpaceList_ordered = new ArrayList();
  
    }
    

    //Check if Automatic Migration( LoadBalance() ) can be running
    private boolean checkLoadBalance() throws SocketException, IOException
    {
        //AN GINEI ME POWER MANAGER NA PROSTHETHEI KAI O PERIORISMO NA EINAI AVAILABLE
        List<Host> list = JPAread.Hosts( "running" );
        
        //List<Host> list_sleep = JPAread.HostbyState( "sleep" );
        
        int vCores;
        
        //Max VCores used by VM or VMs in the cluster
        int MaxVmVCores = 0;
        
        //Total Max Cores on cluster
        int TotalMaxCores = 0;
               
        //Total Free Cores on cluster to use for Automatic Migrations
        int TotalfreeCores = 0;
        
        for( int i=0; i < list.size(); i++ )
        {
            
            TotalMaxCores += list.get(i).getMaxCores();
            
            TotalfreeCores += list.get(i).getUsage().getFreeCores();
            
            for( int j=0; j < list.get(i).getVmsList().size(); j++ )
            {
                vCores = list.get(i).getVmsList().get(j).getVCores();
                
                if( vCores > MaxVmVCores )
                {
                    MaxVmVCores = vCores;
                }
            }      
        }
        
        System.out.println( "Max:\t" + MaxVmVCores 
                          + "\tTotal MaxCores:\t" + TotalMaxCores 
                          + "\nFreeCores:\t" + TotalfreeCores );
        
        if( MaxVmVCores <= TotalfreeCores ) return true;
        //else if( MaxVmVCores <= TotalfreeCores ) power.enableHost(null, null);
        else return false;
        
    }
    
    
    //Check if host can be migrating vm's and will be close.
    private boolean checkOrderVMs( Host Sender )
    {
        boolean check_result = false;
        
        //Get list of Hosts without Sender
        List<Host> list = JPAread.Hosts( "running", Sender.getHostid() );
        
        int HostTotalUsedCores;
        

        if( list.size() > 0 )
        {
            
            HostTotalUsedCores = Sender.getMaxCores() -
                                 Sender.getUsage().getFreeCores();
            
            //Total Free Cores on cluster to use for Automatic Migrations
            int TotalfreeCores = 0;
            
            for( int i = 0; i < list.size(); i++ )
            {         
                TotalfreeCores += list.get(i).getUsage().getFreeCores();  
            
                System.out.println( "check---->" + list.get(i).getName() );
            }

            list.clear();
            
            System.out.println( "\nName:  " + Sender.getName()
			      + "\nMaxCores:  " + Sender.getMaxCores() 
                              + "\nFreeCores:  " + Sender.getUsage().getFreeCores()
                              + "\nHost Total Used Cores:\t" + HostTotalUsedCores 
                              + "\nTotal Free Cores On The Network:\t" + TotalfreeCores );

            
            if( TotalfreeCores == 0 ) check_result = false;
            else if( HostTotalUsedCores <= TotalfreeCores ) check_result = true;
            
        }
        else 
        {
            //Total Free Cores on cluster to use for Automatic Migrations
            int TotalfreeCores = 0;
            
            HostTotalUsedCores = Sender.getMaxCores() -
                                 Sender.getUsage().getFreeCores();
            
            if( TotalfreeCores == 0 ) check_result = false;
            else if( HostTotalUsedCores <= TotalfreeCores ) check_result = true;
            
            System.out.println( "\nName:  " + Sender.getName() 
                              + "\nMaxCores:  " + Sender.getMaxCores() 
                              + "\nFreeCores:  " + Sender.getUsage().getFreeCores() 
                              + "\nHost Total Used Cores:  " + HostTotalUsedCores );
            
        }
        
        return check_result;
              
    }

    
    //Check if host of list its valid to create space - TESTING METHOD
    private boolean checkComputeSpace( int Sender_vm_cores, Host final_host, long sender_id )
    {
        
        boolean check_result = false;
        
        Host final_host_bup = final_host;
        
        //Get list of Hosts without Sender and possibly final Receiver
        List<Host> hosts_remain = JPAread.Hosts( "running", sender_id, final_host.getHostid() );
        List<Host> hosts_remain_bup = hosts_remain;

        
        String do_migrate;
        /*
         * THELO METRITES VMCORES, POSIBLYFREECORES
         */
        for(int h=0; h < hosts_remain_bup.size(); h++)
        {
            for( int v=0; v < final_host_bup.getVmsList().size(); v++)
            {
                if( final_host_bup.getVmsList().get(v).getVCores() <=
                    hosts_remain_bup.get(h).getUsage().getFreeCores() )
                {
                    hosts_remain_bup.get(h).getVmsList().add( final_host_bup.getVmsList().get(h) );
                    
                    do_migrate = final_host_bup.getVmsList().get( v ).getListid() + " "
                                 + hosts_remain_bup.get(h).getIp();
                    
                    final_host_bup.getVmsList().remove(v);
                    
                    be_migrating.add(do_migrate);
                }
                
            }
            
        }
        
        if( Sender_vm_cores <= final_host_bup.getUsage().getFreeCores() )
        {
            check_result = true;
            
            return check_result;
        }
        else 
        {
            be_migrating.clear();
            
            return check_result;
        }
        
        
    }
    
    //NA DIORTHOTHEI - (LISTA APO TOPSIS)
    //Check for Load Balance on every Host and make Migrations
    public void LoadBalanceHosts() throws InterruptedException, SQLException, SocketException, IOException
    {
        if( checkLoadBalance() == true )
        {
            
            Host_SenderList = JPAread.Hosts( "running", ">" );

            int sender_pos;

            if( Host_SenderList.size() > 1 )
            {
                 Host_SenderList_ordered = TOPSIS.run("SENDER", "HOST", Host_SenderList);
                 
                 sender_pos = 0;
                 
            }
            else
            {
                sender_pos = 0;

                System.out.println( "\n-->Only ONE Host need Migration, We don't use Topsis<--\n" ); 
            }


            Host Sender = Host_SenderList.get( sender_pos );

            long sender_id = Sender.getHostid();

            Host_SenderList.clear();


            System.out.println( "\nFrom: " + Sender.getName() + "\n" );

            System.out.println("\n========================\n");


            int vmsend_pos;

            if( Sender.getVmsList().size() > 1 )
            {   

                VmList_ordered = TOPSIS.run( "SENDER", "VM", Sender.getVmsList() );


                vmsend_pos = 0;
                
                System.out.println("\nSelect: " + Sender.getVmsList().get(0).getVName());


                findReceiverHost( Sender, vmsend_pos );

            }
            else
            {

                vmsend_pos = 0;


                System.out.println( "\n-->Only ONE VM have this Host, We don't use Topsis<--\n" );

                System.out.println("\nSelect: " + Sender.getVmsList().get(0).getVName());


                findReceiverHost( Sender, vmsend_pos );

            }
            
        }
        else 
        {
            
            System.out.println( "ERROR:\tThere is not enough Cores to do Migration" );
            
        }
        

    }
    
        
    //Order Virtual Machines for not use all Hosts
    public void OrderVMs() throws InterruptedException, SQLException, SocketException, IOException
    {
           
        while( Order_not_finish )
        {
            
            Host_SenderList = JPAread.Hosts( "running", ">" );
            
            System.out.println( "\nWe decide for sender between " 
                                + Host_SenderList.size() + " Host:\n" );
            
            
            for( int i=0; i<Host_SenderList.size(); i++ )
            {
                System.out.println( Host_SenderList.get(i).getName() + "\n");
            }

            int sender_pos;

            if( Host_SenderList.size() > 1 )
            {
                //Winner of Selection
                Host_SenderList_ordered = TOPSIS.run( "SENDER", "HOST", Host_SenderList ); 
                
                sender_pos = 0;
                
                //epeidi exei ginei taksinomisi o xeiroteros einai stin thesi 0
                System.out.println( "\nWorst Host: " + sender_pos + " " 
                                    + Host_SenderList_ordered.get(sender_pos).getName() );
                
            }
            else
            {
                //Winner of Selection
                sender_pos = 0;
//ALLAGI DOYLEVOUME ME LISTA OXI ME POSITION
                System.out.println( "\n-->Only ONE Host need Migration, We don't use Topsis<--\n" );
                
            }
            

            //Winner of Selection
            Host Sender = Host_SenderList.get( sender_pos );
            //ALLAGI DOYLEVOUME ME LISTA OXI ME POSITION
            long sender_id = Sender.getHostid();
            
            Host_SenderList.clear();
            
            
            if ( checkOrderVMs( Sender ) == true )
            {                

                System.out.println( "\nFrom: " + Sender.getName() + "\n" );

                System.out.println("\n========================\n");

                
                //Run while send ALL VMs to Other Hosts
                while( !jdbc.checkMaxCores_equal_FreeCores( sender_id ) )
                {
                    int vmsend_pos;
                    //ALLAGI DOYLEVOUME ME LISTA OXI ME POSITION
                    if( Sender.getVmsList().size() > 1 )
                    {  
                    
                        VmList_ordered = TOPSIS.run("SENDER", "VM", Sender.getVmsList());
                    
                        vmsend_pos = 0;//Logo taksinomimenis listas
                        System.out.println("\nSelect: " + vmsend_pos + "  " 
                                           + Sender.getVmsList().get( vmsend_pos ).getVName());
                    
                    
                        findReceiverHost( Sender, vmsend_pos );
                    
                    }
                    else if( Sender.getVmsList().size() == 1 )
                    {
                    
                        vmsend_pos = 0;
                    //ALLAGI DOYLEVOUME ME LISTA OXI ME POSITION

                        System.out.println( "\n-->Only ONE VM have this Host, We don't use Topsis<--\n" );
                    
                        System.out.println("\nSelect: " + Sender.getVmsList().get( 0 ).getVName());
                    
                    
                        findReceiverHost( Sender, vmsend_pos );
                    
                    }
                    
                    
                    Sender = JPAread.Hosts( sender_id );
            
                }

                //close this Host who not have virtual machines
                power.disableHost( Sender.getHostid() );
                
                //We turn operation in sleep to take true values from DB
                Thread.sleep(1000);
                

            }
            else
            {

                System.out.println( "You cannot close any more Host,"
                                  + "\ncluster don't have much compute space."
                                  + "\n\nOrder Operation Done Successful" );
                
                Order_not_finish = false;
                
            }
            
            
        }             

    }

    
    //Find Receivers Hosts for VMs, we follow 3 Scenarios 
    private void findReceiverHost( Host Sender, int vmsend_pos ) throws InterruptedException, InterruptedException, SQLException, SocketException, IOException
    {

        System.out.println("\n========================\n");

        //Check before select RECEIVER
        Host_ReceiverList = JPAread.Hosts( "running", 
                                           Sender.getHostid(),
                                           ">=",
                                           Sender.getVmsList().get(vmsend_pos).getVCores()
                                         );

        
        //Print RECEIVER_LIST
        for( int i=0; i < Host_ReceiverList.size(); i++ )
        {
            System.out.println( "Choise: " + Host_ReceiverList.get(i).getName()
                              + "\tFreeCores:\t" + Host_ReceiverList.get(i).getUsage().getFreeCores() + "\n" );
        }


        int receiver_pos;
        //ALLAGI DOYLEVOUME ME LISTA OXI ME POSITION
        if( Host_ReceiverList.size() > 1 )
        {

            Host_ReceiverList_ordered = TOPSIS.run( "RECEIVER", "HOST", Host_ReceiverList );

            receiver_pos = 0;
            //Send message to this Host for migration VM
            System.out.println( "\nGo to: " + Host_ReceiverList_ordered.get( receiver_pos ).getName() );

            System.out.println("\n========================\n");


            //Build Message fot Send it To Host
            String message = Sender.getVmsList().get( vmsend_pos ).getListid() + " "
                           + Host_ReceiverList_ordered.get( receiver_pos ).getIp();

            
            udp.run( Sender.getIp(), message );

            
            //Erase Lists for new Desicion                            
            Host_ReceiverList.clear();
            Host_ReceiverList_ordered.clear();

        }
        else if( Host_ReceiverList.size() == 1 )
        {
//ALLAGI DOYLEVOUME ME LISTA OXI ME POSITION
            receiver_pos = 0;

            System.out.println( "-->Only ONE Host can RECEIVE this VM, We don't use Topsis<--\n" );

            
            //Send message to this Host for migration VM
            System.out.println( "\nGo to: " + Host_ReceiverList.get( receiver_pos ).getName() );

            System.out.println("\n========================\n");


            //Build Message fot Send it To Host
            String message = Sender.getVmsList().get( vmsend_pos ).getListid() + " "
                           + Host_ReceiverList.get( receiver_pos ).getIp();

            
            udp.run( Sender.getIp(), message );

            
            //Erase Lists for new Desicion
            Host_ReceiverList.clear();

        }
        else
        {   
//ALLAGI TO TI DINOYME STO CREATE COMPUTE SPACE
            Host_ReceiverList.clear();


            System.out.println( "-->You Cannot send this VM on this Host because it don't have much space<--\n" );


            CreateComputeSpace( Sender, vmsend_pos );

        }


    }
    
    
    //NA GINEI OPOSDIMPOTE TEST ME TO CREATECOMPUTESPACE
    
    //BinPackin Problem
    //Vasiko kritirio na apoteloun ta cores
    //IDEES: 1 NA GINETAI APOFASI GIA TA VMS KAI TOUS RECEIVERS ME VASI TA CORES
    
    //Create Compute Space if cores are not enough
    private void CreateComputeSpace( Host Sender, int vmsend_pos ) throws InterruptedException, SQLException, SocketException, IOException
    {
        
        int vmsend_cores = Sender.getVmsList()
                                 .get( vmsend_pos )
                                 .getVCores();
        
        
        //Find Receiver which create space, we get Hosts who have VMs
        Host_ReceiverForSpaceList = JPAread.Hosts( "running", Sender.getHostid(), ">", 0 );
        
        int final_receiver_pos = 0;
        
        
        Host Final_Receiver = new Host();
        
        Host temp_host = new Host();
       
        boolean foundit = false;
        
        Host_ReceiverForSpaceList_ordered = TOPSIS.run( "RECEIVER", "HOST", Host_ReceiverForSpaceList );
        //final_receiver_pos = 0;
        //CHECK Receiver with check()
        while( !Host_ReceiverForSpaceList_ordered.isEmpty() )
        {

            temp_host = Host_ReceiverForSpaceList_ordered.get( final_receiver_pos );
            
            if( checkComputeSpace( vmsend_cores, temp_host, Sender.getHostid() ) == true )
            {                
                Final_Receiver = temp_host;
                
                foundit = true;
                
                break;
            }
            else
            {
                Host_ReceiverForSpaceList_ordered.remove( final_receiver_pos );
            }
  
        }
        
        if( foundit == false )
        {
            //NA CHEKARO AN TERMATIZEI ME AYTO TON TROPO
            Order_not_finish = false;   
        }
        else
        {
    
            long final_receiver_id = Final_Receiver.getHostid();
        
            
            for( int m=0; m < be_migrating.size(); m++ ) 
            {
            //THELEI DIORTHOSI

                System.out.println( "\nWill send to:\t" + Final_Receiver.getIp() 
                                   +"\nthis:\t" + be_migrating.get(m) );


                udp.run( Final_Receiver.getIp(), be_migrating.get(m) );
     

            }

            //Build Message for Finaly Send it To Host
            String message = Sender.getVmsList().get( vmsend_pos ).getListid() + " "
                           + Final_Receiver.getIp();


            System.out.println( "\nWill send to:\t" + Sender.getIp() 
                               +"\nthis:\t" + message );


            udp.run( Sender.getIp(), message );


            //Erase Lists for new Desicion 
            Host_ReceiverList.clear();
            Host_ReceiverForSpaceList.clear();
            Host_ReceiverForSpaceList_ordered.clear();
              
        }
                
    }
    
}

/*
   //APO EDO YPARXEI THEMA
   * while(
                   ( vmsend_cores >= DBM.getUsage_ctrl().findUsage( final_receiver_id )
                                                        .getFreeCores() 
                   )  
                 )
            {
                for( int i=0; i < DBM.getHost_ctrl().findHost( final_receiver_id ).getVMsList().size(); i++ )
                {

                    int vcores = DBM.getHost_ctrl().findHost( final_receiver_id ).getVMsList().get(i).getVCores();


                    if( ( vcores > 0 ) && ( vcores == 1 || vcores < vmsend_cores ) )
                    {

                        VmList.add( 
                                    DBM.getHost_ctrl()
                                       .findHost( final_receiver_id )
                                       .getVMsList()
                                       .get(i)
                                  );

                    }

                }
             }
               //EWS EDO

*/