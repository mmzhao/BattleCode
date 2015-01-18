package testing;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Tank extends BaseBot{

	private Mover move;
	private MapLocation targetLoc;
	private boolean defender;
	
    public Tank(RobotController rc) throws GameActionException {
        super(rc);
        move = new Mover(rc);
        targetLoc = null;
        defender = false;
//        if(rc.readBroadcast(1000) < 10){
//        	defender = true;
//        	rc.broadcast(1000, rc.readBroadcast(6) + 1);
//        }
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
        if (rc.isCoreReady()) {
        	if(defender){
        		if(rc.getLocation().distanceSquaredTo(myHQ) > 10){
        			tryMove(rc.getLocation().directionTo(myHQ));
        		}
        		else{
        			tryMove(directions[rand.nextInt(8)]);
        		}
        	}
        	else{
	            int rallyX = rc.readBroadcast(1001);
	            int rallyY = rc.readBroadcast(1002);
	            MapLocation rallyPoint = new MapLocation(rallyX, rallyY);
//	            rc.setIndicatorString(0, rallyX + " " + rallyY);
//	            rc.setIndicatorString(0, rallyX + " " + rallyY);
	            tryMove(rc.getLocation().directionTo(rallyPoint));
        	}
        }
        
        transferSupplies();
        
        rc.yield();
    }
	
    public boolean isSafe(MapLocation ml) throws GameActionException{
    	if(Clock.getRoundNum() > 1500) return true;
    	MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
    	for(MapLocation m: enemyTowers){
    		if(ml.distanceSquaredTo(m) <= RobotType.TOWER.attackRadiusSquared){
    			return false;
    		}
    	}
    	if(ml.distanceSquaredTo(theirHQ) <= RobotType.HQ.attackRadiusSquared){
    		return false;
    	}
    	RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), 20, theirTeam);
    	for(RobotInfo enemy:enemies){
    		if(ml.distanceSquaredTo(enemy.location) <= enemy.type.attackRadiusSquared){
    			return false;
    		}
    	}
    	return true;
    }
    
}
