package testing;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

//BROADCASTING INDEX MEANINGS
//0 -- rally x position
//1 -- rally y position
//2 -- number of beavers
//3 -- number of soldiers
//5 -- number of barracks to build
//10 -- queue of crap


public class HQ extends BaseBot {
	
    public HQ(RobotController rc) {
        super(rc);
        this.myHQ = rc.senseHQLocation();
        this.theirHQ = rc.senseEnemyHQLocation();
    }

    public void execute() throws GameActionException {
    	RobotInfo[] ri = rc.senseNearbyRobots(1000, rc.getTeam());
    	int numBeavers = 0;
    	int numSoldiers = 0;
    	int numBarracks = 0;
    	for(int i = 0; i < ri.length; i++){
    		if(ri[i].type == RobotType.BEAVER){
    			numBeavers++;
    		}
    		if(ri[i].type == RobotType.SOLDIER){
    			numSoldiers++;
    		}
    		if(ri[i].type == RobotType.BARRACKS){
    			numBarracks++;
    		}
    	}
    	
        rc.setIndicatorString(0, numBeavers + "");
        rc.setIndicatorString(1, numSoldiers + "");
        
        if(rc.isWeaponReady()){
        	attackLeastHealthEnemy(getEnemiesInAttackingRange());
        }

        if (rc.isCoreReady()){
        	if(numBeavers < 15) {
        		if(rc.getTeamOre() > 100){
		            Direction newDir = getSpawnDirection(RobotType.BEAVER);
		            if (newDir != null) {
		                rc.spawn(newDir, RobotType.BEAVER);
		                rc.broadcast(2, numBeavers + 1);
		                rc.broadcast(10, 1);
		            }
        		}
        	}
        	else if(rc.getTeamOre() > RobotType.BARRACKS.oreCost){
        		rc.broadcast(5, 3);
        	}
        }
        MapLocation rallyPoint = null;
        if (Clock.getRoundNum() < 1000 || numSoldiers < 50) {
        	MapLocation[] ourTowers = rc.senseTowerLocations();
            rallyPoint = ourTowers[0];
        } 
        else {
        	MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
        	MapLocation currRally = new MapLocation(rc.readBroadcast(0), rc.readBroadcast(1));
        	if (enemyTowers.length>0) {
        		rallyPoint = closestLocation(currRally, enemyTowers);
        		if(currRally.distanceSquaredTo(rallyPoint) > theirHQ.distanceSquaredTo(currRally)){
            		rallyPoint = theirHQ;
            	}
        	}
        	else{
        		rallyPoint = theirHQ;
        	}
        	
        }
        rc.broadcast(0, rallyPoint.x);
        rc.broadcast(1, rallyPoint.y);

        
        rc.yield();
    }
    
    public MapLocation closestLocation(MapLocation currRally, MapLocation[] ml) throws GameActionException{
    	int minDist = currRally.distanceSquaredTo(ml[0]);
    	int minIndex = 0;
    	for(int i = 1; i < ml.length; i++){
    		int currDist = currRally.distanceSquaredTo(ml[i]);
    		if(currDist < minDist){
    			minDist = currDist;
    			minIndex = i;
    		}
    	}
    	return ml[minIndex];
    }
    
}