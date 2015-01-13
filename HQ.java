package testing;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
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
//6 -- number of solders defending base
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
    	
    	int numDefenders = 0;
    	RobotInfo[] ri2 = rc.senseNearbyRobots(10, rc.getTeam());
    	for(int i = 0; i < ri.length; i++){
    		if(ri[i].type == RobotType.SOLDIER){
    			numDefenders++;
    		}
    	}
    	rc.broadcast(6, numDefenders);
    	
        rc.setIndicatorString(0, numBeavers + "");
//        rc.setIndicatorString(1, numSoldiers + "");
        
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
        		rc.broadcast(5, 5);
        	}
        }
        MapLocation rallyPoint = new MapLocation(rc.readBroadcast(0), rc.readBroadcast(1));

//    	MapLocation[] ourTowers = rc.senseTowerLocations();
        if (Clock.getRoundNum() < 1500) {
//            rallyPoint = ourTowers[0];
        	MapLocation initialRally = rc.getLocation().add(rc.getLocation().directionTo(theirHQ), 7);
        	rallyPoint = initialRally;
        }
        else{
//        	int byRallyPoint = rc.senseNearbyRobots(rallyPoint, 50, rc.getTeam()).length;
//        	rc.setIndicatorString(1, byRallyPoint + "");
//        	if(byRallyPoint > 50){
	        	MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
	        	MapLocation currRally = new MapLocation(rc.readBroadcast(0), rc.readBroadcast(1));
	        	if (enemyTowers.length > 0) {
	        		rallyPoint = closestLocation(currRally, enemyTowers);
	        		if(currRally.distanceSquaredTo(rallyPoint) > theirHQ.distanceSquaredTo(currRally)){
	            		rallyPoint = theirHQ;
	            	}
	        	}
	        	else{
	        		rallyPoint = theirHQ;
	        	}
//	        }
        }
        rc.broadcast(0, rallyPoint.x);
        rc.broadcast(1, rallyPoint.y);

        transferSupplies();
        
        
        
        
        rc.yield();
    }
    
//    public void transferSupplies() throws GameActionException{
//    	rc.setIndicatorString(0, GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED + "");
//    	int HQDistance = rc.getLocation().distanceSquaredTo(theirHQ);
//    	RobotInfo[] transferable = rc.senseNearbyRobots(rc.getLocation(), GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, rc.getTeam());
//    	for(RobotInfo ri:transferable){
//    		if(ri.location.distanceSquaredTo(theirHQ) > HQDistance){
//    			if(ri.supplyLevel < ri.type.supplyUpkeep){
//    				rc.transferSupplies((int)(2 * ri.type.supplyUpkeep - ri.supplyLevel + 1), ri.location);
//    			}
//    		}
//    		else if(ri.supplyLevel < rc.getSupplyLevel()){
//    			rc.transferSupplies((int)((rc.getSupplyLevel() - ri.supplyLevel)/2), ri.location);
//    		}
//    	}
//    }
    
    
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