 /****************************************\
  *        UDP Connection to Hosts.      *
  *                                      *
  *      Created by Giorgos Perreas      *
  *                                      *
  * Technological Educational Institute  *
  *  		     of Lamia                *
  *                                      *
 \****************************************/

/*
 * - Periorismo tou receive() se leitourgeia mono gia migrate
 */

package odcm.UDP;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectToHost
{
    private String address;
    
    InetAddress HostAddress;

    private int HostPort;

    private DatagramPacket theSendPacket;

    private DatagramPacket theReceivedPacket;

    private DatagramSocket theSocket = null;

    // the place to store the sending and receiving data
    private byte[] outBuffer = new byte[500];

    private byte[] inBuffer = new byte[500];
    

    public void run( String address, String message ) throws SocketException, IOException
    {
        this.address = address;

        createSocketHost();

        send( message );

        receive();

        theSocket.close();
    }

    private void createSocketHost()
    {
        try
        {
            this.HostPort = 9999;
            
            theSocket = new DatagramSocket();

            //Address of Host - address
            InetAddress theHost = InetAddress.getByName(address);
            theSocket.connect( theHost, HostPort );

            System.out.println("Host socket created");
	}
        catch (SocketException ExceSocket)
	{
            System.out.println("Socket creation error  : "+ExceSocket.getMessage());
	}
	catch (UnknownHostException ExceHost)
	{
            System.out.println("Socket host unknown : "+ExceHost.getMessage());
	}
    }
//Send to ODCM_Client to DO MIGRATION
    private void send( String message )
    {

	try
        {

            outBuffer = message.getBytes();

            //Build packet to send to the Host
            theSendPacket = new DatagramPacket(outBuffer, outBuffer.length, theSocket.getInetAddress(), HostPort);
            
            //Send the data
            theSocket.send(theSendPacket);

            System.out.println("Message sending is : " + message);

        }
        catch (IOException ExceIO)
        {
            System.out.println("Host getting data error : "+ExceIO.getMessage());
        }
    }

    private boolean receive()
    {
        boolean ok = false;

        try
        {
            //Get RESULT Message for MIGRATION from Host
            theReceivedPacket = new DatagramPacket(inBuffer, inBuffer.length);

            theSocket.receive(theReceivedPacket);

            ok = Boolean.valueOf( new String( theReceivedPacket.getData(), 0, theReceivedPacket.getLength() ) );
            
            //Host response is...
            System.out.println("Host responce...\t" + ok );

            
        } 
        catch (IOException ExceIO)
        {
            System.out.println("Host getting data error : "+ExceIO.getMessage());
        }


        return ok;

    }
    
//https://compilr.com/safina/mac-address/WOL.java
    public void wake_on_lan( String mac ) throws IOException
    {
        
        this.HostPort = 9;
        
        this.address = "255.255.255.0";
        
        //port = 9
        createSocketHost();
        
        final byte[] MACBYTE = new byte[6];
        
        final String[] hex = mac.split("(\\:|\\-)");
        
        //MAC: 00-00-00-00-00-00
        for (int i = 0; i < 6; i++) {
            outBuffer[i] = (byte) Integer.parseInt(hex[i], 16);
        }
        
        //hex
        outBuffer = new byte[6 + 16 * MACBYTE.length];
        for (int i = 0; i < 6; i++) {
            outBuffer[i] = (byte) 0xff;
        }
        for (int i = 6; i < outBuffer.length; i += MACBYTE.length) {
            System.arraycopy(MACBYTE, 0, outBuffer, i, MACBYTE.length);
        }

        // Send UDP packet here
        theSendPacket = new DatagramPacket( outBuffer, 
                                                          outBuffer.length,
                                                          theSocket.getInetAddress(), 
                                                          HostPort 
                                                        );
        
        theSocket.send(theSendPacket);        
        theSocket.close();
        
        System.out.println( "Wake-on-LAN packet sent to:" );
        
    }
    
}