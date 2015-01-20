package testing;

import battlecode.common.*;

public class Miner extends BaseBot {

	public MapLocation targetLoc;
	public Direction facing;
	private boolean attackTarget;
	public boolean frontliner;
	public int turnsSinceMine;

	public Miner(RobotController rc) {
		super(rc);
		targetLoc = null;
		facing = myHQ.directionTo(rc.getLocation());
		frontliner = false;
		turnsSinceMine = 0;
		attackTarget = false;
	}

	public void execute() throws GameActionException {
		if(rc.readBroadcast(4000) == 1){
			frontliner = true;
		}
		
		if(frontliner){
			frontliner();
		}
		else{
			regularMiner();
		}
		
    	
    	transferSupplies();
    	
    	rc.yield();
    }
	
	public void frontliner() throws GameActionException{
		RobotInfo[] enemies = getEnemiesInAttackingRange(rc.getType());
        
        //micro
        if(rc.isCoreReady()){
        	micro();
        }
        
        if (enemies.length > 0) {
            //attack!
            if (rc.isWeaponReady()) {
                attackForValue(enemies);
            }
        }
        

        
        if (rc.isCoreReady()) {
    		if(rc.readBroadcast(2000) == 1){
    			micro();
    			if(rc.isCoreReady()){
	    			attackTarget = true;
	    			int rallyX = rc.readBroadcast(2001);
		            int rallyY = rc.readBroadcast(2002);
		            targetLoc = new MapLocation(rallyX, rallyY);
		            tryMove(rc.getLocation().directionTo(targetLoc));
    			}
    		}
    		else{
    			micro();
    			if(rc.isCoreReady()){
	    			attackTarget = false;
		            int rallyX = rc.readBroadcast(1001);
		            int rallyY = rc.readBroadcast(1002);
		            targetLoc = new MapLocation(rallyX, rallyY);
	//	            rc.setIndicatorString(0, rallyX + " " + rallyY);
	//	            rc.setIndicatorString(0, rallyX + " " + rallyY);
		            tryMove(rc.getLocation().directionTo(targetLoc));
    			}
    		}
        }
        if(rc.senseOre(rc.getLocation()) >= 10){
        	frontliner = false;
        }
	}
	
	//PROBABLY CHANGE THRESHOLD FOR BECOMING FRONTLINER
	public void regularMiner() throws GameActionException{
		RobotInfo[] enemies = getEnemiesInAttackingRange(rc.getType());

        if (enemies.length > 0) {
            //attack!
            if (rc.isWeaponReady()) {
                attackForValue(enemies);
            }
        }
        
    	if (rc.isCoreReady()) {
    		if(rc.readBroadcast(2000) == 1){
    			if(rc.isCoreReady()){
	    			int rallyX = rc.readBroadcast(2001);
		            int rallyY = rc.readBroadcast(2002);
		            targetLoc = new MapLocation(rallyX, rallyY);
//		            System.out.println(targetLoc.distanceSquaredTo(rc.getLocation()));
		            if(targetLoc.distanceSquaredTo(rc.getLocation()) <= 25){
		            	tryMove(rc.getLocation().directionTo(targetLoc));
		            }
		            //for running away
		    		else{
			    		RobotInfo[] enemies2 = rc.senseNearbyRobots(rc.getLocation(), 24, theirTeam);
				        for(int i = 0; i < enemies2.length; i++){
				        	if(enemies2[i].location.distanceSquaredTo(rc.getLocation()) < enemies2[i].type.attackRadiusSquared){
				        		if(runAway(enemies2[i].location.directionTo(rc.getLocation()))){
				        			break;
				        		}
				        	}
				        }
		    		}
    			}
    		}
    		else if(rc.readBroadcast(4000) == 1){
    	        int rallyX = rc.readBroadcast(1001);
    	        int rallyY = rc.readBroadcast(1002);
    	        targetLoc = new MapLocation(rallyX, rallyY);
    	        tryMove(rc.getLocation().directionTo(targetLoc));
            }
    		if(rc.isCoreReady()){
	    		if(rc.senseOre(rc.getLocation()) >= 4){
		    		if(rc.canMine()){
		    			rc.broadcast(2500, (int) (rc.readBroadcast(2500) + (rc.senseOre(rc.getLocation()) / 4)));
		    			rc.mine();
		    			turnsSinceMine = 0;
		    		}
		    	}
	    		else{
	    			optimalMove();
	    		}
    		}
    		
    		turnsSinceMine++;
    		if(turnsSinceMine > 50){
    			frontliner = true;
    		}
			
    	}
	}
	
	public void optimalMove() throws GameActionException{
		Direction dir = facing;
		double maxOre = 4;
		if(rc.canMove(dir) && isSafe(rc.getLocation().add(dir))) maxOre = rc.senseOre(rc.getLocation().add(dir));
		if(rc.getTeam() == Team.A){
			Direction[] dirs = {Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH_WEST, Direction.NORTH_WEST, Direction.NORTH_EAST, Direction.SOUTH_EAST};
//			Direction[] dirs = {Direction.SOUTH_WEST, Direction.NORTH_WEST, Direction.NORTH_EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};
			for(Direction d: dirs){
				if(rc.canMove(d) && isSafe(rc.getLocation().add(d))){
					double curr = rc.senseOre(rc.getLocation().add(d));
					if(curr > maxOre){
						maxOre = curr;
						dir = d;
					}
				}
			}
		}
		else{
			Direction[] dirs = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH_EAST, Direction.SOUTH_EAST, Direction.SOUTH_WEST, Direction.NORTH_WEST};
//			Direction[] dirs = {Direction.NORTH_EAST, Direction.SOUTH_EAST, Direction.SOUTH_WEST, Direction.NORTH_WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
			for(Direction d: dirs){
				if(rc.canMove(d) && isSafe(rc.getLocation().add(d))){
					double curr = rc.senseOre(rc.getLocation().add(d));
					if(curr > maxOre){
						maxOre = curr;
						dir = d;
					}
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
	
//	private void runAway() throws GameActionException {
//		if(rc.isCoreReady()){
//			RobotInfo[] enemies = rc.senseNearbyRobots(20, theirTeam);
//			for(RobotInfo enemy: enemies){
//				if(enemy.type.attackRadiusSquared > rc.getLocation().distanceSquaredTo(enemy.location)){
//					tryMove(enemy.location.directionTo(rc.getLocation()));
//				}
//			}
//		}
//		
//	}

	public boolean isSafe(MapLocation ml) throws GameActionException{
    	MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
    	for(MapLocation m: enemyTowers){
    		if(targetLoc != null && targetLoc.x == m.x && targetLoc.y == m.y){
    			continue;
    		}
    		if(ml.distanceSquaredTo(m) <= RobotType.TOWER.attackRadiusSquared){
    			return false;
    		}
    	}
    	int HQRadiusAdd = 0;
    	if(enemyTowers.length >= 2){
    		HQRadiusAdd = 11;
    	}
    	if(ml.distanceSquaredTo(theirHQ) <= RobotType.HQ.attackRadiusSquared + HQRadiusAdd){
    		if(targetLoc == null || targetLoc.x != theirHQ.x || targetLoc.y != theirHQ.y){
        		return false;
    		}
    	}
    	if(attackTarget) return true;
    	if(rc.readBroadcast(4000) == 1) return true;
    	RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), 24, theirTeam);
    	for(RobotInfo enemy:enemies){
    		if(enemy.type == RobotType.LAUNCHER || ml.distanceSquaredTo(enemy.location) <= enemy.type.attackRadiusSquared){
    			return false;
    		}
    	}
    	return true;
    }

}
