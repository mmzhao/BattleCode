package launcherStrat;

import battlecode.common.*;

public class Missile extends BaseBot{
	
	public int turnsLived;
	int[] offsets = {0,1,-1,2,-2};
	
	public Missile(RobotController rc) throws GameActionException {
		super(rc);
		turnsLived = 0;
	}
	
	public void execute() throws GameActionException { 
		turnsLived++;
//		if (turnsLived == 5)
//			rc.explode();
		
		MapLocation cur = rc.getLocation();
		
		int range = 5 - turnsLived + 1;
		if(rc.isCoreReady()){
			RobotInfo[] enemies = rc.senseNearbyRobots(cur, range * range + range, theirTeam);
//			MapLocation avg = average(enemies);
//			if(avg != null){
//				rc.setIndicatorString(1, avg.x + " " + avg.y);
//				tryMove(cur.directionTo(avg));
//			}
			tryMove(cur.directionTo(enemies[0].location));
		}
		

		System.out.print(Clock.getBytecodeNum() + "   ");
		
		if (inPositionToExplode(cur))
			rc.explode();
		
		System.out.println(Clock.getBytecodeNum());
		
		
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
	
	public void tryMove(Direction d) throws GameActionException {
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
		}
	}
}
