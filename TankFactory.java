package testing;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class TankFactory extends BaseBot{

	public TankFactory(RobotController rc) {
		super(rc);
	}

	public void execute() throws GameActionException {
		int numTanks = rc.readBroadcast(getUnit(RobotType.TANK) + 10);
		rc.setIndicatorString(0, "Number of Tanks: " + numTanks);
		if (rc.getCoreDelay()<1 && rc.getTeamOre() > RobotType.TANK.oreCost) {
            Direction newDir = getSpawnDirection(RobotType.TANK);
            if (newDir != null) {
                rc.spawn(newDir, RobotType.TANK);
                rc.broadcast(getUnit(RobotType.TANK) + 10, numTanks + 1);
            }
        }
		
		transferSupplies();
		rc.yield();
	}
	
}
