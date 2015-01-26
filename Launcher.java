package launcherStrat;

import battlecode.common.*;

public class Launcher extends BaseBot {
	
	private boolean attackTarget;
	MapLocation targetLoc;
	
	public Launcher(RobotController rc) throws GameActionException {
        super(rc);
        targetLoc = null;
    }
	
	public void execute() throws GameActionException {
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
    	tryMove(rc.getLocation().directionTo(closestLocation(rc.getLocation(), rc.senseTowerLocations())));
//		RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), 36, theirTeam);
//		for(RobotInfo e: enemies){
//			MapLocation loc = canMoveHit(e);
//			if(loc != null){
//				tryMove(loc.directionTo(rc.getLocation()));
//				return;
//			}
//		}
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
	
	private void launchMissile(MapLocation cur, MapLocation average) throws GameActionException { //launches missle in direction of enemy average position
		Direction dir = cur.directionTo(average);
		//check if we can launch to dir, or dir rotated right or left once (maybe add rotate left/right twice?)
		if (rc.canLaunch(dir)) 
			rc.launchMissile(dir);
		else if (rc.canLaunch(dir.rotateLeft()))
			rc.launchMissile(dir.rotateLeft());
		else if (rc.canLaunch(dir.rotateRight()))
			rc.launchMissile(dir.rotateRight());
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
