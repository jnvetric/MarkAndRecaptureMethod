/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package routing.community;

import java.util.ArrayList;
import java.util.List;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import routing.RoutingDecisionEngine;
import static routing.SprayAndWaitRouter.BINARY_MODE;
import static routing.SprayAndWaitRouter.MSG_COUNT_PROPERTY;
import static routing.SprayAndWaitRouter.NROF_COPIES;
import static routing.SprayAndWaitRouter.SPRAYANDWAIT_NS;

/**
 * Implementation of Spray and wait router as depicted in
 * <I>Spray and Wait: An Efficient Routing Scheme for Intermittently Connected
 * Mobile Networks</I> by Thrasyvoulos Spyropoulus et al.
 *
 */
public class SprayAndWaitKu implements RoutingDecisionEngine {

    /**
     * identifier for the initial number of copies setting ({@value})
     */
    public static final String NROF_COPIES = "nrofCopies";
    /**
     * identifier for the binary-mode setting ({@value})
     */
    public static final String BINARY_MODE = "binaryMode";
    /**
     * SprayAndWait router's settings name space ({@value})
     */
    public static final String SPRAYANDWAIT_NS = "SprayAndWaitKu";
    /**
     * Message property key
     */
    public static final String MSG_COUNT_PROPERTY = SPRAYANDWAIT_NS + "."
            + "copies";

    protected int initialNrofCopies;
    protected boolean isBinary;

    public SprayAndWaitKu(Settings s) {
        //super(s);
//        Settings snwSettings = new Settings(SPRAYANDWAIT_NS);
//
//        initialNrofCopies = snwSettings.getInt(NROF_COPIES);
//        isBinary = snwSettings.getBoolean(BINARY_MODE);
//        if (s.contains(NROF_COPIES)) {
//            initialNrofCopies = snwSettings.getInt(NROF_COPIES);
//        } else {
//            initialNrofCopies = 25;
//        }
//
//        if (s.contains(BINARY_MODE)) {
//            isBinary = snwSettings.getBoolean(BINARY_MODE);
//        } else {
//            isBinary = true;
//        }
        Settings snwSettings = new Settings(SPRAYANDWAIT_NS);

        initialNrofCopies = snwSettings.getInt(NROF_COPIES);
        isBinary = snwSettings.getBoolean(BINARY_MODE);
    }

    /**
     * Copy constructor.
     *
     * @param r The router prototype where setting values are copied from
     */
    protected SprayAndWaitKu(SprayAndWaitKu r) {
//        super(r);
        this.initialNrofCopies = r.initialNrofCopies;
        this.isBinary = r.isBinary;
    }

    @Override
    public SprayAndWaitKu replicate() {
        return new SprayAndWaitKu(this);
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
        m.addProperty(MSG_COUNT_PROPERTY, initialNrofCopies);
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        if (isBinary) {
            //in binary S'n'M the receiving node gets ceil(n/2) copies
            nrofCopies = (int) Math.ceil(nrofCopies / 2.0);
        } else {
            // in standard S'n'M the receiving node get only single copy
            nrofCopies = 1;
        }

        m.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);

        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
//        int nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
//
//        if (isBinary) {
//            nrofCopies = (int) Math.ceil(nrofCopies / 2.0);
//        } else {
//            nrofCopies = 1;
//        }
//
//        m.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
//
//        return m.getTo() == thisHost;
        return m.getTo() != thisHost; //pesan tidak akan disimpan jika itu node tujuannya
    }

//    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
////        if (m.getTo() == otherHost) {
////            return true;
////        }
////
////        int nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
////
////        if (nrofCopies > 1) {
////            return true;
////        }
////
//        return false;
//    }
    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        int nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);

        if (isBinary) {
            nrofCopies /= 2;
        } else {
            nrofCopies--;
        }

        m.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);

        return m.getTo() == otherHost;
//        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return false;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
        if (m.getTo() == otherHost) {
            return true;
        }

        int nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);

        if (nrofCopies > 1) {
            return true;
        }

        return false;
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
