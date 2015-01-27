package launcherStrat;

import battlecode.common.*;

public class Miner extends BaseBot {

	public MapLocation targetLoc;
	public Direction facing;
	public int turnsSinceMined; 
	private double minOre;

	public Miner(RobotController rc) {
		super(rc);
		targetLoc = null;
		facing = myHQ.directionTo(rc.getLocation());
		turnsSinceMined = 0;
		minOre = 3;
	}

	public void execute() throws GameActionException {
		RobotInfo[] enemies = getEnemiesInAttackingRange(rc.getType());

        if (enemies.length > 0) {
        	
        	if (Clock.getRoundNum() <= 500) {
        		rc.broadcast(10000, 1);
        		rc.broadcast(10001, enemies[0].location.x);
        		rc.broadcast(10002, enemies[0].location.y); //enemies are scouting; broadcast their presence
        	}
        	
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
    		if(rc.readBroadcast(4000) == 1){
    	        int rallyX = rc.readBroadcast(1001);
    	        int rallyY = rc.readBroadcast(1002);
    	        MapLocation rallyPoint = new MapLocation(rallyX, rallyY);
    	        tryMove(rc.getLocation().directionTo(rallyPoint));
            }
    		else{
	    		if(rc.senseOre(rc.getLocation()) >= minOre){
		    		if(rc.canMine()){
		    			rc.broadcast(2500, (int) (rc.readBroadcast(2500) + (rc.senseOre(rc.getLocation()) / 4)));
		    			rc.mine();
		    		}
		    	}
	    		else{
	    			turnsSinceMined++;
	    			if (turnsSinceMined % 20 == 0) { //decrement the minimum amount we want to mine
	    				minOre -= .5;
	    			}
	    			optimalMove();
	    		}
    		}
			
    	}
    	
    	transferSupplies();
    	
    	double supplies = rc.getSupplyLevel();
    	if(rc.readBroadcast(getUnit(RobotType.DRONE) + 10) > 0 && (supplies < 250 || !calledForSupply)){
    		calledForSupply = true;
    		addToSupplyQueue();
    	}
    	else if(supplies >= 250){
    		calledForSupply = false;
    	}
    	
    	rc.yield();
    }
	
	public void optimalMove() throws GameActionException{
		Direction dir = facing;
		int minBaseDist = rc.getLocation().add(dir).distanceSquaredTo(myHQ);
		double minOre = this.minOre - .001;
		if(rc.canMove(dir) && isSafe(rc.getLocation().add(dir))) 
			minOre = rc.senseOre(rc.getLocation().add(dir));
//		Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH_EAST, Direction.SOUTH_EAST, Direction.SOUTH_WEST, Direction.NORTH_WEST};
		Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
		for(Direction d: directions){
			if(rc.canMove(d) && isSafe(rc.getLocation().add(d))){
				double curr = rc.senseOre(rc.getLocation().add(d));
				int dist = rc.getLocation().add(d).distanceSquaredTo(myHQ);
				if(curr > minOre){
					minOre = curr;
					dir = d;
					minBaseDist = dist;
				}
				else if(curr == minOre){
					if(dist < minBaseDist){
						minBaseDist = dist;
						dir = d;
					}
				}
			}
		}
		if(minOre > this.minOre && rc.canMove(dir) && isSafe(rc.getLocation().add(dir))){
			previous = rc.getLocation();
			rc.move(dir);
			facing = dir;
		}
		else{
			tryMove(rc.getLocation().directionTo(theirHQ));
		}
	}
	
	public void tryMove(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,2,3,4,5,6,7};
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
			facing = directions[(dirint+offsets[offsetIndex]+8)%8];
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
