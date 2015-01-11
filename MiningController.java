package testing;

import battlecode.common.*;

public class MiningController {
	public RobotController rc;
	public FastIterableLocSet safeLocations;
	public FastLocSet visitedLocs;
	
	public MiningController(RobotController rc) {
		this.rc = rc;
		safeLocations.add(rc.senseHQLocation());
		for (MapLocation tower:rc.senseTowerLocations()) {
			safeLocations.add(tower);
		}
	}
	
	public MapLocation retreat() {
		MapLocation cur = rc.getLocation();
		MapLocation[] locationsToCheck = safeLocations.getKeys();
		int length = locationsToCheck.length;
		if (length==1) {
			return locationsToCheck[0];
		}
		int distance = Integer.MAX_VALUE;
		MapLocation result = null;
		MapLocation check;
		for (int i = length-1; --i >= 0;) {
			check = locationsToCheck[i];
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
		MapLocation[] ourTowers = rc.senseTowerLocations();
		for (int i = 0; i < enemyTowers.length; i++) { //iterate through enemy towers
			if (enemyTowers[i].distanceSquaredTo(loc) <= 24) { //if tower is in attacking range
				weight = weight * .8;
			}
			else if (enemyTowers[i].distanceSquaredTo(loc) <= 35) { //if tower is in sensing range
				weight = weight * .9;
			}
		}
		
		for (int i = 0; i < ourTowers.length; i++) { //iterate through our towers
			if (ourTowers[i].distanceSquaredTo(loc) <= 24) { //if tower is in defending range
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
