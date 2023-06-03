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
public class MarkAndRecaptureReport_3 extends Report {

    public MarkAndRecaptureReport_3() {
        super.init();
    }

    @Override
    public void done() {
        List<DTNHost> observer = SimScenario.getInstance().getObserver();
        for (DTNHost obs : observer) {
            String obser;
            String estimasiPerObs;
            MessageRouter mr = obs.getRouter();
            RoutingDecisionEngine de = ((DecisionEngineRouter) mr).getDecisionEngine();
            ObserverNode ob = (ObserverNode) de;
            for (Map.Entry<DTNHost, Map<Double, Integer>> entry : ob.getEstimasi().entrySet()) {
                obser = "" + entry.getKey();
                Map<Double, Integer> innerMap = entry.getValue();
                write(obser);
                for (Map.Entry<Double, Integer> innerEntry : innerMap.entrySet()) {
                    double key = innerEntry.getKey();
                    String formattedKey = String.format("%.6f", key);
                    estimasiPerObs = formattedKey + " : " + innerEntry.getValue();
                    write(estimasiPerObs);
                }
            }
        }
        super.done();
    }
}
