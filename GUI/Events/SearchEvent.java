package GUI.Events;

import java.util.EventObject;

/**
 * Created by Oliver on 2014-10-14.
 */
public class SearchEvent extends EventObject {
    String host;
    int port;
    String period;
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public SearchEvent(Object source, String host, int port, String period) {
        super(source);
        this.host = host;
        this.port = port;
        this.period = period;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPeriod() {
        return period;
    }
}
