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

    public BaseBot(RobotController rc) {
        this.rc = rc;
        this.myTeam = rc.getTeam();
        this.myHQ = rc.senseHQLocation();
        this.theirTeam = this.myTeam.opponent();
        this.theirHQ = rc.senseEnemyHQLocation();
        rand = new Random(rc.getID());
    }
    
    public void transferSupplies() throws GameActionException{
    	rc.setIndicatorString(0, GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED + "");
//    	int HQDistance = rc.getLocation().distanceSquaredTo(theirHQ);
    	
    	double minSupply = rc.getSupplyLevel();
    	MapLocation toTransfer = null;
    	double transferAmount = 0;
    	
    	RobotInfo[] transferable = rc.senseNearbyRobots(rc.getLocation(), GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, rc.getTeam());
    	for(RobotInfo ri:transferable){
    		if(ri.supplyLevel < minSupply){
    			minSupply = ri.supplyLevel;
    			toTransfer = ri.location;
    			transferAmount = (rc.getSupplyLevel() - ri.supplyLevel) / 2;
    		}
//    		if(ri.location.distanceSquaredTo(theirHQ) > HQDistance || ri.type == RobotType.BEAVER || Clock.getRoundNum() < 1000){
//    			if(ri.supplyLevel < ri.type.supplyUpkeep){
//    				rc.transferSupplies((int)(ri.type.supplyUpkeep - ri.supplyLevel + 1), ri.location);
//    			}
//    		}
//    		else if(ri.supplyLevel < rc.getSupplyLevel()){
//    			rc.transferSupplies((int)((rc.getSupplyLevel() - ri.supplyLevel)/2), ri.location);
//    		}
    		
    	}
    	if(toTransfer != null){
    		rc.transferSupplies((int)(transferAmount), toTransfer);
    	}
    }
    
    public void tryMove(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 5 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 5) {
			rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
		}
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
	public void tryBuild(Direction d, RobotType type) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2,3,-3,4};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 8 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 8) {
			rc.build(directions[(dirint+offsets[offsetIndex]+8)%8], type);
		}
	}

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
        Direction[] dirs = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
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

    public RobotInfo[] getEnemiesInAttackingRange() {
        RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.SOLDIER.attackRadiusSquared, theirTeam);
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