package testing;

import java.util.Random;

import battlecode.common.*;

public class Beaver extends BaseBot {
	public Mover move;
	public MapLocation targetLoc;
	public BuildingController bc;
	public MiningController mc;
	public boolean building;
	public boolean movingInitialized;
	public State state;
	
    public Beaver(RobotController rc) {
        super(rc);
        move = new Mover(rc);
        targetLoc = null;
        bc = new BuildingController(rc);
        mc = new MiningController(rc);
        building = false;
        movingInitialized = false;
        state = State.IDLE;
    }

    public void execute() throws GameActionException {
    	rc.setIndicatorString(1, rc.getType().supplyUpkeep + "");
    	rc.setIndicatorString(2, state + " " + rc.readBroadcast(5));
    	if(rc.getHealth() <= 0){
    		rc.broadcast(2, rc.readBroadcast(2) - 1);
    	}
    	switch(state) {
    		case IDLE: 
    			int instruction = rc.readBroadcast(5); //change when messaging system is finished
    			if (instruction == 0) {
    				state = State.MINING;
    				mineBehavior();
    			}
    			else {
    				rc.broadcast(5, instruction - 1);
    				state = State.BUILDING;
    				buildBehavior();
    			}
    		case MINING:
    			instruction = rc.readBroadcast(5);
    			if (instruction > 0) {
    				rc.broadcast(5, instruction - 1);
    				state = State.BUILDING;
    				buildBehavior();
    			}
    			else 
    				mineBehavior();
    		case BUILDING:
    			instruction = rc.readBroadcast(5);
    			if (instruction <= 0) {
    				state = State.MINING;
    				mineBehavior();
    			}
    			else
    				buildBehavior();
    		default: break;
    	}
    	
    	transferSupplies();
    	
    	rc.yield();
    }
    
    public void mineBehavior() throws GameActionException {
    	rc.setIndicatorString(0, rc.readBroadcast(5) + "");
//    	rc.setIndicatorString(1, rc.getLocation().distanceSquaredTo(rc.senseHQLocation()) + "");
    	if (rc.isCoreReady()) {
    		if(Clock.getRoundNum() < 500 && rc.getLocation().distanceSquaredTo(rc.senseHQLocation()) < 10){//
    			tryMove(rc.senseHQLocation().directionTo(rc.getLocation()));
    		}
    	}
    	if(rc.senseOre(rc.getLocation()) > 1){
    		if(rc.isCoreReady() && rc.canMine()){
//    			rc.setIndicatorString(2, "MINING");
    			rc.mine();
    		}
    	}
//    	MapLocation toMine = mc.findMiningLocation();
//    	if(rc.isCoreReady()){
//    		rc.setIndicatorString(0, toMine.x + "");
//    		rc.setIndicatorString(1, toMine.y + "");
//    		rc.setIndicatorString(2, "MOVING");
//    		rc.move(getMoveDir(toMine));
		// }
		if (rc.isCoreReady()) {

			int fate = rand.nextInt(1000);
			// if (fate < 8 && rc.getTeamOre() >= 300) {
			// tryBuild(directions[rand.nextInt(8)],RobotType.BARRACKS);
			// }
			if (fate < 600) {
				rc.mine();
			} else if (fate < 900) {
				tryMove(directions[rand.nextInt(8)]);
			} else {
//				tryMove(rc.senseHQLocation().directionTo(rc.getLocation()));
				tryMove(rc.getLocation().directionTo(rc.senseEnemyHQLocation()));
			}

		}
	}
    
    public void buildBehavior() throws GameActionException {
    	if (building) { 
    		if (!rc.senseRobotAtLocation(rc.getLocation()).type.isBuilding){ //building done:
    			building = false;
    		} else {
    			//extra time: do calculations
    		}
    	} 
    	else {
//    		if (!movingInitialized) { //start finding where to build
//    			targetLoc = rc.getLocation();
//    			MapLocation targetBuildLocation = bc.getBuildLocation();
//    			targetLoc = targetBuildLocation.subtract(rc.getLocation().directionTo(targetBuildLocation));
//    			move.startBug(targetLoc); //beaver goes to space in front of targetBuildLocation
//    			movingInitialized=true;
//    		}
    		if (rc.isCoreReady()){
//    			targetLoc = rc.senseHQLocation();
//    			Direction moveDir = move.getNextMove();
//    			Direction moveDir = rc.getLocation().directionTo(targetLoc);
//    			if (moveDir != null && moveDir!=Direction.NONE && moveDir!=Direction.OMNI) {
//    				rc.move(moveDir);
//    			} 
    			if (rc.getTeamOre() >= RobotType.BARRACKS.oreCost){ //arrived at location and start building:
//    				building = true;
//    				movingInitialized = false; 
//    				rc.build(rc.getLocation().directionTo(targetLoc), RobotType.BARRACKS);
    				tryBuild(directions[rand.nextInt(8)],RobotType.BARRACKS);
//    				bc.structureToBeBuilt = null; 
    			}
    		}
    		else {
    			//extra time: do calculations
    		}
    	}
    }
    

}