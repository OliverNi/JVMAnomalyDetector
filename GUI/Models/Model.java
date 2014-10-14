package GUI.Models;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Oliver on 2014-10-14.
 */
public abstract class Model<M> {
    private Collection<M> observers = new ArrayList<>();
    /**
     * Subscribes a new subject to the observer
     * @param subject the subject that wishes to subscribe
     */
    public void subscribe(M subject) {
        if (!this.observers.contains(subject))
            this.observers.add(subject);
    }

    /**
     * Unsubscribes a new subject from the observer
     * @param subject the subject that wishes to unsubscribe
     */
    public void unsubscribe(M subject) {
        this.observers.remove(subject);
    }

    /**
     * @return returns a collection of observers
     */
    public Collection<M> getObservers() {
        return this.observers;
    }
}
