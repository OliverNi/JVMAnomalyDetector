package GUI.Events;

import java.util.EventObject;

/**
 * Created by Oliver on 2014-10-14.
 */
public class ListUpdatedEvent extends EventObject {
    int nrOfRows;
    String period;
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public ListUpdatedEvent(Object source,int nrOfRows, String period) {
        super(source);
        this.nrOfRows = nrOfRows;
        this.period = period;
    }

    public int getNrOfRows() {
        return nrOfRows;
    }

    public String getPeriod() {
        return period;
    }
}
