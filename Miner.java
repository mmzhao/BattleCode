package testing;

import battlecode.common.*;

//SO SKETCH MININGSPOT
public class Miner extends BaseBot {

	public MapLocation targetLoc;
	public Direction facing;
	private boolean attackTarget;
	public boolean frontliner;
	public int turnsSinceMine;
	public MapLocation miningSpot;

	public Miner(RobotController rc) {
		super(rc);
		targetLoc = null;
		facing = myHQ.directionTo(rc.getLocation());
		frontliner = false;
		turnsSinceMine = 0;
		attackTarget = false;
		miningSpot = null;
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
		            if(targetLoc.distanceSquaredTo(rc.getLocation()) <= 36){
//		            	frontliner = true;
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
    			MapLocation currLoc = rc.getLocation();
	    		if(rc.senseOre(currLoc) >= 4){
		    		if(rc.canMine()){
		    			rc.broadcast(2500, (int) (rc.readBroadcast(2500) + (rc.senseOre(currLoc) / 4)));
		    			rc.mine();
		    			turnsSinceMine = 0;
		    			double localValue = assessSpot(currLoc);
		    			if(localValue > rc.readBroadcast(1503)){
		    				rc.broadcast(1501, currLoc.x);
		    				rc.broadcast(1502, currLoc.y);
		    				rc.broadcast(1503, (int)(localValue));
		    			}
		    		}
		    	}
	    		else if(miningSpot != null){
    				tryMove(rc.getLocation().directionTo(miningSpot));
    			}
	    		else{
	    			optimalMove();
	    		}
    		}
    		
    		turnsSinceMine++;
    		if(turnsSinceMine > 10){
    			findGoodSpot(); //SKETCCCHCHHHHH
    		}
    		else if(turnsSinceMine > 100){
    			frontliner = true;
    		}
			
    	}
	}
	
	public void micro() throws GameActionException{
    	RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), 24, theirTeam);
    	if(enemies.length == 0) return;
    	boolean noAttackersInRange = true;
    	for(RobotInfo e: enemies){
    		if(e.type.attackRadiusSquared >= e.location.distanceSquaredTo(rc.getLocation())){
    			noAttackersInRange = false;
    		}
    	}
    	if(noAttackersInRange){ 
    		return;
    	}
    	int totalX = 0;
    	int totalY = 0;
    	for(RobotInfo e: enemies){
    		totalX += e.location.x;
    		totalY += e.location.y;
    	}
    	MapLocation enemyCenter = new MapLocation(totalX/enemies.length, totalY/enemies.length);
    	tryMove(enemyCenter.directionTo(rc.getLocation()));
    	
    }
	
	private double assessSpot(MapLocation ml){
		int value = 0;
		for(int i = -7; i < 8; i++){
			for(int j = -7; j < 8; j++){
				double ore = rc.senseOre(ml.add(i, j));
				if(ore >= 4){
					value += ore;
				}
			}
		}
		return value;
	}
	
	private void findGoodSpot() throws GameActionException {
//		int closest = Integer.MAX_VALUE;
//		for(int i = 0; i < rc.readBroadcast(1500); i++){
//			MapLocation newSpot = new MapLocation(rc.readBroadcast(1501 + 10 * i), rc.readBroadcast(1502 + 10 * i));
//			int dist = rc.getLocation().distanceSquaredTo(newSpot);
//			if(dist < closest){
//				closest = dist;
//				miningSpot = newSpot;
//			}
//		}
		miningSpot = new MapLocation(rc.readBroadcast(1501), rc.readBroadcast(1502));
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
