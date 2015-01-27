package launcherStratPlusSoldiers;


import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Drone extends BaseBot {
	
    public Drone(RobotController rc) throws GameActionException {
        super(rc);
    }
    
    public void execute() throws GameActionException{
    	if(rc.getSupplyLevel() > 1000 && rc.readBroadcast(50) != 0){
    		int id = rc.readBroadcast(53);
    		MapLocation cur = rc.getLocation();
			RobotInfo[] allies = getAllies();
			for(RobotInfo a: allies){
				if(a.ID == id){
					if(a.location.distanceSquaredTo(cur) <= GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED){
						int maxToGive = 2500;
	                	if(a.type == RobotType.LAUNCHER){
	                		maxToGive = 10000;
	                	}
	                	int transferAmount = (int) Math.min(rc.getSupplyLevel() - 1000, maxToGive);
	                	rc.transferSupplies(transferAmount, a.location);
	                	rc.broadcast(50, 0);
					}
					else{
						if(rc.isCoreReady()) tryMove(cur.directionTo(a.location));
					}
					break;
				}
			}
    		
    	}
//    	else if (rc.getSupplyLevel() <= 1000) {
    	if(rc.isCoreReady()){
            tryMove(rc.getLocation().directionTo(myHQ));
        }
        
		rc.yield();
    }
    
    public void executeOld() throws GameActionException {
//    	System.out.println(rc.readBroadcast(96) + " " + rc.readBroadcast(97));
    	if(rc.getSupplyLevel() > 1000){
    		int queueStart = rc.readBroadcast(96), queueEnd = rc.readBroadcast(97);
    		int queueStart2 = rc.readBroadcast(98), queueEnd2 = rc.readBroadcast(99);
        	if (queueStart != queueEnd) {
            	MapLocation cur = rc.getLocation();
                int targetID = rc.readBroadcast(queueStart);
//                System.out.println(targetID);
                MapLocation targetLoc = new MapLocation(rc.readBroadcast(queueStart + 1), rc.readBroadcast(queueStart + 2));
                if(rc.isCoreReady()){
    	            if(cur.distanceSquaredTo(targetLoc) > GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED){
    	            	System.out.println("e");
    	            	tryMove(cur.directionTo(targetLoc));
    	            }
                }
                else{
                	checkLauncherQueue(targetID, queueStart);
                }

                rc.setIndicatorString(0, targetID + "  " + cur.distanceSquaredTo(targetLoc) + " " + targetLoc.x + " " + targetLoc.y);
        	}
        	else if(queueStart2 != queueEnd2){
        		MapLocation cur = rc.getLocation();
                int targetID = rc.readBroadcast(queueStart2);
                rc.setIndicatorString(0, targetID + "");
                MapLocation targetLoc = new MapLocation(rc.readBroadcast(queueStart2 + 1), rc.readBroadcast(queueStart2 + 2));
                if(rc.isCoreReady()){
    	            if(cur.distanceSquaredTo(targetLoc) > GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED){
    	            	tryMove(cur.directionTo(targetLoc));
    	            }
                }
                else{
                	checkMinerQueue(targetID, queueStart2);
                }
                rc.setIndicatorString(0, targetID + "  " + cur.distanceSquaredTo(targetLoc) + " " + targetLoc.x + " " + targetLoc.y);
            	
        	}
    	}
    	
        if (rc.isCoreReady()) {
            if (rc.getSupplyLevel() <= 1000) {
                tryMove(rc.getLocation().directionTo(myHQ));
            }
        }
        
		rc.yield();
	}
    
    public boolean checkLauncherQueue(int targetID, int queueStart) throws GameActionException{
    	RobotInfo[] friends = rc.senseNearbyRobots(rc.getLocation(), GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, myTeam);
        for(int i = 0; i < friends.length; i++){
        	if(friends[i].type != RobotType.MINER || friends[i].type != RobotType.LAUNCHER){
        		continue;
        	}
        	if(targetID == friends[i].ID){
        		if((friends[i].supplyLevel <= 250 && friends[i].type == RobotType.MINER) || 
        				(friends[i].supplyLevel <= 500 && friends[i].type == RobotType.LAUNCHER)){
            		int maxToGive = 2500;
                	if(friends[i].type == RobotType.LAUNCHER){
                		maxToGive = 5000;
                	}
                	int transferAmount = (int) Math.min(rc.getSupplyLevel() - 1000, maxToGive);
                	rc.transferSupplies(transferAmount, friends[i].location);
                	queueStart += 3;
            		if (queueStart >= 1997) {
            			queueStart = 1000;
            		}
                	rc.broadcast(96, queueStart);
                	return true;
        		}
        		queueStart += 3;
        		if (queueStart >= 1997) {
        			queueStart = 1000;
        		}
            	rc.broadcast(96, queueStart);
        	}
//        	else{
//        		if((friends[i].supplyLevel <= 250 && friends[i].type == RobotType.MINER) || 
//        				(friends[i].supplyLevel <= 500 && friends[i].type == RobotType.LAUNCHER)){
//        			int maxToGive = 2500;
//                	if(friends[i].type == RobotType.LAUNCHER){
//                		maxToGive = 5000;
//                	}
//                	int transferAmount = (int) Math.min(rc.getSupplyLevel() - 1000, maxToGive);
//                	rc.transferSupplies(transferAmount, friends[i].location);
//                	break;
//        		}
//        	}
        }
    	return false;
    }
    
    public boolean checkMinerQueue(int targetID, int queueStart) throws GameActionException{
    	
            RobotInfo[] friends = rc.senseNearbyRobots(rc.getLocation(), GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, myTeam);
            for(int i = 0; i < friends.length; i++){
            	if(friends[i].type != RobotType.MINER || friends[i].type != RobotType.LAUNCHER){
            		continue;
            	}
            	if(targetID == friends[i].ID){
            		if((friends[i].supplyLevel <= 250 && friends[i].type == RobotType.MINER) || 
            				(friends[i].supplyLevel <= 500 && friends[i].type == RobotType.LAUNCHER)){
	            		int maxToGive = 2500;
	                	if(friends[i].type == RobotType.LAUNCHER){
	                		maxToGive = 5000;
	                	}
	                	int transferAmount = (int) Math.min(rc.getSupplyLevel() - 1000, maxToGive);
	                	rc.transferSupplies(transferAmount, friends[i].location);
	                	queueStart += 3;
	            		if (queueStart >= 1000) {
	            			queueStart = 100;
	            		}
	                	rc.broadcast(98, queueStart);
	                	return true;
            		}
            		queueStart += 3;
            		if (queueStart >= 1000) {
            			queueStart = 100;
            		}
                	rc.broadcast(98, queueStart);
            	}
//            	else{
//            		if((friends[i].supplyLevel <= 250 && friends[i].type == RobotType.MINER) || 
//            				(friends[i].supplyLevel <= 500 && friends[i].type == RobotType.LAUNCHER)){
//            			int maxToGive = 2500;
//                    	if(friends[i].type == RobotType.LAUNCHER){
//                    		maxToGive = 5000;
//                    	}
//                    	int transferAmount = (int) Math.min(rc.getSupplyLevel() - 1000, maxToGive);
//                    	rc.transferSupplies(transferAmount, friends[i].location);
//                    	break;
//            		}
//            	}
            }
            
    	return false;
    }
    
    public void tryMove(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < offsets.length && 
				(!rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8]) || 
						!isSafe(rc.getLocation().add(directions[(dirint+offsets[offsetIndex]+8)%8])))) {
			offsetIndex++;
		}
		if (offsetIndex < offsets.length) {
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
    	int HQRadiusAdd = 0;
    	if(enemyTowers.length >= 2){
    		HQRadiusAdd = 11;
    	}
    	if(ml.distanceSquaredTo(theirHQ) <= RobotType.HQ.attackRadiusSquared + HQRadiusAdd){
    		return false;
    	}
    	RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), 24, theirTeam);
    	for(RobotInfo enemy:enemies){
    		if(ml.distanceSquaredTo(enemy.location) <= enemy.type.attackRadiusSquared + 10){
    			return false;
    		}
    		else if(ml.distanceSquaredTo(enemy.location) <= 16 && enemy.type == RobotType.MISSILE){
    			return false;
    		}
    	}
    	return true;
    }
    
}