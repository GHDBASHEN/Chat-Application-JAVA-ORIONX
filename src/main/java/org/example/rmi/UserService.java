package org.example.rmi;
import org.example.domain.User;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UserService extends Remote {
    User getUser(int id) throws RemoteException;
    User checkEmailAndPassword(String email, String password) throws RemoteException;

}