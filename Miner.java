package testing;

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
            //attack!
//        	runAway();
        	
            if (rc.isWeaponReady()) {
                attackLeastHealthEnemy(enemies);
            }
        }
    	if (rc.isCoreReady()) {
    		if(rc.senseOre(rc.getLocation()) >= 5){
	    		if(rc.canMine()){
	    			rc.mine();
	    		}
	    	}
    		else{
    			optimalMove();
    		}
			
    	}
    	
    	transferSupplies();
    	
    	rc.yield();
    }
	
	public void optimalMove() throws GameActionException{
		Direction dir = facing;
		double maxOre = 5;
		if(rc.canMove(dir) && isSafe(rc.getLocation().add(dir))) maxOre = rc.senseOre(rc.getLocation().add(dir));
		Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH_EAST, Direction.SOUTH_EAST, Direction.SOUTH_WEST, Direction.NORTH_WEST};
		for(Direction d: directions){
			if(rc.canMove(d) && isSafe(rc.getLocation().add(d))){
				double curr = rc.senseOre(rc.getLocation().add(d));
				if(curr > maxOre){
					maxOre = curr;
					dir = d;
				}
			}
		}
		if(rc.canMove(dir) && isSafe(rc.getLocation().add(dir))){
			rc.move(dir);
			facing = dir;
		}
		else{
			tryMove(rc.getLocation().directionTo(theirHQ));
		}
	}
	
	public void tryMove(Direction d) throws GameActionException {
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

}
