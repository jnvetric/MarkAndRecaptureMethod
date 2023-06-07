
/*
 * Bubble Rap (by Antok)
 */
package routing.community;

import java.util.*;

import core.*;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

public class SimalirityRouting implements RoutingDecisionEngine, CommunityDetectionEngine {

    // Start-initialisation
    public static final String COMMUNITY_ALG_SETTING = "communityDetectAlg";  //added
    public static final String CENTRALITY_ALG_SETTING = "centralityAlg";

    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;

    protected CommunityDetection community;  //added
    protected Centrality centrality;

    //End-initialisation
    //Constructor based on the settings
    public SimalirityRouting(Settings s) {
        if (s.contains(COMMUNITY_ALG_SETTING)) //added
        {
            this.community = (CommunityDetection) s.createIntializedObject(s.getSetting(COMMUNITY_ALG_SETTING));
        } else {
            this.community = new SimpleCommunityDetection(s);
        }

        if (s.contains(CENTRALITY_ALG_SETTING)) {
            this.centrality = (Centrality) s.createIntializedObject(s.getSetting(CENTRALITY_ALG_SETTING));
        } else {
            this.centrality = new AverageWinCentrality1(s);
        }
    }

    //Constructor based on the argument prototype
    public SimalirityRouting(SimalirityRouting proto) {
        this.community = proto.community.replicate();	//added
        this.centrality = proto.centrality.replicate();
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
    }

    public void connectionUp(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost myHost = con.getOtherNode(peer);
        SimalirityRouting de = this.getOtherDecisionEngine(peer);

        this.startTimestamps.put(peer, SimClock.getTime());
        de.startTimestamps.put(myHost, SimClock.getTime());

        this.community.newConnection(myHost, peer, de.community);	//added	
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
//        double time = startTimestamps.get(peer);
        double time = cek(thisHost, peer);
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
        community.connectionLost(thisHost, peer, peerCD, history); //added

        startTimestamps.remove(peer);
    }
    
    public double cek(DTNHost thisHost, DTNHost peer){
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
            return true;
        }
        
        DTNHost dest = m.getTo();
        SimalirityRouting de = getOtherDecisionEngine(otherHost);
        
        double mySimalirity = de.SimalirityCount(dest);
        double peerSimalirity = this.SimalirityCount(dest);
        
        if(mySimalirity < peerSimalirity){
            return true;
        }else {
            return true;
        }
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        // delete the message once it is forwarded to the node in the dest'community

        SimalirityRouting de = this.getOtherDecisionEngine(otherHost);
        return de.commumesWithHost(m.getTo())
                && !this.commumesWithHost(m.getTo());
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        //BubbleRap de = this.getOtherDecisionEngine(hostReportingOld);
        //return de.commumesWithHost(m.getTo()) &&
        //		!this.commumesWithHost(m.getTo());

        return true;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new SimalirityRouting(this);
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

    private SimalirityRouting getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (SimalirityRouting) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }
    
    protected double SimalirityCount(DTNHost dest) {
        double avg = 0;
        for(Map.Entry<DTNHost, List<Duration>> conn : connHistory.entrySet()){
            if(conn.getKey() == dest){
                List<Duration> dur = conn.getValue();
                Iterator<Duration> i = dur.iterator();
                int frek = 0;
                double totalDuration = 0;
                double total = 0;
                
                Duration d = new Duration(0, 0);
                while(i.hasNext()) {
                    i.next();
                    total = d.end - d.start;
                    totalDuration += total;
                    frek++;
                }
                if(!dur.isEmpty()){
                    avg = totalDuration / frek;
                }
            }
        }
        return avg;
    }

    //for REPORT purpose: CommunityDetectionReport
    @Override
    public Set<DTNHost> getLocalCommunity() {
        return this.community.getLocalCommunity();
    }

    @Override
    public void update(DTNHost thisHost) {}

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean shouldSendMarkToHost(Message m, DTNHost otherHost) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
