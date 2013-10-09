 /****************************************\
  *      UDP Connection Echo Server.     *
  *                                      *
  *      Created by Giorgos Perreas      *
  *                                      *
  * Technological Educational Institute  *
  *  		     of Lamia                *
  *                                      *
 \****************************************/

/* Received Message: 1) SUSPEND
 *                   2) HIBERNATE
 *                   3) SUSPEND-HYBRID : SUSPEND+HYBERNATE
 *                   4) SHUTDOWN
 */ 

package odcm.connect.udp.server;


import java.io.*;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;


public class EchoServer
{
    
    DatagramSocket theSocket = null;

    DatagramChannel channel;

    int Port = 9999;

    DatagramPacket SendPacket;
    DatagramPacket ReceivedPacket;

    String message;
    
    String[] state = { 
                        "suspend", //0
                        "hibernate", //1
                        "suspend-hybrid", //2
                        "shutoff" //3
                     };

   /*Responce  
    * 0 - true -> migrate success
    * 1 - false -> migrate failed
    */
    String[] responce = { "true", "false" };

    // create some space for the text to send and recieve data
    //ByteBuffer outBuffer;
    //ByteBuffer inBuffer;
    byte[] outBuffer = new byte[500];
    byte[] inBuffer = new byte[500];

    
    public EchoServer()
    {
        try
        {
            
            // create the server UDP end point
            theSocket = new DatagramSocket( Port );

            //channel = DatagramChannel.open();
            
            //inBuffer = ByteBuffer.allocateDirect(500);

            System.out.println("UDP Socket (end point) created");

	}
        catch (SocketException ExceSocket)
	{
            System.out.println("Socket creation error : "+ ExceSocket.getMessage());
	}
    }

    
    //Get Received Message
    public String getMessage()
    {
        return message;
    }
    
    
    //Close socket when Host shutted down
    public void closeSocket()
    {
        theSocket.close();
    }

    
    //Receive Arguments for MIGRATION or POWER STATE
    public boolean receive() throws IOException, InterruptedException
    {

        ReceivedPacket = new DatagramPacket( inBuffer, inBuffer.length );

        theSocket.receive( ReceivedPacket );

        this.message = new String (ReceivedPacket.getData(), 0 , ReceivedPacket.getLength() );

        //System.out.println( "Server sended migration arguments:\t" + ReceivedPacket.getAddress() );
        if( ReceivedPacket.getAddress() != null )
        {
            System.out.println("------>>I receive a message, i do my best...!!");
            
            
            if( message.equals( state[0] ) ) return PowerState();
            else if( message.equals( state[1] ) ) return PowerState();
            else if( message.equals( state[2] ) ) return PowerState();
            else if( message.equals( state[3] ) ) return PowerState();
            else return Migration();
            
        }
        
        return true;

    }

    
    //Send Responce to ODCM_Server when MIGRATION its DONE
    private void send(int option) throws IOException
    {
        outBuffer = responce[option].getBytes();

        // send some data to the client
	SendPacket = new DatagramPacket(
                                         outBuffer, outBuffer.length,
                                         ReceivedPacket.getAddress(),
                                         ReceivedPacket.getPort()
                                       );

        theSocket.send( SendPacket );
    }

    
    //Do Migration request
    private boolean Migration() throws IOException, InterruptedException
    {
        /* 0 - host of vm on host list
           1 - destination ip for vm
        */
        
        String[] details = new String[2];

        String rgex = "\\s+";

        details = message.split( rgex );

        System.out.println("****LIVE MIGRATION START****");

        //Execute Migration
        Process p_vms = Runtime.getRuntime().exec( "xm migrate -l " + details[0] + " " + details[1] );

        //wait until finished
        p_vms.waitFor();

        if( p_vms.exitValue() != 0 )
        {

            System.out.println("Migrate failed");

            send(1);//migrate fail

        }
        else
        {

            System.out.println("Migrate succed");

            send(0);//migrate success

        }
        
        return true;

    }
    
    
    //Power request
    private boolean PowerState() throws IOException
    {
        
        Process power;
        
        System.out.println( "System will " + message + "...." );
        
        
        //Power Manager via Terminal
        if( message.equals( state[0] ) )  power = Runtime.getRuntime().exec( "pm-suspend" );
        else if( message.equals( state[1] ) ) power = Runtime.getRuntime().exec( "pm-hibernate" );
        else if( message.equals( state[2] ) ) power = Runtime.getRuntime().exec( "pm-suspend-hybrid" );
        //OTAN KANEI SHUTDOWN EXEI PERITHORIO ENOS LEPTOU 
        else if( message.equals( state[3] ) ) power = Runtime.getRuntime().exec( "shutdown" );
        
        
        //Send Responce to Server
        send(1);//ok
        
        //Close socket when Host shutted down
        closeSocket();
        
        
        System.out.println( "\n-->Goodbye :) (smile)<--" );
        
        return false;
        
    }
    
    
}