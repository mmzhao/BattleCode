package testing;

import battlecode.common.*;
public class Helipad  extends BaseBot {
	
	public Helipad(RobotController rc) {
		super(rc);
	}
	
	public void execute() throws GameActionException {
		int numDrones = rc.readBroadcast(5);
        if (rc.isCoreReady() && rc.getTeamOre() > 125) {
            Direction newDir = getSpawnDirection(RobotType.DRONE);
            rc.setIndicatorString(0, newDir.toString());
            if (newDir != null) {
                rc.spawn(newDir, RobotType.DRONE);
                rc.broadcast(5, numDrones + 1);
            }
        }

        rc.yield();
	}
}
