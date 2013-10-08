 /****************************************\
  *                TOPSIS                *    
  *   Technique of Order Preference by   *
  *     Similarity to Ideal Solution.    *
  *                                      *
  *      Created by Giorgos Perreas      *
  *                                      *
  * Technological Educational Institute  *
  *  		     of Lamia                *
  *                                      *
 \****************************************/


/* ------------------------------------ 
 * -   Take Decision for Migrator()   -
 * ------------------------------------
 * 
 * Based on paper: A new model for virtual machine migration
 *      &
 * TOPSIS2.pdf
 * 
 */


package odcm.Functions;


import odcmdb.manager.DBM;
import odcmdb.Host;
import odcmdb.Vms;

import java.sql.SQLException;

import java.util.Arrays;
import java.math.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Topsis
{
    
    private List list;

    
    private Average Average;
    
/*
 * Creteria-Weights (Column)
 *
 * More OR Less = MoL
 * VH = Very High
 */
    
/* ----------------
 * - Host_Weights -
 * ----------------
 * 1 - Cpu% = 9 VH
 * 2 - Ram = 4 MoL LOW
 * 3 - Cores (free) = 6 MoL HIGH
 * 4 - Cpu (GHz) = 9 VH
 * 5 - Ram (total) = 4 MoL LOW
 * 6 - Cores (total) = 6 MoL HIGH
 * 7 - VMs (total Run) = 3 LOW
 */

    private double[] Host_Weights = {
                                        9,
                                        4,
                                        6,
                                        9,
                                        4,
                                        6,
                                        3
                                    };

/* --------------
 * - VM_Weights -
 * --------------
 * 
 * 1 - Cpu% = 9 VH
 * 2 - Ram (total) = 7 Hight
 * 2 - Cores (total) = 6 MoL HIGH I' 7 = Hight
 *
 */
    
    private double[] VM_Weights = {
                                    9,
                                    4,
                                    6
                                  };

    public Topsis()
    {
        this.Average = new Average();
    }

    
    
    public List run( String FindDecision, String CreteriaSelection, List list ) throws InterruptedException, SQLException
    {
        this.list = Average.run( FindDecision, CreteriaSelection, list );
        
        double[][] Records = CreateTopsisTable( CreteriaSelection );

        int Rows = Records.length;
        int Columns = Records[0].length;

        double[][] V = new double[Rows][Columns];
        double[] positiveIdeal = new double[Columns];
        double[] negativeIdeal = new double[Columns];
        double[] positiveSeparation = new double[Rows];
        double[] negativeSeparation = new double[Rows];


        if( CreteriaSelection.equals("HOST") )
        {
            V = Step_1AND2( Records, Host_Weights, Rows, Columns );
        }
        else if( CreteriaSelection.equals("VM") )
        {
            V = Step_1AND2(Records, VM_Weights, Rows, Columns);
        }

        /*
          TIP: 0 - Ideal Positive Solution
               1 - Ideal Negative Solution
         */
        positiveIdeal = Step_3_IdealSolution( 0, V, Rows, Columns );

        negativeIdeal = Step_3_IdealSolution( 1, V, Rows, Columns );

        positiveSeparation = Step_4_Seperation( V, positiveIdeal, Rows, Columns );

        negativeSeparation = Step_4_Seperation( V, negativeIdeal, Rows, Columns );

        List ordered_list = Step_5( list, FindDecision, CreteriaSelection, positiveSeparation, negativeSeparation, Rows );

        
        return ordered_list;

    }

    
    
    private double[][] CreateTopsisTable( String CreteriaDecision ) throws InterruptedException, SQLException
    {

        if( CreteriaDecision.equals("HOST") )
        {
             List<Host> Hosts = list;
             
             double[][] Records = new double[ Hosts.size() ][ Host_Weights.length ];


             for( int i=0; i < Hosts.size(); i++ )
             {
                 Records[i][0] = Hosts.get(i).getUsage().getCpu();
                 Records[i][1] = Hosts.get(i).getUsage().getRam();
                 Records[i][2] = Hosts.get(i).getUsage().getFreeCores();
                 Records[i][3] = Hosts.get(i).getCpuGhz();
                 Records[i][4] = Hosts.get(i).getMaxRam();
                 Records[i][5] = Hosts.get(i).getMaxCores();
                 Records[i][6] = Hosts.get(i).getVmsList().size();

                 System.out.println("\nName: " + Hosts.get(i).getName() + 
                                    "\tCpu%: " + Records[i][0] + 
                                    "\tRam: " + Records[i][1] + 
                                    "\tFreeCores: " + Records[i][2] +
                                    "\tTotalVMs: " + Records[i][6] + "\n" );
             }

             return Records;
        }
        else if( CreteriaDecision.equals("VM") )
        {
            List<Vms> VMs = list;

            double[][] Records = new double[ VMs.size() ][ VM_Weights.length ];
         

            for( int i=0; i < VMs.size(); i++ )
            {
                 Records[i][0] = VMs.get(i).getVmusage().getVCpu();
                 Records[i][1] = VMs.get(i).getVRam();
                 Records[i][2] = VMs.get(i).getVCores();

                 System.out.println("\nName: " + VMs.get(i).getVName() + 
                                    "\tVCpu%: " + Records[i][0] + 
                                    "\tVRam: " + Records[i][1] + 
                                    "\tVCores: " + Records[i][2] + "\n" );
            }

            return Records;
        }
        else return null;//Error
    }

    
    
    private double[][] Step_1AND2 ( double[][] Records, double[] Weights, int Rows, int Columns )
    {
        double[][] NormalizeR = new double[Rows][Columns];
        double[] total = new double [Columns];
        Arrays.fill(total, 0);

        //STEP -1
        for( int i=0; i < Columns; i++ )
        {
            for( int j=0; j < Rows; j++)
            {
                total[i] += Math.pow(Records[j][i], 2);
            }
        }

        for( int i=0; i < Columns; i++ )
        {
            for( int j=0; j < Rows; j++)
            {
                NormalizeR[j][i] = Records[j][i]/total[i];
            }
        }

        //STEP -2
        double[][] V = new double[Rows][Columns];

        for( int i=0; i < Rows; i++ )
        {
            for( int j=0; j < Columns; j++)
            {
                V[i][j] = Weights[j]*NormalizeR[i][j];
            }
        }

        return V;

    }

    
    
    private double[] Step_3_IdealSolution ( int option, double[][] V, int Rows, int Columns )
    {
        if( option == 0)//Positive Solution
        {
            double[] positiveIdeal = new double[Columns];

            for( int i=0; i < Columns; i++ )
            {
                double max = 0;

                for( int j=0; j< Rows; j++ )
                {
                    while( V[j][i] > max ) max = V[j][i];
                    positiveIdeal[i] = max;
                }

            }

            return positiveIdeal;
        }
        else if( option == 1 )//Negative Solution
        {
            double[] negativeIdeal = new double[Columns];

            for( int i=0; i < Columns; i++ )
            {
                double min = 10;

                for( int j=0; j< Rows; j++ )
                {
                    while( V[j][i] < min ) min = V[j][i];
                    negativeIdeal[i] = min;
                }
            }

            return negativeIdeal;
        }
        else return null; //Error

        
    }

    
    
    private double[] Step_4_Seperation ( double[][] V, double[] IdealSolution, int Rows, int Columns )
    {
        double[] Separation = new double[Rows];
        Arrays.fill(Separation, 0);

        for( int i=0; i < Rows; i++ )
        {
           for( int j=0; j < Columns; j++ )
           {
               Separation[i] = Math.sqrt(
                       Math.pow( (
                        Separation[i] + Math.abs( IdealSolution[j]-V[i][j]) ), 2
                       ) );
           }
        }

        return Separation;
    }

    //ALLAGI NA EPISTREFEI LISTA
    //will be check in real and then change it 
    //- List list einai gia test
    private List Step_5 ( List list, String Decision, String CreteriaSelection, double[] positiveSeparation, double[] negativeSeparation, int Rows )
    {   
        
        if( CreteriaSelection.equals("HOST") )
        {
            
            List<Host> hosts = list;
            
            List<Host> ordered = new ArrayList();
            
            if( Decision.equals("SENDER") )
            {
                double[] C = new double[Rows];

                double worstSol;
                int position = 0;

                //Rows = Total Hosts
                for( int i=0; i < Rows; i++ )
                {
                    C[i] = negativeSeparation[i] / ( positiveSeparation[i]+negativeSeparation[i] );
                    System.out.println("\npos: " + i + "  " + C[i]*100);
                }

                worstSol = C[0];

                for( int i=0; i < Rows; i++ )
                {
                    while(C[i] > worstSol)
                    {
                        worstSol = C[i];
                        position = i;
                    }
                    
                    ordered.add( hosts.get(position) );
                }

                
                //PRINT
                for( int i=0; i < ordered.size(); i++ )
                {
                    System.out.println("\nWinner -> " + ordered.get(position).getName() + ": " + C[position]*100);
                }
            }
            else if( Decision.equals("RECEIVER") )
            {
                double[] C = new double[Rows];

                double bestSol;
                int position = 0;


                for( int i=0; i < Rows; i++ )
                {
                    C[i] = negativeSeparation[i] / ( positiveSeparation[i]+negativeSeparation[i] );
                    System.out.println("\n" + hosts.get(i).getName() + ": " + C[i]*100);
                }

                bestSol = C[0];

                for( int i=0; i < Rows; i++ )
                {
                    while(C[i] < bestSol)
                    {
                        bestSol = C[i];
                        position = i;
                    }
                }
                
                ordered.add( hosts.get(position) );
                
                //PRINT
                for( int i=0; i < ordered.size(); i++ )
                {
                    System.out.println("\nWinner -> " + ordered.get(position).getName() + ": " + C[position]*100);
                }
            }
            
            return ordered;
        }
        else if( CreteriaSelection.equals("VM") )
        {
            
            List<Vms> Vms = list;
            
            List<Vms> ordered = new ArrayList();

            if( Decision.equals("SENDER") )
            {
                double[] C = new double[Rows];

                double worstSol;
                int position = 0;

                //Rows = Total Hosts
                for( int i=0; i < Rows; i++ )
                {
                    C[i] = negativeSeparation[i] / ( positiveSeparation[i]+negativeSeparation[i] );
                    System.out.println("\npos: " + i + "  " + C[i]*100);
                }

                worstSol = C[0];

                for( int i=0; i < Rows; i++ )
                {
                    while(C[i] > worstSol)
                    {
                        worstSol = C[i];
                        position = i;
                    }
                    
                    ordered.add( Vms.get(position) );
                }

                
                //PRINT
                for( int i=0; i < ordered.size(); i++ )
                {
                    System.out.println("\nWinner -> " + ordered.get(position).getVName() + ": " + C[position]*100);
                }
            
            }
            
            return ordered;
            
        }
        else return null;

        
    }

}
