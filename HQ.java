package testing;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class HQ extends BaseBot {
	
    public HQ(RobotController rc) {
        super(rc);
        this.myHQ = rc.senseHQLocation();
        this.theirHQ = rc.senseEnemyHQLocation();
    }

    public void execute() throws GameActionException {
        int numBeavers = rc.readBroadcast(2);

        if (rc.isCoreReady() && rc.getTeamOre() > 100 && numBeavers < 10) {
            Direction newDir = getSpawnDirection(RobotType.BEAVER);
            if (newDir != null) {
                rc.spawn(newDir, RobotType.BEAVER);
                rc.broadcast(2, numBeavers + 1);
            }
        }
        MapLocation rallyPoint;
        if (Clock.getRoundNum() < 600) {
        	MapLocation[] ourTowers = rc.senseTowerLocations();
            rallyPoint = ourTowers[0];
        } else {
        	MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
        	if (enemyTowers.length>0) {
        		rallyPoint = enemyTowers[0];
        	} else {
        		rallyPoint = theirHQ;
        	}
        }
        rc.broadcast(0, rallyPoint.x);
        rc.broadcast(1, rallyPoint.y);

        
        rc.yield();
    }
    
}