/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package concurrent.Threads;

import java.util.Random;

/**
 * @author NICK
 * simulate how long may each individual passenger will act
 */
public class Passenger implements Runnable{
    private final Plane plane;
    private final Random random;
    
    public Passenger(Plane plane){
        this.plane = plane;
        this.random = new Random();
    }
    
    public void run(){
        try{
            //simulate each passenger boarding/disembark time.
            Thread.sleep(100+random.nextInt(900));
        } catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }
}
