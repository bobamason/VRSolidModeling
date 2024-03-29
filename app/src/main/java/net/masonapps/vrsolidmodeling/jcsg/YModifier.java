/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.masonapps.vrsolidmodeling.jcsg;

import eu.mihosoft.vvecmath.Vector3d;

/**
 * Modifies along y axis.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class YModifier implements WeightFunction {

    private Bounds bounds;
    private double min = 0;
    private double max = 1.0;

    private double sPerUnit;
    private boolean centered;

    /**
     * Constructor.
     */
    public YModifier() {
    }

    /**
     * Constructor.
     *
     * @param centered defines whether to center origin at the csg location
     */
    public YModifier(boolean centered) {
        this.centered = centered;
    }

    @Override
    public double eval(Vector3d pos, CSG csg) {

        if (bounds == null) {
            this.bounds = csg.getBounds();
            sPerUnit = (max - min) / (bounds.getMax().y() - bounds.getMin().y());
        }

        double s = sPerUnit * (pos.y() - bounds.getMin().y());

        if (centered) {
            s = s - (max - min) / 2.0;

            s = Math.abs(s) * 2;
        }

        return s;
    }

}
