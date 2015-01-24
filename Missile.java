package testing;

import battlecode.common.*;

public class Missile extends BaseBot{
	
	private RobotController rc;
	public int turnsLived;
	
	public Missile(RobotController rc) {
		super(rc);
		turnsLived = 0;
	}
	
	public void execute() throws GameActionException { 
		turnsLived++;
		if (turnsLived == 5)
			rc.explode();
		
		MapLocation cur = rc.getLocation();
		
		if (inPositionToExplode(cur))
			rc.explode();
		
		int range = 5 - turnsLived + 1;
		RobotInfo[] enemies = rc.senseNearbyRobots(range * range , rc.getTeam().opponent());
		tryMove(cur.directionTo(average(enemies)));
		
		rc.yield();
	}
	
	private boolean inPositionToExplode(MapLocation cur) throws GameActionException { //determines if you should explode at your current position
		boolean criticalHealth = (rc.getHealth() == 1);
		Team opp = rc.getTeam().opponent();
		int count = 0;
		int x = cur.x, y = cur.y;
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				if (i != 0 && j != 0) {
					RobotInfo r = rc.senseRobotAtLocation(new MapLocation(x+i, y+j));
					if (r != null && r.team == opp) {
						if (criticalHealth) return true;
						count++;
					}
				}
			}
		}
		if (count>=3) return true; //number adjustable
		return false;
	}
}
