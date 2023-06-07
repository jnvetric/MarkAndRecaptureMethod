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
import routing.community.PeREngine;
import routing.community.PeopleRankNew;

/**
 *
 * @author user
 */
public class pRankReport extends Report {
    public pRankReport() {
        init();
    }

    @Override
    public void done() {
        List<DTNHost> hosts = SimScenario.getInstance().getHosts();
        
        String write = "";
        
        for (DTNHost h : hosts) {
            MessageRouter mr = h.getRouter();
            DecisionEngineRouter de = (DecisionEngineRouter) mr;
            PeopleRankNew pRank = (PeopleRankNew) de.getDecisionEngine();
            
            String peopleRank = String.format("%.2f", Float.valueOf((float)pRank.countRank()));
            write += h + " People Rank : "+peopleRank+"\tFriend List : " + pRank.peopleRank();
            
            write += "\n";
        }
        write(write);
        super.done();
    }

}
