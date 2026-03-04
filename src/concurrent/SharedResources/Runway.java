package concurrent.SharedResources;

import concurrent.Threads.Plane;
import java.util.concurrent.locks.ReentrantLock;

public class Runway {

    //lock that ensure plane user the runway in order
    private final ReentrantLock lock = new ReentrantLock(true);
    private Plane currentPlane;

    //secure the runway for a specific plane
    public void occupy(Plane plane){
        lock.lock();
        try {
            currentPlane = plane;
        } finally {}
    }
    //free the runway for the next plane in queue
    public void release(){
        try {
            currentPlane = null;
        } finally {
            lock.unlock(); //unlocked when the plane safely leaves
        }
    }
    
    //getter
    public Plane getCurrentPlane() {
        return currentPlane;
    }
    
    //condition check
    public boolean isAvailable() {
        return !lock.isLocked();
    }
}