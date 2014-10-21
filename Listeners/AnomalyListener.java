package Listeners;

import AnomalyDetector.AnomalyReport;

import java.util.EventListener;

/**
 * Created by Oliver on 2014-10-06.
 */
public interface AnomalyListener extends EventListener {
    void anomalyFound(AnomalyEvent e);
}
