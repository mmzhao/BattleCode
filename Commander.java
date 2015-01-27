package launcherStratPlusSoldiers;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Commander extends BaseBot{

	MapLocation targetLoc;
	
    public Commander(RobotController rc) throws GameActionException {
        super(rc);
        targetLoc = theirHQ;
    }

    public void execute() throws GameActionException {
        
//    	if(rc.getLocation().distanceSquaredTo(theirHQ) > 100){
//    		if(rc.getFlashCooldown() == 0){
//    			
//    		}
//    	}
    	
    	RobotInfo[] enemiesInRange = getEnemiesInAttackingRange(rc.getType());
        
        if (enemiesInRange.length > 0) {
            //attack!
            if (rc.isWeaponReady()) {
                attackForValue(enemiesInRange);
            }
        }
        
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), 36, theirTeam);
        //micro
        if(enemies.length > 0){
        	micro(enemies);
        }
        
        
    	if(rc.getFlashCooldown() == 0){
    		
    	}
        
    	if(rc.isCoreReady()){
    		tryMove(rc.getLocation().directionTo(targetLoc));
    	}
    	
        transferSupplies();
        
        rc.yield();
    }
    
    public void micro(RobotInfo[] enemies) throws GameActionException{
		Direction[] dirs = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH_EAST, Direction.SOUTH_EAST, Direction.SOUTH_WEST, Direction.NORTH_WEST};
		int minAttackers = 0;
		for(RobotInfo e: enemies){
			if(e.location.distanceSquaredTo(rc.getLocation()) <= e.type.attackRadiusSquared){
				minAttackers++;
			}
		}
		if(minAttackers == 0) return;
		int index = -1;
		for(int i = 0; i < dirs.length; i++){
			MapLocation newLoc = rc.getLocation().add(dirs[i]);
			if(rc.canMove(dirs[i]) && isSafe(newLoc)){
				int currAttackers = 0;
				for(RobotInfo e: enemies){
					if(e.location.distanceSquaredTo(newLoc) <= e.type.attackRadiusSquared){
						currAttackers++;
					}
				}
				if(currAttackers <= minAttackers){
					minAttackers = currAttackers;
					index = i;
				}
			}
		}
		if(index != -1){
			rc.move(dirs[index]);
		}
    }
    
    public void findFlashLoc(Direction d){
    	MapLocation curr = rc.getLocation();
//    	rc.senseTerrainTile(arg0)
    }
    
    public void tryMove(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,2,3,4,5,6,7};
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
//    	RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), 24, theirTeam);
//    	for(RobotInfo enemy:enemies){
//    		if(ml.distanceSquaredTo(enemy.location) <= enemy.type.attackRadiusSquared){
//    			return false;
//    		}
//    	}
    	return true;
    }
    
    
    
}