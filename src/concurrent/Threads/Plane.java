package concurrent.Threads;

import concurrent.Main;
import concurrent.SharedResources.Gate;
import java.util.Random;

/**
 *
 * @author NICK
 */
public class Plane implements Runnable {
    
    private static int nextPlaneId = 1;
    private final int planeId;
    private final String planeName;
    
    private long arrivalTime;
    private long landingTime;
    
    private int passengerCount;
    private final Random random;
    private final boolean fuelShortage;
    private Gate assignedGate;
    
    private volatile boolean clearedForTakeoff = false;
    
    public Plane(boolean isEmergency){
        this.planeId = nextPlaneId++;
        this.fuelShortage = isEmergency;
        
        this.planeName = "Plane-" + planeId;
        
        this.random = new Random();
        this.passengerCount = random.nextInt(50) + 1;
    }
    
    public void run(){
        try{
            arrivalTime = System.currentTimeMillis();
            requestLanding();
            land();
            coastToGate();
            dock();
            performGateOperation();
            undock(); 
            coastToRunway();
            takeOff();
            
            Main.statistics.recordPlane(this);
            
        } catch(InterruptedException e){
            System.out.println(planeName + " was interrupted.");
            Thread.currentThread().interrupt();
        }
    }
    
    private void requestLanding() throws InterruptedException{
        //print the emergency tag
        if (fuelShortage) {
            System.out.println(planeName + ": Requesting Landing. [EMERGENCY]");
        } else {
            System.out.println(planeName + ": Requesting Landing.");
        }
        
        assignedGate = Main.atc.requestLandingPermission(this);
        landingTime = System.currentTimeMillis();
    }
    
    private void land() throws InterruptedException{
        System.out.println(planeName + ": Landing.");
        Main.runway.occupy(this); 
        Thread.sleep(1000);
        System.out.println(planeName + ": Landed.");
    }
    
    private void coastToGate() throws InterruptedException{
        System.out.println(planeName + ": Coasting to Gate-" + assignedGate.getGateId());
        Main.runway.release();
        Thread.sleep(500);
    }
    
    private void dock() throws InterruptedException{
        assignedGate.dock(this);
        System.out.println(planeName + ": Docked at Gate-" + assignedGate.getGateId());
    }
    
    private void performGateOperation()throws InterruptedException{
        // 1. unload arrival passengers
        disembark();
        
        // 2. concurrent services
        System.out.println(planeName + ": Requesting Cleaning at Gate-" + assignedGate.getGateId());
        assignedGate.requestCleaning(this); // Tell cleaners to start
        
        refuelPlane(); // Go get fuel at the same time
        
        assignedGate.waitForCleaning(); // Make sure cleaners are done before boarding
        
        // 3. load new departing passengers
        boardPassengers();
    }
    
    private void undock() throws InterruptedException{
        System.out.println(planeName + ": Undock from Gate-" + assignedGate.getGateId());
        assignedGate.undock();
    }
    
    private void coastToRunway() throws InterruptedException{
        System.out.println(planeName + ": Coasting to Runway.");
        Thread.sleep(500);
    }
    
    private void takeOff() throws InterruptedException{
        System.out.println(planeName + ": Requesting Taking off.");
        Main.atc.requestTakeOffPermission(this);
        Main.runway.occupy(this);
        System.out.println(planeName + ": Taking-off.");
        Thread.sleep(1000);
        Main.runway.release();
        Main.airport.planeLeft();
    }
    
    private void disembark() throws InterruptedException {
        Passenger disembarkingGroup = new Passenger(passengerCount, "Disembarking");
        
        Thread passengerThread = new Thread(disembarkingGroup, planeName);
        passengerThread.start();
        
        passengerThread.join(); 
    }
    
    private void refuelPlane() throws InterruptedException{
        System.out.println(planeName + ": Request refueling.");
        Main.refuelingTruck.refuel(this);
        System.out.println(planeName + ": Refueling completed.");
    }

    private void boardPassengers() throws InterruptedException {
        this.passengerCount = random.nextInt(50) + 1;
        
        Passenger boardingGroup = new Passenger(passengerCount, "Boarding");
        
        Thread passengerThread = new Thread(boardingGroup, planeName);
        passengerThread.start();
        
        passengerThread.join();
    }
    
    // Getters and setters
    public int getPlaneId(){ 
        return planeId; 
    }
    public long getWaitingTime(){ 
        return landingTime - arrivalTime; 
    }
    public int getPassengerCount(){ 
        return passengerCount; 
    }
    public Gate getAssignedGate(){ 
        return this.assignedGate; 
    }
    
    public void setAssignedGate(Gate gate){ 
        this.assignedGate = gate; 
    }
    public void setClearedForTakeoff(boolean cleared){ 
        this.clearedForTakeoff = cleared; 
    }
    
    //condition check
    public boolean hasFuelShortage(){ 
        return fuelShortage; 
    }
    public boolean isClearedForTakeoff(){ 
        return clearedForTakeoff; 
    }
}