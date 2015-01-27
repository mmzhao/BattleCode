package launcherStrat;


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
    
    public void execute() throws GameActionException {
    	int queueStart = rc.readBroadcast(98), queueEnd = rc.readBroadcast(99);
//    	System.out.println(queueStart + " " + queueEnd);

        if (rc.isCoreReady()) {
            if (queueStart != queueEnd && rc.getSupplyLevel() > 1000) {
                RobotInfo[] allies = getAllies();

                int target = rc.readBroadcast(queueStart);

                for (int i=0; i<allies.length; ++i) {
                    if (allies[i].ID == target) {
                        if (rc.getLocation().distanceSquaredTo(allies[i].location) <= GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) {
                            int amount = 2500;
                        	if(allies[i].type == RobotType.LAUNCHER){
                            	amount = 5000;
                            }
                        	rc.transferSupplies(amount, allies[i].location);
                            rc.broadcast(98, queueStart+1);
                        }
                        else {
                            Direction toGoDir = getMoveDir(allies[i].location);

                            if (toGoDir != null) {
                                tryMove(toGoDir);
                            }
                        }
                        break;
                    }
                }
            }
            if (rc.getSupplyLevel() <= 1000) {
                Direction toGoDir = getMoveDir(this.myHQ);

                if (toGoDir != null) {
                    tryMove(toGoDir);
                }
            }
        }
        
		rc.yield();
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
    	RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), 20, theirTeam);
    	for(RobotInfo enemy:enemies){
    		if(ml.distanceSquaredTo(enemy.location) <= enemy.type.attackRadiusSquared){
    			return false;
    		}
    		else if(ml.distanceSquaredTo(enemy.location) <= 8 && enemy.type == RobotType.MISSILE){
    			return false;
    		}
    	}
    	return true;
    }
    
}