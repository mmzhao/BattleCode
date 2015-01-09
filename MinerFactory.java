package testing;

import battlecode.common.*;

public class MinerFactory extends BaseBot {
	
	public MinerFactory(RobotController rc) {
		super(rc);
	}
	
	public void execute() throws GameActionException{
		int numMiners = rc.readBroadcast(4);
		if (rc.getCoreDelay()<1 && rc.getTeamOre()>50) {
			Direction newDir = getSpawnDirection(RobotType.MINER);
            if (newDir != null) {
                rc.spawn(newDir, RobotType.MINER);
                rc.broadcast(4, numMiners + 1);
            }
		}
	}
	
}
