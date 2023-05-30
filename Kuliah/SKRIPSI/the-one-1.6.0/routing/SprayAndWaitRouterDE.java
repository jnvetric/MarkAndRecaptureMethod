/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.ObserverNode;
import routing.RoutingDecisionEngine;

/**
 *
 * @author Juan Vetric Kelas routing ini menggunakan decisionEngine Router dan
 * algoritma mark and recapture
 */
public class SprayAndWaitRouterDE implements RoutingDecisionEngine, ObserverNode {

    public static final String NROF_COPIES = "nrofCopies";
    public static final String NROF_MARK = "nrofMark";
    public static final String BINARY_MODE = "binaryMode";
    public static final String RECAPTURE_INTERVAL = "recaptureInterval";
    public static final String SPRAYANDWAIT_NS = "SprayAndWaitRouterDE";
    public static final String MSG_COUNT_PROPERTY = SPRAYANDWAIT_NS + "." + "copies";
    public static final String MSG_MARK_PROPERTY = SPRAYANDWAIT_NS + "." + "copies";

    protected int initialNrofCopies;
    protected boolean isBinary;
    public int initialNrofMark;
    public static final int DEFAULT_INTERVAL = 3600;
    private double lastUpdate = Double.MAX_VALUE;
    protected boolean observerNode;
    //public final String markPrefix;
    private int estimation;
    private int interval;
    private int mark;
    private String mId;
    private Set<DTNHost> markedNode;
    private Set<DTNHost> recaptureNode;
    private Map<DTNHost, Map<Double, Integer>> estimasi;
    private List<String> markMessage;

    public SprayAndWaitRouterDE(Settings s) {
        if (s.contains(BINARY_MODE)) {
            isBinary = s.getBoolean(BINARY_MODE);
        } else {
            isBinary = false; // default value
        }

        if (s.contains(NROF_COPIES)) {
            initialNrofCopies = s.getInt(NROF_COPIES);
        }
        if (s.contains(NROF_MARK)) {
            initialNrofMark = s.getInt(NROF_MARK);
        }

        if (s.contains(RECAPTURE_INTERVAL)) {
            interval = s.getInt(RECAPTURE_INTERVAL);
        } else {
            interval = DEFAULT_INTERVAL;
        }

        // this.markPrefix = s.getSetting(MARK_PREFIX);
        //this.observerNode = false;
        this.mark = 0;
        this.estimation = 0;
    }

    public SprayAndWaitRouterDE(SprayAndWaitRouterDE proto) {
        this.initialNrofCopies = proto.initialNrofCopies;
        this.initialNrofMark = proto.initialNrofMark;
        this.isBinary = proto.isBinary; //this.isBinary = proto.isBinary;
        //this.observerNode = proto.observerNode;
        this.mark = proto.mark;
        //  this.markPrefix = cs.markPrefix;
        this.markedNode = new HashSet<DTNHost>();
        this.recaptureNode = new HashSet<DTNHost>();
        this.interval = proto.interval;
        this.estimation = proto.estimation;
        this.markMessage = new ArrayList<>();
        this.estimasi = new HashMap<DTNHost, Map<Double, Integer>>();

    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {

    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
    }

    @Override
    public boolean newMessage(Message m) {
        DTNHost thisHost = null;

        List<DTNHost> listHop = m.getHops();
        Iterator it = listHop.iterator();

        if (it.hasNext()) {
            thisHost = (DTNHost) it.next();
        }

        if (thisHost.isRadioActive() == false) {

            return false;
        }

        if (m.getPrefix().equals(Observer.getInstance().getMarkPrefix())) {
//            if nya dipakai hanya untuk run random
//            if(!this.markMessage.isEmpty()){
//                return false;
//            }
            m.addProperty(MSG_MARK_PROPERTY, initialNrofMark);
            this.markMessage.add(m.getId());
            //this.mId = this.markMessage.get(0);
            //System.out.println(thisHost + "new" + this.markMessage.get(0));
            return true;
        } else {
            m.addProperty(MSG_COUNT_PROPERTY, copy());
            return true;
        }
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        Integer nrofMark = (Integer) m.getProperty(MSG_MARK_PROPERTY);

        if (isBinary) {
            if (m.getPrefix().equals(Observer.getInstance().getMarkPrefix())) {
                nrofMark = (int) Math.ceil(nrofMark / 2.0);

            }
            nrofCopies = (int) Math.ceil(nrofCopies / 2.0);
        } else {
            if (m.getPrefix().equals(Observer.getInstance().getMarkPrefix())) {
                nrofMark = 1;
            }
            nrofCopies = 1;
        }
        m.updateProperty(MSG_MARK_PROPERTY, nrofMark);
        m.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
        return m.getTo() != thisHost;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
//        if(isObserver(otherHost)){
//           return false;
//       }
        if (m.getTo() == otherHost) {
            return true;
        }
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);

        if (nrofCopies > 1) {
            return true;
        }
        return false;

    }

    @Override
    public boolean shouldSendMarkToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
        String cekHost = "" + thisHost;
        if (isObserver(otherHost)) {
            return false;
        }
        Collection<Message> messages = otherHost.getMessageCollection();
        String markPrefix = Observer.getInstance().getMarkPrefix();

        if (otherHost.isRadioActive() == true) {

            if (m.getPrefix().equals(markPrefix)) {

                if (m.getTo() == otherHost) {
                    return true;
                }

                Integer nrofMark = (Integer) m.getProperty(MSG_MARK_PROPERTY);

                for (Iterator<Message> iterator = messages.iterator(); iterator.hasNext();) {
                    Message msg = iterator.next();
                    if (!thisHost.equals(msg.getFrom()) && nrofMark > 1) {
                        return true;
                    }
                }

                //memulai recapture
                if (cekHost.startsWith("Obs") && !markMessage.isEmpty() && nrofMark == 1) {
                    //String markMessageId = this.markMessage.get(0);
                    //System.out.println(cekHost + "should sent" + markMessageId);
                    for (Iterator<Message> iterator = messages.iterator(); iterator.hasNext();) {
                        Message msgItr = iterator.next();
                        //if(m.getPrefix().equals(markPrefix)  ){
                        if (!recaptureNode.contains(otherHost)) {
                            recaptureNode.add(otherHost);

                        }
                        if (thisHost.equals(msgItr.getFrom()) && markMessage.get(0).equals(msgItr.getId())) {

                            markedNode.add(otherHost);
                            //recaptureNode.add(otherHost);
                        }
//                            else {
//                                recaptureNode.add(otherHost);
//                            } 

                        //}
                    }
                }

            }
        }

        return false;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        if (m.getTo() == otherHost) {
            return false;
        }
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        Integer nrofMark = (Integer) m.getProperty(MSG_MARK_PROPERTY);
        if (isBinary) {
            if (m.getPrefix().equals(Observer.getInstance().getMarkPrefix())) {
                nrofMark /= 2;
            }
            nrofCopies /= 2;
        } else {
            if (m.getPrefix().equals(Observer.getInstance().getMarkPrefix())) {
                nrofMark--;
            }
            nrofCopies--;
        }
        m.updateProperty(MSG_MARK_PROPERTY, nrofMark);
        m.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return m.getTo() == hostReportingOld;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new SprayAndWaitRouterDE(this);
    }

    private SprayAndWaitRouterDE getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + "with other routers of same type";

        return (SprayAndWaitRouterDE) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public void update(DTNHost host) {
        String cekHost = "" + host;
        double currentTime = SimClock.getIntTime();

        Set<String> messageToDelete = new HashSet<>();
        String markPrefix = Observer.getInstance().getMarkPrefix();
        Collection<Message> messagesCollection = host.getMessageCollection();

        for (Iterator<Message> iterator = messagesCollection.iterator(); iterator.hasNext();) {
            Message msg = iterator.next();

            if (msg.getPrefix().equals(markPrefix)) {
                if (msg.getFrom() == host) {
                    int messageTtl = msg.getTtl();
                    if (messageTtl <= 0) {

                        messageToDelete.add(msg.getId());
                        msg.removeProperty(MSG_MARK_PROPERTY);
                        this.setEstimation(0);
                        this.markedNode.clear();
                        this.recaptureNode.clear();
                        if (markMessage.contains(msg.getId())) {
                            this.markMessage.clear();
                        }
                        //this.markMessage.remove(0);
//                        this.mId = this.markMessage.get(0);

                    }
                } else {
                    int messageTtl = msg.getTtl();
                    if (messageTtl <= 0) {
                        messageToDelete.add(msg.getId());
                        msg.removeProperty(MSG_MARK_PROPERTY);
                        if (markMessage.contains(msg.getId())) {
                            this.markMessage.clear();
                        }
                    }
                }

            }
        }

        for (String messageId : messageToDelete) {
            //System.out.println("");
            host.deleteMessage(messageId, true);
            //this.markMessage.remove(messageId);
        }

        if (currentTime - lastUpdate >= interval) {
            //System.out.println(currentTime-lastUpdate);
            if (cekHost.startsWith("Obs") && !markMessage.isEmpty()) {

                for (Message msg : host.getMessageCollection()) {

                    if (msg.getPrefix().equals(markPrefix)) {
                        if (host.equals(msg.getFrom())) {
                            Integer nrofMark = (Integer) msg.getProperty(MSG_MARK_PROPERTY);

                            if (nrofMark == 1) {
                                //                            System.out.println("");
                                //                            System.out.println("Node " + host.getName());
                                //                            System.out.println("nrofMark " + nrofMark);
                                //                            System.out.println("Interval " + SimClock.getIntTime());
                                //                            System.out.println("TTL "+ msg.getTtl());
                                //                            System.out.println("m " + this.markedNode.size());
                                if (!markedNode.isEmpty()) {
                                    int tempEstimation = (initialNrofMark * this.recaptureNode.size()) / this.markedNode.size();
                                    this.setEstimation(tempEstimation);
                                    System.out.println("Interval " + SimClock.getIntTime());
                                    System.out.println("Node " + host.getName());
                                    System.out.println("Mark " + initialNrofMark);
                                    System.out.println("Recapture " + this.recaptureNode.size());
                                    System.out.println("m " + this.markedNode.size());
                                    System.out.println("Estimasi " + this.getEstimation());
                                    System.out.println("");
                                    Map<Double, Integer> innerMap = new HashMap<>();
                                    innerMap.put(currentTime, tempEstimation);
                                    if (!estimasi.containsKey(host)) {
                                        estimasi.put(host, innerMap);
                                    } else {
                                        estimasi.get(host).put(currentTime, tempEstimation);
                                    }

                                }
                            }
                        }
                    }
                }
            }

        }
        this.lastUpdate = currentTime - currentTime % interval;

    }

    public void setEstimation(int estimation) {
        this.estimation = estimation;
    }

    private int copy() {
        return this.estimation / 2;
    }

    @Override
    public boolean getObserver() {
        return this.observerNode;
    }

    @Override
    public int getEstimation() {
        return estimation;
    }

    private boolean isObserver(DTNHost otherHost) {
        if (otherHost.toString().startsWith("Obs")) {
            return true;
        }
        return false;
    }

    @Override
    public Map<DTNHost, Map<Double, Integer>> getEstimasi() {
        return estimasi;
    }
}
