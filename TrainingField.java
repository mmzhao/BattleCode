package launcherStratPlusSoldiers;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class TrainingField extends BaseBot{
	
	public TrainingField(RobotController rc) {
		super(rc);
	}

	public void execute() throws GameActionException {
		int numCommander = rc.readBroadcast(getUnit(RobotType.COMMANDER) + 10);
		rc.setIndicatorString(0, "Number of Commanders: " + numCommander);
		if (rc.isCoreReady() && numCommander == 0 && RobotType.COMMANDER.oreCost <= 400 && rc.getTeamOre() > RobotType.COMMANDER.oreCost) {
            Direction newDir = getSpawnDirection(RobotType.COMMANDER);
            if (newDir != null) {
                rc.spawn(newDir, RobotType.COMMANDER);
                rc.broadcast(5000, rc.readBroadcast(5000) + RobotType.COMMANDER.oreCost);
                rc.broadcast(getUnit(RobotType.COMMANDER) + 10, numCommander + 1);
            }
        }
		
		transferSupplies();
		rc.yield();
	}

}
