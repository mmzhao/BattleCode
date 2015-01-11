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
