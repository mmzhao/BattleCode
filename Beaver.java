package launcherSoldierStrat;

import java.util.Random;

import battlecode.common.*;

public class Beaver extends BaseBot {
	public boolean building;
	public State state;
	public RobotType toBuild;

	public Beaver(RobotController rc) {
		super(rc);
		state = State.IDLE;
		toBuild = null;
	}
	
	public void execute() throws GameActionException {
		if(Clock.getRoundNum() < 19){
			if(rc.isCoreReady()){
				rc.mine();
				rc.yield();
			}
		}
		
		RobotInfo[] enemies = getEnemiesInAttackingRange(rc.getType());

//		if (enemies.length > 0) {
//			// attack!
//			if (rc.isWeaponReady()) {
//				attackLeastHealthEnemy(enemies);
//			}
//		}
		
		if(!isOkaySpot(rc.getLocation())){
			if(rc.isCoreReady()){
				tryMove(rc.getLocation().directionTo(myHQ));
//				if(!isOkaySpot(rc.getLocation())){
//					if(rc.isCoreReady()){
//					tryMove(myHQ.directionTo(rc.getLocation()));
//					}
//				}
			}
		}
		int instruction = rc.readBroadcast(0); // change when messaging system
												// is finished
		if (state == State.BUILDING) {
			building();
		}
		else{
			if (instruction == 0) {
				mineBehavior(); //doesn't really actually mine
			} else {
				// rc.setIndicatorString(0, (int)(instruction/10) + "");
				if (startBuilding(getBuilding(instruction % 10))) {
					rc.broadcast(0, instruction / 10);
					toBuild = getBuilding(instruction % 10);
					state = State.BUILDING;
				}
			}
		}
		

		rc.setIndicatorString(0, state.toString());

		transferSupplies();

		rc.yield();
	}
	
	
	public void tryMove(Direction d) throws GameActionException {
		int offsetIndex = 0;
//		int[] offsets = {0,1,-1,2,-2};
		int dirint = directionToInt(d);
		while (offsetIndex < offsets.length && 
				(!rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8]) || 
						!isSafe(rc.getLocation().add(directions[(dirint+offsets[offsetIndex]+8)%8])) || 
						!isOkaySpot(rc.getLocation().add(directions[(dirint+offsets[offsetIndex]+8)%8])))) {
			offsetIndex++;
		}
		if (offsetIndex < offsets.length) {
			rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
		}
	}
	
	public boolean isOkaySpot(MapLocation ml){
		if((ml.x + ml.y + myHQ.x + myHQ.y) % 2 == 0){
			return false;
		}
		return true;
	}
	
	

 	public void executeOld() throws GameActionException {
		RobotInfo[] enemies = getEnemiesInAttackingRange(rc.getType());

		if (enemies.length > 0) {
			// attack!
			if (rc.isWeaponReady()) {
				attackLeastHealthEnemy(enemies);
			}
		}
		int instruction = rc.readBroadcast(0); // change when messaging system
												// is finished
		if (state == State.IDLE) {
			if (instruction == 0) {
				state = State.MINING;
				mineBehavior(); //doesn't really actually mine
			} else {
				// rc.setIndicatorString(0, (int)(instruction/10) + "");
				if (startBuilding(getBuilding(instruction % 10))) {
					rc.broadcast(0, instruction / 10);
					toBuild = getBuilding(instruction % 10);
					state = State.BUILDING;
				}
			}
		} else if (state == State.MINING) {
			instruction = rc.readBroadcast(0);
			if (instruction > 0) {
				if (startBuilding(getBuilding(instruction % 10))) {
					rc.broadcast(0, instruction / 10);
					toBuild = getBuilding(instruction % 10);
					state = State.BUILDING;
				}
			} else
				mineBehavior();
		} else if (state == State.BUILDING) {
			building();
		}

		rc.setIndicatorString(0, state.toString());

		transferSupplies();

		rc.yield();
	}

	public void building() throws GameActionException {
		if (!rc.senseRobotAtLocation(rc.getLocation()).type.isBuilding) { // building
																			// done:
		// inQueue[getBuilding(toBuild)]--; //don't know why this line isn't
		// working
			toBuild = null;
			state = State.IDLE;
		}
	}

	public boolean startBuilding(RobotType type) throws GameActionException {
		int cost = type.oreCost;
		if (type == RobotType.SUPPLYDEPOT) {
			cost += 250;
		}
		if(rc.getTeam() == Team.A){
			if (rc.isCoreReady()) {
				if (rc.getTeamOre() >= cost) {
					if(rc.canBuild(Direction.SOUTH, type)){
						rc.build(Direction.SOUTH, type);
						rc.broadcast(5000, rc.readBroadcast(5000) + type.oreCost);
						return true;
					}
					else if(rc.canBuild(Direction.WEST, type)){
						rc.build(Direction.WEST, type);
						rc.broadcast(5000, rc.readBroadcast(5000) + type.oreCost);
						return true;
					}
					else if(rc.canBuild(Direction.NORTH, type)){
						rc.build(Direction.NORTH, type);
						rc.broadcast(5000, rc.readBroadcast(5000) + type.oreCost);
						return true;
					}
					else if(rc.canBuild(Direction.EAST, type)){
						rc.build(Direction.EAST, type);
						rc.broadcast(5000, rc.readBroadcast(5000) + type.oreCost);
						return true;
					}
	//				rc.setIndicatorString(2, "North: " + rc.canBuild(Direction.NORTH, type) + " East: " + rc.canBuild(Direction.EAST, type) + " South: " + rc.canBuild(Direction.SOUTH, type) + " West: " + rc.canBuild(Direction.WEST, type));
					moveOutwards();
				}
			}
		}
		else{
			if (rc.isCoreReady()) {
				if (rc.getTeamOre() >= cost) {
					if(rc.canBuild(Direction.NORTH, type)){
						rc.build(Direction.NORTH, type);
						rc.broadcast(5000, rc.readBroadcast(5000) + type.oreCost );
						return true;
					}
					else if(rc.canBuild(Direction.EAST, type)){
						rc.build(Direction.EAST, type);
						rc.broadcast(5000, rc.readBroadcast(5000) + type.oreCost);
						return true;
					}
					else if(rc.canBuild(Direction.SOUTH, type)){
						rc.build(Direction.SOUTH, type);
						rc.broadcast(5000, rc.readBroadcast(5000) + type.oreCost);
						return true;
					}
					else if(rc.canBuild(Direction.WEST, type)){
						rc.build(Direction.WEST, type);
						rc.broadcast(5000, rc.readBroadcast(5000) + type.oreCost);
						return true;
					}
	//				rc.setIndicatorString(2, "North: " + rc.canBuild(Direction.NORTH, type) + " East: " + rc.canBuild(Direction.EAST, type) + " South: " + rc.canBuild(Direction.SOUTH, type) + " West: " + rc.canBuild(Direction.WEST, type));
					moveOutwards();
				}
			}
		}
		mineBehavior();
		return false;
	}
	
	public void moveOutwards() throws GameActionException{
		if(rc.isCoreReady()){
			tryMove(myHQ.directionTo(rc.getLocation()));
		}
	}

	public void mineBehavior() throws GameActionException {
		if (rc.isCoreReady()) {
			tryMove(rc.getLocation().directionTo(myHQ));
		}
	}


}