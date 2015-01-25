package launcherStrat;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Drone extends BaseBot {
	int squadNum;
	int targetID;
	
    public Drone(RobotController rc) throws GameActionException {
        super(rc);
        squadNum = -1;
        targetID = 0;
    }
    
    public void execute() throws GameActionException {
    	rc.setIndicatorString(1, rc.getType().supplyUpkeep + "");
        RobotInfo[] enemies = getEnemiesInAttackingRange(rc.getType());

        if (enemies.length > 0) {
            //attack!
            if (rc.isWeaponReady()) {
            	
                attackLeastHealthEnemy(enemies);
            }
        }
        else if (rc.isCoreReady()) {
        	
        	checkSquads();
        	
        	if(squadNum != -1){
        		int rallyX = rc.readBroadcast(101 + squadNum * 10);
                int rallyY = rc.readBroadcast(102 + squadNum * 10);
                MapLocation rallyPoint = new MapLocation(rallyX, rallyY);
                tryMove(rc.getLocation().directionTo(rallyPoint));
        	}
        	else{
	            int rallyX = rc.readBroadcast(1001);
	            int rallyY = rc.readBroadcast(1002);
	            MapLocation rallyPoint = new MapLocation(rallyX, rallyY);
	            rc.setIndicatorString(0, rallyX + " " + rallyY);
	            tryMove(rc.getLocation().directionTo(rallyPoint));
        	}
        }
        
        transferSupplies();
        
        rc.yield();
    }
    
    public void checkSquads() throws GameActionException{
    	squadNum = -1;
    	int numSquads = rc.readBroadcast(100);
    	int closest = -1;
    	int minDist = Integer.MAX_VALUE;
    	MapLocation myLoc = rc.getLocation();
    	for(int i = 0; i < numSquads; i++){
    		if(rc.readBroadcast(104 + i * 10) == targetID){
    			squadNum = i;
        		rc.broadcast(103 + 10 * squadNum, rc.readBroadcast(103 + 10 * squadNum) + 1);
    			return;
    		}
    	}
    	for(int i = 0; i < numSquads; i++){
    		int squadSize = rc.readBroadcast(103 + i * 10);
    		if(squadSize < 2 + (int)(Clock.getRoundNum()/1000)){
    			MapLocation ml = new MapLocation(rc.readBroadcast(101 + i * 10), rc.readBroadcast(102 + i * 10));
    			int currDist = ml.distanceSquaredTo(myLoc);
//    			System.out.print(ml.x + " " + myLoc.x + " " + ml.y + " " + myLoc.y + "        ");
    			if(currDist < minDist){
    				minDist = currDist;
    				closest = i;
    			}
    		}
    	}
    	if(minDist <= 50 || (minDist < 100 && rc.readBroadcast(103 + 10 * squadNum) == 0)){
			//join squad
    		squadNum = closest;
    		rc.broadcast(103 + 10 * squadNum, rc.readBroadcast(103 + 10 * squadNum) + 1);
//    		System.out.println(squadNum);
		}
    	
    }
    
    public void tryMove(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 5 && 
				(!rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8]) || 
						!isSafe(rc.getLocation().add(directions[(dirint+offsets[offsetIndex]+8)%8])))) {
			offsetIndex++;
		}
		if (offsetIndex < 5) {
			rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
		}
	}
    
    public boolean isSafe(MapLocation ml) throws GameActionException{
    	MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
    	for(MapLocation m: enemyTowers){
    		if(ml.distanceSquaredTo(m) <= RobotType.TOWER.attackRadiusSquared){
    			return false;
    		}
    	}
    	if(ml.distanceSquaredTo(theirHQ) <= RobotType.HQ.attackRadiusSquared){
    		return false;
    	}
    	if(squadNum != -1 && rc.readBroadcast(103 + squadNum * 10) > 1) return true;
    	RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), 20, theirTeam);
    	for(RobotInfo enemy:enemies){
    		if(ml.distanceSquaredTo(enemy.location) <= enemy.type.attackRadiusSquared){
    			return false;
    		}
    	}
    	return true;
    }
    
}