
/*
 * Bubble Rap (by Antok)
 */
package routing.community;

import java.util.*;

import core.*;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

public class PeopleRankNew implements RoutingDecisionEngine, PeREngine {

    // Start-initialisation
    public static final String COMMUNITY_ALG_SETTING = "communityDetectAlg";  //added
    public static final String CENTRALITY_ALG_SETTING = "centralityAlg";

    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Friend>> pRank;
    protected Map<DTNHost, Tuple<Double, Integer>> peopleRank;
    protected HashMap<DTNHost, List<Duration>> connHistory;
    public Map<DTNHost, Double> contactDur;
    
    protected Double d = 0.5;
    public int threshold = 15;
    public double thisRank = 0;

    protected CommunityDetection community;  //added
    protected Centrality centrality;

    //End-initialisation
    //Constructor based on the settings
    public PeopleRankNew(Settings s) {

    }

    //Constructor based on the argument prototype
    public PeopleRankNew(PeopleRankNew proto) {
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
        peopleRank = new HashMap<DTNHost, Tuple<Double, Integer>>();
        contactDur = new HashMap<DTNHost, Double>();
    }

    public void connectionUp(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost myHost = con.getOtherNode(peer);
        PeopleRankNew de = this.getOtherDecisionEngine(peer);

        this.startTimestamps.put(peer, SimClock.getTime());
        de.startTimestamps.put(myHost, SimClock.getTime());

//        this.community.newConnection(myHost, peer, de.community);	//added	
        //buat kalo udah temenan
        if (this.peopleRank.keySet().contains(peer)) {
            Tuple<Double, Integer> f = new Tuple<Double, Integer>(de.countRank(), de.countPeer());

            this.peopleRank.put(peer, f);
            this.thisRank = this.countRank();
        }
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        //buat cek temen baru, apakah udah bisa jadi temen atau belum?
        PeopleRankNew de = this.getOtherDecisionEngine(peer);
        double time = startTimestamps.get(peer);
        //double time = cek(thisHost, peer);
        double etime = SimClock.getTime();

        // Find or create the connection history list
        List<Duration> history;
        if (!connHistory.containsKey(peer)) {
            history = new LinkedList<Duration>();
            connHistory.put(peer, history);
        } else {
            history = connHistory.get(peer);
        }

        double dur = (etime - time);
        // add this connection to the list
        if (etime - time > 0) {
            history.add(new Duration(time, etime));
        }

        if (contactDur.containsKey(peer)) {
            dur += (etime - time) + contactDur.get(peer);
        }
        contactDur.put(peer, dur);

        //hitung kontak durasi
        if (!contactDur.containsKey(peer)) { //kalo belum pernah ketemu
            contactDur.put(peer, dur);
        } else { //kalo udah pernah ketemu
            if (this.contactDur.get(peer) >= threshold) { //kalo udah kontak lebih dari threshold
                Tuple f = new Tuple<Double, Integer>(de.countRank(), de.countPeer());
                peopleRank.put(peer, f);
            }
        }

        CommunityDetection peerCD = this.getOtherDecisionEngine(peer).community; //added
//        community.connectionLost(thisHost, peer, peerCD, history); //added
        startTimestamps.remove(peer);
    }

    public double cek(DTNHost thisHost, DTNHost peer) {
        if (startTimestamps.containsKey(thisHost)) {
            startTimestamps.get(peer);
        }
        return 0;
    }

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

        // Which of us has the dest in our local communities, this host or the peer
        PeopleRankNew de = this.getOtherDecisionEngine(otherHost);
        double myRank = this.countRank();
        double peerRank = de.countRank();

        if (myRank < peerRank) {
            return true;
        } else {
            return false;
        }
    }
    
    public Map<DTNHost, Tuple<Double, Integer>> peopleRank(){
        return peopleRank;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return false;
    }
    

    @Override
    public RoutingDecisionEngine replicate() {
        return new PeopleRankNew(this);
    }

    protected Double sumContactTime(DTNHost dest) {
        double sumDuration = 0;
        for (Map.Entry<DTNHost, List<Duration>> conn : connHistory.entrySet()) {
            if (conn.getKey() == dest) {
                List<Duration> dur = conn.getValue();
                Iterator<Duration> i = dur.iterator();
//                int frek = 0;
                double total = 0;

                Duration d = new Duration(0, 0);
                while (i.hasNext()) {
                    i.next();
                    total = d.end - d.start;
                    sumDuration += total;
//                    frek++;
                }

//                if (!dur.isEmpty()) {
//                    avg = totalDuration / frek;
//                }
            }
        }
        return sumDuration;
    }

    public double countRank() {
        double avg = 0.0;
        for (DTNHost h : this.peopleRank.keySet()){
            if(this.peopleRank.get(h).getValue() != 0){
                avg += (Double) this.peopleRank.get(h).getKey() /
                        this.peopleRank.get(h).getValue();
            }
        }
        return (1 - d )  + (d * avg);
    }

    protected int countPeer() {
        return this.peopleRank.size();
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

    private PeopleRankNew getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (PeopleRankNew) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public void update(DTNHost thisHost) {
    }

    @Override
    public double getRank() {
        return thisRank;
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
