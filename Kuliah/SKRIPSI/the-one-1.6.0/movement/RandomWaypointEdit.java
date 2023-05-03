/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package movement;

import core.Coord;
import core.Settings;

/**
 * Random waypoint movement model. Creates zig-zag paths within the
 * simulation area.
 */
public class RandomWaypointEdit extends MovementModel {
	/** how many waypoints should there be per path */
	private static final int PATH_LENGTH = 1;
	private Coord lastWaypoint;
	
	public RandomWaypointEdit(Settings settings) {
		super(settings);
	}
	
	protected RandomWaypointEdit(RandomWaypointEdit rwp) {
		super(rwp);
	}
	
	/**
	 * Returns a possible (random) placement for a host
	 * @return Random position on the map
	 */
	@Override
	public Coord getInitialLocation() {
		assert rng != null : "MovementModel not initialized!";
		Coord c = randomCoord();

		this.lastWaypoint = c;
		return c;
	}
	
	@Override
	public Path getPath() {
		Path p;
		p = new Path(generateSpeed());
		p.addWaypoint(lastWaypoint.clone());
		Coord c = lastWaypoint;
		
		for (int i=0; i<PATH_LENGTH; i++) {
			c = randomCoord();
			p.addWaypoint(c);	
		}
		
		this.lastWaypoint = c;
		return p;
	}
	
	@Override
	public RandomWaypointEdit replicate() {
		return new RandomWaypointEdit(this);
	}
	
	protected Coord randomCoord() {
		return new Coord(rng.nextDouble() * getMaxX(),
				rng.nextDouble() * getMaxY());
	}
}
