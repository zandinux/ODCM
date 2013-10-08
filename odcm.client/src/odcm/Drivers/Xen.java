 /****************************************\
  *     Xen Driver for ODCM DataBase.    *
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
 * - NA DIORTHOTHEI O TIPOS TIS RAM KAI FREERAM STI BASI DEDOMENON
 * - NA MPOYN SXOLIA
 * - TA e.printStackTrace(); MPOROUN NA MPOUN SXOLIA KAI MIN EKTYPONEI KATI
 *
*/

package odcm.Drivers;


import odcmdb.controller.exceptions.IllegalOrphanException;
import odcmdb.controller.exceptions.NonexistentEntityException;
import odcmdb.manager.DBM;
import odcmdb.*;

import java.io.*;
import java.net.*;

/*
 * Monitor pou doulevei me to libvirt
  - CPU speed on Linux only reports
    correct values for Intel CPUs
*/
import com.jezhumble.javasysmon.JavaSysMon;


import java.math.BigInteger;

import java.util.ArrayList;

import java.util.List;
import java.util.UUID;



import org.libvirt.*;
import org.libvirt.jna.*;

//Xen_version2
public class Xen 
{
    
    Connect xen = null;
    

    JavaSysMon jmonitor = new JavaSysMon();//**PROKEITAI NA SVISTEI**
    

    Domain LocalDomain;

    NodeInfo HostNode;

    long host_id;
    
    /*
        The Id from 'xm list'
        pos = 0 -> Domain-0
    */
    int[] list_id;

    String[] ListInActiveDomains;

    int tDomains;   //Total Domains
    
    int MaxCores;

    int TotalVMsCoresUsed;
    
    long MaxRam;
    
    long TotalVMsRamUsed;
    
    //Log Data of Host cpu and vpu of VMs
    //Total backup on DB
    int TotalValues = 101;
    
    LogCpuPK[] log_cpuPK = new LogCpuPK[ TotalValues ];
    LogCpu[] log_cpu = new LogCpu[ TotalValues ];

    //id of value
    int value = 1;
    
    //Temp value of cpu and vcpu
    float temp_cpu;
    
    float temp_vcpu;

    //NetworkInterface For MacAddress
    NetworkInterface ni;

    InetAddress ipaddr;
    
    byte[] mac;
    
    String MAC;
    
    //Get Data for utilization ****DEN XREIAZETAI AKOMA****
    //XMLReader vm = new XMLReader();

    //Commands
    String vms_command = null;
    String images_command = null;

    File hd = new File( "/home" );//mporoume na dosoume tin dieuthinsi tou diskou me ta vms


    //Initialize First Arguments
    public Xen() throws LibvirtException, UnknownHostException, SocketException
    {

        //Connect to XEN
        this.xen = new Connect( "xen:///", true );
        
        
        if( !xen.isConnected() )
        {            
            System.out.println( "Not connected to (XEN)" );
            
            System.exit(-1);           
        }
        else
        {
            System.out.println( "Success connected (XEN)" );
        }
        

        //na doume pos leitourgei
        this.HostNode = xen.nodeInfo();
        

        //Get IP address & Hostname
        this.ipaddr = InetAddress.getLocalHost();
        
        //NetworkInterface
        this.ni = NetworkInterface.getByInetAddress(ipaddr);
        
        
        this.MAC = createMAC();
        
        //get ID
        this.host_id = createID();

        //Total Domains List
        this.tDomains = xen.numOfDomains();

        //Total INACTIVE DOMAINS-VMS(NOT RUN)
        this.ListInActiveDomains = xen.listDefinedDomains();

        //Get DOMAIN IDS and Import to int[] Array
        this.list_id = xen.listDomains();

        //Max Cores
        this.MaxCores = jmonitor.numCpus();
        
        //Max Ram
        //Conver to Mb with /1024/1024
        this.MaxRam = HostNode.memory/1024;
        
        //Initialize Log_Cpu Primary keys and Table
        for( int i=1; i < TotalValues; i++ )
        {
            this.log_cpuPK[i] = new LogCpuPK();
            
            this.log_cpu[i] = new LogCpu();
        }
        
        this.log_cpuPK = getLogCpuPK();
    }

    
    //Run Xen Driver
    public void exec() throws LibvirtException
    {

        try
        {

            //Write and Update To DB
            HostInfo();
            
            HostUsage();
            
            Log_cpu();
            
            if( ListInActiveDomains.length > 0 ) 
            {               
                DeleteInactiveVMsFromDB();                         
            }
            
            
            /*if( this.tDomains > 1 ) 
            {
                Vms();
            }

            
           /* Update Values For get Informations
            * 1 - tDomains
            * 2 - list_id
            * 3 - ListInActiveDomains
            */
            this.tDomains = xen.numOfDomains();

            this.list_id = xen.listDomains();

            this.ListInActiveDomains = xen.listDefinedDomains();

            if( value == TotalValues ) this.value = 1;
            else this.value++;
            
            
            //System.out.println();

        }
        catch(Exception Xenrun)
        {
            Xenrun.printStackTrace();
        }

    }


    //Get and Update Host Informations
    private void HostInfo()
    {

        Host Host = new Host();
        
       
        try
        {

            if( DBM.getHost_ctrl().findHost( host_id ) == null )
            {
                //Initialize Host for First Time to ODCM_DataBase

                //Import ID (It's unique for Every Host
                Host.setHostid( host_id );

                //Import Ip
                Host.setIp( ipaddr.getHostAddress() );
                
                //Import MacAddress ( WE NEED IT FOR WAKE-ON-LAN )
                Host.setMacAddress( MAC );

                //Import Host Name
                Host.setName( ipaddr.getHostName() );

                //Get & Import Cpu (to GHz) - Na KRATAEI MONO TO .[1-9]
                Host.setCpuGhz( (float) HostNode.mhz/1000 );

                //Import Max Cpus Cores
                Host.setMaxCores( MaxCores );

                //Import Total Ram (Gb)
                Host.setMaxRam( (float) MaxRam / 1024 );//Convert to Gb with /1024


                Host.setUsage( DBM.getUsage_ctrl().findUsage( host_id ) );
                
                
                if( tDomains > 1 ) 
                {
                    Host.setVmsList( Vms() );//DBM.getVms_ctrl().findVMsEntities();
                }


                DBM.getHost_ctrl().create(Host);
                
            }
            else
            {

                Host = DBM.getHost_ctrl().findHost( host_id );

                //HostUsage();

                Host.setUsage( DBM.getUsage_ctrl().findUsage( host_id ) );

                //We Update if only a host have vms
                if( tDomains > 1 ) 
                {
                    Host.setVmsList( Vms() );//DBM.getVms_ctrl().findVMsEntities();
                }

                
                DBM.getEm_host().getEntityManagerFactory().getCache().evictAll();
                
                DBM.getHost_ctrl().edit( Host );

            }

        }
        catch( Exception Hostinfo )
        {
            //Hostinfo.printStackTrace();
        }

        //return Host; type = Host
    }


    //Get and Update Host Usage
    private void HostUsage()
    {

        Usage usage = new Usage(); //Host Usage

        try
        {
            
            LocalDomain = xen.domainLookupByID( list_id[0] );

            
            //Import HD free memory******NOT NEED YET
            /*float freegb = hd.getFreeSpace()/1024/1024/1024;

            Host_usage.setHDfreemem( freegb );
            *
            */
            if( DBM.getUsage_ctrl().findUsage( host_id ) == null )
            {
                
                usage.setHostid( host_id );
                
                
                usage.setState( getState( LocalDomain ) );
                
                usage.setCpu( CpuUsage( LocalDomain.getName(), MaxCores, tDomains ) );
                              
                //**FREECORES**
                usage.setFreeCores( FreeCores() );

                //Import free Ram (Mb)
                usage.setRam( (float) FreeRam() );
                
                usage.setLogCpuList( DBM.getLogCpu_ctrl().findLogCpuEntities() );
                
                
                DBM.getUsage_ctrl().create( usage );

            }
            else
            {
                
                usage = DBM.getUsage_ctrl().findUsage( host_id );
                
                
                usage.setState( getState( LocalDomain ) );
                
                usage.setCpu( CpuUsage( LocalDomain.getName(), MaxCores, tDomains ) );
                
                //For Log_Cpu Table
                temp_cpu = usage.getCpu();

                //**FREECORES**
                usage.setFreeCores( FreeCores() );

                //Import free Ram (Mb)
                usage.setRam( FreeRam() );
                
                
                usage.setLogCpuList( DBM.getLogCpu_ctrl().findLogCpuEntities() );

                DBM.getEm_log_cpu().getEntityManagerFactory().getCache().evictAll();
                
                
                DBM.getUsage_ctrl().edit( usage );

            }

        }
        catch( LibvirtException e )
        {
            //System.out.println( "exception caught:"+e );
            //System.out.println( e.getError() );
        }
        catch (Exception ex)
        {
            //Logger.getLogger(Xen.class.getName()).log(Level.SEVERE, null, ex);
        }

        //return Host_usage; //type = Usage

    }

    
    //We keep log data for Host Cpu
    private void Log_cpu()
    {
        try
        {
            if( DBM.getLogCpu_ctrl().findLogCpu( log_cpuPK[ value ] ) == null )
            {
                
                log_cpu[ value ].setCpu( temp_cpu );

                log_cpu[ value ].setLogCpuPK( log_cpuPK[ value ] );
                
                log_cpu[ value ].setUsage( DBM.getUsage_ctrl().findUsage( host_id ) );
                
                DBM.getLogCpu_ctrl().create( log_cpu[ value ] );
            }
            else
            {
                log_cpu[ value ] = DBM.getLogCpu_ctrl().findLogCpu( log_cpuPK[ value ] );

                log_cpu[ value ].setCpu( temp_cpu );
                
                DBM.getLogCpu_ctrl().edit( log_cpu[ value ] );
            }    
        }
        catch (Exception ex)
        {
            //Logger.getLogger(Xen.class.getName()).log(Level.SEVERE, null, ex);
        }
          
    }
    
    //Initialize Log_Cpu Table Primary Keys: PK
    private LogCpuPK[] getLogCpuPK()
    {    
        for( int i=1; i < TotalValues; i++ )
        {     
           log_cpuPK[i].setIdValue( i );
           
           log_cpuPK[i].setHostid( host_id );
        }
        
        return log_cpuPK;
    }
    
    
    //Get and Update Information of Total Actice Virtual Machines
    private List Vms()
    {
               
        List<Vms> vmList = new ArrayList();//Test List will be UPDATE OF logic structure
        
        List<VmsPK> VmsPK = new ArrayList();
        
        List<Vmusage> vm_usage = new ArrayList();
        
        List<VmusagePK> vm_usagePK = new ArrayList();
        
        List<LogVcpu> log_vcpu = new ArrayList();
        
        List<LogVcpuPK> log_vcpuPK = new ArrayList();
        
        
        String VmName;
        
        int TotalValuesofVCPU = 101;
        int value = 1;
 
        try
        {
            
            for( int i=0; i < tDomains-1; i++ )
            {
                
                vmList.add( new Vms() );
                
                VmsPK.add( new VmsPK() );
                
                vm_usage.add( new Vmusage() );
                
                vm_usagePK.add( new VmusagePK() );
                
                log_vcpu.add( new LogVcpu() );
                
                log_vcpuPK.add( new LogVcpuPK() );
                

                LocalDomain = xen.domainLookupByID( list_id[i+1] );
                

                VmsPK.add( i, getVMsPK( LocalDomain ) );
                
                vm_usagePK.add( i, getVMUsagePK( VmsPK.get(i) ) );
                
                log_vcpuPK.add( i, getLogVcpuPK( VmsPK.get(i) ) );

                
                VmName = LocalDomain.getName();

                if( DBM.getVms_ctrl().findVms( VmsPK.get(i) ) == null )
                {

                    vmList.get(i).setVmsPK( VmsPK.get(i) );

                    vmList.get(i).setListid( list_id[i+1] );


                    vmList.get(i).setVName( VmName );


                    vmList.get(i).setVCores( LocalDomain.getMaxVcpus() );
                    

                    TotalVMsCoresUsed += vmList.get(i).getVCores();
                    
                    TotalVMsRamUsed += vmList.get(i).getVRam();
                    

                    vmList.get(i).setVRam( (int) LocalDomain.getMaxMemory()/1024 );


                    vmList.get(i).setHost( DBM.getHost_ctrl().findHost( host_id ) );


                    vm_usage.add( i, VmUsage( VmsPK.get(i), vm_usagePK.get(i), list_id[i+1] ) );
                    
                    log_vcpu.add( i, Log_vcpu( log_vcpuPK.get(i), vm_usagePK.get(i) ) );
                    
                    
                    vmList.get(i).setVmusage( DBM.getVmusage_ctrl().findVmusage( vm_usagePK.get(i) ) );


                    DBM.getVms_ctrl().create( vmList.get(i) );

                }
                else
                {

                    vmList.set( i, DBM.getVms_ctrl().findVms( VmsPK.get(i) ) );

                    vmList.get(i).setListid( list_id[i+1] );


                    vmList.get(i).setVName( VmName );


                    vmList.get(i).setVCores( LocalDomain.getMaxVcpus() );
                    

                    TotalVMsCoresUsed += vmList.get(i).getVCores();
                    
                    TotalVMsRamUsed += vmList.get(i).getVRam();
                    

                    vmList.get(i).setVRam( (int) LocalDomain.getMaxMemory()/1024 );

                    vmList.get(i).setHost( DBM.getHost_ctrl().findHost( host_id ) );


                    vm_usage.set( i, VmUsage( VmsPK.get(i), vm_usagePK.get(i), list_id[i+1] ) );
                    
                    log_vcpu.set( i, Log_vcpu( log_vcpuPK.get(i), vm_usagePK.get(i) ) );
                    

                    vmList.get(i).setVmusage( DBM.getVmusage_ctrl().findVmusage( vm_usagePK.get(i) ) );

                    
                    DBM.getVms_ctrl().edit( vmList.get(i) );

                }

            }

        }
        catch( LibvirtException e )
        {
            //System.out.println( "exception caught:"+e );
            //System.out.println( e.getError() );
        }
        catch( Exception VMlist )
        {
            VMlist.printStackTrace();
        }

        return vmList;

    }


    //Initialize VMs Table Primary Keys: PK
    private VmsPK getVMsPK( Domain dom ) throws LibvirtException
    {
        VmsPK vmsPK = new VmsPK();


        vmsPK = new VmsPK();

        vmsPK.setHostid( host_id );

        vmsPK.setVmid( Math.abs( (long) UUID.fromString( dom.getUUIDString() )
                                          .getMostSignificantBits() 
                             ) 
                   );


        return vmsPK;
    }


    //Get and Update Virtual Machines Usage
    private Vmusage VmUsage( VmsPK VmsPK, VmusagePK VmusagePK, int DomU_id )
    {

        Vmusage vm_usage = new Vmusage();

        try
        {

            LocalDomain = xen.domainLookupByID( DomU_id );

            String VmName = LocalDomain.getName();
            //System.out.println(  vmList.get(i) );

            if( DBM.getVmusage_ctrl().findVmusage( VmusagePK ) == null )
            {

                vm_usage.setVms( DBM.getVms_ctrl().findVms( VmsPK ) );


                vm_usage.setVCpu( CpuUsage( VmName, MaxCores, tDomains ) );
      
                
                vm_usage.setVState( getState( LocalDomain ) );

                vm_usage.setLogVcpuList( DBM.getLogVcpu_ctrl().findLogVcpuEntities() );
                
                DBM.getEm_log_vcpu().getEntityManagerFactory().getCache().evictAll();

                
                DBM.getVmusage_ctrl().create( vm_usage );

             }
             else
             {
                    
                vm_usage = DBM.getVmusage_ctrl().findVmusage( VmusagePK );

                vm_usage.setVCpu( CpuUsage( VmName, MaxCores, tDomains ) );
                
                //For Log_VCpu Table
                temp_vcpu = vm_usage.getVCpu();


                vm_usage.setVState( getState( LocalDomain ) );
                
                vm_usage.setLogVcpuList( DBM.getLogVcpu_ctrl().findLogVcpuEntities() ); 

                DBM.getEm_log_vcpu().getEntityManagerFactory().getCache().evictAll();

                
                DBM.getVmusage_ctrl().edit( vm_usage );

             }

        }
        catch( LibvirtException e )
        {
            //System.out.println( "exception caught:"+e );
            //System.out.println( e.getError() );
        }
        catch( Exception VMusage )
        {
            //VMusage.printStackTrace();
        }

        return vm_usage;// type VMUsage[]

    }


    //Initialize VMUsage Table Primary Keys: PK
    private VmusagePK getVMUsagePK( VmsPK VmsPK )
    {
        VmusagePK vm_usagePK = new VmusagePK();


        vm_usagePK.setHostid( host_id );

        vm_usagePK.setVmid( VmsPK.getVmid() );


        return vm_usagePK;
    }
    
    
    //We keep log data for every vm VCpu 
    private LogVcpu Log_vcpu( LogVcpuPK log_vcpuPK, VmusagePK VmusagePK )
    {
        LogVcpu log_vcpu = new LogVcpu();
        
        try
        {
            if( DBM.getLogVcpu_ctrl().findLogVcpu( log_vcpuPK ) == null )
            {
                log_vcpu.setLogVcpuPK( log_vcpuPK );

                log_vcpu.setVcpu( temp_vcpu );

                log_vcpu.setVmusage( DBM.getVmusage_ctrl().findVmusage( VmusagePK ) );


                DBM.getLogVcpu_ctrl().create( log_vcpu );
            }
            else
            {
                log_vcpu.setLogVcpuPK( log_vcpuPK );

                log_vcpu.setVcpu( temp_vcpu );

                log_vcpu.setVmusage( DBM.getVmusage_ctrl().findVmusage( VmusagePK ) );


                DBM.getLogVcpu_ctrl().edit( log_vcpu );
            }
        }
        catch( Exception LogVcpu )
        {
            //LogVcpu.printStackTrace();
        }
 
        
        return log_vcpu;
    }
    
    
    //Initialize Log_VCpu Table Primary Keys: PK
    private LogVcpuPK getLogVcpuPK( VmsPK VmsPK )
    {
        LogVcpuPK log_vcpuPK = new LogVcpuPK();
        
        log_vcpuPK.setIdValue(value);
        
        log_vcpuPK.setHostid( VmsPK.getHostid() );
        
        log_vcpuPK.setVmid( VmsPK.getVmid() );
        
        return  log_vcpuPK;
    }

    
    //Get Cpu% Usage with XenTop
    private float CpuUsage( String DomainName, int Mvcpus, int tDomains )
    {

        String line;

        String[] result = new String [ tDomains*2+2 ];//length tDomains*2+2
        String[] XenTop = new String [ 19 ];//19 elements of output

        String[] Names = new String [ tDomains ];//Length tDomains
        float[] tCpu = new float [ tDomains ];//temp array of cpu usage

        float Cpu = 0;

        //Recognition String we get from XenTop
        String rgex = "\\s+";
        String begin_line = "^\\s+";


        try
        {
            int i = 0, j = 0;

            Process p = Runtime.getRuntime().exec("xentop -b -i 2 -d.1");

            p.waitFor();
            if( p.exitValue() != 0 )
            {
                System.out.println("Xentop Command Failure");
            }

            BufferedReader in = new BufferedReader(
                                    new InputStreamReader( p.getInputStream() ) );


            while( (line = in.readLine()) != null )
            {
                result[i] = line;

                i++;
            }

            //place 1 is for names, place 4 is for cpu usage

            for( i=tDomains+2; i<result.length; i++ )//begin from tDomains+2
            {
                result[i] = result[i].replaceAll( begin_line, "" );

                XenTop = result[i].split( rgex );

                Names[j] = XenTop[0];
                tCpu[j] = Float.valueOf( XenTop[3] )/Mvcpus;
                //System.out.println( Names[j] + " " + tCpu[j] );
                j++;
            }

            for( i=0; i<tDomains; i++ )
            {
                if( Names[i].equals(DomainName) )
                {
                    Cpu = tCpu[i];
                }
            }

        }
        catch(Exception cpu)
        {
                //cpu.printStackTrace();
        }

        return Cpu;
    }


    //Calculate Freecores of Host
    private int FreeCores()
    {
        int cores;

        cores = MaxCores - TotalVMsCoresUsed;

        TotalVMsCoresUsed = 0;

        return cores;
    }
    
    
    //Calculate FreeRam of Host
    private float FreeRam()
    {
        float ram;
        
        ram = ( ( (float) TotalVMsRamUsed ) / MaxRam )*100;
        
        TotalVMsRamUsed = 0;
        
        return ram;
    }


    //Define State of Libvirt State_Flags
    private String getState( Domain LocalDomain )
    {

        String[] states = {
                            "blocked on resource", //0
                            "crashed", //1
                            "no state", //2
                            "paused by user", //3
                            "running", //4
                            "being shut down", //5
                            "shut off", //6
                          };

        String v_state = null;

        try
        {
           String temp = LocalDomain.getInfo().state.toString();

           if( temp.equals("VIR_DOMAIN_BLOCKED") ) v_state = states[0];
           else if( temp.equals("VIR_DOMAIN_CRASHED") ) v_state = states[1];
           else if( temp.equals("VIR_DOMAIN_NOSTATE") ) v_state = states[2];
           else if( temp.equals("VIR_DOMAIN_PAUSED") ) v_state = states[3];
           else if( temp.equals("VIR_DOMAIN_RUNNING") ) v_state = states[4];
           else if( temp.equals("VIR_DOMAIN_SHUTDOWN") ) v_state = states[5];
           else if( temp.equals("VIR_DOMAIN_SHUTOFF") ) v_state = states[6];
        }
        catch(Exception state)
        {
            //state.printStackTrace();
        }

        return v_state;

    }
    
    
    //Create ID : mac Address to Long Value
    private long createID() throws SocketException
    {
        
        long id = 0;
       
        
        //Get MacAddress from NetworkInterface
        mac = ni.getHardwareAddress();

        //cast from byte[] to BigInteger
        BigInteger n = new BigInteger(mac); 

        //ID
        id = Math.abs( n.longValue() );

        
        return id;
        
    }
    
  
    //Create MacAddress to string
    //http://www.mkyong.com/java/how-to-get-mac-address-in-java/
    private String createMAC() throws SocketException
    {
        
        //Get Mac Address to byte[]
        StringBuilder MacAddr = new StringBuilder();
        
        byte[] mac = ni.getHardwareAddress();
        
        for (int i = 0; i < mac.length; i++) 
        {
            MacAddr.append( String.format( "%02X%s", mac[i], (i < mac.length - 1) ? ":" : "" ) );
        }
              
        //String MacAddr = sb.toString();
        
        
        return MacAddr.toString();
        
    }
    
      
    //Delete InActive VMs From Database or VMs which Migrated
    private void DeleteInactiveVMsFromDB() throws NonexistentEntityException, IllegalOrphanException
    {
        //Delete InActive VM from DB
        List<Vms> DelVM = DBM.getHost_ctrl().findHost(host_id).getVmsList();
        
        for( int i=0; i < DelVM.size(); i++ )
        {
            for( int j=0; j < ListInActiveDomains.length; j++ )
            {
                //System.out.println( "List Domains to Delete:\t" + "migrating-" + DelVM.get(i).getVName() );

                if( DelVM.get(i).getVName().equals(ListInActiveDomains[j])
                    && (DelVM.get(i).getVmsPK().getHostid() == host_id )
                    && (DelVM.get(i).getVmusage().getVmusagePK().getHostid() == host_id ) 
                  )
                {
                    
                    DBM.getVmusage_ctrl().destroy( DelVM.get(i).getVmusage().getVmusagePK() );
                    DBM.getVms_ctrl().destroy( DelVM.get(i).getVmsPK() );
                    
                    List<LogVcpu> Del_logVcpu = DelVM.get(i).getVmusage().getLogVcpuList();
                    
                    for(int l=0; l<Del_logVcpu.size(); l++)
                    {
                        DBM.getLogVcpu_ctrl().destroy( Del_logVcpu.get(l).getLogVcpuPK() );
                    }                 
                            
                }
                else if( ListInActiveDomains[j].equals("migrating-" + DelVM.get(i).getVName() ) )
                {

                    DBM.getVmusage_ctrl().destroy( DelVM.get(i).getVmusage().getVmusagePK() );
                    DBM.getVms_ctrl().destroy( DelVM.get(i).getVmsPK() );
                    
                    List<LogVcpu> Del_logVcpu = DelVM.get(i).getVmusage().getLogVcpuList();
                    
                    for(int l=0; l < Del_logVcpu.size(); l++)
                    {
                        DBM.getLogVcpu_ctrl().destroy( Del_logVcpu.get(l).getLogVcpuPK() );
                    }
                    
                }
                
            }
            
        }
        
    }

    
    /*Set DEFAULT VALUES to Usage Table 
    and set State that Received from Server via UDP connection*/
    public void setDEFAULT_VALUEStoDB( String state ) throws IllegalOrphanException, NonexistentEntityException, Exception
    {
        Usage usage = DBM.getUsage_ctrl().findUsage( host_id );
        
        usage.setCpu( 0.0f );
        
        usage.setFreeCores(0);
        
        usage.setRam(0);
        
        usage.setState( state );
                        

        DBM.getUsage_ctrl().edit( usage );

        DBM.getHost_ctrl().findHost( host_id ).setUsage( usage );
    }
    
    
    //Return values
    
    //Get Host ID
    public long getID()
    {
        return host_id;
    }
    
    
    //Get MAC Address
    public String getMAC()
    {
        return MAC;
    }
    
    
    //Get String Table of Inactive Virtual Machines
    public String[] getListInactiveVMs()
    {
        return ListInActiveDomains;
    }
    
}