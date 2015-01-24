package testing;

import battlecode.common.*;

public class Launcher extends BaseBot {
	
	private RobotController rc;
	private MapLocation targetLoc;
	
	public Launcher(RobotController rc){
		super(rc);
		targetLoc = null;
	}
	
	public void execute() throws GameActionException {
		int numMissiles = rc.getMissileCount();
		MapLocation cur = rc.getLocation();
		RobotInfo[] enemies = rc.senseNearbyRobots(35, rc.getTeam().opponent());
		MapLocation average = average(enemies);
		
		if (rc.isWeaponReady() && numMissiles > 0) { //if we can shoot and there are enemies, shoot!
			if (enemies.length>0) {
				launchMissile(cur, average);
				if (numMissiles == 0)
					targetLoc = cur.subtract(cur.directionTo(average)); //if we've shot all our missiles, retreat
			}
		}
		
		if (rc.isCoreReady()) {
			if(evaluateSafety(cur)) {
				targetLoc = cur.subtract(cur.directionTo(average)); //if we're not safe, retreat
			}
			if (targetLoc != null)
				tryMove(cur.directionTo(targetLoc));
			else {
				int x = rc.readBroadcast(1001); //go to rally point
				int y = rc.readBroadcast(1002);
				targetLoc = new MapLocation(x, y);
				tryMove(cur.directionTo(targetLoc));
			}
		}
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
