package launcherStrat;

import battlecode.common.Clock;
import battlecode.common.DependencyProgress;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;

//BROADCASTING INDEX MEANINGS
//0 -- queue -- at most 9 in queue, first in first out, adds to left, takes out from right
//1 -- number of barracks
//2 -- number of minerfactories
//3 -- number of helipads
//4 -- number of supplydepos
//5 -- number of tank factories
//6 -- number of tech institutes
//7 -- number of aerospace labs
//8 -- number of training fields
//9 -- number of handwash stations

//11 -- number of beavers
//12 -- number of miners
//13 -- number of soldiers
//14 -- number of bashers
//15 -- number of drones
//16 -- number of tanks
//17 -- number of launchers
//18 -- number of commanders
//19 -- number of computers

//20 -- number of turrets
//21 + 10n -- x position for turret n
//22 + 10n -- y position for turret n
//23 + 10n -- number of defenders for turret n

//100 -- max n
//1n1 -- hit squad rally x
//1n2 -- hit squad rally y
//1n3 -- number of drones in hit squad n
//1n4 -- ID of target


//1000 -- number of soldiers defending base
//1001 -- rally x position
//1002 -- rally y position
//1003 -- no longer stay out of range

//1500 -- number of good mining spots FOR NOW ONLY USING ONE SPOT
//15n1 -- mining spot n x position
//15n2 -- mining spot n y position
//15n3 -- spot value in 14 by 14 grid

//2000 -- target boolean, 0-no, 1-yes
//2001 -- extra unit target point x
//2002 -- extra unit target point y
//2003 -- number of units in target force
//2004 -- target force x sum
//2005 -- target force y sum

//2500 -- per turn mining total - if less than 1 avg then stop building miners

//3000 -- attacking units yolo rush
//4000 -- yolo rush mode, 0-no, 1-yes

//5000 -- total ore used

public class HQ extends BaseBot {

	int[] units; // don't use 0th index
	int[] buildings;

	public int xmin, xmax, ymin, ymax;
	public boolean isFinished;
	public int xpos, ypos;



	public HQ(RobotController rc) {
		super(rc);
		this.myHQ = rc.senseHQLocation();
		this.theirHQ = rc.senseEnemyHQLocation();
		inQueue = new int[10];
	}

	public void execute() throws GameActionException {
		
		//amount of every ally unit
		getInitialInfoTankSpecial();

		//attack if possible
		if (rc.isWeaponReady()) {
			attackForValue(getEnemiesInAttackingRange(rc.getType()));
		}

		//always have 2 beavers up
		keepingSomeBeavers(2);
		
		//set up strat
		//IF NOT USING TANK STRAT CHANGE OTHER CLASS ATTACK PATTERNS TO WHAT TANK HAS NOW - VALUE ATTACKS
		MapLocation rallyPoint;
		int mapSize = myHQ.distanceSquaredTo(theirHQ);
		if(mapSize < 500){
//			aggroTankStrat();
			soldierRushStrat();
			
			//set up rally point
			rallyPoint = theirHQ;
			if(Clock.getRoundNum() < 1500){
				setTarget();
			}
			else if(Clock.getRoundNum() < 1800){
				rc.broadcast(2000, 0); //remove all targets
				rc.broadcast(3000, 1); //start attack rush
				rallyPoint = nextTower();
			}
			else{
				rc.broadcast(4000, 1); //throw everything
				rallyPoint = nextTower();
			}
		} //PERHAPS MAKE A MIDDLE GROUND
		else{
//			tankStratWithCommander();
			launcherStrat1();
//			tankStrat(); 
//			soldierRushStrat();
			
			//set up rally point
//			rallyPoint = normalRushRally();
//			rallyPoint = new MapLocation((myHQ.x + theirHQ.x)/2, (myHQ.y + theirHQ.y)/2);
			rallyPoint = theirHQ;
			if(Clock.getRoundNum() < 1500){
				setTarget();
			}
			else if(Clock.getRoundNum() < 1800){
				rc.broadcast(2000, 0); //remove all targets
				rc.broadcast(3000, 1); //start attack rush
				rallyPoint = nextTower();
			}
			else{
				rc.broadcast(4000, 1); //throw everything
				rallyPoint = nextTower();
			}
		}
		
		
		for (int i = 1; i < 10; i++) {
			rc.broadcast(i, buildings[i]);
			rc.broadcast(i + 10, units[i]);
		}
//		rc.broadcast(1000, numDefenders);
		rc.broadcast(1001, rallyPoint.x);
		rc.broadcast(1002, rallyPoint.y);
		
//		System.out.println(rallyPoint.x + " " + rallyPoint.y);

		// reset Queue info
		setUpQueueInfo();

		
		rc.setIndicatorString(1, "used: "+ rc.readBroadcast(5000) + " mining per turn: " + rc.readBroadcast(2500));
		

		resetMining();
		
		transferSupplies();



		rc.yield();
	}
	
	public void resetMining() throws GameActionException{
		rc.broadcast(2500, 0);
	}

	public MapLocation nextTower() throws GameActionException{
		MapLocation targetTower = null;
		int minProtection = Integer.MAX_VALUE;
		MapLocation[] towers = rc.senseEnemyTowerLocations();
		for(MapLocation t1: towers){
			int prot = 0;
			for(MapLocation t2: towers){
				if(t1.distanceSquaredTo(t2) == 0){
					//it's the same tower
				}
				else if(t1.distanceSquaredTo(t2) <= 25){
					prot += 2;
				}
				else if(t1.distanceSquaredTo(t2) <= 36){
					prot++;
				}
			}
			MapLocation curr = new MapLocation(rc.readBroadcast(2004), rc.readBroadcast(2005));
			prot += Math.sqrt(curr.distanceSquaredTo(t1));
			if(prot < minProtection){
				minProtection = prot;
				targetTower = t1;
			}
		}
		if(targetTower == null) targetTower = theirHQ;
		return targetTower;
//		if(rc.senseEnemyTowerLocations().length == 0) return theirHQ;
//		return closestLocation(new MapLocation(rc.readBroadcast(1001), rc.readBroadcast(1002)), rc.senseEnemyTowerLocations());
	}
	
	public void setTarget() throws GameActionException{
//		rc.broadcast(2000, 0);
//		rc.broadcast(2001, 0);
//		rc.broadcast(2002, 0);
		
		MapLocation ml = new MapLocation(rc.readBroadcast(2004), rc.readBroadcast(2005));
		if(ml.x == 0 && ml.y == 0){
			
		}
		int minIndex = -1;
		double minValue = Integer.MAX_VALUE;
		RobotInfo[] targets = rc.senseNearbyRobots(myHQ, 10000000, theirTeam);
		MapLocation[] towers = rc.senseTowerLocations();
		for(int i = 0; i < targets.length; i++){
			if(targets[i].type == RobotType.HQ || targets[i].type == RobotType.TOWER){
				continue;
			}
			double value = targets[i].location.distanceSquaredTo(ml);
			RobotInfo ri = targets[i];
			if(getBuilding(ri.type) != 0){
        		value /= 1;
        	}
        	else{
	        	if(ri.type == RobotType.BEAVER){
	        		value /= 2;
	        	}
	        	else if(ri.type == RobotType.MINER){
	        		value /= 2;
	        	}
	        	else if(ri.type == RobotType.SOLDIER){
	        		value /= 2;
	        	}
	        	else if(ri.type == RobotType.BASHER){
	        		value /= 2;
	        	}
	        	else if(ri.type == RobotType.DRONE){
	        		value /= 2;
	        	}
	        	else if(ri.type == RobotType.TANK){
	        		value /= 8;
	        	}
	        	else if(ri.type == RobotType.LAUNCHER){
	        		value /= 8;
	        	}
	        	else if(ri.type == RobotType.COMMANDER){
	        		value /= 8;
	        	}
	        	else if(ri.type == RobotType.COMPUTER){
	        		value /= 1;
	        	}
	        	else if(ri.type == RobotType.MISSILE){
	        		value /= 4;
	        	}
        	}
			if(value < minValue){
				minValue = value;
				minIndex = i;
			}
		}
		if(minIndex != -1){
			rc.broadcast(2000, 1);
			rc.broadcast(2001, targets[minIndex].location.x);
			rc.broadcast(2002, targets[minIndex].location.y);
			rc.setIndicatorDot(targets[minIndex].location, 100, 0, 0);
		}
	}
	
	public void keepingSomeBeavers(int num) throws GameActionException{
		if (rc.isCoreReady()) {
			if (units[getUnit(RobotType.BEAVER)] < 1 || (units[getUnit(RobotType.BEAVER)] < num && buildings[getBuilding(RobotType.MINERFACTORY)] > 0)) {
				if (rc.getTeamOre() > RobotType.BEAVER.oreCost) {
					Direction newDir = getSpawnDirection(RobotType.BEAVER);
					if (newDir != null) {
						rc.spawn(newDir, RobotType.BEAVER);
						rc.broadcast(5000, rc.readBroadcast(5000) + RobotType.BEAVER.oreCost);
						units[getUnit(RobotType.BEAVER)]++;
					}
				}
			}
		}
	}
	
	public void setHitSquads() throws GameActionException{ //2 drones per target
		for(int i = 0; i < rc.readBroadcast(100); i++){
			rc.broadcast(101 + 10 * i, 0);
			rc.broadcast(102 + 10 * i, 0);
//			rc.broadcast(103 + 10 * i, 0);
		}
		int index = 0;
//		RobotInfo[] targets = rc.senseNearbyRobots(myHQ, (int)(Math.pow(Clock.getRoundNum(), 2) / 200), theirTeam);
		RobotInfo[] targets = rc.senseNearbyRobots(myHQ, 10000000, theirTeam);
		for(RobotInfo t: targets){
//			if(t.type.attackRadiusSquared >= RobotType.DRONE.attackRadiusSquared){
//				if(t.location.distanceSquaredTo(myHQ) > (int)(Math.pow(Clock.getRoundNum(), 2) / 200)){
//					continue;
//				}
//			}
			
			rc.broadcast(101 + 10 * index, t.location.x);
			rc.broadcast(102 + 10 * index, t.location.y);
			rc.broadcast(103 + 10 * index, t.ID);
			index++;
		}
		rc.broadcast(100, index);
	}
	
	public void droneRushStrat() throws GameActionException{
		if (rc.isCoreReady()) {
			if (units[getUnit(RobotType.BEAVER)] < 1) {
				if (rc.getTeamOre() > 100) {
					Direction newDir = getSpawnDirection(RobotType.BEAVER);
					if (newDir != null) {
						rc.spawn(newDir, RobotType.BEAVER);
						units[getUnit(RobotType.BEAVER)]++;
					}
				}
			}
		}
		
		if (buildings[getBuilding(RobotType.MINERFACTORY)]
				+ inQueue[getBuilding(RobotType.MINERFACTORY)] < 1) {
			addToQueue(getBuilding(RobotType.MINERFACTORY));
		}

		if (buildings[getBuilding(RobotType.HELIPAD)]
						+ inQueue[getBuilding(RobotType.HELIPAD)] < Clock.getRoundNum() / 100) {
			addToQueue(getBuilding(RobotType.HELIPAD));
		}
	}
	
	public void tankStratWithCommander() throws GameActionException {

		if (buildings[getBuilding(RobotType.MINERFACTORY)]
				+ inQueue[getBuilding(RobotType.MINERFACTORY)] < 1) {
			addToQueue(getBuilding(RobotType.MINERFACTORY));
		}
		
		if (buildings[getBuilding(RobotType.TRAININGFIELD)]
				+ inQueue[getBuilding(RobotType.TRAININGFIELD)] > 0 && buildings[getBuilding(RobotType.MINERFACTORY)]
				+ inQueue[getBuilding(RobotType.MINERFACTORY)] < 2) {
			addToQueue(getBuilding(RobotType.MINERFACTORY));
		}

		if (buildings[getBuilding(RobotType.MINERFACTORY)]
				+ inQueue[getBuilding(RobotType.MINERFACTORY)] > 0 && 
				buildings[getBuilding(RobotType.TECHNOLOGYINSTITUTE)]
				+ inQueue[getBuilding(RobotType.TECHNOLOGYINSTITUTE)] < 1) {
			addToQueue(getBuilding(RobotType.TECHNOLOGYINSTITUTE));
		}

		if (buildings[getBuilding(RobotType.TECHNOLOGYINSTITUTE)] > 0 && buildings[getBuilding(RobotType.TRAININGFIELD)]
						+ inQueue[getBuilding(RobotType.TRAININGFIELD)] < 1) {
			addToQueue(getBuilding(RobotType.TRAININGFIELD));
		}
		
		if (buildings[getBuilding(RobotType.TRAININGFIELD)] > 0 && 
				buildings[getBuilding(RobotType.BARRACKS)]
				+ inQueue[getBuilding(RobotType.BARRACKS)] < 1) {
			addToQueue(getBuilding(RobotType.BARRACKS));
		}

		if (buildings[getBuilding(RobotType.BARRACKS)] > 0 && buildings[getBuilding(RobotType.TANKFACTORY)]
						+ inQueue[getBuilding(RobotType.TANKFACTORY)] < 4) {
			addToQueue(getBuilding(RobotType.TANKFACTORY));
		}
		
		if(buildings[getBuilding(RobotType.SUPPLYDEPOT)]
						+ inQueue[getBuilding(RobotType.SUPPLYDEPOT)] < (int) ((Clock.getRoundNum() - 600) / 100)) {
			addToQueue(getBuilding(RobotType.SUPPLYDEPOT));
		}
	}
	
	public void tankStrat() throws GameActionException {

		if (buildings[getBuilding(RobotType.MINERFACTORY)]
				+ inQueue[getBuilding(RobotType.MINERFACTORY)] < 2) {
			addToQueue(getBuilding(RobotType.MINERFACTORY));
		}

		else if (buildings[getBuilding(RobotType.BARRACKS)]
				+ inQueue[getBuilding(RobotType.BARRACKS)] < 1) {
			addToQueue(getBuilding(RobotType.BARRACKS));
		}

		else if (buildings[getBuilding(RobotType.BARRACKS)] > 0 && buildings[getBuilding(RobotType.TANKFACTORY)]
						+ inQueue[getBuilding(RobotType.TANKFACTORY)] < 5) {
			addToFrontQueue(getBuilding(RobotType.TANKFACTORY));
		}
		
		if(buildings[getBuilding(RobotType.SUPPLYDEPOT)]
						+ inQueue[getBuilding(RobotType.SUPPLYDEPOT)] < (int) ((Clock.getRoundNum() - 200) / 100)) {
			addToQueue(getBuilding(RobotType.SUPPLYDEPOT));
		}
	}
	
	//LARGE MAP WITH ORE
	public void launcherStrat1() throws GameActionException {

		if (buildings[getBuilding(RobotType.MINERFACTORY)]
				+ inQueue[getBuilding(RobotType.MINERFACTORY)] < 2) {
			addToQueue(getBuilding(RobotType.MINERFACTORY));
		}
		
//		else if (buildings[getBuilding(RobotType.BARRACKS)]
//				+ inQueue[getBuilding(RobotType.BARRACKS)] < 2) {
//			addToQueue(getBuilding(RobotType.BARRACKS));
//		}

		else if (buildings[getBuilding(RobotType.HELIPAD)]
				+ inQueue[getBuilding(RobotType.HELIPAD)] < 1) {
			addToQueue(getBuilding(RobotType.HELIPAD));
		}

		else if (buildings[getBuilding(RobotType.HELIPAD)] > 0 && buildings[getBuilding(RobotType.AEROSPACELAB)]
						+ inQueue[getBuilding(RobotType.AEROSPACELAB)] < 3) {
			addToFrontQueue(getBuilding(RobotType.AEROSPACELAB));
		}
		
		if(buildings[getBuilding(RobotType.SUPPLYDEPOT)]
						+ inQueue[getBuilding(RobotType.SUPPLYDEPOT)] < (int) ((Clock.getRoundNum() - 500) / 100)) {
			addToQueue(getBuilding(RobotType.SUPPLYDEPOT));
		}
	}
	
	public void aggroTankStrat() throws GameActionException {

		if (buildings[getBuilding(RobotType.MINERFACTORY)]
				+ inQueue[getBuilding(RobotType.MINERFACTORY)] < 1) {
			addToQueue(getBuilding(RobotType.MINERFACTORY));
		}
		
		if (buildings[getBuilding(RobotType.TANKFACTORY)]
				+ inQueue[getBuilding(RobotType.TANKFACTORY)] > 0 && buildings[getBuilding(RobotType.MINERFACTORY)]
				+ inQueue[getBuilding(RobotType.MINERFACTORY)] < 2) {
			addToQueue(getBuilding(RobotType.MINERFACTORY));
		}

		if (buildings[getBuilding(RobotType.MINERFACTORY)]
				+ inQueue[getBuilding(RobotType.MINERFACTORY)] > 0 && 
				buildings[getBuilding(RobotType.BARRACKS)]
				+ inQueue[getBuilding(RobotType.BARRACKS)] < 1) {
			addToQueue(getBuilding(RobotType.BARRACKS));
		}

		if (buildings[getBuilding(RobotType.BARRACKS)] > 0 && buildings[getBuilding(RobotType.TANKFACTORY)]
						+ inQueue[getBuilding(RobotType.TANKFACTORY)] < 4) {
			addToQueue(getBuilding(RobotType.TANKFACTORY));
		}
		
		if(buildings[getBuilding(RobotType.SUPPLYDEPOT)]
						+ inQueue[getBuilding(RobotType.SUPPLYDEPOT)] < (int) ((Clock.getRoundNum() - 600) / 100)) {
			addToQueue(getBuilding(RobotType.SUPPLYDEPOT));
		}
	}
	
	public void tankRushStrat() throws GameActionException{
		
		if (buildings[getBuilding(RobotType.MINERFACTORY)]
				+ inQueue[getBuilding(RobotType.MINERFACTORY)] < 2) {
			addToQueue(getBuilding(RobotType.MINERFACTORY));
		}

		if (buildings[getBuilding(RobotType.BARRACKS)]
						+ inQueue[getBuilding(RobotType.BARRACKS)] < 1) {
			addToQueue(getBuilding(RobotType.BARRACKS));
		}
		
		if (buildings[getBuilding(RobotType.MINERFACTORY)] > 1 && buildings[getBuilding(RobotType.TANKFACTORY)]
				+ inQueue[getBuilding(RobotType.TANKFACTORY)] < 4) {
			addToQueue(getBuilding(RobotType.TANKFACTORY));
		}
	}
	
	public void soldierRushStrat() throws GameActionException{
		
		if (buildings[getBuilding(RobotType.MINERFACTORY)]
				+ inQueue[getBuilding(RobotType.MINERFACTORY)] < 2) {
			addToQueue(getBuilding(RobotType.MINERFACTORY));
		}

		else if (buildings[getBuilding(RobotType.MINERFACTORY)]
				+ inQueue[getBuilding(RobotType.MINERFACTORY)] > 0 && buildings[getBuilding(RobotType.BARRACKS)]
				+ inQueue[getBuilding(RobotType.BARRACKS)] < 4) {
			addToQueue(getBuilding(RobotType.BARRACKS));
		}

		if(buildings[getBuilding(RobotType.SUPPLYDEPOT)]
						+ inQueue[getBuilding(RobotType.SUPPLYDEPOT)] < (int) ((Clock.getRoundNum() - 500) / 30)) {
			addToQueue(getBuilding(RobotType.SUPPLYDEPOT));
		}
	}
	
	public MapLocation normalRushRally() throws GameActionException{
		MapLocation rallyPoint = new MapLocation(rc.readBroadcast(1001),
				rc.readBroadcast(1002));

		if (Clock.getRoundNum() < 1500) {
//			MapLocation initialRally = rc.getLocation().add(
//					rc.getLocation().directionTo(theirHQ), 7);
			MapLocation initialRally = closestLocation(theirHQ, rc.senseTowerLocations());
			rallyPoint = initialRally;
		} else {
			MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
			MapLocation currRally = rallyPoint;
			if (enemyTowers.length > 0) {
				rallyPoint = closestLocation(currRally, enemyTowers);
				if (currRally.distanceSquaredTo(rallyPoint) > theirHQ
						.distanceSquaredTo(currRally)) {
					rallyPoint = theirHQ;
				}
			} else {
				rallyPoint = theirHQ;
			}
			rc.broadcast(1003, 1);
		}
		return rallyPoint;
	}

	//SPECIALIZED FOR TANKS CHANGE LATER
	public void getInitialInfoTankSpecial() throws GameActionException {
		//reset tank info
		rc.broadcast(2003, 0);
		rc.broadcast(2004, 0);
		rc.broadcast(2005, 0);
		
		RobotInfo[] ri = rc.senseNearbyRobots(rc.getLocation(), 100000000,
				rc.getTeam());
		units = new int[10];
		buildings = new int[10];
		for (int i = 0; i < ri.length; i++) {
			if (ri[i].type == RobotType.BEAVER) {
				units[1]++;
			} else if (ri[i].type == RobotType.MINER) {
				units[2]++;
			} else if (ri[i].type == RobotType.SOLDIER) {
				units[3]++;
			} else if (ri[i].type == RobotType.BASHER) {
				units[4]++;
			} else if (ri[i].type == RobotType.DRONE) {
				units[5]++;
			} else if (ri[i].type == RobotType.TANK) {
				units[6]++;
				rc.broadcast(2003, rc.readBroadcast(2003) + 1);
				rc.broadcast(2004, rc.readBroadcast(2004) + ri[i].location.x);
				rc.broadcast(2005, rc.readBroadcast(2005) + ri[i].location.y);
			} else if (ri[i].type == RobotType.LAUNCHER) {
				units[7]++;
			} else if (ri[i].type == RobotType.COMMANDER) {
				units[8]++;
			} else if (ri[i].type == RobotType.COMPUTER) {
				units[9]++;
			} else if (ri[i].type == RobotType.BARRACKS) {
				buildings[1]++;
			} else if (ri[i].type == RobotType.MINERFACTORY) {
				buildings[2]++;
			} else if (ri[i].type == RobotType.HELIPAD) {
				buildings[3]++;
			} else if (ri[i].type == RobotType.SUPPLYDEPOT) {
				buildings[4]++;
			} else if (ri[i].type == RobotType.TANKFACTORY) {
				buildings[5]++;
			} else if (ri[i].type == RobotType.TECHNOLOGYINSTITUTE) {
				buildings[6]++;
			} else if (ri[i].type == RobotType.AEROSPACELAB) {
				buildings[7]++;
			} else if (ri[i].type == RobotType.TRAININGFIELD) {
				buildings[8]++;
			} else if (ri[i].type == RobotType.HANDWASHSTATION) {
				buildings[9]++;
			}
		}
		
//		MapLocation[] towers = rc.senseTowerLocations();
//		rc.broadcast(20, towers.length);
//		for(int i = 0; i < towers.length; i++){
//			rc.broadcast(21 + 10 * i, towers[i].x);
//			rc.broadcast(22 + 10 * i, towers[i].y);
//			rc.broadcast(23 + 10 * i, rc.senseNearbyRobots(towers[i], 9, myTeam).length);
//		}
		
	}
	
	public void getInitialInfo() throws GameActionException {
		RobotInfo[] ri = rc.senseNearbyRobots(rc.getLocation(), 100000000,
				rc.getTeam());
		units = new int[10];
		buildings = new int[10];
		for (int i = 0; i < ri.length; i++) {
			if (ri[i].type == RobotType.BEAVER) {
				units[1]++;
			} else if (ri[i].type == RobotType.MINER) {
				units[2]++;
			} else if (ri[i].type == RobotType.SOLDIER) {
				units[3]++;
			} else if (ri[i].type == RobotType.BASHER) {
				units[4]++;
			} else if (ri[i].type == RobotType.DRONE) {
				units[5]++;
			} else if (ri[i].type == RobotType.TANK) {
				units[6]++;
			} else if (ri[i].type == RobotType.LAUNCHER) {
				units[7]++;
			} else if (ri[i].type == RobotType.COMMANDER) {
				units[8]++;
			} else if (ri[i].type == RobotType.COMPUTER) {
				units[9]++;
			} else if (ri[i].type == RobotType.BARRACKS) {
				buildings[1]++;
			} else if (ri[i].type == RobotType.MINERFACTORY) {
				buildings[2]++;
			} else if (ri[i].type == RobotType.HELIPAD) {
				buildings[3]++;
			} else if (ri[i].type == RobotType.SUPPLYDEPOT) {
				buildings[4]++;
			} else if (ri[i].type == RobotType.TANKFACTORY) {
				buildings[5]++;
			} else if (ri[i].type == RobotType.TECHNOLOGYINSTITUTE) {
				buildings[6]++;
			} else if (ri[i].type == RobotType.AEROSPACELAB) {
				buildings[7]++;
			} else if (ri[i].type == RobotType.TRAININGFIELD) {
				buildings[8]++;
			} else if (ri[i].type == RobotType.HANDWASHSTATION) {
				buildings[9]++;
			}
		}
	}

	public void addToQueue(int num) throws GameActionException {
		int curr = rc.readBroadcast(0);
		int pow = 1;
		int length = Integer.toString(curr).length();
		if (curr == 0)
			length = 0;
		int queue;
		if (length > 8)
			return;
		else {
			queue = (int) (curr + Math.pow(10, length) * num);
			rc.broadcast(0, queue);
		}
	}
	
	public void addToFrontQueue(int num) throws GameActionException {
		int curr = rc.readBroadcast(0);
		int length = Integer.toString(curr).length();
		if (length > 8)
			return;
		else {
			rc.broadcast(0, curr * 10 + num);
		}
	}

	public void setUpQueueInfo() throws GameActionException {
		int queue = rc.readBroadcast(0);
		for (int i = 0; i < inQueue.length; i++) {
			inQueue[i] = 0;
		}
		while (queue > 0) {
			inQueue[queue % 10]++;
			queue /= 10;
		}
		 rc.setIndicatorString(0, "Queue: " + rc.readBroadcast(0));
		 rc.setIndicatorString(2, buildings[1] + " " + buildings[2] + " "
		 + buildings[3] + " " + buildings[4] + " " + buildings[5] + " "
		 + buildings[6] + " " + buildings[7] + " " + buildings[8] + " "
		 + buildings[9] + "       " + inQueue[1] + " " + inQueue[2]
		 + " " + inQueue[3] + " " + inQueue[4] + " " + inQueue[5] + " "
		 + inQueue[6] + " " + inQueue[7] + " " + inQueue[8] + " "
		 + inQueue[9] + "       " + units[1] + " " + units[2] + " "
		 + units[3] + " " + units[4] + " " + units[5] + " " + units[6]
		 + " " + units[7] + " " + units[8] + " " + units[9]);
	}


	

}

