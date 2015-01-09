package testing;

import battlecode.common.*;

public class Beaver extends BaseBot {
	private Mover move;
	private MapLocation targetLoc;
	private boolean mining;
	private boolean movingInitialized;
	
    public Beaver(RobotController rc) {
        super(rc);
        move = new Mover(rc);
        targetLoc = null;
        mining = false;
        movingInitialized = false;
    }

    public void execute() throws GameActionException {
    	rc.setIndicatorString(0, String.valueOf(mining));
        if (rc.isCoreReady()) {
            if (rc.getTeamOre() < 300) {
                //mine
                if (!mining && !movingInitialized) {
                	move.startBug(findMiningLocation());
                	rc.setIndicatorString(1, Integer.toString(findMiningLocation().x) +
                			", " + Integer.toString(findMiningLocation().y));
                	movingInitialized = true;
                	Direction newDir = move.getNextMove();
                    if (newDir != Direction.NONE && newDir != Direction.OMNI) {
                        rc.move(newDir);
                    } 
                }
                
                else if (!mining) {
                	Direction newDir = move.getNextMove();
                    if (newDir != Direction.NONE && newDir != Direction.OMNI) {
                        rc.move(newDir);
                    } 
                    else {
                    	mining = true;
                    } 
                }
                
                else {
                	if (rc.senseOre(rc.getLocation())>0) {
                		rc.mine();
                	} else {
                		mining = false;
                	}
                }
                rc.setIndicatorString(2, move.getNextMove().toString());
            }
            
            else {
                //build barracks
                Direction newDir = getBuildDirection(RobotType.BARRACKS);
                if (newDir != null) {
                    rc.build(newDir, RobotType.BARRACKS);
                }
            }
        }

        rc.yield();
    }
}