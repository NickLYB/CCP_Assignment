package concurrent.Threads;

import java.util.Random;

/**
 * @author NICK
 */
public class Passenger implements Runnable {
    private final int passengerCount;
    private final String actionType; 
    
    public Passenger(int passengerCount, String actionType){
        this.passengerCount = passengerCount;
        this.actionType = actionType;
    }
    
    @Override
    public void run() {
        //grab the name directly from the current running thread
        String currentPlaneName = Thread.currentThread().getName();
        
        if (actionType.equals("Disembarking")) {
            System.out.println(currentPlaneName + "'s Passengers: Disembarking " + passengerCount + " passengers...");
        } else {
            System.out.println(currentPlaneName + "'s Passengers: Boarding " + passengerCount + " new passengers...");
        }

        Random random = new Random();
        int totalActionTime = 0;
        
        for(int i = 0; i < passengerCount; i++){
            totalActionTime += 100 + random.nextInt(900);
        }
        
        try {
            Thread.sleep(totalActionTime / 10);
        } catch (InterruptedException e) {
            System.out.println(currentPlaneName + "'s Passenger group was interrupted while " + actionType);
            Thread.currentThread().interrupt();
        }
    }
}