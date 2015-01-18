package testing;

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

//100 -- max n
//1n1 -- hit squad rally x
//1n2 -- hit squad rally y
//1n3 -- number of drones in hit squad n
//1n4 -- ID of target


//1000 -- number of soldiers defending base
//1001 -- rally x position
//1002 -- rally y position
//1003 -- no longer stay out of range


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
//		beginMapAnalyze();
//		map = new int[Math.abs(distx) + 1][Math.abs(disty) + 1];
//		blockValue = new int[3][3];
//		minValue = 0;
//		bestPath = "";
	}

	public void execute() throws GameActionException {
		
		//amount of every ally unit
		getInitialInfo();

		//find out about HQ defenders
		int numDefenders = 0;
		RobotInfo[] ri2 = rc.senseNearbyRobots(10, rc.getTeam());
		for (int i = 0; i < ri2.length; i++) {
			if (ri2[i].type == RobotType.SOLDIER) {
				numDefenders++;
			}
		}

		//attack if possible
		if (rc.isWeaponReady()) {
			attackLeastHealthEnemy(getEnemiesInAttackingRange(rc.getType()));
		}

		//always have 2 beavers up
		keepingSomeBeavers(2);
		
		//set up strat
//		soldierRushStrat();
		tankStrat();
		
		//set up rally point
		MapLocation rallyPoint = normalRushRally();
//		rallyPoint = theirHQ;
		
		
		//squads for drones
//		String squadCounts = "num squads: " + rc.readBroadcast(100) + "        ";
//		for(int i = 0; i < rc.readBroadcast(100); i++){
//			squadCounts += rc.readBroadcast(103 + i * 10) + " ";
//		}
		
//		setHitSquads();
		
//		rc.setIndicatorString(0, squadCounts);
		
		
		
		for (int i = 1; i < 10; i++) {
			rc.broadcast(i, buildings[i]);
			rc.broadcast(i + 10, units[i]);
		}
		rc.broadcast(1000, numDefenders);
		rc.broadcast(1001, rallyPoint.x);
		rc.broadcast(1002, rallyPoint.y);
		
//		System.out.println(rallyPoint.x + " " + rallyPoint.y);

		// reset Queue info
		setUpQueueInfo();

		
		rc.setIndicatorString(1, rc.readBroadcast(5000) + "");
		
		transferSupplies();



		rc.yield();
	}
	
	public void keepingSomeBeavers(int num) throws GameActionException{
		if (rc.isCoreReady()) {
			if (units[getUnit(RobotType.BEAVER)] < 1 || (units[getUnit(RobotType.BEAVER)] < 2 && buildings[getBuilding(RobotType.MINERFACTORY)] > 1)) {
				if (rc.getTeamOre() > 100) {
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
			if(t.type.attackRadiusSquared >= RobotType.DRONE.attackRadiusSquared){
				if(t.location.distanceSquaredTo(myHQ) > (int)(Math.pow(Clock.getRoundNum(), 2) / 200)){
					continue;
				}
			}
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
	
	public void tankStrat() throws GameActionException {

		if (buildings[getBuilding(RobotType.MINERFACTORY)]
				+ inQueue[getBuilding(RobotType.MINERFACTORY)] < 2) {
			addToQueue(getBuilding(RobotType.MINERFACTORY));
		}

		else if (buildings[getBuilding(RobotType.SUPPLYDEPOT)] > 0 && buildings[getBuilding(RobotType.BARRACKS)]
				+ inQueue[getBuilding(RobotType.BARRACKS)] < 1) {
			addToQueue(getBuilding(RobotType.BARRACKS));
		}

		else if (buildings[getBuilding(RobotType.BARRACKS)] > 0 && buildings[getBuilding(RobotType.TANKFACTORY)]
						+ inQueue[getBuilding(RobotType.TANKFACTORY)] < 5) {
			addToQueue(getBuilding(RobotType.TANKFACTORY));
		}
		
		if(buildings[getBuilding(RobotType.SUPPLYDEPOT)]
						+ inQueue[getBuilding(RobotType.SUPPLYDEPOT)] < (int) (Clock.getRoundNum() / 200) - 1) {
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
				+ inQueue[getBuilding(RobotType.MINERFACTORY)] < 3) {
			addToQueue(getBuilding(RobotType.MINERFACTORY));
		}

		if (buildings[getBuilding(RobotType.BARRACKS)]
						+ inQueue[getBuilding(RobotType.BARRACKS)] < (int) (Clock.getRoundNum() / 200) + 1) {
			addToQueue(getBuilding(RobotType.BARRACKS));
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

	public void beginMapAnalyze() {
		xmin = Math.min(myHQ.x, theirHQ.x);
		ymin = Math.min(myHQ.y, theirHQ.y);
		xmax = Math.max(myHQ.x, theirHQ.x);
		ymax = Math.max(myHQ.y, theirHQ.y);

		xpos = xmin;
		ypos = ymin;
		
//		distx = theirHQ.x - myHQ.x;
//		disty = theirHQ.y - myHQ.y;
//		stepx = distx / 3;
//		stepy = disty / 3;
	}

	public void analyzeMap() throws GameActionException { // 0 - normal, 1 -
															// void, 2 - tower

//		rc.setIndicatorString(2, "Bytecodes left: " + Clock.getBytecodesLeft());
		// distx = xmax - xmin;
		// disty = ymax - ymin;

//		while (ypos <= ymax) {
//			TerrainTile t = rc.senseTerrainTile(new MapLocation(xpos, ypos));
//			if (t == TerrainTile.NORMAL) {
//				RobotInfo atLoc = rc.senseRobotAtLocation(new MapLocation(xpos,
//						ypos));
//				if (atLoc != null && atLoc.type == RobotType.TOWER
//						&& atLoc.team == theirTeam) {
//					map[xpos - xmin][ypos - ymin] = 2;
//				} else
//					map[xpos - xmin][ypos - ymin] = 0;
//			} else if (t == TerrainTile.VOID) {
//				map[xpos - xmin][ypos - ymin] = 1;
//			}
//			xpos++;
//			if (xpos > xmax) {
//				xpos = xmin;
//				ypos++;
//			}
//		}
//		
//		for(int i = 0; i < 3; i ++){
//			for(int j = 0; j < 0; j++){
//				blockValue[i][j] = analyzeBlock(myHQ.x + i * stepx, myHQ.y + j * stepy, myHQ.x + (i + 1) * stepx, myHQ.y + (j + 1) * stepy);
//			}
//		}
//		
//		isFinished = true;

	}
	
	public String analyzePaths(){
//		while(index < paths.length){
//			
//			index++;
//		}
		return "";
	}
	
	public void analyzeBlock(int minx, int miny, int maxx, int maxy)
			throws GameActionException {
		// int value = 0;
//		int voids = 0;
//		int towers = 0;
//		if (minx > maxx) {
//			int temp = maxx;
//			maxx = minx;
//			minx = temp;
//		}
//		if (miny > maxy) {
//			int temp = maxy;
//			maxy = miny;
//			miny = temp;
//		}
//		for (int i = minx; i <= maxx; i++) {
//			for (int j = miny; j <= maxy; j++) {
//				if (i >= xmin && i <= xmax && j >= ymin && j <= ymax) {
//					if (map[i - xmin][j - ymin] == 1) {
//						voids++;
//					} else if (map[i - xmin][j - ymin] == 2) {
//						towers++;
//					}
//				} else {
//					RobotInfo atLoc = rc.senseRobotAtLocation(new MapLocation(
//							i, j));
//					if (atLoc.type == RobotType.TOWER
//							&& atLoc.team == theirTeam) {
//						towers++;
//					}
//				}
//			}
//		}
//		return (int) (Math.pow(towers - 1, 3) + voids * 2);
	}

	public MapLocation closestLocation(MapLocation currRally, MapLocation[] ml)
			throws GameActionException {
		int minDist = currRally.distanceSquaredTo(ml[0]);
		int minIndex = 0;
		for (int i = 1; i < ml.length; i++) {
			int currDist = currRally.distanceSquaredTo(ml[i]);
			if (currDist < minDist) {
				minDist = currDist;
				minIndex = i;
			}
		}
		return ml[minIndex];
	}

}

//
// public void analyzeMap(){
// while(ypos <= ymax){
// TerrainTile t = rc.senseTerrainTile(new MapLocation(xpos, ypos));
// if(t == TerrainTile.NORMAL){
// totalNormal++;
// totalProcessed++;
// }
// else if(t == TerrainTile.VOID){
// totalVoid++;
// totalProcessed++;
// }
// xpos++;
// if(xpos > xmax){
// xpos = xmin;
// ypos++;
// }
// }
// ratio = (double)(totalNormal/totalProcessed);
// isFinished = true;
// }


//public void makePaths() {
	// while(starts.size() > 0){
	// Path p = starts.remove(0);
	// if (p.finished) {
	// finished.add(p);
	// }
	// if (p.x < 5)
	// starts.add(p.add(0));
	// if (p.y < 5)
	// starts.add(p.add(2));
	// if (p.x < 5 && p.y < 5)
	// starts.add(p.add(1));
	// }
	// }

	// 3 types of steps -- 0-left/right,1-diag combo,2-up/down
	// public Path bestPath(int currx, int curry) throws GameActionException{
	// //value is bad
	// int min = Integer.MAX_VALUE;
	// String path = "";
	// boolean canAdd = false;
	// if(currx < distx){
	// canAdd = true;
	// Path up = bestPath(currx + stepx, curry);
	// int value = up.value + analyzeBlock(currx, curry - stepy/2, currx + stepx
	// - 1, curry + stepy/2);
	// if(value < min){
	// min = value;
	// }
	// path = "0" + up.path;
	// }
	// if(curry < disty){
	// canAdd = true;
	// Path right = bestPath(currx, curry + stepy);
	// int value = right.value + analyzeBlock(currx - stepx/2, curry, currx +
	// stepx/2, curry + stepy - 1);
	// if(value < min){
	// min = value;
	// }
	// path = "2" + right.path;
	// }
	// if(currx < distx && curry < disty){
	// canAdd = true;
	// Path diag = bestPath(currx + stepx, curry + stepy);
	// int value = diag.value + analyzeBlock(currx, curry, currx + stepx - 1,
	// curry + stepy - 1);
	// if(value < min){
	// min = value;
	// }
	// path = "1" + diag.path;
	// }
	// if(!canAdd){
	// return new Path();
	// }
	// return new Path(path, min);
	//
	// }

//int[][] map;
//int[][] blockValue;
//int stepx, stepy;
//int distx, disty;
//int minValue;
//String bestPath;
//int index = 0;


//String[] paths = { "111", "0211", "0121", "0112", "2011", "2101", "2110",
//		"1021", "1012", "1201", "1210", "1102", "1120", "00221", "00212",
//		"00122", "02021", "02012", "02201", "02210", "02102", "02120",
//		"01022", "01202", "01220", "20021", "20012", "20201", "20210",
//		"20102", "20120", "22001", "22010", "22100", "21002", "21020",
//		"21200", "10022", "10202", "10220", "12002", "12020", "12200",
//		"000222", "002022", "002202", "002220", "020022", "020202",
//		"020220", "022002", "022020", "022200", "200022", "200202",
//		"200220", "202002", "202020", "202200", "220002", "220020",
//		"220200", "222000" };