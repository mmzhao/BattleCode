package testing;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Drone extends BaseBot {
	
    public Drone(RobotController rc) throws GameActionException {
        super(rc);
    }
    
    public void execute() throws GameActionException {
    	rc.setIndicatorString(1, rc.getType().supplyUpkeep + "");
        RobotInfo[] enemies = getEnemiesInAttackingRange();

        if (enemies.length > 0) {
            //attack!
            if (rc.isWeaponReady()) {
            	
                attackLeastHealthEnemy(enemies);
            }
        }
        else if (rc.isCoreReady()) {
            int rallyX = rc.readBroadcast(1001);
            int rallyY = rc.readBroadcast(1002);
            MapLocation rallyPoint = new MapLocation(rallyX, rallyY);
            rc.setIndicatorString(0, rallyX + " " + rallyY);
            tryMove(rc.getLocation().directionTo(rallyPoint));
        }
        
        transferSupplies();
        
        rc.yield();
    }
    
}