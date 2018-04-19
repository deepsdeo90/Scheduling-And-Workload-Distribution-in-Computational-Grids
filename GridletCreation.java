package AntColonyProject;
import java.util.Scanner;
import java.awt.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

import gridsim.*;

public class GridletCreation extends GridSim{

	private Integer ID_;
    private String name_;
    private GridletList list_;
    private GridletList receiveList_;
    private int totalResource_;   
	public static int noOfGridlets;
	private static double PV[][] = null;
	public static long[] gridletLength;
	public static double[] deadline;
	
    static ArrayList<Integer> resMIPS = new ArrayList<Integer>();
    static ArrayList<Integer> resFreePE = new ArrayList<Integer>();

	private Scanner in;
 
	public static Ants ant = new Ants();
    public GridletCreation(String name, double baud_rate, int total_resource)
            throws Exception
    {
        super(name, baud_rate);
        this.name_ = name;
        this.totalResource_ = total_resource;
        this.receiveList_ = new GridletList();

        // Gets an ID for this entity
        this.ID_ = new Integer( getEntityId(name) );
        System.out.println("Creating a grid user entity with name = " +
                name + ", and id = " + this.ID_);

        // Creates a list of Gridlets or Tasks for this grid user
        this.list_ = createGridlet( this.ID_.intValue() );
        System.out.println("Creating " + this.list_.size() + " Gridlets");
    }

    /**
     * The core method that handles communications among GridSim entities
     */
    public void body()
    {
        int resourceID[] = new int[this.totalResource_];
        double resourceCost[] = new double[this.totalResource_];
        String resourceName[] = new String[this.totalResource_];

        LinkedList resList;
        ResourceCharacteristics resChar;
        
    	
        // waiting to get list of resources. Since GridSim package uses
        // multi-threaded environment, your request might arrive earlier
        // before one or more grid resource entities manage to register
        // themselves to GridInformationService (GIS) entity.
        // Therefore, it's better to wait in the first place
        while (true)
        {
            // need to pause for a while to wait GridResources finish
            // registering to GIS
            super.gridSimHold(1.0);    // hold by 1 second

            resList = super.getGridResourceList();
            if (resList.size() == this.totalResource_)
                break;
            else
                System.out.println("Waiting to get list of resources ...");
        }

        int i = 0;

        // a loop to get all the resources available
        for (i = 0; i < this.totalResource_; i++)
        {

        	System.out.println("this.totalResource_" + this.totalResource_);
            // Resource list contains list of resource IDs not grid resource
            // objects.
            resourceID[i] = ( (Integer)resList.get(i) ).intValue();

            // Requests to resource entity to send its characteristics
            super.send(resourceID[i], GridSimTags.SCHEDULE_NOW,
                       GridSimTags.RESOURCE_CHARACTERISTICS, this.ID_);

            // waiting to get a resource characteristics
            resChar = (ResourceCharacteristics) super.receiveEventObject();
            resourceName[i] = resChar.getResourceName();
            resourceCost[i] = resChar.getCostPerSec();
            //Get the resource MIPS
            resMIPS.add(resChar.getMIPSRating());
          //Get the number of free PE
            resFreePE.add(resChar.getNumFreePE());
        
            System.out.println("Received ResourceCharacteristics from " +
                    resourceName[i] + ", with id = " + resourceID[i]);

            // record this event into "stat.txt" file
            super.recordStatistics("\"Received ResourceCharacteristics " +
                    "from " + resourceName[i] + "\"", "");
        }

        Gridlet gridlet;
        String info;

        System.out.println("Please choose the scheduling algorithm FCFS(1)/Ant Colony Algorithm(2) ");
        int scheduleType = Integer.valueOf(in.nextLine());
        //Read the input PV matrix for ant colony optimization
        ant.readGraph();
        if(scheduleType==2){
        //Solve the optimization problem using ACO
        for (int ii = 0;ii < 100 ;ii ++ ) {
            ant.solve();
        }
        
        //Get the gridlet priority list by analysing trail info.
        Ants.gridletPriorityList();
        
        //Get the best resource list for each gridlet.
        Ants.bestResource();
        }
        // a loop to get one Gridlet at one time and sends it to a random grid
        // resource entity. Then waits for a reply
        Random random = new Random();
        int id = 0;
        for (i = 0; i < this.list_.size(); i++)
        {	
        	
        	
        	
        	int gr;
        	if(scheduleType == 2){
        		gr = ant.gridletPriorityList().get(i);
        		id = ant.bestResource().get(i);
        	}
        	
        	else{
        	gr = i;
        	id = random.nextInt(this.totalResource_);
        	}
        	
            gridlet = (Gridlet) this.list_.get(gr);
        	info = "Gridlet_" + gridlet.getGridletID();
            System.out.println("Sending " + info + " to " + resourceName[id] +
                    " with id = " + resourceID[id]);

            // Sends one Gridlet to a grid resource specified in "resourceID"
            super.gridletSubmit(gridlet, resourceID[id]);

            // OR another approach to send a gridlet to a grid resource entity
            //super.send(resourceID[id], GridSimTags.SCHEDULE_NOW,
            //      GridSimTags.GRIDLET_SUBMIT, gridlet);

            // Records this event into "stat.txt" file for statistical purposes
            super.recordStatistics("\"Submit " + info + " to " +
                    resourceName[id] + "\"", "");

            // waiting to receive a Gridlet back from resource entity
            gridlet = super.gridletReceive();
            System.out.println("Receiving Gridlet " + gridlet.getGridletID());

            // Records this event into "stat.txt" file for statistical purposes
            super.recordStatistics("\"Received " + info +  " from " +
                    resourceName[id] + "\"", gridlet.getActualCPUTime());

            // stores the received Gridlet into a new GridletList object
            this.receiveList_.add(gridlet);
        }

        // shut down all the entities, including GridStatistics entity since
        // we used it to record certain events.
        super.shutdownGridStatisticsEntity();
        super.shutdownUserEntity();
        super.terminateIOEntities();
    }

    /**
     * Gets the list of Gridlets
     * @return a list of Gridlets
     */
    public GridletList getGridletList() {
        return this.receiveList_;
    }

    /**
     * This method will show you how to create Gridlets with and without
     * GridSimRandom class.
     * @param userID    the user entity ID that owns these Gridlets
     * @return a GridletList object
     */
    private GridletList createGridlet(int userID)
    {
        // Creates a container to store Gridlets
        GridletList list = new GridletList();
        //ArrayList<Gridlet> list = new ArrayList<Gridlet>();
        int id = 0;
        //double length = 3500.0;
        double length = 0;
        //long file_size = 300;
        long file_size = 0;
        long output_size = 0;
        in = new Scanner(System.in);  
        System.out.println("Please Choose the method for gridlet creation Manual(M)/Random(R)");
        char gridCreationMethod = in.nextLine().charAt(0);
        System.out.println("Enter the no of gridlet ");
		noOfGridlets = Integer.valueOf(in.nextLine());
		//Memory allocations done here
		gridletLength = new long[noOfGridlets];
		deadline = new double[noOfGridlets];
        if (gridCreationMethod == 'M')
        {        		    
        	for (int i = 0; i < noOfGridlets ; i++)
        	{
        		id=i;
        		System.out.println("Gridlet"+i);
        		System.out.println("Enter the length");
        		gridletLength[i]=Long.valueOf(in.nextLine());
        		length = gridletLength[i];
        		System.out.println("Enter the file_size");
        		file_size=Long.valueOf(in.nextLine());
        		System.out.println("Enter the output file_size");
        		output_size=Long.valueOf(in.nextLine());
        		System.out.println("Enter the deadline");
        		deadline[i] = Double.valueOf(in.nextLine());
        		
        		Gridlet gridletM = new Gridlet(id + i, length, file_size,output_size);
        	    list.add(gridletM);
        	    gridletM.setUserID(userID);        	           	    
        	}        	
    
        }
        
        // We create three Gridlets or jobs/tasks manually without the help
        // of GridSimRandom
        else
        {
        // We create Gridlets with the help of GridSimRandom and
        // GriSimStandardPE class
        	long seed = 11L*13*17*19*23+1;
        	Random random = new Random(seed);

        // sets the PE MIPS Rating
        	GridSimStandardPE.setRating(100);

        // creates Gridlets
        
       
        	int count = noOfGridlets ;
        	for (int i = 0; i < count; i++)
        	{
            // the Gridlet length determines from random values and the
            // current MIPS Rating for a PE
        		length = GridSimStandardPE.toMIs(random.nextDouble()*50);
        		
        		gridletLength[i] = (long) length;

            // determines the Gridlet file size that varies within the range
            // 100 + (10% to 40%)
        		file_size = (long) GridSimRandom.real(100, 0.10, 0.40,
                random.nextDouble());
        		
            // determines the Gridlet output size that varies within the range
            // 250 + (10% to 50%)
        		output_size = (long) GridSimRandom.real(250, 0.10, 0.50,
                random.nextDouble());
        		
        	//determines the deadline randomly 
        		deadline[i] = (double)GridSimRandom.real(50, 0.10, 0.20,
                        random.nextDouble());
            // creates a new Gridlet object
        		Gridlet gridletR = new Gridlet(id + i, length, file_size,
                                    output_size);

        		gridletR.setUserID(userID);

            //add the Gridlet into a list
        		list.add(gridletR);
        		
        	}

        	
        }
        return list;
    }
    /**
     * Prints the Gridlet objects
     * @param list  list of Gridlets
     */
    public static void printGridletList(GridletList list)
    {
        int size = list.size();
        Gridlet gridlet;

        String indent = "    ";
        System.out.println();
        System.out.println("========== OUTPUT ==========");
        System.out.println("Gridlet ID" + indent + "STATUS" + indent +
                "Resource ID" + indent + "Cost");

        for (int i = 0; i < size; i++)
        {
        	
           
        	gridlet = (Gridlet) list.get(i);
            System.out.print(indent + gridlet.getGridletID() + indent
                    + indent);

            if (gridlet.getGridletStatus() == Gridlet.SUCCESS)
                System.out.print("SUCCESS");

            System.out.println( indent + indent + gridlet.getResourceID() +
                    indent + indent + gridlet.getProcessingCost());
        }
    }
    
    //Method to get the heuristic matrix based on the gridlet and grid resource charachteristics.
	public static double[][] getPVMatrix(){
		
		int n = AntColonyScheduler.getnoOfGridResouces();	
		int m = noOfGridlets;
		
		//Memory Allocations are done here
		PV = new double[n][m];	
		for(int i = 0; i < n;i++){
			for(int j = 0; j < m; j++){
				
				double[][] val = new double[n][m];
				PV[i][j]= (double) ((gridletLength[j]/100)+deadline[j]/(double)(resMIPS.get(i)*resFreePE.get(i)));
				val[i][j] = (double)(1/PV[i][j]);//Heuristic info	
			}
			System.out.println();
		}
		return PV;
	}
}