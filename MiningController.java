package testing;

import battlecode.common.*;

public class MiningController {
	public RobotController rc;
	public FastIterableLocSet safeLocations;
	
	public MiningController(RobotController rc) {
		this.rc = rc;
		safeLocations.add(rc.senseHQLocation());
		for (MapLocation tower:rc.senseTowerLocations()) {
			safeLocations.add(tower);
		}
	}
	
	public MapLocation retreat(Direction enemyDir) { //go to closest safepoint (may want to check for direction later)
		if (safeLocations.size == 1) { //only HQ is left:
			return rc.senseHQLocation();
		}
		//otherwise:
		MapLocation[] points = safeLocations.getKeys();
		int minDist = Integer.MAX_VALUE;
		MapLocation result=null, test;
		int distance;
		
		for (int i = 0; i<points.length; i++) {
			test = points[i];
			distance = rc.getLocation().distanceSquaredTo(test);
			if (distance<minDist) {
				result = test;
			}
		}
		return result;
	}
	
}