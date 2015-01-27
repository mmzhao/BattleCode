package launcherStrat;

import battlecode.common.*;

public class Missile extends BaseBot{
	
	public int turnsLived;
	public MapLocation prevTarget;
	
	public Missile(RobotController rc) throws GameActionException {
		this.rc = rc;
		rc.setIndicatorString(1, Clock.getBytecodeNum() + " ");
		turnsLived = 0;
		prevTarget = null;
	}
	
	public void execute() throws GameActionException {

		turnsLived++;
		
		MapLocation curr = rc.getLocation();
		MapLocation target = getTarget();
		
		if(target == null && prevTarget != null){
			target = prevTarget;
		}
		
		if(target != null && curr.isAdjacentTo(target)){
			rc.explode();
		}
		if(target != null) rc.setIndicatorString(0, target.x + " " + target.y);
		
		if(rc.isCoreReady() && target != null){
//			curr = tryMoveMissile(curr.directionTo(target));
			Direction dir = curr.directionTo(target);
			prevTarget = target;
			if(rc.canMove(dir)){
				rc.move(dir);
//				prev = dir;
			}
			else if(rc.canMove(dir.rotateLeft())){
				rc.move(dir.rotateLeft());
//				prev = dir.rotateLeft();
			}
			else if(rc.canMove(dir.rotateRight())){
				rc.move(dir.rotateRight());
//				prev = dir.rotateRight();
			}
		}
		
//		if (inPositionToExplode(rc.getLocation()))
//			rc.explode();
		
//		System.out.println(Clock.getBytecodeNum());
		
		
		rc.yield();
	}
	
	public MapLocation getTarget() throws GameActionException{
		int count = rc.readBroadcast(20000);
		MapLocation cur = rc.getLocation();
		
		if(turnsLived == 1){
			for(int i = count - 1; i >= 0; i--){
				if(rc.readBroadcast(20003 + 10 * i) == cur.x && rc.readBroadcast(20004 + 10 * i) == cur.y){
					return new MapLocation(rc.readBroadcast(20001 + 10 * i), rc.readBroadcast(20002 + 10 * i));
				}
			}
		}
		else{
			for(int i = 0; i < count; i++){
				if(rc.readBroadcast(20003 + 10 * i) == cur.x && rc.readBroadcast(20004 + 10 * i) == cur.y){
					return new MapLocation(rc.readBroadcast(20001 + 10 * i), rc.readBroadcast(20002 + 10 * i));
				}
			}
		}
		return null;
	}
	
	public MapLocation closestEnemy() throws GameActionException{
		int minDist = Integer.MAX_VALUE;
		MapLocation closest = null;
		MapLocation cur = rc.getLocation();
		int count = rc.readBroadcast(10000);
//		System.out.println(count);
		for(int i = 0; i < count; i++){
			if((rc.readBroadcast(10003 + 10 * i) - Clock.getRoundNum()) < -1){
				continue;
			}
			MapLocation ml = new MapLocation(rc.readBroadcast(10001 + 10 * i), rc.readBroadcast(10002 + 10 * i));
			int dist = cur.distanceSquaredTo(ml);
			if(dist < minDist){
				minDist = dist;
				closest = ml;
			}
		}
		return closest;
	}
	
	public void executeOld() throws GameActionException {
//		if (turnsLived == 5)
//			rc.explode();
		
		MapLocation cur = rc.getLocation();
		
		System.out.print("e");
		
		int range = 5 - turnsLived + 1;
		if(rc.isCoreReady()){
			RobotInfo[] enemies = rc.senseNearbyRobots(cur, range * range * 2, theirTeam);
//			MapLocation avg = average(enemies);
//			if(avg != null){
//				rc.setIndicatorString(1, avg.x + " " + avg.y);
//				tryMove(cur.directionTo(avg));
//			}
			if(enemies.length > 0){
				tryMove(cur.directionTo(enemies[0].location));
			}
			else{
				MapLocation closest = closestLocation(rc.getLocation(), rc.senseEnemyTowerLocations());
				if(closest == null) closest = theirHQ;
				tryMove(cur.directionTo(closest));
			}
			
		}
		
		System.out.println("ee");
		
		turnsLived++;
//		System.out.print(Clock.getBytecodeNum() + "   ");
		
		if (inPositionToExplode(cur))
			rc.explode();
		
//		System.out.println(Clock.getBytecodeNum());
		
		
		rc.yield();
	}
	
	
	private boolean inPositionToExplode(MapLocation cur) throws GameActionException { //determines if you should explode at your current position
//		boolean criticalHealth = (rc.getHealth() == 1);
//		int count = 0;
//		int x = cur.x, y = cur.y;
//		for (int i = -1; i < 2; i++) {
//			for (int j = -1; j < 2; j++) {
//				if (i != 0 && j != 0) {
//					RobotInfo r = rc.senseRobotAtLocation(new MapLocation(x+i, y+j));
//					if (r != null && r.team == theirTeam) {
//						if (criticalHealth) return true;
//						count++;
//					}
//				}
//			}
//		}
//		if (count>=3) return true; //number adjustable
		if(rc.senseNearbyRobots(rc.getLocation(), 2, theirTeam).length >= 1) return true;
		return false;
	}
	
	public MapLocation tryMoveMissile(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < offsets.length && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < offsets.length) {
//			MapLocation ml= rc.getLocation().add(directions[(dirint+offsets[offsetIndex]+8)%8]);
//			rc.setIndicatorString(0, ml.x + " " + ml.y);
			rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
			return rc.getLocation().add(directions[(dirint+offsets[offsetIndex]+8)%8]);
		}
		return rc.getLocation();
	}
}
