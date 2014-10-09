package Listeners;

import AnomalyDetector.AnomalyReport;

import java.util.Calendar;
import java.util.Date;
import java.util.EventObject;

/**
 * Created by Oliver on 2014-10-07.
 */
public class AnomalyEvent extends EventObject{
    private Date date;
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public AnomalyEvent(AnomalyReport source) {
        this(source, Calendar.getInstance().getTime());
    }

    public AnomalyEvent(AnomalyReport source, Date date){
        super(source);
        this.date = Calendar.getInstance().getTime();
    }

    public String getDate(){
        return date.toString();
    }

    @Override
    public String toString(){
        return source.toString();
    }
}
