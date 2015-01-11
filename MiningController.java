package testing;

import battlecode.common.*;

public class MiningController {
	public RobotController rc;
	public FastLocSet visitedLocs;
	public MapLocation[] safeLocations;
	
	public MiningController(RobotController rc) {
		this.rc = rc;
		MapLocation[] towerArray = rc.senseTowerLocations();
		int length = towerArray.length;
		safeLocations = new MapLocation[length];
		safeLocations[0] = rc.senseHQLocation();
		for (int i = 1; i <= length; i++) {
			safeLocations[i] = towerArray[i-1];
		}
	}
	
	public MapLocation retreat() {
		MapLocation cur = rc.getLocation();
		int length = safeLocations.length;
		if (length==1) {
			return safeLocations[0];
		}
		int distance = Integer.MAX_VALUE;
		MapLocation result = null;
		MapLocation check;
		for (int i = length-1; --i >= 0;) {
			check = safeLocations[i];
			int checkDist = cur.distanceSquaredTo(check);
			if (checkDist < distance) {
				result = check;
				distance = checkDist;
			}
		}
		return result;
	}
	
	public MapLocation findMiningLocation() {
		MapLocation cur = rc.getLocation();
		int x = cur.x, y = cur.y; 
		MapLocation[] testLocationIndices = {new MapLocation(x, y+1), new MapLocation(x+1, y+1), 
				new MapLocation(x+1, y), new MapLocation(x+1, y-1), new MapLocation(x, y-1), new MapLocation(x-1, y-1),
				new MapLocation(x-1, y), new MapLocation(x-1, y+1), new MapLocation(x, y+2), new MapLocation(x+1, y+2),
				new MapLocation(x+2, y+2), new MapLocation(x+2, y+1), new MapLocation(x+2, y), new MapLocation(x+2, y-1),
				new MapLocation(x+2, y-2), new MapLocation(x+1, y-2), new MapLocation(x, y-2), new MapLocation(x-1, y-2),
				new MapLocation(x-2, y-2), new MapLocation(x-2, y-1), new MapLocation(x-2, y), new MapLocation(x-2, y+1),
				new MapLocation(x-2, y+2), new MapLocation(x-1, y+2), new MapLocation(x, y+3), new MapLocation(x+1, y+3),
				new MapLocation(x+2, y+3), new MapLocation(x+3, y+3), new MapLocation(x+3, y+2), new MapLocation(x+3, y+1),
				new MapLocation(x+3, y), new MapLocation(x+3, y-1), new MapLocation(x+3, y-2), new MapLocation(x+3, y-3), 
				new MapLocation(x+2, y-3), new MapLocation(x+1, y-3), new MapLocation(x, y-3), new MapLocation(x-1, y-3), 
				new MapLocation(x-2, y-3), new MapLocation(x-3, y-3), new MapLocation(x-3, y-2), new MapLocation(x-3, y-1), 
				new MapLocation(x-3, y), new MapLocation(x-3, y+1), new MapLocation(x-3, y+2), new MapLocation(x-3, y+3),
				new MapLocation(x-2, y+3), new MapLocation(x-1, y+3), new MapLocation(x, y+4), new MapLocation(x+1, y+4), 
				new MapLocation(x+2, y+4), new MapLocation(x+4, y+2), new MapLocation(x+4, y+1), new MapLocation(x+4, y),
				new MapLocation(x+4, y-1), new MapLocation(x+4, y-2), new MapLocation(x+2, y-4), new MapLocation(x+1, y-4), 
				new MapLocation(x, y-4), new MapLocation(x-1, y-4), new MapLocation(x-2, y-4), new MapLocation(x-4, y-2), 
				new MapLocation(x-4, y-1), new MapLocation(x-4, y), new MapLocation(x-4, y+1), new MapLocation(x-4, y+2),
				new MapLocation(x-2, y+4), new MapLocation(x-1, y+4)}; 
    	MapLocation check;
    	double maxOre = 0;
    	MapLocation result = null;
    	int length = testLocationIndices.length;
    	for (int i = 0; i<length; i++) {
    		check = testLocationIndices[i];
    		if (!visitedLocs.contains(check) && rc.senseOre(check) * getLocationWeighting(check) > maxOre) {
    			result = check;
    			maxOre = rc.senseOre(check);
    		}
    	}
    	visitedLocs.add(result);
    	return result;
    }
	
	public double getLocationWeighting(MapLocation loc) {
		double weight = 1;

		MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
		int length = enemyTowers.length;
		for (int i = 0; i < length; i++) { //iterate through enemy towers
			if (enemyTowers[i].distanceSquaredTo(loc) <= 24) { //if tower is in attacking range
				weight = 0;
			}
			else if (enemyTowers[i].distanceSquaredTo(loc) <= 35) { //if tower is in sensing range
				weight = weight * .5;
			}
		}
		length = safeLocations.length;
		for (int i = 0; i < length; i++) { //iterate through our towers
			if (safeLocations[i].distanceSquaredTo(loc) <= 24) { //if tower is in defending range
				weight = weight * 1.25;
			}
		}
		return weight;
	}
	
	public boolean goodMiningLocation(MapLocation curLoc, Direction curDir) { //checks 5 directions around target if they're good mining locations
		Direction startDir = curDir.rotateRight().rotateRight();
		for (int i = 4; --i>=0;) {
			if (rc.senseOre(curLoc.add(curDir))<30) {
				return false;
			}
			curDir.rotateLeft();
		}
		return true;
	}
	
}
