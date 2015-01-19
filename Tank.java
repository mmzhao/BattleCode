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
	
    public Tank(RobotController rc) throws GameActionException {
        super(rc);
        towerToProtect = -1;
        attackTarget = false;
    }

    public void execute() throws GameActionException {
    	rc.setIndicatorString(1, towerToProtect + "");
        RobotInfo[] enemies = getEnemiesInAttackingRange(rc.getType());

        if (enemies.length > 0) {
            //attack!
            if (rc.isWeaponReady()) {
                attackLeastHealthEnemy(enemies);
            }
        }
        
        towerToProtect = -1;
        int closestDist = Integer.MAX_VALUE;
        for(int i = 0; i < rc.readBroadcast(20); i++){
        	int numProtect = rc.readBroadcast(23 + 10 * i);
        	if(numProtect < 2){
        		towerToProtect = i;
        		break;
        	}
        	if(numProtect < 3){
        		MapLocation ml = new MapLocation(rc.readBroadcast(21 + 10 * i), rc.readBroadcast(22 + 10 * i));
        		int dist = rc.getLocation().distanceSquaredTo(ml);
        		if(dist < closestDist){
        			closestDist = dist;
        			towerToProtect = i;
        		}
        	}
        }
        if(towerToProtect != -1){
        	rc.broadcast(23 + 10 * towerToProtect, rc.readBroadcast(23 + 10 * towerToProtect) + 1);
        }
        
        if (rc.isCoreReady()) {
        	if(towerToProtect != -1){
        		MapLocation ml = new MapLocation(rc.readBroadcast(21 + 10 * towerToProtect), rc.readBroadcast(22 + 10 * towerToProtect));
        		tryMove(rc.getLocation().directionTo(ml));
        	}
        	else{
        		if(rc.readBroadcast(2000) == 1){
        			attackTarget = true;
        			int rallyX = rc.readBroadcast(2001);
    	            int rallyY = rc.readBroadcast(2002);
    	            MapLocation rallyPoint = new MapLocation(rallyX, rallyY);
    	            tryMove(rc.getLocation().directionTo(rallyPoint));
        		}
        		else{
        			attackTarget = false;
		            int rallyX = rc.readBroadcast(1001);
		            int rallyY = rc.readBroadcast(1002);
		            MapLocation rallyPoint = new MapLocation(rallyX, rallyY);
	//	            rc.setIndicatorString(0, rallyX + " " + rallyY);
	//	            rc.setIndicatorString(0, rallyX + " " + rallyY);
		            tryMove(rc.getLocation().directionTo(rallyPoint));
        		}
        	}
        }
        
        transferSupplies();
        
        rc.yield();
    }
	
    public boolean isSafe(MapLocation ml) throws GameActionException{
    	if(rc.readBroadcast(4000) == 1 || rc.readBroadcast(3000) == 1) return true;
    	MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
    	for(MapLocation m: enemyTowers){
    		if(ml.distanceSquaredTo(m) <= RobotType.TOWER.attackRadiusSquared){
    			return false;
    		}
    	}
    	if(ml.distanceSquaredTo(theirHQ) <= RobotType.HQ.attackRadiusSquared){
    		return false;
    	}
    	if(attackTarget) return true;
    	RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), 20, theirTeam);
    	for(RobotInfo enemy:enemies){
    		if(ml.distanceSquaredTo(enemy.location) <= enemy.type.attackRadiusSquared){
    			return false;
    		}
    	}
    	return true;
    }
    
}
