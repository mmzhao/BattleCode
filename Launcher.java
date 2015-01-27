package launcherSoldierStrat;

import battlecode.common.*;

public class Launcher extends BaseBot {
	
	private boolean attackTarget;
	MapLocation targetLoc;
	private int missileTargetIndex;
//	int[] offsets = {0,1,-1,2,3,4};
	
	public Launcher(RobotController rc) throws GameActionException {
        super(rc);
        targetLoc = null;
        missileTargetIndex = rc.readBroadcast(10000);
        rc.broadcast(10000, missileTargetIndex + 1);
        if(rc.getID() % 2 == 0){
	        for(int i = 1; i < offsets.length; i++){
				offsets[i] *= -1;
			}
        }
    }
	
	public void execute() throws GameActionException {
		rc.broadcast(10003 + 10 * missileTargetIndex, Clock.getRoundNum());
		int numMissiles = rc.getMissileCount();
		MapLocation cur = rc.getLocation();
		RobotInfo[] enemies = rc.senseNearbyRobots(cur, 72, theirTeam);
		
		MapLocation targetLoc = closestHitable(enemies);
		if(numMissiles > 0 && targetLoc != null && atFrontLine(cur.directionTo(targetLoc))){
			MapLocation launchLoc = launchMissile(cur, targetLoc);
			if(launchLoc != null) setTarget(launchLoc, targetLoc);
		}
		else if(targetLoc == null){
			targetLoc = closestHitable(rc.senseEnemyTowerLocations());
			if(numMissiles > 0 && targetLoc != null && atFrontLine(cur.directionTo(targetLoc))){
				MapLocation launchLoc = launchMissile(cur, targetLoc);
				if(launchLoc != null) setTarget(launchLoc, targetLoc);
			}
		}
		if(rc.isCoreReady()){
			MapLocation enemy = enemiesNearby2(enemies);
			if(enemy != null){ //regular is by max direction distance, 2 is by squared distance
				MapLocation safe = closestLocation(cur, rc.senseTowerLocations());
				if(safe == null) safe = myHQ;
				tryMove(cur.directionTo(safe));
//				tryMove(enemy.directionTo(cur));
			}
			if(rc.isCoreReady()){
//				if(!enemiesToHit(enemies)){
				if(targetLoc == null){
					MapLocation target = closestLocation(cur, rc.senseEnemyTowerLocations());
					if(target == null) target = theirHQ;
					tryMove(cur.directionTo(target));
				}
			}
		}
		
		
		transferSupplies();
		
		double supplies = rc.getSupplyLevel();
    	if(supplies < 500 || !calledForSupply){
    		calledForSupply = true;
    		addToSupplyQueueFront();
    	}
    	else if(supplies >= 500){
    		calledForSupply = false;
    	}
		
		rc.yield();
	}
	
	
	public boolean atFrontLine(Direction dir) throws GameActionException{
		RobotInfo oneForward = rc.senseRobotAtLocation(rc.getLocation().add(dir));
		if(oneForward != null && oneForward.type == RobotType.LAUNCHER) return false;
		else{
			RobotInfo twoForward = rc.senseRobotAtLocation(rc.getLocation().add(dir, 2));
			if(twoForward != null && twoForward.type == RobotType.LAUNCHER) return false;
		}
		return true;
	}
	
	public void setTarget(MapLocation missile, MapLocation ml) throws GameActionException{
//		System.out.println(ml.x + " " + ml.y);
//			rc.broadcast(10001 + 10 * missileTargetIndex, ml.x);
//			rc.broadcast(10002 + 10 * missileTargetIndex, ml.y);
//			rc.broadcast(10000, missileTargetIndex + 1);
		int count = rc.readBroadcast(20000);
		rc.broadcast(20001 + count * 10, ml.x);
		rc.broadcast(20002 + count * 10, ml.y);
		rc.broadcast(20003 + count * 10, missile.x);
		rc.broadcast(20004 + count * 10, missile.y);
		rc.broadcast(20000, count + 1);
		
	}
	
	public MapLocation enemiesNearby(RobotInfo[] enemies){
		MapLocation cur = rc.getLocation();
		int count = 0;
		int xtotal = 0;
		int ytotal = 0;
		for(RobotInfo e: enemies){
			MapLocation loc = e.location;
			if(Math.abs(loc.x - cur.x) <= 4 || Math.abs(loc.y - cur.y) <= 4){
				xtotal += loc.x;
				ytotal += loc.y;
				count++;
			}
		}
		if(count != 0){
			return new MapLocation((int)(xtotal / count), (int)(ytotal / count));
		}
		return null;
	}
	
	public MapLocation enemiesNearby2(RobotInfo[] enemies){
		MapLocation cur = rc.getLocation();
		for(RobotInfo e: enemies){
			MapLocation loc = e.location;
			if(loc.distanceSquaredTo(cur) < 25){
				return loc;
			}
		}
		return null;
	}
	
	public boolean enemiesToHit(RobotInfo[] enemies){
		MapLocation cur = rc.getLocation();
		for(RobotInfo e: enemies){
			MapLocation loc = e.location;
			if(Math.abs(loc.x - cur.x) <= 6 || Math.abs(loc.y - cur.y) <= 6){
				return true;
			}
		}
		return false;
	}
	
	public MapLocation canMoveHit(RobotInfo ri){
		MapLocation curr = rc.getLocation();
		int xdif = Math.abs(ri.location.x - curr.x);
		int ydif = Math.abs(ri.location.y - curr.y);
		if(xdif != 0) xdif--;
		if(ydif != 0) ydif--;
		if(ri.type.attackRadiusSquared >= xdif * xdif + ydif * ydif){
			return ri.location;
		}
		return null;
	}
	
	public MapLocation closestHitable(RobotInfo[] enemies){
		MapLocation curr = rc.getLocation();
		int index = -1;
		int dist1 = 8;
		int dist2 = 8;
		for(int i = 0; i < enemies.length; i++){
			MapLocation loc = enemies[i].location;
			int smaller = Math.min(Math.abs(loc.x - curr.x), Math.abs(loc.y - curr.y));
			int larger = Math.max(Math.abs(loc.x - curr.x), Math.abs(loc.y - curr.y));
			if(larger > 6) continue;
			if(smaller < dist1){
				dist1 = smaller;
				dist2 = larger;
				index = i;
			}
			else if(smaller == dist1){
				if(larger < dist2){
					dist2 = larger;
					index = i;
				}
			}
		}
		if(index != -1)
			return enemies[index].location;
		return null;
	}
	
	public MapLocation closestHitable(MapLocation[] enemies){
		MapLocation curr = rc.getLocation();
		int index = -1;
		int dist1 = 8;
		int dist2 = 8;
		for(int i = 0; i < enemies.length; i++){
			MapLocation loc = enemies[i];
			int smaller = Math.min(Math.abs(loc.x - curr.x), Math.abs(loc.y - curr.y));
			int larger = Math.max(Math.abs(loc.x - curr.x), Math.abs(loc.y - curr.y));
			if(larger > 6) continue;
			if(smaller < dist1){
				dist1 = smaller;
				dist2 = larger;
				index = i;
			}
			else if(smaller == dist1){
				if(larger < dist2){
					dist2 = larger;
					index = i;
				}
			}
		}
		if(index != -1)
			return enemies[index];
		return null;
	}
	
	public void executeOld() throws GameActionException {
		int numMissiles = rc.getMissileCount();
		MapLocation cur = rc.getLocation();
		RobotInfo[] enemies = rc.senseNearbyRobots(cur, 36, theirTeam);
		MapLocation average = average(enemies);
		
		if (rc.isWeaponReady() && numMissiles > 0) { //if we can shoot and there are enemies, shoot!
			if (enemies.length > 0 && average != null) {
				launchMissile(cur, average);
				if (numMissiles == 0)
					targetLoc = cur.subtract(cur.directionTo(average)); //if we've shot all our missiles, retreat
			}
		}
		
		if(rc.isCoreReady()){
			micro();
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
		
		transferSupplies();
		
		double supplies = rc.getSupplyLevel();
    	if(supplies < 500 || !calledForSupply){
    		calledForSupply = true;
    		addToSupplyQueueFront();
    	}
    	else if(supplies >= 500){
    		calledForSupply = false;
    	}
		
		rc.yield();
	}
	
	public void micro() throws GameActionException{
		RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), 24, theirTeam);
    	int length = enemies.length;
    	if(length == 0) return;
    	boolean noAttackersInRange = true;
    	for(int i = length - 1; --i>=0;){
    		RobotType iType = enemies[i].type;
    		if(iType == RobotType.TOWER || iType == RobotType.HQ) continue;
    		if(iType.attackRadiusSquared >= enemies[i].location.distanceSquaredTo(rc.getLocation())){
    			noAttackersInRange = false;
    			break;
    		}
    	}
    	if(noAttackersInRange){ 
    		return;
    	}
    	int totalX = 0;
    	int totalY = 0;
    	for(int i = length; --i>0;){
    		totalX += enemies[i].location.x;
    		totalY += enemies[i].location.y;
    	}
    	MapLocation enemyCenter = new MapLocation(totalX/enemies.length, totalY/enemies.length);
//    	tryMove(enemyCenter.directionTo(rc.getLocation()));
    	MapLocation closestTurret = closestLocation(rc.getLocation(), rc.senseTowerLocations());
    	if(closestTurret == null){
    		closestTurret = myHQ;
    	}
    	tryMove(rc.getLocation().directionTo(closestTurret));
//		RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), 36, theirTeam);
//		for(RobotInfo e: enemies){
//			MapLocation loc = canMoveHit(e);
//			if(loc != null){
//				tryMove(loc.directionTo(rc.getLocation()));
//				return;
//			}
//		}
	}
	
	
	
	private MapLocation launchMissile(MapLocation cur, MapLocation average) throws GameActionException { //launches missile in direction of enemy average position
		Direction dir = cur.directionTo(average);
		if (rc.canLaunch(dir)){
			rc.launchMissile(dir);
			return cur.add(dir);
		}
		else if(cur.x == average.x || cur.y == average.y){
//			check if we can launch to dir, or dir rotated right or left once (maybe add rotate left/right twice?)
			if (rc.canLaunch(dir.rotateLeft())){
				rc.launchMissile(dir.rotateLeft());
				return cur.add(dir.rotateLeft());
			}
			else if (rc.canLaunch(dir.rotateRight())){
				rc.launchMissile(dir.rotateRight());
				return cur.add(dir.rotateRight());
			}
		}
		else{
			Direction[] dirs = {dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight()}; 
			for(Direction d: dirs){
				MapLocation ml = cur.add(d);
				if(rc.canLaunch(cur.directionTo(ml))){
					if(Math.abs(cur.x - average.x) >= Math.abs(ml.x - average.x) && Math.abs(cur.y - average.y) >= Math.abs(ml.y - average.y)){
						rc.launchMissile(d);
						return cur.add(d);
					}
				}
			}
		}
		
		
		return null;
	}
	
	private boolean evaluateSafety(MapLocation cur) { //if there are enemies that can attack us, return true
		RobotInfo[] closeEnemies = rc.senseNearbyRobots(24, rc.getTeam().opponent());
		for (RobotInfo r : closeEnemies) {
			switch(r.type) {
				case MISSILE: 
					if (r.location.distanceSquaredTo(cur) <= 3)
						return true; 
					break;
				case LAUNCHER:
					if (r.location.distanceSquaredTo(cur) <= 4) 
						return true; //3 is subject to change
					break;
				default:
					if (r.location.distanceSquaredTo(cur) <= r.type.attackRadiusSquared) {
						return true;
					}
					break;
			}
		}
		return false;
	}
	
}
