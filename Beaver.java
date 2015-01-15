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
	public RobotType toBuild;

	public Beaver(RobotController rc) {
		super(rc);
		move = new Mover(rc);
		bc = new BuildingController(rc);
		mc = new MiningController(rc);
		state = State.IDLE;
		toBuild = null;
	}

	public void execute() throws GameActionException {
		RobotInfo[] enemies = getEnemiesInAttackingRange();

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
				mineBehavior();
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
		if (rc.isCoreReady()) {
			if (rc.getTeamOre() >= type.oreCost) {
				return tryBuild(directions[rand.nextInt(8)], type);

			} else
				mineBehavior();
		}
		return false;
	}

	public void mineBehavior() throws GameActionException {
		if (rc.isCoreReady()) {
			// if(Clock.getRoundNum() < 500 &&
			// 		rc.getLocation().distanceSquaredTo(rc.senseHQLocation()) < 10){//
			// 		tryMove(rc.senseHQLocation().directionTo(rc.getLocation()));
			if (rc.senseOre(rc.getLocation()) >= 4) {
				if (rc.canMine()) {
					rc.mine();
				}
			}
			else{
				int fate = rand.nextInt(1000);
				if (fate < 750) {
					tryMove(directions[rand.nextInt(8)]);
				} else {
					tryMove(rc.getLocation().directionTo(rc.senseEnemyHQLocation()));
				}
			}
		}
	}

	// public void buildBehavior() throws GameActionException {
	// if (building) {
	// if (!rc.senseRobotAtLocation(rc.getLocation()).type.isBuilding){
	// //building done:
	// building = false;
	// inQueue[getBuilding(toBuild)]--;
	// toBuild = null;
	// state = State.IDLE;
	// } else {
	// //extra time: do calculations
	// }
	// }
	// else {
	// if (rc.isCoreReady()){
	// if (rc.getTeamOre() >= toBuild.oreCost){
	// tryBuild(directions[rand.nextInt(8)],toBuild);
	// }
	// }
	// else {
	// //extra time: do calculations
	// }
	// }
	// }
	//
	public boolean tryBuild(Direction d, RobotType type)
			throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = { 0, 1, -1, 2, -2, 3, -3, 4 };
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 8
				&& !rc.canMove(directions[(dirint + offsets[offsetIndex] + 8) % 8])) {
			offsetIndex++;
		}
		if (offsetIndex < 8) {
			rc.build(directions[(dirint + offsets[offsetIndex] + 8) % 8], type);
			return true;
		}
		return false;
	}

}