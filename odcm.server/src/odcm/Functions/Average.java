 /****************************************\
  *                                      *
  *       Weighted Moving Average.       *
  *                                      *
  *      Created by Giorgos Perreas      *
  *                                      *
  * Technological Educational Institute  *
  *  		     of Lamia                *
  *                                      *
 \****************************************/


/*http://daytrading.about.com/od/indicators/a/MovingAverages.htm
 * 
 *http://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average
 * 
 * Exponential Moving Average (AYTOS O TYPOS XRISIMOPOIITHIKE)

    Description: The exponential moving average is a weighted average of the last n prices, 
    where the weighting decreases exponentially with each previous price.
    Calculation: EMAn-1 + ((2 / (n + 1)) * (Pn - EMAn-1))
    Example: A 4 bar exponential moving average with prices of 
    1.5554, 1.5555, 1.5558, and 1.5560 would give a moving average of 1.5558 using the calculation 1.5556 + ((2 / (4 + 1)) * (1.5560 - 1.5556)) = 1.5558
 * 
 * Weighted Moving Average (AUTOS XRISIMOPOIITAI STO PROGRAMMA)

    Description: The weighted moving average is a weighted average of the last n prices, 
    where the weighting decreases by 1 with each previous price.
    Calculation: ((n * Pn) + ((n - 1) * Pn-1) + ((n - 2) * Pn-2) + ... ((n - (n - 1)) * Pn-(n-1)) / (n + (n - 1) + ... + (n - (n - 1)))
    Example: A 4 bar weighted moving average with prices of 1.2900, 1.2900, 1.2903, and 1.2904 would give a moving average of 1.2903 
    using the calculation ((4 * 1.2904) + (3 * 1.2903) + (2 * 1.2900) + (1 * 1.2900)) / (4 + 3 + 2+ 1) = 1.2903
 *
 * Simple Moving Average

    Description: The simple moving average is simply the average of the last n prices.
    Calculation: (P1 + P2 + P3 + P4 + ... + Pn) / n
    Example: A 4 bar simple moving average with prices of 1.2640, 1.2641, 1.2642, and 1.2641 
    would give a moving average of 1.2641 using the calculation (1.2640 + 1.2641 + 1.2642 + 1.2641) / 4 = 1.2641
    
 */

 /*Exponential Moving Average
  *
  * float alpha = 0.8f;
  * EMA[i] += alpha * ( cpu - EMA[i] );
  */


/*
 * TODO LIST
 * 
 * - ESTIMATE AVERAGE (REAL TIME + LOG FILE)
 * - DIAXORISMOS SENDERS APO RECEIVERS
 * - OPTION FILE GIA THRESHOLD
 */


package odcm.Functions;

import odcmdb.Host;
import odcmdb.Vms;

import odcmdb.manager.JDBC;

import java.util.List;

import java.sql.SQLException;


public class Average
{
    //MPOROUN NA EINAI OPTIONS GIA TO AVERAGE:  THRESHOLD, VM_THRESHOLD, delay, period
    float THRESHOLD = 50;
    float VM_THRESHOLD = 50;
    
    
    //2000 milliseconds 2 seconds
    long delay = 1000;
    
    //60000 milliseconds 1 minute
    long period = 10000;

    
    List list;
    
    
    JDBC jdbc = new JDBC();

    
    public List run( String FindDecision, String CreteriaSelection, List list ) throws InterruptedException, SQLException
    {
        if( FindDecision.equals("SENDER") )
        {
            this.list = Analysis( FindDecision, CreteriaSelection, list );
        }
        else if( FindDecision.equals("RECEIVER") )
        {   
            this.list = Analysis( FindDecision, CreteriaSelection, list );
        }

        return list;
    }

    
    //ALLAGI NA EPISTREFEI ALLA APOTELESMATA GIA SENDER KAI ALLA GIA RECEIVERS

    public List Analysis( String Decision, String CreteriaDecision, List list ) throws InterruptedException, SQLException
    {
        
        float N = (float) ( period/delay );
 

        float[] EWMA = new float[ list.size() ];
        
        float[] EMA = new float[ list.size() ];
        

        long startTime = System.currentTimeMillis();
            
        int step = 1;
            
        int total = 0;
        
        
        if( CreteriaDecision.equals("HOST") )
        {
            
            List<Host> Hosts = list;
            
            if( Decision.equals("SENDER") )
            {
                //Arxikopoihsh EMA logo kai apo WIKIPEDIA S1 = Y1
                for( int i = 0; i < Hosts.size(); i++ )
                {
                    EMA[i] = jdbc.Cpu( CreteriaDecision, Hosts.get(i).getHostid() );
                }

                float cpu;

                while ( ( System.currentTimeMillis() - startTime ) <  period )
                {
                        for( int i = 0; i < Hosts.size(); i++ )
                        {

                            cpu = jdbc.Cpu( CreteriaDecision, Hosts.get(i).getHostid() );

                            //Exponential Weighted Moving Average
                            EWMA[i] += step * cpu;                        


                            System.out.println( "value:  " + step 
                                              + "\tHost:\t" + Hosts.get(i).getName()
                                              + "\tCPU:\t" + cpu );

                        }
                        Thread.sleep(delay);

                        total += step;

                        step++;

                }

                for( int i = 0; i < EWMA.length; i++ )
                {
                    EWMA[i] = EWMA[i] / total;

                    Hosts.get(i).getUsage().setCpu( EWMA[i] );

                    System.out.println( "\nEWMA-CPU%:\t" + EWMA[i] );
                }


                for( int i = 0; i < EWMA.length; i++ )
                {   
                    if( EWMA[i] < THRESHOLD )
                    {
                        Hosts.remove(i);
                    }
                }    
            }
            else if( Decision.equals("RECEIVER") )
            {
                //Arxikopoihsh EMA logo kai apo WIKIPEDIA S1 = Y1
                for( int i = 0; i < Hosts.size(); i++ )
                {
                    EMA[i] = jdbc.Cpu( CreteriaDecision, Hosts.get(i).getHostid() );
                }

                float cpu;

                while ( ( System.currentTimeMillis() - startTime ) <  period )
                {
                        for( int i = 0; i < Hosts.size(); i++ )
                        {

                            cpu = jdbc.Cpu( CreteriaDecision, Hosts.get(i).getHostid() );

                            //Exponential Weighted Moving Average
                            EWMA[i] += step * cpu;                        


                            System.out.println( "value:  " + step 
                                              + "\tHost:\t" + Hosts.get(i).getName()
                                              + "\tCPU:\t" + cpu );

                        }
                        Thread.sleep(delay);

                        total += step;

                        step++;

                }

                for( int i = 0; i < EWMA.length; i++ )
                {
                    EWMA[i] = EWMA[i] / total;

                    Hosts.get(i).getUsage().setCpu( EWMA[i] );

                    System.out.println( "\nEWMA-CPU%:\t" + EWMA[i] );
                }


                //GIA TON RECEIVER DEN EKSETAZOYME THRESHOLD
                
                /*for( int i = 0; i < EWMA.length; i++ )
                {   
                    if( EWMA[i] < THRESHOLD )
                    {
                        Hosts.remove(i);
                    }
                } 
                */
            }
                     

            return Hosts;
        }
        else if( CreteriaDecision.equals("VM") )
        {
            List<Vms> Vms = list;

            while ( ( System.currentTimeMillis() - startTime ) <  period )
            {
                    for( int i = 0; i < Vms.size(); i++ )
                    {
                        //Weighted Moving Average
                        EWMA[i] += step * jdbc.Cpu( CreteriaDecision,  Vms.get(i).getVmsPK().getVmid() );
                        
                    }
                    Thread.sleep(delay);
                    
                    total += step;
                    
                    step++;
            }

            for( int i = 0; i < EWMA.length; i++ )
            {
                EWMA[i] = EWMA[i] / total;

                Vms.get(i).getVmusage().setVCpu( (float) EWMA[i] );
            }
/*
            for( int i = 0; i < EWMA.length; i++ )
            {
                if( EWMA[i] < VM_THRESHOLD )
                {
                    Vms.remove(i);
                }
            }
*/
            return Vms;

        }else return null;
    }
    
}