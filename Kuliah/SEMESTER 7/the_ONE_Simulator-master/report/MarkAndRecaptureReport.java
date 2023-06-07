/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.SimScenario;
import java.util.List;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.SprayAndWaitRouterDE;
import routing.community.PeREngine;
import routing.community.PeopleRankNew;

/**
 *
 * @author user
 */
public class MarkAndRecaptureReport extends Report {
    public MarkAndRecaptureReport() {
        init();
    }

    @Override
    public void done() {
        List<DTNHost> hosts = SimScenario.getInstance().getHosts();
        
        String write = "";
        
        for (DTNHost h : hosts) {
            MessageRouter mr = h.getRouter();
            DecisionEngineRouter de = (DecisionEngineRouter) mr;
           SprayAndWaitRouterDE snw= (SprayAndWaitRouterDE) de.getDecisionEngine();
            
//            String sNw = String.format("%.2f", Float.valueOf((float) snw.getEstimation()));
            write += h + " " + snw.getEstimation();
            
            write += "\n";
        }
        write(write);
        super.done();
    }

}
