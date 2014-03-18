package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;
    static int ncOahu;
    static int naOahu;
    static int ncMolokai;
    static int naMolokai;
    static int boatLoc;
    static boolean done;
    static Lock boatLock;
    static Condition2 childReady;
    static Condition2 adultReady; 
    static Lock endLock;
    static Condition2 end; 
    
    public static void selfTest()
    {
	BoatGrader b = new BoatGrader();
	
	//System.out.println("\n ***Testing Boats with only 2 children***");
	//begin(0, 2, b);

  	//System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
  	//begin(1, 2, b);

	//System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
 	//begin(3, 3, b);
	
	begin(4,3,b);
    }

    public static void begin( int adults, int children, BoatGrader b )
    {
		// Store the externally generated autograder in a class
		// variable to be accessible by children.
		bg = b;
	
		// Instantiate global variables here
		ncOahu = children;
		naOahu = adults;
		ncMolokai = 0;
		naMolokai = 0;
		boatLoc =0;
		done = false;
		boatLock = new Lock();
		childReady = new Condition2(boatLock);
		adultReady = new Condition2(boatLock);
		endLock = new Lock();
		end = new Condition2(endLock);
		
		// Create threads here. See section 3.4 of the Nachos for Java
		// Walkthrough linked from the projects page.

		Runnable childRunnable = new Runnable() {
		    public void run() {
	                ChildItinerary();
	            }
        };
        
        Runnable adultRunnable = new Runnable() {
		    public void run() {
	                AdultItinerary();
	            }
        };
        
        for(int i=0;i<children;i++){
            KThread t = new KThread(childRunnable);
            t.setName("child");
            t.fork();
        }
        
        for(int i=0;i<adults;i++){
            KThread t = new KThread(adultRunnable);
            t.setName("adult");
            t.fork();
        }
        
        endLock.acquire();
    	while (!done) {	
    		end.sleep();

    		// If we've moved all adults and children from Oahu, then we're done!
    		if (ncOahu + naOahu == 0){
	    		System.out.println("Game over! Whoo!");	
	    		done = true;
	    		break;
	    	}	
    	}	

    }
	
    static void printState() {
		System.out.println("Oahu:[" + naOahu + ","+ ncOahu + "]" + " Molokai:[" + naMolokai + ","+ ncMolokai + "]");
	}

    static void AdultItinerary()
    {
	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/
    	boatLock.acquire();
    	
    	if(boatLoc == 1 && ncMolokai >= 1){
    		adultReady.sleep();
    	}else if(boatLoc == 0 && ncOahu >=2){
    		adultReady.sleep();
    	}else if(done == true){
    		adultReady.sleep();
    	}
    	if(boatLoc == 0 && ncOahu < 2 && naOahu >= 1){
    		bg.AdultRowToMolokai();
    		boatLoc = 1;
    		naOahu--;
    		naMolokai++;
    		printState();
    	}
    	if(ncOahu == 0 && naOahu == 0){
    		done = true;
    		endLock.acquire();
    		end.wake();
    		endLock.release();
    	}else if(boatLoc == 1 && ncMolokai >=1){
    		childReady.wake();
    	}
    	
    	boatLock.release();
    }

    static void ChildItinerary()
    {
    	boatLock.acquire();
    	
    	while(!done){
	    	if(boatLoc == 0 && ncOahu < 2 && naOahu >=1){
	    		childReady.sleep();
	    	}else if(done == true){
	    		childReady.sleep();
	    	}
	    	if(boatLoc == 1 && ncMolokai >= 1){
	    		bg.ChildRowToOahu();
	    		boatLoc = 0;
	    		ncOahu++;
	    		ncMolokai--;
	    		printState();
	    	}else if(boatLoc == 0 && ncOahu >= 2){
	    		bg.ChildRowToMolokai();
	    		bg.ChildRideToMolokai();
	    		boatLoc = 1;
	    		ncOahu = ncOahu-2;
	    		ncMolokai = ncMolokai+2;
	    		printState();
	    	}else if(boatLoc == 0 && ncOahu < 2){
	    		bg.ChildRowToMolokai();
	    		boatLoc = 1;
	    		ncOahu--;
	    		ncMolokai++;
	    		printState();
	    	}
	    	if(ncOahu == 0 && naOahu == 0){
	    		done = true;
	    		endLock.acquire();
	    		end.wake();
	    		endLock.release();
	    	}else if(boatLoc == 0 && ncOahu < 2){
	    		adultReady.wake();
	    	}
    	}
    	boatLock.release();
    }

    static void SampleItinerary()
    {
	// Please note that this isn't a valid solution (you can't fit
	// all of them on the boat). Please also note that you may not
	// have a single thread calculate a solution and then just play
	// it back at the autograder -- you will be caught.
	System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
	bg.AdultRowToMolokai();
	bg.ChildRideToMolokai();
	bg.AdultRideToMolokai();
	bg.ChildRideToMolokai();
    }
    
}
