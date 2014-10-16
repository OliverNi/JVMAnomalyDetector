package GUI;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Oliver on 2014-10-16.
 */
public class GridBagUtilities {

    public static GridBagConstraints makeCell(JComponent parent, JComponent component, Point point, double weightx, double weighty, int width, int fill, int anchor) {
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = fill;
        constraints.gridwidth = width;
        constraints.weightx = weightx;
        constraints.weighty = weighty;
        constraints.gridx = point.x;
        constraints.gridy = point.y;
        constraints.anchor = anchor;

        parent.add(component, constraints);

        return constraints;
    }
}
