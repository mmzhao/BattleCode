package launcherStratPlusSoldiers;

import battlecode.common.*;
public class Helipad  extends BaseBot {
	
	public Helipad(RobotController rc) {
		super(rc);
	}
	
	public void execute() throws GameActionException {
		int numDrones = rc.readBroadcast(getUnit(RobotType.DRONE) + 10);
        if (numDrones < 1 && rc.getCoreDelay() < 1 && rc.getTeamOre() > RobotType.DRONE.oreCost) {
            Direction newDir = getSpawnDirection(RobotType.DRONE);
            if (newDir != null) {
                rc.spawn(newDir, RobotType.DRONE);
                rc.broadcast(5000, rc.readBroadcast(5000) + RobotType.DRONE.oreCost);
                rc.broadcast(getUnit(RobotType.DRONE) + 10, numDrones + 1);
            }
        }
//        
        transferSupplies();

        rc.yield();
	}
}
