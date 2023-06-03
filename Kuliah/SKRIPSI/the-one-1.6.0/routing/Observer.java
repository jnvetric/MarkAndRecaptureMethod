/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing;

import core.DTNHost;
import core.Settings;
import java.util.List;

/**
 *
 * @author acer
 */
public class Observer {

    public static final String DEFAULT_MARK_PREFIX = "markMessage";
    private static Observer instance;

    private String markPrefix = DEFAULT_MARK_PREFIX;
    protected List<DTNHost> observer;

    public Observer() {

    }

    public String getMarkPrefix() {
        return markPrefix;
    }

    public void setMarkPrefix(String markPrefix) {
        this.markPrefix = markPrefix;
    }

    public static Observer getInstance() {
        if (instance == null) {
            return instance = new Observer();
        }
        return instance;
    }
}
