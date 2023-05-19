/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.SimClock;
import core.SimScenario;
import core.UpdateListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.ObserverNode;
import routing.RoutingDecisionEngine;

/**
 *
 * @author Juan V
 */
public class MarkAndRecaptureReport_2 extends Report {

    private static Map<DTNHost, Map<Double, ArrayList<Integer>>> estimasi;
    private static List<Double> intervalTime;
    private double lastUpdate = 0;
    private double updateInterval = 3600;

    public MarkAndRecaptureReport_2() {
        super.init();

    }

    @Override
    public void done() {
        List<DTNHost> observer = SimScenario.getInstance().getObserver();
        for (DTNHost obs : observer) {
            String obsX;
            String estPerObs;
            MessageRouter mr = obs.getRouter();
            RoutingDecisionEngine de = ((DecisionEngineRouter) mr).getDecisionEngine();
            ObserverNode ob = (ObserverNode) de;
            for (Map.Entry<DTNHost, Map<Double, Integer>> entry : ob.getEstimasi().entrySet()) {
                obsX = "" + entry.getKey();
                Map<Double, Integer> innerMap = entry.getValue();
                write(obsX);
                for (Map.Entry<Double, Integer> innerEntry : innerMap.entrySet()) {
                    estPerObs = BigDecimal.valueOf(innerEntry.getKey()) + " : " + innerEntry.getValue();
                    write(estPerObs);
                }
            }
        }
        super.done();
    }

}
