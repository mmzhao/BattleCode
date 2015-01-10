package testing;

import battlecode.common.*;

public class Beaver extends BaseBot {
	public Mover move;
	public MapLocation targetLoc;
	public BuildingController bc;
	public boolean building;
	public boolean movingInitialized;
	
    public Beaver(RobotController rc) {
        super(rc);
        move = new Mover(rc);
        targetLoc = null;
        bc = new BuildingController(rc);
        building = false;
        movingInitialized = false;
    }

    public void execute() throws GameActionException {
    	if (building) { 
    		if (!rc.senseRobotAtLocation(rc.getLocation()).type.isBuilding){ //building done:
    			building = false;
    		} else {
    			//extra time: do calculations
    		}
    	} else {
    		if (!movingInitialized) { //start finding where to build
    			MapLocation targetBuildLocation = bc.getBuildLocation();
    			targetLoc = targetBuildLocation.subtract(rc.getLocation().directionTo(targetBuildLocation));
    			move.startBug(targetLoc); //beaver goes to space in front of targetBuildLocation
    			movingInitialized=true;
    		}
    		if (rc.getCoreDelay()<1) {
    			Direction moveDir = move.getNextMove();
    			if (moveDir!=Direction.NONE || moveDir!=Direction.OMNI) {
    				rc.move(moveDir);
    			} else if (rc.getTeamOre()>=bc.structureToBeBuilt.oreCost){ //arrived at location and start building:
    				building = true;
    				movingInitialized=false; 
    				rc.build(rc.getLocation().directionTo(targetLoc), bc.structureToBeBuilt);
    				bc.structureToBeBuilt = null; 
    			}
    		} else {
    			//extra time: do calculations
    		}
    	}
        rc.yield();
    }
}