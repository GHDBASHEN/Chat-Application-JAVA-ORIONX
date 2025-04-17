// ChatObserver.java
package org.example.rmi;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatObserver extends Remote {
    void notifyNewMessage(String message) throws RemoteException;
}