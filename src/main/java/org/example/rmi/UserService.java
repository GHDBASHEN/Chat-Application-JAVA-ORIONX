package org.example.rmi;
import org.example.domain.User;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface UserService extends Remote {
    User getUserByUsername(String username) throws RemoteException;
    List<User> getAllUsers() throws RemoteException;  // Add this
    void deleteUser(int userId) throws RemoteException;

    User getUser(int id) throws RemoteException;
    User checkEmailAndPassword(String email, String password) throws RemoteException;

<<<<<<< HEAD
    void updateUser(User user) throws RemoteException;
}
=======

}
>>>>>>> 2132bd8979b17800366de2397af14704be006710
