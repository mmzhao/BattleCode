package testing;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Barracks extends BaseBot {
    public Barracks(RobotController rc) {
        super(rc);
    }

    public void execute() throws GameActionException {
    	int numSoldiers = rc.readBroadcast(3);
        if (rc.getCoreDelay()<1 && rc.getTeamOre() > 200) {
            Direction newDir = getSpawnDirection(RobotType.SOLDIER);
            if (newDir != null) {
                rc.spawn(newDir, RobotType.SOLDIER);
            }
        }

        rc.yield();
    }
}