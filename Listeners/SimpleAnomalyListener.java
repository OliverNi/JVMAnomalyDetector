package Listeners;

/**
 * Created by Oliver on 2014-10-09.
 */

/**
 * An AnomalyListener which prints the AnomalyEvent
 */
public class SimpleAnomalyListener implements AnomalyListener {
    @Override
    public void anomalyFound(AnomalyEvent e) {
        System.out.println(e.toString());
    }
}
