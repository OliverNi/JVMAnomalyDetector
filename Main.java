import AnomalyDetector.AnomalyDetector;
import AnomalyDetector.JMXAgent;

import java.io.Console;
import java.io.IOException;

/**
 * Created by Oliver on 2014-09-10.
 */
public class Main {
    public static void main(String args[]) throws IOException {
        AnomalyDetector ad = new AnomalyDetector();
        ad.connect("localhost", 3500);

        while (true){
            System.in.read();
            ad.poll();
        }

    }


}
