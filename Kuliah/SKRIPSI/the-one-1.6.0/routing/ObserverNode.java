/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing;

import core.DTNHost;
import java.util.Map;

/**
 *
 * @author Juan V
 * 
 */
public interface ObserverNode {
    public boolean getObserver();
    public int getEstimation();
    public Map<DTNHost, Map<Double, Integer>> getEstimasi();
}
