package launcherStrat;

import battlecode.common.*;

public class MinerFactory extends BaseBot {

	public MinerFactory(RobotController rc) {
		super(rc);
	}

	public void execute() throws GameActionException {
		int numMiners = rc.readBroadcast(getUnit(RobotType.MINER) + 10);
		rc.setIndicatorString(0, "Number of Miners: " + numMiners);
		if (rc.isCoreReady()) {
			if (numMiners < 30 && rc.getTeamOre() > RobotType.MINER.oreCost) {
//			if ((numMiners < 30 || (rc.readBroadcast(2500) / rc.readBroadcast(getUnit(RobotType.MINER) + 10)) > 10) && rc.getTeamOre() > RobotType.MINER.oreCost) {
				spawn();
			}
		}
		
		transferSupplies();
		
		rc.yield();
	}
	
	public void spawn() throws GameActionException{
		Direction newDir = getSpawnDirection(RobotType.MINER);
		if (newDir != null) {
			rc.spawn(newDir, RobotType.MINER);
            rc.broadcast(5000, rc.readBroadcast(5000) + RobotType.MINER.oreCost);
		}
	}

}
