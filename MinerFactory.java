package testing;

import battlecode.common.*;

public class MinerFactory extends BaseBot {

	public MinerFactory(RobotController rc) {
		super(rc);
	}

	public void execute() throws GameActionException {
		int numMiners = rc.readBroadcast(getUnit(RobotType.MINER) + 10);
		rc.setIndicatorString(0, "Number of Miners: " + numMiners);
		if (rc.isCoreReady()) {
			if (numMiners < 15 && rc.getTeamOre() > RobotType.MINER.oreCost) {
				spawn();
			}
//			else if(rc.getTeamOre() > RobotType.MINER.oreCost + RobotType.DRONE.oreCost){
//				spawn();
//				rc.broadcast(getUnit(RobotType.MINER) + 10, numMiners + 1);
//			}
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
