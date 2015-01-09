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
	
	public MapLocation retreat(MapLocation enemyLoc) {
		return enemyLoc;
		
	}
	
	public MapLocation findMiningLocation() {
    	MapLocation check;
    	boolean[][] seeable = {{true, true, true, true, true}, {true, true, true, true, true}, 
    			{true, true, true, true, true}, {true, true, true, true, false}, {true, true, true, false, false}};
    	int sightRange = (int)Math.floor(Math.sqrt(24));
    	double maxOre = 0;
    	MapLocation result = null;
    	for (int i = sightRange; i>=0; i--) {
    		for (int j = sightRange; j>=0; j--) {
    			if (seeable[i][j] && (i != 0 && j != 0)) {
    				check = new MapLocation(rc.getLocation().x + i, rc.getLocation().y + j);
    				if (rc.senseOre(check) * getLocationWeighting(check) > maxOre) {
    					result = check;
    					maxOre = rc.senseOre(check);
    				}
    				check = new MapLocation(rc.getLocation().x - i, rc.getLocation().y - j);
    				if (rc.senseOre(check) * getLocationWeighting(check) > maxOre) {
    					result = check;
    					maxOre = rc.senseOre(check);
    				}
    			}
    				
    		}
    	}
    	return result;
    }
	
	public double getLocationWeighting(MapLocation loc) {
		double weight = 1;
		if (safeLocations.contains(loc)) {
			weight = weight * 1.5;
		}
		RobotInfo[] potentialTowers = rc.senseNearbyRobots();
		for (int i = 0; i > potentialTowers.length; i++) {
			if (potentialTowers[i].type == RobotType.TOWER) {
				weight = weight * .8;
			}
		}
		return weight;
	}
	
}
