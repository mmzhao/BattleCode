package launcherStratPlusSoldiers;

import battlecode.common.*;

public class Miner extends BaseBot {

	public MapLocation targetLoc;
	public Direction facing;

	public Miner(RobotController rc) {
		super(rc);
		targetLoc = null;
		facing = myHQ.directionTo(rc.getLocation());
	}

	public void execute() throws GameActionException {
		RobotInfo[] enemies = getEnemiesInAttackingRange(rc.getType());

        if (enemies.length > 0) {
        	
        	if(rc.isCoreReady()){
        		tryMove(enemies[0].location.directionTo(rc.getLocation()));
        	}
        	
            //attack!
//        	runAway();
        	
            if (rc.isWeaponReady()) {
                attackLeastHealthEnemy(enemies);
            }
        }
        
        
    	if (rc.isCoreReady()) {
    		MapLocation cur = rc.getLocation();
    		MapLocation enemy = enemiesNearby(rc.senseNearbyRobots(cur, 25, theirTeam));
			if(rc.readBroadcast(4000) == 1){
    	        int rallyX = rc.readBroadcast(1001);
    	        int rallyY = rc.readBroadcast(1002);
    	        MapLocation rallyPoint = new MapLocation(rallyX, rallyY);
    	        tryMove(rc.getLocation().directionTo(rallyPoint));
            }
			if(enemy != null){ //regular is by max direction distance, 2 is by squared distance
//				MapLocation safe = closestLocation(cur, rc.senseTowerLocations());
//				if(safe == null) safe = myHQ;
//				tryMove(cur.directionTo(safe));
				if(rc.isCoreReady()) tryMove(enemy.directionTo(cur));
			}
    		else if(rc.isCoreReady()){
	    		if(rc.senseOre(rc.getLocation()) >= 3){
		    		if(rc.canMine()){
		    			rc.broadcast(2500, (int) (rc.readBroadcast(2500) + (rc.senseOre(rc.getLocation()) / 4)));
		    			rc.mine();
		    		}
		    	}
	    		else{
	    			optimalMove();
	    		}
    		}
			
    	}
    	
    	transferSupplies();
    	
    	double supplies = rc.getSupplyLevel();
    	if(rc.readBroadcast(getUnit(RobotType.DRONE) + 10) > 0 && (supplies < 250 || !calledForSupply)){
    		calledForSupply = true;
    		supplyQueue(25);
    	}
    	else if(supplies >= 250){
    		calledForSupply = false;
    	}
    	
    	rc.yield();
    }
    	
    	public MapLocation enemiesNearby(RobotInfo[] enemies){
    		MapLocation cur = rc.getLocation();
    		for(RobotInfo e: enemies){
    			MapLocation loc = e.location;
				if(loc.distanceSquaredTo(cur) < e.type.attackRadiusSquared){
					return loc;
				}
				else if(e.type == RobotType.LAUNCHER){
					return loc;
				}
    		}
    		return null;
    	}
	
	public void optimalMove() throws GameActionException{
		Direction dir = facing;
		double maxOre = 5;
		int minBaseDist = rc.getLocation().add(dir).distanceSquaredTo(myHQ);
		if(rc.canMove(dir) && isSafe(rc.getLocation().add(dir))) maxOre = rc.senseOre(rc.getLocation().add(dir));
//		Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH_EAST, Direction.SOUTH_EAST, Direction.SOUTH_WEST, Direction.NORTH_WEST};
		Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
		for(Direction d: directions){
			if(rc.canMove(d) && isSafe(rc.getLocation().add(d))){
				double curr = rc.senseOre(rc.getLocation().add(d));
				int dist = rc.getLocation().add(d).distanceSquaredTo(myHQ);
				if(curr > maxOre){
					maxOre = curr;
					dir = d;
					minBaseDist = dist;
				}
				else if(curr == maxOre){
					if(dist < minBaseDist){
						minBaseDist = dist;
						dir = d;
					}
				}
			}
		}
		if(maxOre > 5 && rc.canMove(dir) && isSafe(rc.getLocation().add(dir))){
			previous = rc.getLocation();
			rc.move(dir);
			facing = dir;
		}
		else{
			if(rc.isCoreReady()) tryMove(rc.getLocation().directionTo(theirHQ));
		}
	}
	
	private void runAway() throws GameActionException {
		if(rc.isCoreReady()){
			RobotInfo[] enemies = rc.senseNearbyRobots(20, theirTeam);
			for(RobotInfo enemy: enemies){
				if(enemy.type.attackRadiusSquared > rc.getLocation().distanceSquaredTo(enemy.location)){
					tryMove(enemy.location.directionTo(rc.getLocation()));
				}
			}
		}
		
	}

	public boolean isSafe(MapLocation ml) throws GameActionException{
		if(rc.readBroadcast(4000) == 1) return true;
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
    		if(enemy.type == RobotType.LAUNCHER || ml.distanceSquaredTo(enemy.location) <= enemy.type.attackRadiusSquared){
    			return false;
    		}
    	}
    	return true;
    }
	
	public boolean runAway(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 5 && 
				(!rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8]) || 
						!isSafe(rc.getLocation().add(directions[(dirint+offsets[offsetIndex]+8)%8])))) {
			offsetIndex++;
		}
		if (offsetIndex < 5) {
			rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
			facing = directions[(dirint+offsets[offsetIndex]+8)%8];
			return true;
		}
		return false;
	}

}
