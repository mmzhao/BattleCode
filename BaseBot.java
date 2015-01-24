package testing;

import java.util.Random;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class BaseBot {
	
    protected RobotController rc;
    protected MapLocation myHQ, theirHQ;
    protected Team myTeam, theirTeam;
    static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	static Random rand;
	int[] offsets = {0,1,2,3,4,5,6,7};

	public MapLocation previous;
	
	static int[] inQueue;

    public BaseBot(RobotController rc) {
        this.rc = rc;
        this.myTeam = rc.getTeam();
        this.myHQ = rc.senseHQLocation();
        this.theirTeam = this.myTeam.opponent();
        this.theirHQ = rc.senseEnemyHQLocation();
        rand = new Random(rc.getID());
        previous = new MapLocation(0, 0);
    }
    
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
    
    public RobotType getUnit(int type){
    	if(type == 1) return RobotType.BEAVER;
    	else if(type == 2) return RobotType.MINER;
    	else if(type == 3) return RobotType.SOLDIER;
    	else if(type == 4) return RobotType.BASHER;
    	else if(type == 5) return RobotType.DRONE;
    	else if(type == 6) return RobotType.TANK;
    	else if(type == 7) return RobotType.LAUNCHER;
    	else if(type == 8) return RobotType.COMMANDER;
    	else if(type == 9) return RobotType.COMPUTER;
    	return null;
    }
    
    public int getUnit(RobotType type){
    	if(type == RobotType.BEAVER) return 1;
    	else if(type == RobotType.MINER) return 2;
    	else if(type == RobotType.SOLDIER) return 3;
    	else if(type == RobotType.BASHER) return 4;
    	else if(type == RobotType.DRONE) return 5;
    	else if(type == RobotType.TANK) return 6;
    	else if(type == RobotType.LAUNCHER) return 7;
    	else if(type == RobotType.COMMANDER) return 8;
    	else if(type == RobotType.COMPUTER) return 9;
    	return 0;
    }
    
    public RobotType getBuilding(int type){
    	if(type == 1) return RobotType.BARRACKS;
    	else if(type == 2) return RobotType.MINERFACTORY;
    	else if(type == 3) return RobotType.HELIPAD;
    	else if(type == 4) return RobotType.SUPPLYDEPOT;
    	else if(type == 5) return RobotType.TANKFACTORY;
    	else if(type == 6) return RobotType.TECHNOLOGYINSTITUTE;
    	else if(type == 7) return RobotType.AEROSPACELAB;
    	else if(type == 8) return RobotType.TRAININGFIELD;
    	else if(type == 9) return RobotType.HANDWASHSTATION;
    	return null;
    }
    
    public int getBuilding(RobotType type){
    	if(type == RobotType.BARRACKS) return 1;
    	else if(type == RobotType.MINERFACTORY) return 2;
    	else if(type == RobotType.HELIPAD) return 3;
    	else if(type == RobotType.SUPPLYDEPOT) return 4;
    	else if(type == RobotType.TANKFACTORY) return 5;
    	else if(type == RobotType.TECHNOLOGYINSTITUTE) return 6;
    	else if(type == RobotType.AEROSPACELAB) return 7;
    	else if(type == RobotType.TRAININGFIELD) return 8;
    	else if(type == RobotType.HANDWASHSTATION) return 9;
    	return 0;
    }
    
    public void attackForValue(RobotInfo[] enemies) throws GameActionException {
        if (enemies.length == 0) {
            return;
        }
        
        int maxValue = -1;
        MapLocation toAttack = null;
        for (RobotInfo ri : enemies) {
        	int currValue = -1;
        	if(ri.type == RobotType.TOWER || ri.type == RobotType.HQ){
        		currValue = 0;
        	}
        	else if(getBuilding(ri.type) != 0){
        		currValue = 1;
        	}
        	else{
	        	if(ri.type == RobotType.BEAVER){
	        		currValue = 2;
	        	}
	        	else if(ri.type == RobotType.MINER){
	        		currValue = 3;
	        	}
	        	else if(ri.type == RobotType.SOLDIER){
	        		currValue = 4;
	        	}
	        	else if(ri.type == RobotType.BASHER){
	        		currValue = 5;
	        	}
	        	else if(ri.type == RobotType.DRONE){
	        		currValue = 6;
	        	}
	        	else if(ri.type == RobotType.TANK){
	        		currValue = 7;
	        	}
	        	else if(ri.type == RobotType.LAUNCHER){
	        		currValue = 8;
	        	}
	        	else if(ri.type == RobotType.COMMANDER){
	        		currValue = 9;
	        	}
	        	else if(ri.type == RobotType.COMPUTER){
	        		currValue = 1;
	        	}
	        	else if(ri.type == RobotType.MISSILE){
	        		currValue = 5;
	        	}
	        	if(ri.type != RobotType.MISSILE){
	        		if(ri.health < rc.getType().attackPower){
	        			currValue += 10;
	        		}
	        	}
	        	else{
	        		if(ri.health == 1){
	        			currValue += 10;
	        		}
	        	}
        	}
	        if(currValue > maxValue){
	        	maxValue = currValue;
	        	toAttack = ri.location;
	        }
        }

        rc.attackLocation(toAttack);
    }
    
    public void micro() throws GameActionException{
    	MapLocation center = new MapLocation(rc.readBroadcast(2004), rc.readBroadcast(2005));
    	RobotInfo[] enemies = rc.senseNearbyRobots(center, 100, theirTeam);
    	RobotInfo[] allies = rc.senseNearbyRobots(center, 25, myTeam);
    	int enemyStrength = 0;
    	for(RobotInfo e: enemies){
    		if(e.type == RobotType.TOWER || e.type == RobotType.HQ || e.location.distanceSquaredTo(rc.getLocation()) > e.type.attackRadiusSquared){
    			continue;
    		}
    		if(e.weaponDelay < 1){
    			enemyStrength += e.health * e.type.attackPower;
    		}
    		else{
    			enemyStrength += e.health;
    		}
    	}
    	int allyStrength = 0;
    	for(RobotInfo a: allies){
    		if(a.type == RobotType.TOWER || a.type == RobotType.HQ){
    			continue;
    		}
    		if(a.weaponDelay < 1){
    			allyStrength += a.health * a.type.attackPower;
    		}
    		else{
    			allyStrength += a.health;
    		}
    	}
//    	System.out.println(enemyStrength + "    " + allyStrength);
    	if(enemyStrength > allyStrength){
    		if(rc.senseTowerLocations().length > 0){
    			tryMove(rc.getLocation().directionTo(closestLocation(center, rc.senseTowerLocations())));
    		}
    		else{
    			tryMove(rc.getLocation().directionTo(myHQ));
    		}
    	}
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
    
    public void transferSupplies() throws GameActionException{
    	
    	double minSupply = rc.getSupplyLevel();
    	MapLocation toTransfer = null;
    	double transferAmount = 0;
    	
    	RobotInfo[] transferable = rc.senseNearbyRobots(rc.getLocation(), GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, rc.getTeam());
    	for(RobotInfo ri:transferable){
    		if(ri.supplyLevel < minSupply){
    			minSupply = ri.supplyLevel;
    			toTransfer = ri.location;
    			transferAmount = (rc.getSupplyLevel() - ri.supplyLevel) / 2;
    			if(getBuilding(rc.getType()) != 0){
    				transferAmount = rc.getSupplyLevel();
    			}
    		}
    	}
    	if(toTransfer != null && rc.senseRobotAtLocation(toTransfer) != null){
    		rc.transferSupplies((int)(transferAmount), toTransfer);
    	}
    	
    	//transfer if close to death
    	if(rc.getHealth() < 10){
    		double minSupplyDivHealth = rc.getSupplyLevel()/rc.getHealth();
        	MapLocation toTransfer2 = null;
    		for(RobotInfo ri:transferable){
        		if(ri.supplyLevel/ri.health < minSupplyDivHealth){
        			minSupplyDivHealth = ri.supplyLevel/ri.health;
        			toTransfer = ri.location;
        		}
        	}
        	if(toTransfer2 != null && rc.senseRobotAtLocation(toTransfer2) != null){
        		rc.transferSupplies((int)(rc.getSupplyLevel() - rc.getType().supplyUpkeep * 2), toTransfer2);
        	}
    	}
    }
    
    public void tryMove(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 8 && 
				(!rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8]) || 
						!isSafe(rc.getLocation().add(directions[(dirint+offsets[offsetIndex]+8)%8])) ||
						rc.getLocation().add(directions[(dirint+offsets[offsetIndex]+8)%8]).equals(previous))) {
			offsetIndex++;
		}
		if (offsetIndex < 8) {
			previous = rc.getLocation();
			rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
		}
	}
    
    public boolean isSafe(MapLocation ml) throws GameActionException{
    	MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
    	for(MapLocation m: enemyTowers){
    		if(ml.distanceSquaredTo(m) <= RobotType.TOWER.attackRadiusSquared){
    			return false;
    		}
    	}
    	if(ml.distanceSquaredTo(theirHQ) <= RobotType.HQ.attackRadiusSquared){
    		return false;
    	}
    	RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), 24, theirTeam);
    	for(RobotInfo enemy:enemies){
    		if(ml.distanceSquaredTo(enemy.location) <= enemy.type.attackRadiusSquared){
    			return false;
    		}
    	}
    	return true;
    }

	// This method will attempt to spawn in the given direction (or as close to it as possible)
	public void trySpawn(Direction d, RobotType type) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2,3,-3,4};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 8 && !rc.canSpawn(directions[(dirint+offsets[offsetIndex]+8)%8], type)) {
			offsetIndex++;
		}
		if (offsetIndex < 8) {
			rc.spawn(directions[(dirint+offsets[offsetIndex]+8)%8], type);
		}
	}

	// This method will attempt to build in the given direction (or as close to it as possible)
	

	public int directionToInt(Direction d) {
		switch(d) {
			case NORTH:
				return 0;
			case NORTH_EAST:
				return 1;
			case EAST:
				return 2;
			case SOUTH_EAST:
				return 3;
			case SOUTH:
				return 4;
			case SOUTH_WEST:
				return 5;
			case WEST:
				return 6;
			case NORTH_WEST:
				return 7;
			default:
				return -1;
		}
	}

    public Direction[] getDirectionsToward(MapLocation dest) {
        Direction toDest = rc.getLocation().directionTo(dest);
        Direction[] dirs = {toDest,
	    		toDest.rotateLeft(), toDest.rotateRight(),
			toDest.rotateLeft().rotateLeft(), toDest.rotateRight().rotateRight()};

        return dirs;
    }

    public Direction getMoveDir(MapLocation dest) {
        Direction[] dirs = getDirectionsToward(dest);
        for (Direction d : dirs) {
            if (rc.canMove(d)) {
                return d;
            }
        }
        return null;
    }

    public Direction getSpawnDirection(RobotType type) {
//        Direction[] dirs = getDirectionsToward(this.theirHQ);
        Direction[] dirs = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST,};
        int offset = (int) (Math.random() * dirs.length);
        for (int i = 0; i < dirs.length; i++) {
            if (rc.canSpawn(dirs[(i + offset) % dirs.length], type)) {
                return dirs[(i + offset) % dirs.length];
            }
        }
        return null;
    }

    public Direction getBuildDirection(RobotType type) {
        Direction[] dirs = getDirectionsToward(this.theirHQ);
        for (Direction d : dirs) {
            if (rc.canBuild(d, type)) {
                return d;
            }
        }
        return null;
    }

    public RobotInfo[] getAllies() {
        RobotInfo[] allies = rc.senseNearbyRobots(Integer.MAX_VALUE, myTeam);
        return allies;
    }

    public RobotInfo[] getEnemiesInAttackingRange(RobotType type) {
        RobotInfo[] enemies = rc.senseNearbyRobots(type.attackRadiusSquared, theirTeam);
        return enemies;
    }

    public void attackLeastHealthEnemy(RobotInfo[] enemies) throws GameActionException {
        if (enemies.length == 0) {
            return;
        }

        double minEnergon = Double.MAX_VALUE;
        MapLocation toAttack = null;
        for (RobotInfo info : enemies) {
            if (info.health < minEnergon) {
                toAttack = info.location;
                minEnergon = info.health;
            }
        }

        rc.attackLocation(toAttack);
    }

    public MapLocation findMiningLocation() {
    	MapLocation check;
    	boolean[][] seeable = {{true, true, true, true, true}, {true, true, true, true, true}, 
    			{true, true, true, true, true}, {true, true, true, true, false}, {true, true, true, false, false}};
    	int sightRange = (int)Math.floor(Math.sqrt(24));
    	double maxOre = 0;
    	MapLocation result = null;
    	for (int i = sightRange; i>0; i--) {
    		for (int j = sightRange; j>0; j--) {
    			if (seeable[i][j]) {
    				check = new MapLocation(rc.getLocation().x + i, rc.getLocation().y+j);
    				if (rc.senseOre(check)>maxOre) {
    					result = check;
    					maxOre = rc.senseOre(check);
    				}
    				check = new MapLocation(rc.getLocation().x - i, rc.getLocation().y - j);
    				if (rc.senseOre(check)>maxOre) {
    					result = check;
    					maxOre = rc.senseOre(check);
    				}
    			}
    				
    		}
    	}
    	return result;
    }
    
    public void go() throws GameActionException {
        execute();
    }
    
    public void beginningOfTurn() throws GameActionException {
    	this.theirHQ = rc.senseEnemyHQLocation();
    }
    public void execute() throws GameActionException {
    	beginningOfTurn();
        rc.yield();
    }
}