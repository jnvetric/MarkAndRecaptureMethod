package routing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import core.*;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

public class ProphetDF implements RoutingDecisionEngine {

    protected final static String BETA_SETTING = "beta";
    protected final static String FORWARDING_THERESHOLD = "ForwardingThereshold";
    protected final static String P_INIT_SETTING = "initial_p";
    protected final static String SECONDS_IN_UNIT_S = "secondsInTimeUnit";

    protected static final double DEFAULT_P_INIT = 0.75;
    protected static final double GAMMA = 0.92;
    protected static final double DEFAULT_BETA = 0.45;
    protected static final int DEFAULT_UNIT = 30;

    protected double beta;
    protected double pinit;
    protected double lastAgeUpdate;
    protected int secondsInTimeUnit;
    protected double ft;

    private Set<Message> msgStamp;
    private Map<DTNHost, Integer> relayed;
    private DTNHost meHost;

    /**
     * delivery predictabilities
     */
    private Map<DTNHost, Double> preds;

    public ProphetDF(Settings s) {
        if (s.contains(FORWARDING_THERESHOLD)) {
            beta = s.getDouble(FORWARDING_THERESHOLD);
        } else {
            beta = DEFAULT_BETA;
        }

        if (s.contains(P_INIT_SETTING)) {
            pinit = s.getDouble(P_INIT_SETTING);
        } else {
            pinit = DEFAULT_P_INIT;
        }

        if (s.contains(SECONDS_IN_UNIT_S)) {
            secondsInTimeUnit = s.getInt(SECONDS_IN_UNIT_S);
        } else {
            secondsInTimeUnit = DEFAULT_UNIT;
        }

        preds = new HashMap<DTNHost, Double>();
        this.lastAgeUpdate = 0.0;
    }

    public ProphetDF(ProphetDF de) {
        beta = de.beta;
        pinit = de.pinit;
        secondsInTimeUnit = de.secondsInTimeUnit;
        meHost = de.meHost;
        ft = de.ft;
        msgStamp = new HashSet<>();
        relayed = new HashMap<>();
        preds = new HashMap<DTNHost, Double>();
        this.lastAgeUpdate = de.lastAgeUpdate;
    }

    public RoutingDecisionEngine replicate() {
        return new ProphetDF(this);
    }

    public void connectionUp(DTNHost thisHost, DTNHost peer) {
    }

    public void connectionDown(DTNHost thisHost, DTNHost peer) {
    }

    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost myHost = con.getOtherNode(peer);
        ProphetDF de = getOtherProphetDecisionEngine(peer);
        Set<DTNHost> hostSet = new HashSet<DTNHost>(this.preds.size()
                + de.preds.size());
        hostSet.addAll(this.preds.keySet());
        hostSet.addAll(de.preds.keySet());

        this.agePreds();
        de.agePreds();

        // Update preds for this connection
        double myOldValue = this.getPredFor(peer),
                peerOldValue = de.getPredFor(myHost),
                myPforHost = myOldValue + (1 - myOldValue) * pinit,
                peerPforMe = peerOldValue + (1 - peerOldValue) * de.pinit;
        preds.put(peer, myPforHost);
        de.preds.put(myHost, peerPforMe);

        // Update transistivities
        for (DTNHost h : hostSet) {
            myOldValue = 0.0;
            peerOldValue = 0.0;

            if (preds.containsKey(h)) {
                myOldValue = preds.get(h);
            }

            if (de.preds.containsKey(h)) {
                peerOldValue = de.preds.get(h);
            }

            if (h != myHost) {
                preds.put(h, myOldValue + (1 - myOldValue) * myPforHost * peerOldValue * beta);
            }
            if (h != peer) {
                de.preds.put(h, peerOldValue + (1 - peerOldValue) * peerPforMe * myOldValue * beta);

            }
        }
    }

    public boolean newMessage(Message m) {
        m.addProperty(FORWARDING_THERESHOLD, ft);

        return true;
    }

    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost;
    }

    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        msgStamp.add(m);
        meHost = thisHost;
        return m.getTo() != thisHost;
    }

    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
        if (m.getTo() == otherHost) {
            return true;
        }

        ProphetDF de = getOtherProphetDecisionEngine(otherHost);

        ft = (Double) m.getProperty(FORWARDING_THERESHOLD);

        if (de.getPredFor(m.getTo()) > this.getPredFor(m.getTo())
                && de.getPredFor(m.getTo()) > ft) {
            ft = de.getPredFor(m.getTo());
            m.updateProperty(FORWARDING_THERESHOLD, ft);
            return true;
        } else {
            return false;
        }
    }

    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return false;
    }

    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return m.getTo() == hostReportingOld;
    }

    private ProphetDF getOtherProphetDecisionEngine(DTNHost host) {
        MessageRouter otherRouter = host.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (ProphetDF) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    private void agePreds() {
        double timeDiff = (SimClock.getTime() - this.lastAgeUpdate)
                / secondsInTimeUnit;

        if (timeDiff == 0) {
            return;
        }

        double mult = Math.pow(GAMMA, timeDiff);
        for (Map.Entry<DTNHost, Double> e : preds.entrySet()) {
            e.setValue(e.getValue() * mult);
        }

        this.lastAgeUpdate = SimClock.getTime();
    }

    /**
     * Returns the current prediction (P) value for a host or 0 if entry for the
     * host doesn't exist.
     *
     * @param host The host to look the P for
     * @return the current P value
     */
    private double getPredFor(DTNHost host) {
        agePreds(); // make sure preds are updated before getting
        if (preds.containsKey(host)) {
            return preds.get(host);
        } else {
            return 0;
        }
    }

    @Override
    public void update(DTNHost thisHost) {

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
