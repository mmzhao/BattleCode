package testing;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Soldier extends BaseBot {
	private Mover move;
	private MapLocation targetLoc;
	
    public Soldier(RobotController rc) {
        super(rc);
        move = new Mover(rc);
        targetLoc = null;
    }

    public void execute() throws GameActionException {
        RobotInfo[] enemies = getEnemiesInAttackingRange();

        if (enemies.length > 0) {
            //attack!
            if (rc.isWeaponReady()) {
                attackLeastHealthEnemy(enemies);
            }
        }
        else if (rc.isCoreReady()) {
            int rallyX = rc.readBroadcast(0);
            int rallyY = rc.readBroadcast(1);
            MapLocation rallyPoint = new MapLocation(rallyX, rallyY);
            move.startBug(rallyPoint);
            Direction newDir = move.getNextMove();

            if (newDir != Direction.NONE && newDir != Direction.OMNI) {
                rc.move(newDir);
            } 
        }
        rc.yield();
    }
}