/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package concurrent.SharedResources;

import concurrent.Threads.Plane;
import java.util.concurrent.locks.ReentrantLock;


/**
 *
 * @author NICK
 */
public class Runway {

    private final ReentrantLock lock = new ReentrantLock(true);
    private Plane currentPlane;

    public void occupy(Plane plane) {
        lock.lock();
        try {
            currentPlane = plane;
        } catch (Exception e) {
            lock.unlock();
        }
    }

    public void release() {
        try {
            currentPlane = null;
        } finally {
            lock.unlock();
        }
    }

    public boolean isAvailable() {
        return !lock.isLocked();
    }

    public Plane getCurrentPlane() {
        return currentPlane;
    }
}
