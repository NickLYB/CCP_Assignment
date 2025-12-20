/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package concurrent.Threads;

import concurrent.Main;
import concurrent.SharedResources.Gate;
import java.util.Random;

/**
 *
 * @author NICK
 */
public class Plane implements Runnable{
    
    //auto increment
    private static int nextPlaneId = 1;
    private final int planeId;
    
    //statistic
    private long arrivalTime;
    private long landingTime;
    
    private int passengerCount;
    private final Random random;
    private final boolean fuelShortage;
    private Gate assignedGate;
    private Thread[] disembarkingPassengers;
    private Thread[] embarkingPassengers;
    private volatile boolean clearedForTakeoff = false;
    
    public Plane(boolean isEmergency){
        this.planeId = nextPlaneId++;
        this.fuelShortage = isEmergency;
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
            
        }
    }
    
    private void requestLanding() throws InterruptedException{
        System.out.println(Thread.currentThread().getName() + ":Requesting Landing.");
        assignedGate = Main.atc.requestLandingPermission(this);
        landingTime = System.currentTimeMillis();
    }
    private void land() throws InterruptedException{
        System.out.println(Thread.currentThread().getName() + ":Landing.");
        Main.runway.occupy(this); 
        Thread.sleep(1000);
        System.out.println(Thread.currentThread().getName() + ":Landed.");
    }
    private void coastToGate() throws InterruptedException{
        System.out.println(Thread.currentThread().getName() + ":Coasting to Gate-" + assignedGate.getGateId());
        Main.runway.release();
        Thread.sleep(500);
    }
    private void dock() throws InterruptedException{
        assignedGate.dock(this);
        System.out.println(Thread.currentThread().getName() + ":Docked at Gate-" + assignedGate.getGateId());
    }
    private void performGateOperation()throws InterruptedException{
        
        //1. unload arrival passengers
        disembark();
        disembarkWait();
        
        //2. services
        Thread cleaningThread = cleaning();
        refuelPlane();
        cleaningWait(cleaningThread);

        //3. load new departing passengers
        boardPassengers();
        boardingWait();
    }
    private void undock() throws InterruptedException{
        System.out.println(Thread.currentThread().getName() + ":Undock from Gate-" + assignedGate.getGateId());
        assignedGate.undock();
    }
    private void coastToRunway() throws InterruptedException{
        System.out.println(Thread.currentThread().getName() + ":Coasting to Runway.");
        Thread.sleep(500);
    }
    private void takeOff() throws InterruptedException{
        System.out.println(Thread.currentThread().getName() + ":Requesting Taking off.");
        Main.atc.requestTakeOffPermission(this);
        Main.runway.occupy(this);
        System.out.println(Thread.currentThread().getName() + ":Taking-off.");
        Thread.sleep(1000);
        Main.runway.release();
        Main.airport.planeLeft();
        System.out.println("");
    }
    
    //individual operation in gate operation
    private void disembark(){
        System.out.println(Thread.currentThread().getName() + "'s Passengers: Disembarking out of " + Thread.currentThread().getName());
        
        disembarkingPassengers = new Thread[passengerCount];
        for(int i = 0; i < passengerCount; i++){
            Passenger passenger = new Passenger(this);
            disembarkingPassengers[i] = new Thread(passenger);
            disembarkingPassengers[i].start();
        }
        
    }
    private void disembarkWait() throws InterruptedException{
        for(Thread passenger : disembarkingPassengers){
            passenger.join();
        }
    }
    private void refuelPlane() throws InterruptedException{
        System.out.println(Thread.currentThread().getName() + ":Request refueling.");
        Main.refuelingTruck.refuel(this);
        System.out.println(Thread.currentThread().getName() + ":Refueling completed.");
    }
    private Thread cleaning(){
        //lambda
        Thread cleaningThread = new Thread(()-> {
        try{
            System.out.println(Thread.currentThread().getName() + ":Cleaning and refilling supplies.");
            Thread.sleep(2000);
            System.out.println(Thread.currentThread().getName() + ":Cleaning completed.");
        } catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }
        },Thread.currentThread().getName()+ "-Cleaning");
        
        cleaningThread.start();
        return cleaningThread;
    }
    private void cleaningWait(Thread cleaning) throws InterruptedException{
        cleaning.join();
    }
    private void boardPassengers() {
        this.passengerCount = random.nextInt(50) + 1;
        System.out.println(Thread.currentThread().getName()+"'s Passengers: Boarding " + Thread.currentThread().getName() + ".");

        embarkingPassengers = new Thread[passengerCount];
        for (int i = 0; i < passengerCount; i++) {
            Passenger passenger = new Passenger(this);
            embarkingPassengers[i] = new Thread(passenger);
            embarkingPassengers[i].start();
        }
    }
    private void boardingWait() throws InterruptedException{
        for(Thread passenger : embarkingPassengers){
            passenger.join();
        }
    }
    
    //getter
    public int getPlaneId() {
        return planeId;
    }
    public long getWaitingTime() {
    return landingTime - arrivalTime;
    }
    public int getPassengerCount() {
        return passengerCount;
    }
    public Gate getAssignedGate() {
        return this.assignedGate;
    }
    
    //setter
    public void setAssignedGate(Gate gate) {
        this.assignedGate = gate;
    }
    public void setClearedForTakeoff(boolean cleared) {
        this.clearedForTakeoff = cleared;
    }
    
    //flag
    public boolean hasFuelShortage() {
        return fuelShortage;
    }
    public boolean isClearedForTakeoff() {
        return clearedForTakeoff;
    }

    
    
}
