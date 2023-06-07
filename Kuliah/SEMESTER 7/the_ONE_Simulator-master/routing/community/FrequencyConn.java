
/*
 * Bubble Rap (by Antok)
 */
package routing.community;

import java.util.*;

import core.*;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

public class FrequencyConn implements RoutingDecisionEngine,
        CommunityDetectionEngine {

    // Start-initialisation
    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;

    protected CommunityDetection community;  //added
    protected Centrality centrality;

    //End-initialisation
    //Constructor based on the settings
    public FrequencyConn(Settings s) {

    }

    //Constructor based on the argument prototype
    public FrequencyConn(FrequencyConn proto) {
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
    }

    public void connectionUp(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost myHost = con.getOtherNode(peer);
        FrequencyConn de = this.getOtherDecisionEngine(peer);

        this.startTimestamps.put(peer, SimClock.getTime());
        de.startTimestamps.put(myHost, SimClock.getTime());

//        this.community.newConnection(myHost, peer, de.community);	//added	
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        double time = startTimestamps.get(peer);
//        double time = cek(thisHost, peer);
        double etime = SimClock.getTime();

        // Find or create the connection history list
        List<Duration> history;
        if (!connHistory.containsKey(peer)) {
            history = new LinkedList<Duration>();
            connHistory.put(peer, history);
        } else {
            history = connHistory.get(peer);
        }

        // add this connection to the list
        if (etime - time > 0) {
            history.add(new Duration(time, etime));
        }

        CommunityDetection peerCD = this.getOtherDecisionEngine(peer).community; //added
//        community.connectionLost(thisHost, peer, peerCD, history); //added

        startTimestamps.remove(peer);
////        double time = startTimestamps.get(peer);
//        double time = cek(thisHost, peer);
//        double etime = SimClock.getTime();
//
//        // Find or create the connection history list
//        List<Duration> history;
//        if (!connHistory.containsKey(peer)) {
//            history = new LinkedList<Duration>();
//            connHistory.put(peer, history);
//        } else {
//            history = connHistory.get(peer);
//        }
//
//        // add this connection to the list
//        if (etime - time > 0) {
//            history.add(new Duration(time, etime));
//        }
//
//        CommunityDetection peerCD = this.getOtherDecisionEngine(peer).community; //added
//        community.connectionLost(thisHost, peer, peerCD, history); //added
//
//        startTimestamps.remove(peer);
    }

//    public double cek(DTNHost thisHost, DTNHost peer) {
//        if (startTimestamps.containsKey(thisHost)) {
//            startTimestamps.get(peer);
//        }
//        return 0;
//    }
    @Override
    public boolean newMessage(Message m) {
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost;

    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        return m.getTo() != thisHost;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
        if (m.getTo() == otherHost) {
            return true; // deliver to final destination
        }
        //now we decide where to forward a message to relay node
        DTNHost dest = m.getTo();
        FrequencyConn de = getOtherDecisionEngine(otherHost);

        if (de.hitung(dest) > this.hitung(dest)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        // delete the message once it is forwarded to the node in the dest'community

//        FrequencyConn de = this.getOtherDecisionEngine(otherHost);
//        return de.commumesWithHost(m.getTo())
//                && !this.commumesWithHost(m.getTo());
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        //BubbleRap de = this.getOtherDecisionEngine(hostReportingOld);
        //return de.commumesWithHost(m.getTo()) &&
        //		!this.commumesWithHost(m.getTo());

        return false;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new FrequencyConn(this);
    }

    protected boolean commumesWithHost(DTNHost h) {
        return community.isHostInCommunity(h);
    }

    protected double getLocalCentrality() {
        return this.centrality.getLocalCentrality(connHistory, community);
    }

    protected double getGlobalCentrality() {
        return this.centrality.getGlobalCentrality(connHistory);
    }

    private FrequencyConn getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (FrequencyConn) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    //for REPORT purpose: CommunityDetectionReport
    @Override
    public Set<DTNHost> getLocalCommunity() {

        return this.community.getLocalCommunity();
    }

    @Override
    public void update(DTNHost thisHost) {

    }

    public int hitung(DTNHost a) {
        if (connHistory.containsKey(a)) { //node yg berada dalam map
            return connHistory.get(a).size(); // return size 
        } else {
            return 0; //return false
        }
    }

    public double sumCon(DTNHost dest) {
        double sumDuration = 0;
        if (connHistory.containsKey(dest)) {
            List<Duration> duration = new LinkedList<Duration>(connHistory.get(dest));
            Iterator<Duration> i = duration.iterator();

            double total = 0;

            Duration d = new Duration(0, 0);
            while (i.hasNext()) {
                i.next();
                total = d.end - d.start;
                sumDuration += total;
            }
        }
        return sumDuration;
    }
    
    public double getInterCon(DTNHost h){
         double sumDuration = 0;
        if (connHistory.containsKey(h)) {
            List<Duration> duration = new LinkedList<Duration>(connHistory.get(h));
            Iterator<Duration> i = duration.iterator();
            
            Duration dummy;
            double total = 0;
            double totalInterCon;
            Duration d = new Duration(0, 0);
            d = i.next();
            
            while (i.hasNext()) {
                dummy = d;
                d = i.next();
                total = d.start - dummy.end;
                sumDuration += total;
            }
                totalInterCon = sumDuration/connHistory.get(h).size();
                return sumDuration;
                
        }else {
            return 0;
        }
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean shouldSendMarkToHost(Message m, DTNHost otherHost) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
