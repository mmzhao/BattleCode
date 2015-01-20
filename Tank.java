package testing;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Tank extends BaseBot{

	private int towerToProtect;
	private boolean attackTarget;
	MapLocation targetLoc;
	
    public Tank(RobotController rc) throws GameActionException {
        super(rc);
        towerToProtect = -1;
        attackTarget = false;
        targetLoc = null;
    }

    public void execute() throws GameActionException {
    	rc.setIndicatorString(1, towerToProtect + "");
        RobotInfo[] enemies = getEnemiesInAttackingRange(rc.getType());
        
        if (enemies.length > 0) {
            //attack!
            if (rc.isWeaponReady()) {
                attackForValue(enemies);
            }
        }
        
        //micro
        if(rc.isCoreReady()){
        	micro();
        }
        
        if (rc.isCoreReady()) {
    		if(rc.readBroadcast(2000) == 1){
    			micro();
    			if(rc.isCoreReady()){
	    			attackTarget = true;
	    			int rallyX = rc.readBroadcast(2001);
		            int rallyY = rc.readBroadcast(2002);
		            targetLoc = new MapLocation(rallyX, rallyY);
		            tryMove(rc.getLocation().directionTo(targetLoc));
    			}
    		}
    		else{
    			micro();
    			if(rc.isCoreReady()){
	    			attackTarget = false;
		            int rallyX = rc.readBroadcast(1001);
		            int rallyY = rc.readBroadcast(1002);
		            targetLoc = new MapLocation(rallyX, rallyY);
	//	            rc.setIndicatorString(0, rallyX + " " + rallyY);
	//	            rc.setIndicatorString(0, rallyX + " " + rallyY);
		            tryMove(rc.getLocation().directionTo(targetLoc));
    			}
    		}
        }
        
        transferSupplies();
        
        rc.yield();
    }
    
    public void micro() throws GameActionException{
    	RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), 24, theirTeam);
    	if(enemies.length == 0) return;
    	boolean noAttackersInRange = true;
    	for(RobotInfo e: enemies){
    		if(e.type == RobotType.TOWER || e.type == RobotType.HQ) continue;
    		if(e.type.attackRadiusSquared >= e.location.distanceSquaredTo(rc.getLocation())){
    			noAttackersInRange = false;
    		}
    	}
    	if(noAttackersInRange){ 
    		return;
    	}
    	int totalX = 0;
    	int totalY = 0;
    	for(RobotInfo e: enemies){
    		totalX += e.location.x;
    		totalY += e.location.y;
    	}
    	MapLocation enemyCenter = new MapLocation(totalX/enemies.length, totalY/enemies.length);
    	tryMove(enemyCenter.directionTo(rc.getLocation()));
    	
    }
    
    
	
    public boolean isSafe(MapLocation ml) throws GameActionException{
    	MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
    	for(MapLocation m: enemyTowers){
    		if(targetLoc != null && targetLoc.x == m.x && targetLoc.y == m.y){
    			continue;
    		}
    		if(ml.distanceSquaredTo(m) <= RobotType.TOWER.attackRadiusSquared){
    			return false;
    		}
    	}
    	int HQRadiusAdd = 0;
    	if(enemyTowers.length >= 2){
    		HQRadiusAdd = 11;
    	}
    	if(ml.distanceSquaredTo(theirHQ) <= RobotType.HQ.attackRadiusSquared + HQRadiusAdd){
    		if(targetLoc == null || targetLoc.x != theirHQ.x || targetLoc.y != theirHQ.y){
        		return false;
    		}
    	}
    	if(attackTarget) return true;
    	if(rc.readBroadcast(4000) == 1 || rc.readBroadcast(3000) == 1) return true;
    	RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), 24, theirTeam);
    	for(RobotInfo enemy:enemies){
    		if(ml.distanceSquaredTo(enemy.location) <= enemy.type.attackRadiusSquared){
    			return false;
    		}
    	}
    	return true;
    }
    
}

//TOWER PROTECTION CODE
//towerToProtect = -1;
//int closestDist = Integer.MAX_VALUE;
//for(int i = 0; i < rc.readBroadcast(20); i++){
//	int numProtect = rc.readBroadcast(23 + 10 * i);
//	if(numProtect < 2){
//		towerToProtect = i;
//		break;
//	}
//	if(numProtect < 3){
//		MapLocation ml = new MapLocation(rc.readBroadcast(21 + 10 * i), rc.readBroadcast(22 + 10 * i));
//		int dist = rc.getLocation().distanceSquaredTo(ml);
//		if(dist < closestDist){
//			closestDist = dist;
//			towerToProtect = i;
//		}
//	}
//}
//if(towerToProtect != -1){
//	rc.broadcast(23 + 10 * towerToProtect, rc.readBroadcast(23 + 10 * towerToProtect) + 1);
//}
