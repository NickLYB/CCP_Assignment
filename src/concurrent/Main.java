package concurrent;

import concurrent.SharedResources.*;
import concurrent.Threads.*;
import java.util.Random;

/**
 *
 * @author NICK
 */
public class Main {
    
    public static Runway runway;
    public static Airport airport;
    public static ATC atc;
    public static RefuelingTruck refuelingTruck;
    public static Gate[] gates;
    public static Statistic statistics;
    
    // Arrays for the cleaning teams
    public static Cleaning[] cleaners;
    public static Thread[] cleanerThreads;
    
    private static final int gatesCount = 3;
    private static final int numPlanes = 6;
    private static final int emergencyPlaneId = 5;
    
    public static void main(String[] args) throws InterruptedException {
        
        // initialize shared resources
        runway = new Runway();
        airport = new Airport();
        gates = new Gate[gatesCount];
        cleaners = new Cleaning[gatesCount];
        cleanerThreads = new Thread[gatesCount];
        
        for(int i = 0; i < gatesCount; i++){
            gates[i] = new Gate();
            // Initialize a cleaning team for each gate
            cleaners[i] = new Cleaning(gates[i]);
            cleanerThreads[i] = new Thread(cleaners[i], "Cleaner-Gate" + (i+1));
        }
        
        statistics = new Statistic();
        refuelingTruck = new RefuelingTruck();
        atc = new ATC();
        
        // start threads
        Thread atcThread = new Thread(atc, "ATC");
        atcThread.start();
        
        Thread truckThread = new Thread(refuelingTruck, "RefuelingTruck");
        truckThread.start();
        
        // Start cleaning teams
        for(int i = 0; i < gatesCount; i++) {
            cleanerThreads[i].start();
        }
        
        // planes 6
        Thread[] planeThreads = new Thread[numPlanes];
        Random rand = new Random();
        for (int i = 1; i <= numPlanes; i++) {
            boolean isEmergency = (i == emergencyPlaneId); 
            Plane plane = new Plane(isEmergency);
            Thread planeThread = new Thread(plane, "Plane-" + i);
            planeThreads[i - 1] = planeThread;
            planeThread.start();
            Thread.sleep(rand.nextInt(2001)); 
        }
        
        // wait for all plane to finish their thread
        for (int i = 0; i < planeThreads.length; i++) {
            planeThreads[i].join();
            System.out.println("[Main] Plane-" + (i + 1) + " thread has completed.");
        }
        
        // safely shutdown all the service threads
        atc.shutdown();
        refuelingTruck.shutdown();
        for(int i = 0; i < gatesCount; i++) {
            cleaners[i].shutdown();
        }
        
        atcThread.join();
        truckThread.join();
        
        for(int i = 0; i < gatesCount; i++) {
            cleanerThreads[i].join();
        }
        
        // sanity check
        boolean allChecksPass = true;
        for (Gate gate : gates) {
            if (!gate.isEmpty()) allChecksPass = false;
        }
        if (airport.getCurrentOccupancy() != 0) allChecksPass = false;
        if (!runway.isAvailable()) allChecksPass = false;
        
        System.out.println("Sanity Check Passed: " + allChecksPass);
        
        // print statistic
        statistics.printStatistic();
    }
}