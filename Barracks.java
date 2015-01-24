package testing;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Barracks extends BaseBot {
    public Barracks(RobotController rc) {
        super(rc);
    }

    public void execute() throws GameActionException {
//    	int numSoldiers = rc.readBroadcast(getUnit(RobotType.SOLDIER) + 10);
//    	int saveForSupply = 0;
//    	if(Clock.getRoundNum() > 1000) saveForSupply = 4;
//    	else if(Clock.getRoundNum() > 500) saveForSupply = 1;
//        if (numSoldiers < 50 - Clock.getRoundNum() / 50 && rc.getCoreDelay() < 1 && rc.getTeamOre() > RobotType.SOLDIER.oreCost + 100 * saveForSupply) {
//            Direction newDir = getSpawnDirection(RobotType.SOLDIER);
//            if (newDir != null) {
//                rc.spawn(newDir, RobotType.SOLDIER);
//                rc.broadcast(5000, rc.readBroadcast(5000) + RobotType.SOLDIER.oreCost);
//                rc.broadcast(getUnit(RobotType.SOLDIER) + 10, numSoldiers + 1);
//            }
//        }
        
        transferSupplies();

        rc.yield();
    }
}