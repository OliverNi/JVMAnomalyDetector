package GUI.Events;

import Logs.GcStats;

import java.util.ArrayList;

/**
 * Created by Oliver on 2014-10-15.
 */
public interface GcStatsResponse {
    public void searchResult(ArrayList<GcStats> result);
}
