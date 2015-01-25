package launcherStrat;

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
//        if(rc.getID() % 2 == 0){
//        	offsets[1] *= -1;
//			offsets[2] *= -1;
//			offsets[3] *= -1;
//        }
    }

    public void execute() throws GameActionException {
    	rc.setIndicatorString(1, towerToProtect + "");
        RobotInfo[] enemies = getEnemiesInAttackingRange(rc.getType());
        
        
        
        //micro
        if(rc.isCoreReady()){
        	micro();
        }
        
        if (enemies.length > 0) {
            //attack!
            if (rc.isWeaponReady()) {
                attackForValue(enemies);
            }
        }
        
        if (rc.isCoreReady()) {
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
        
        transferSupplies();
        
        rc.yield();
    }
    
    public void tryMove(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < offsets.length &&
				(!rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8]) || 
						!isSafe(rc.getLocation().add(directions[(dirint+offsets[offsetIndex]+8)%8])))) {
			offsetIndex++;
		}
		if (offsetIndex < offsets.length) {
			previous = rc.getLocation();
			rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
		}
		else{
			offsets[1] *= -1;
			offsets[2] *= -1;
			offsets[3] *= -1;
		}
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
    	if(rc.readBroadcast(4000) == 1 || rc.readBroadcast(3000) == 1) return true;
    	RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), 36, theirTeam);
    	for(RobotInfo enemy:enemies){
    		if(ml.distanceSquaredTo(enemy.location) <= enemy.type.attackRadiusSquared){
    			return false;
    		}
    		else if(enemy.type == RobotType.LAUNCHER && ml.distanceSquaredTo(enemy.location) <= 16){
    			return false;
    		}
    		else if(enemy.type == RobotType.MISSILE && ml.distanceSquaredTo(enemy.location) <= 4){
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
