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
 * @author aurelius_aria
 */
public class MarkAndRecaptureReport extends Report implements UpdateListener {

    private static Map<DTNHost, Map<Double, ArrayList<Integer>>> estimasi;
    private static List<Double> intervalTime;
    private double lastUpdate = 0;
    private double updateInterval = 3600;

    public MarkAndRecaptureReport() {
        estimasi = new HashMap<DTNHost, Map<Double, ArrayList<Integer>>>();
        intervalTime = new ArrayList<Double>();
        lastUpdate = 0;
        updateInterval = 3600;
    }

    @Override
    public void updated(List<DTNHost> hosts) {
        if (SimClock.getTime() - lastUpdate >= updateInterval) {
            lastUpdate = SimClock.getTime();
            intervalTime.add(lastUpdate);

            List<DTNHost> observer = SimScenario.getInstance().getObserver();
            for (DTNHost obs : observer) {
                for (DTNHost host : hosts) {
                    if (obs == host) {
                        MessageRouter mr = obs.getRouter();
                        RoutingDecisionEngine de = ((DecisionEngineRouter) mr).getDecisionEngine();
                        ObserverNode ob = (ObserverNode) de;
                        Map<Double, ArrayList<Integer>> innerMap = new HashMap<Double, ArrayList<Integer>>();
                        ArrayList<Integer> listEs = new ArrayList<>();

                        listEs.add(ob.getEstimation());

                        innerMap.put(lastUpdate, listEs);
                        if (!estimasi.containsKey(obs)) {
                            estimasi.put(obs, innerMap);
                        } else {
                            estimasi.get(obs).put(lastUpdate, listEs);
                        }
//                          if(!estimasi.containsKey(obs)){
//                              ArrayList listEstimasi = new ArrayList(); 
//                              estimasi.put(obs, new HashMap<Double, ArrayList<Integer>>());
//                              estimasi.get(obs).put(lastUpdate, listEstimasi);
//                          }else{
//                              int es;
//                              es = ob.getEstimation();
//                              Map<Double, ArrayList<Integer>> existingMap = estimasi.get(obs);
//                              if(!existingMap.containsKey(lastUpdate)){
//                                  ArrayList<Integer> existingList = existingMap.get(lastUpdate);
//                                  if(existingList != null){
//                                      existingList.add(es);
//                                  }
//                              }
//                          }
                        //done();

                    }
                }
//                  
            }
        }
    }

    @Override
    public void done() {
        String obs;
        String interval;
        String estimasiPerObs;
        for (Map.Entry<DTNHost, Map<Double, ArrayList<Integer>>> entry : estimasi.entrySet()) {
            obs = "Observer : " + entry.getKey();
            Map<Double, ArrayList<Integer>> innerMap = entry.getValue();

            write(obs);

            for (Map.Entry<Double, ArrayList<Integer>> innerEntry : innerMap.entrySet()) {
                estimasiPerObs = BigDecimal.valueOf(innerEntry.getKey()) + " Estimasi: " + innerEntry.getValue();
                write(estimasiPerObs);
            }
        }
//        for(Map.Entry<DTNHost,ArrayList<Integer>> es : estimasi.entrySet()){
//            obs = "Obs : " + es.getKey();
//            write(obs);
//            for(Integer value : es.getValue() ){
//                estimasiPerObs = updateInterval + " : " + value + "\n";
//                write(estimasiPerObs);
//            }
//            
//        }

        super.done();
    }

}
