/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package concurrent;

import concurrent.SharedResources.*;
import concurrent.Threads.*;
import java.util.Random;

/**
 *
 * @author NICK
 */



public class Main {
    /**
     * @param args the command line arguments
     */
    public static Runway runway;
    public static Airport airport;
    public static ATC atc;
    public static RefuelingTruck refuelingTruck;
    public static Gate[] gates;
    public static Statistic statistics;
    
    
    private static final int gatesCount = 3;
    private static final int numPlanes = 6;
    private static final int emergencyPlaneId = 3;
    public static void main(String[] args) throws InterruptedException{
        // TODO code application logic here
        
        //initialize shared resources
        runway = new Runway();
        airport = new Airport();
        gates = new Gate[gatesCount];
        for(int i = 0; i < gatesCount; i++){
            gates[i] = new Gate();
        }
        
        //initialize statistic
        statistics = new Statistic();
        
        //initialize thread
        refuelingTruck = new RefuelingTruck();
        atc = new ATC();
        
        //start thread
        //atc
        Thread atcThread = new Thread(atc, "ATC");
        atcThread.start();
        //refueling truck
        Thread truckThread = new Thread(refuelingTruck, "RefuelingTruck");
        truckThread.start();
        //planes 6
        Thread[] planeThreads = new Thread[numPlanes];
        Random rand = new Random();
        for (int i = 1; i <= numPlanes; i++) {
            boolean isEmergency = (i == emergencyPlaneId); //flag to determine the plane have fuel shortage
            //generate plane
            Plane plane = new Plane(isEmergency);
            Thread planeThread = new Thread(plane, "Plane-" + i);
            planeThreads[i - 1] = planeThread;
            planeThread.start();

            Thread.sleep(rand.nextInt(2001)); //randomly delay the plane
        }
        
        //wait for all plane to finish their thread
        for (int i = 0; i < planeThreads.length; i++) {
            planeThreads[i].join();
            System.out.println("[Main] Plane-" + (i + 1) + " thread has completed.");
        }
        
        //safely shutdown all the service threads
        atc.shutdown();
        refuelingTruck.shutdown();
        
        atcThread.join();
        truckThread.join();
        
        //sanity check
        boolean allChecksPass = true;

        for (Gate gate : gates) {
            boolean isEmpty = gate.isEmpty();
            if (!isEmpty) allChecksPass = false;
        }

        int occupancy = airport.getCurrentOccupancy();
        if (occupancy != 0) allChecksPass = false;

        boolean runwayFree = runway.isAvailable();
        if (!runwayFree) allChecksPass = false;
        
        //print statistic
        statistics.printStatistic();
        
    }
    
}
