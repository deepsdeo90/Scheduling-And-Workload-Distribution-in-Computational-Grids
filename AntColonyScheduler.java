package AntColonyProject;

import java.util.Calendar;

import java.util.LinkedList;
import java.util.Scanner;
import gridsim.*;


public class AntColonyScheduler {
    

	public static int noOfGridResouces;
	private static Scanner sc;
	public static Ants ant = new Ants();
	//Returns the number of grid resources
	public static int getnoOfGridResouces(){
		return noOfGridResouces;
	}

	public static void main(String[] args)
    {
        
        try
        {
            // First step: Initialize the GridSim package. 
            int num_user = 1;   // number of grid users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;  // mean don't trace GridSim events

            // list of files or processing names to be excluded from any
            // statistical measures
            String[] exclude_from_file = { "" };
            String[] exclude_from_processing = { "" };

            // the name of a report file to be written. 
            String report_name = null;

            // Initialize the GridSim package
            System.out.println("Initializing GridSim package");
            GridSim.init(num_user, calendar, trace_flag, exclude_from_file,
                    exclude_from_processing, report_name);
            sc = new Scanner(System.in);
            // Second step: Creates one or more GridResource objects
            System.out.print("Enter no of Grid Resources : ");
          
            noOfGridResouces = sc.nextInt();
            GridResource[] resource = new GridResource[10];
            for(int i=0;i<noOfGridResouces;i++){
            	 System.out.print("Enter Grid resource name : ");
            	 String name = sc.next();
            	 System.out.print("Enter no of Machines for Grid Resource"+i+" : ");
            	 int noOfMachines = sc.nextInt();
            	 System.out.print("Enter MIPS rating for "+i+" Grid Resource"+i+" : ");
            	 int mipsRating = sc.nextInt();
            	 
            	 //int noOfMachines = 2;
            	// int mipsRating = 400;
            	 String arch = "Sun Ultra";
                 String os = "Solaris";
                 double time_zone = 9.0; 
                 double cost = 3.0;  
                 double baud_rate = 100.0;
                
            	 resource[i] = createGridResource(name,noOfMachines,mipsRating,arch,os,time_zone,cost,baud_rate);
            	 
            }
            
            // Third step: Creates the GridletCreation object
            GridletCreation obj = new GridletCreation("GridletCreation", 560.00,noOfGridResouces );

            // Fourth step: Starts the simulation
            GridSim.startGridSimulation();

            // Final step: Prints the Gridlets when simulation is over
            GridletList newList = obj.getGridletList();
            GridletCreation.printGridletList(newList);
            GridletCreation.getPVMatrix();
           
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Unwanted errors happen");
        }
    }

    
     private static GridResource createGridResource(String name, int noOfMachines, int mipsRating,String arch, String os,double time_zone,double cost,double baud_rate)
     {
        System.out.println("Starting to create one Grid resource with " +
        		noOfMachines+" Machines ...");

      
        MachineList mList = new MachineList();
        System.out.println("Creates a Machine list");

        for(int i = 0;i< noOfMachines;i++){
        	/*System.out.print("Enter no of processing elements for Machine"+ i +" : " );
        	s = new Scanner(System.in);
			int pe = s.nextInt();*/
        	int pe = 4;
        	mList.add( new Machine(i, pe, mipsRating));   // First Machine
            System.out.println("Creates Machine" + i +  " that has "+pe+" PEs and " +
                    "stores it into the Machine list");
        }
        

        ResourceCharacteristics resConfig = new ResourceCharacteristics(arch, os, mList, ResourceCharacteristics.TIME_SHARED,time_zone, cost);

        System.out.println();
        System.out.println("Creates the properties of a Grid resource and " +
                "stores the Machine list");

          // communication speed
        long seed = 11L*13*17*19*23+1;
        double peakLoad = 0.0;        // the resource load during peak hour
        double offPeakLoad = 0.0;     // the resource load during off-peak hr
        double holidayLoad = 0.0;     // the resource load during holiday

        // incorporates weekends so the grid resource is on 7 days a week
        LinkedList<Integer> Weekends = new LinkedList<Integer>();
        Weekends.add(new Integer(Calendar.SATURDAY));
        Weekends.add(new Integer(Calendar.SUNDAY));

        // incorporates holidays. However, no holidays are set in this example
        LinkedList<Integer> Holidays = new LinkedList<Integer>();

       GridResource gridRes = null;
        try
        {
            gridRes = new GridResource(name, baud_rate, seed,
                resConfig, peakLoad, offPeakLoad, holidayLoad, Weekends,
                Holidays);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Finally, creates one Grid resource and stores " +
                "the properties of a Grid resource");

        return gridRes;
	}

    

} // end class

