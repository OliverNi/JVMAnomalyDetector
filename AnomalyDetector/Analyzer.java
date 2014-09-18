package AnomalyDetector;

/**
 * Created by Oliver on 2014-09-18.
 */
public class Analyzer {
    private int GcHeapDifferenceWarning = 100; //@TODO RENAME
    private AnomalyDetector ad;
    private JMXAgent agent;
    public Analyzer(JMXAgent agent){
        this.agent = agent;
    }

    /**
     * Analyzes heap after a GarbageCollection has occurred in old gen.
     * @param previousUsedMem Used memory after previous GC.
     * @param usedMem Used memory after this GC.
     */
    public boolean analyzeAfterGC(long previousUsedMem, long usedMem){
        if (previousUsedMem == 0){
            return false;
        }
        else if (usedMem - previousUsedMem > getGcHeapDifferenceWarning()){
            return true;
        }
        else
            return false;
    }

    /**
     * Set the difference in bytes that would be considered an immediate anomaly after GC.
     * @param nrOfBytes difference in bytes
     */
    public void setGcHeapDifferenceWarning(int nrOfBytes){
        this.GcHeapDifferenceWarning = nrOfBytes;
    }

    public int getGcHeapDifferenceWarning(){
        return this.GcHeapDifferenceWarning;
    }
}
