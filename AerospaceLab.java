package launcherStratPlusSoldiers;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class AerospaceLab extends BaseBot{

	public AerospaceLab(RobotController rc) {
		super(rc);
	}

	public void execute() throws GameActionException {
		int numLaunchers = rc.readBroadcast(getUnit(RobotType.LAUNCHER) + 10);
		rc.setIndicatorString(0, "Number of Launchers: " + numLaunchers);
		if (rc.isCoreReady() && rc.getTeamOre() > RobotType.LAUNCHER.oreCost) {
            Direction newDir = getSpawnDirection(RobotType.LAUNCHER);
            if (newDir != null) {
                rc.spawn(newDir, RobotType.LAUNCHER);
                rc.broadcast(5000, rc.readBroadcast(5000) + RobotType.LAUNCHER.oreCost);
                rc.broadcast(getUnit(RobotType.LAUNCHER) + 10, numLaunchers + 1);
            }
        }
		
		transferSupplies();
		rc.yield();
	}
	
}
