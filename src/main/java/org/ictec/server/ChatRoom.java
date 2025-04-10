package org.ictec.server;
import org.ictec.client.ChatObserver;
import java.util.ArrayList;
import java.util.List;

public class ChatRoom {
    private List<ChatObserver> observers = new ArrayList<>();

    public void subscribe(ChatObserver observer) {
        observers.add(observer);
    }

    public void unsubscribe(ChatObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(String message) {
        for (ChatObserver observer : observers) {
            observer.update(message);
        }
    }
}
