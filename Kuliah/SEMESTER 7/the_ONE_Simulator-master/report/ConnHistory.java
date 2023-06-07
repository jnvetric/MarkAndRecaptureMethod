/*
 * @(#)CommunityDetectionReport.java
 *
 * Copyright 2010 by University of Pittsburgh, released under GPLv3.
 * 
 */
package report;

import java.util.*;

import core.*;
import java.util.Map.Entry;
import routing.*;
import routing.community.FrequencyConn;
import routing.community.CommunityDetectionEngine;
import routing.community.ConnDetectionHistory;
import routing.community.DistributedBubbleRap;
import routing.community.Duration;

/**
 * <p>
 * Reports the local communities at each node whenever the done() method is
 * called. Only those nodes whose router is a DecisionEngineRouter and whose
 * RoutingDecisionEngine implements the
 * routing.community.CommunityDetectionEngine are reported. In this way, the
 * report is able to output the result of any of the community detection
 * algorithms.</p>
 *
 * @author PJ Dillon, University of Pittsburgh
 */
public class ConnHistory extends Report {

    public ConnHistory() {
        init();
    }

    @Override
    public void done() {
        List<DTNHost> hosts = SimScenario.getInstance().getHosts();
        
        String print = " ";
        
        for (DTNHost h : hosts) {
            
            MessageRouter mr = h.getRouter();
            DecisionEngineRouter de = (DecisionEngineRouter) mr;
            FrequencyConn dbr = (FrequencyConn) de.getDecisionEngine();
            ConnDetectionHistory hd = (ConnDetectionHistory) dbr;
            
            Map<DTNHost, List<Duration>> connHistory = hd.getConnHistory();
            
            print += h + " ";

//            write += host + " "
            // Test to see if another node already reported this community
            for (Map.Entry<DTNHost, List<Duration>> c : connHistory.entrySet()) {
                DTNHost e = c.getKey();
                List<Duration> d = c.getValue();

                Iterator<Duration> i = d.iterator();
                int frek = 0;
                double sum = 0;

                while (i.hasNext()) {
                    Duration z = i.next();
                    Double a = z.end - z.start;
                    sum += a;
                    frek++;
                }
                if (d.size() != 0) {
                    double mean = sum / frek;
                    print += mean + " ";
                }
            }
            print += "\n";
        }
        write(print);
        super.done();
    }

}
