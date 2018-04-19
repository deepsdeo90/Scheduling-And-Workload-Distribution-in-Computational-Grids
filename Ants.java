package AntColonyProject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class Ants {
    // Ant Colony Optimization parameters:
    // original amount of pheromone trail
    private double c = 1.0;
    // pheromone trail preference
    private double alpha = 1;
    // greedy preference
    private double beta = 5;
    // pheromone trail evaporation coefficient
    private double evaporation = 0.5;
    // new trail deposit coefficient;
    private double Q = 500;
    // number of ants to be placed on the grid = numAntFactor*numTasks
    private double numAntFactor = 0.8;
    // probability of pure random selection of the next Task
    private double pr = 0.01;

    // Reasonable number of iterations
    // results typically settle down by 500
    private int maxIterations = 100;

    public int n = 0; // # Tasks
    public int m = 0; // # ants
    private double graph[][] = null;
    public static double trails[][] = null;
    private Ant ants[] = null;
    private Random rand = new Random();
    private double probs[] = null;

    private int currentIndex = 0;

    public static int[] bestTour;
    public double bestTourLength;
    
    private static int numTasks;
	private int numResources;
	private double[][] PVMatrix;

	 public static ArrayList<Integer> gridletPriorList = new ArrayList<Integer>();
	 public static ArrayList<Integer> resSelectionList = new ArrayList<Integer>();

    // Ant class. Maintains tour and tabu information.
    private class Ant {
        public int tour[] = new int[graph.length];
        // Maintain visited list for tasks, much faster
        // than checking if in tour so far.
        public boolean visited[] = new boolean[graph.length];

        public void visitTask(int Task) {
            tour[currentIndex + 1] = Task;
            visited[Task] = true;
        }

        public boolean visited(int i) {
            return visited[i];
        }

        public double tourLength() {
            double length = graph[tour[n - 1]][tour[0]];
            for (int i = 0; i < n - 1; i++) {
                length += graph[tour[i]][tour[i + 1]];
            }
            return length;
        }

        public void clear() {
            for (int i = 0; i < n; i++)
                visited[i] = false;
        }
    }

    // Read in graph from a file.
    // Allocates all memory.
    // Adds 1 to edge lengths to ensure no zero length edges.
    public void readGraph() {
       
    	getInputData();
        n = numTasks;
        m = (int) (n * numAntFactor);

        // all memory allocations done here
        trails = new double[n][n];
        probs = new double[n];
        ants = new Ant[m];
        for (int j = 0; j < m; j++)
            ants[j] = new Ant();
    }

    // Approximate power function, Math.pow is quite slow and we don't need accuracy.
    public static double pow(final double a, final double b) {
        final int x = (int) (Double.doubleToLongBits(a) >> 32);
        final int y = (int) (b * (x - 1072632447) + 1072632447);
        return Double.longBitsToDouble(((long) y) << 32);
    }

    // Store in probs array the probability of moving to each Task
    // In short: ants like to follow stronger and shorter trails more.
    private void probTo(Ant ant) {
        int i = ant.tour[currentIndex];

        double denom = 0.0;
        for (int l = 0; l < n; l++)
            if (!ant.visited(l))
                denom += pow(trails[i][l], alpha)
                        * pow(1.0 / graph[i][l], beta);


        for (int j = 0; j < n; j++) {
            if (ant.visited(j)) {
                probs[j] = 0.0;
            } else {
                double numerator = pow(trails[i][j], alpha)
                        * pow(1.0 / graph[i][j], beta);
                probs[j] = numerator / denom;
            }
        }

    }

    // Given an ant select the next Task based on the probabilities
    // we assign to each Task. With pr probability chooses
    // totally randomly (taking into account tabu list).
    private int selectNextTask(Ant ant) {
        // sometimes just randomly select
        if (rand.nextDouble() < pr) {
            int t = rand.nextInt(n - currentIndex); // random Task
            int j = -1;
            for (int i = 0; i < n; i++) {
                if (!ant.visited(i))
                    j++;
                if (j == t)
                    return i;
            }

        }
        // calculate probabilities for each task (stored in probs)
        probTo(ant);
        // randomly select according to probs
        double r = rand.nextDouble();
        double tot = 0;
        for (int i = 0; i < n; i++) {
            tot += probs[i];
            if (tot >= r)
                return i;
        }

        throw new RuntimeException("Not supposed to get here.");
    }

    // Update trails based on ants tours
    private void updateTrails() {
        // evaporation
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                trails[i][j] *= evaporation;

        // each ants contribution
        for (Ant a : ants) {
            double contribution = Q / a.tourLength();
            for (int i = 0; i < n - 1; i++) {
                trails[a.tour[i]][a.tour[i + 1]] += contribution;
            }
            trails[a.tour[n - 1]][a.tour[0]] += contribution;
        }
    }

    // Choose the next task for all ants
    private void moveAnts() {
        // each ant follows trails...
        while (currentIndex < n - 1) {
            for (Ant a : ants)
                a.visitTask(selectNextTask(a));
            currentIndex++;
        }
    }

    // m ants with random start task
    private void setupAnts() {
        currentIndex = -1;
        for (int i = 0; i < m; i++) {
            ants[i].clear(); // faster than fresh allocations.
            ants[i].visitTask(rand.nextInt(n));
        }
        currentIndex++;

    }

    private void updateBest() {
        if (bestTour == null) {
            bestTour = ants[0].tour;
            bestTourLength = ants[0].tourLength();
        }
        for (Ant a : ants) {
            if (a.tourLength() < bestTourLength) {
                bestTourLength = a.tourLength();
                bestTour = a.tour.clone();
            }
        }
    }

    public static String tourToString(int tour[]) {
        String t = new String();
        for (int i : tour)
            t = t + " " + i;
        return t;
    }

    public int[] solve() {
        // clear trails
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                trails[i][j] = c;

        int iteration = 0;
        // run for maxIterations
        // preserve best tour
        while (iteration < maxIterations) {
            setupAnts();
            moveAnts();
            updateTrails();
            updateBest();
            iteration++;
        }
        
        
        for(int i = 0; i < numTasks; i++){
        gridletPriorList.add(bestTour[i]);
        }
        

        // Subtract n because we added one to edges on load
        System.out.println("Best tour length: " + (bestTourLength - n));
        System.out.println("Best tour:" + tourToString(bestTour));
        return bestTour.clone();
    }
    
    //Method to read the input data based on gridlet and grid resource characteristics
    public  double[][] getInputData(){
        numTasks = GridletCreation.noOfGridlets; // # tasks
        numResources = AntColonyScheduler.getnoOfGridResouces(); //# resources
    	
    	//Memory Allocations are done here
    	PVMatrix = new double[numResources][numTasks];
    	graph = new double[numResources][numTasks];
    	PVMatrix = GridletCreation.getPVMatrix();
    	for(int i = 0; i < numResources; i++){
    		for(int j = 0; j< numTasks; j++ ){
    		graph[i][j] = PVMatrix[i][j];
    		
    		}
    	}
		return graph;
    		
    }
    
    //Get the gridlet priority list based on the trail information.
    public static ArrayList<Integer> gridletPriorityList() {
        for(int i = 0; i < numTasks; i++){
            gridletPriorList.add(bestTour[i]);
           }
        return gridletPriorList;
        }
    
    //Get the best resource for each gridlet
    public static ArrayList<Integer> bestResource() {
 	   for (int i = 0; i < trails.length; i++) {
 	       double max =  trails[0][i]; // set max to minimum value before starting loop
 	       for (int j = 0; j < trails[i].length; j++)
 	       {
 	           if (trails[i][j] > max)
 	               max = trails[i][j];
 	       }
 	      resSelectionList.add(i);      
 	   }
 	  return resSelectionList;
 	}

   
}

    
    
    

