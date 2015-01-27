package launcherStratPlusSoldiers;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

//NEEDS DECISION MAKING ON WHEN TO SKIRMISH
public class Soldier extends BaseBot{

	private int towerToProtect;
	private boolean attackTarget;
	MapLocation targetLoc;
	
    public Soldier(RobotController rc) throws GameActionException {
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
        
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(rc.getLocation(), 25, theirTeam);
        targetLoc = getTarget(nearbyEnemies);
        
        if (rc.isCoreReady()) {
        	if(targetLoc != null){
        		tryMove(rc.getLocation().directionTo(targetLoc));
        	}
//			micro();
			if(rc.isCoreReady()){
    			attackTarget = false;
	            int rallyX = rc.readBroadcast(1001);
	            int rallyY = rc.readBroadcast(1002);
	            MapLocation target = new MapLocation(rallyX, rallyY);
//	            rc.setIndicatorString(0, rallyX + " " + rallyY);
//	            rc.setIndicatorString(0, rallyX + " " + rallyY);
	            tryMove(rc.getLocation().directionTo(target));
			}
        }
        
        transferSupplies();
        
        rc.yield();
    }
    
    public MapLocation getTarget(RobotInfo[] nearby) throws GameActionException{
		MapLocation target = null;
		MapLocation cur = rc.getLocation();
		int minValue = Integer.MAX_VALUE;
    	for(RobotInfo ri: nearby){
    		int value = cur.distanceSquaredTo(ri.location) - getValue(ri);
    		if(value < minValue){
    			minValue = value;
    			target = ri.location;
    		}
    	}
    	return target;
    	
    }
    
    public int getValue(RobotInfo ri) throws GameActionException {

    	int currValue = -1;
    	if(ri.type == RobotType.TOWER || ri.type == RobotType.HQ){
    		currValue = 0;
    	}
    	else if(getBuilding(ri.type) != 0){
    		currValue = 1;
    	}
    	else{
        	if(ri.type == RobotType.BEAVER){
        		currValue = 2;
        	}
        	else if(ri.type == RobotType.MINER){
        		currValue = 6;
        	}
        	else if(ri.type == RobotType.SOLDIER){
        		currValue = 2;
        	}
        	else if(ri.type == RobotType.BASHER){
        		currValue = 3;
        	}
        	else if(ri.type == RobotType.DRONE){
        		currValue = 3;
        	}
        	else if(ri.type == RobotType.TANK){
        		currValue = 1;
        	}
        	else if(ri.type == RobotType.LAUNCHER){
        		currValue = 8;
        	}
        	else if(ri.type == RobotType.COMMANDER){
        		currValue = 7;
        	}
        	else if(ri.type == RobotType.COMPUTER){
        		currValue = 1;
        	}
        	else if(ri.type == RobotType.MISSILE){
        		currValue = rc.readBroadcast(getUnit(RobotType.SOLDIER)) / 2;
        	}
        	if(ri.type != RobotType.MISSILE){
        		if(ri.health < rc.getType().attackPower){
        			currValue += 10;
        		}
        	}
        	else{
        		if(ri.health == 1){
        			currValue += 10;
        		}
        	}
        }
        return currValue;
    }
	
    public void attackForValue(RobotInfo[] enemies) throws GameActionException {
        if (enemies.length == 0) {
            return;
        }
        
        int maxValue = -1;
        MapLocation toAttack = null;
        for (RobotInfo ri : enemies) {
        	int currValue = -1;
        	if(ri.type == RobotType.TOWER || ri.type == RobotType.HQ){
        		currValue = 0;
        	}
        	else if(getBuilding(ri.type) != 0){
        		currValue = 1;
        	}
        	else{
	        	if(ri.type == RobotType.BEAVER){
	        		currValue = 2;
	        	}
	        	else if(ri.type == RobotType.MINER){
	        		currValue = 6;
	        	}
	        	else if(ri.type == RobotType.SOLDIER){
	        		currValue = 2;
	        	}
	        	else if(ri.type == RobotType.BASHER){
	        		currValue = 3;
	        	}
	        	else if(ri.type == RobotType.DRONE){
	        		currValue = 3;
	        	}
	        	else if(ri.type == RobotType.TANK){
	        		currValue = 1;
	        	}
	        	else if(ri.type == RobotType.LAUNCHER){
	        		currValue = 8;
	        	}
	        	else if(ri.type == RobotType.COMMANDER){
	        		currValue = 7;
	        	}
	        	else if(ri.type == RobotType.COMPUTER){
	        		currValue = 1;
	        	}
	        	else if(ri.type == RobotType.MISSILE){
	        		currValue = rc.readBroadcast(getUnit(RobotType.SOLDIER)) / 2;
	        	}
	        	if(ri.type != RobotType.MISSILE){
	        		if(ri.health < rc.getType().attackPower){
	        			currValue += 10;
	        		}
	        	}
	        	else{
	        		if(ri.health == 1){
	        			currValue += 10;
	        		}
	        	}
        	}
	        if(currValue > maxValue){
	        	maxValue = currValue;
	        	toAttack = ri.location;
	        }
        }
        rc.attackLocation(toAttack);
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
    		if(rc.readBroadcast(3000) == 0 || targetLoc == null || targetLoc.x != theirHQ.x || targetLoc.y != theirHQ.y){
        		return false;
    		}
    	}
    	if(rc.readBroadcast(4000) == 1 || rc.readBroadcast(3000) == 1) return true;
    	RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), 36, theirTeam);
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
