package launcherStrat;

import battlecode.common.*;

public class Mover {
	
	private RobotController rc;
	private MapLocation cur;
	private MapLocation from;
	private MapLocation to;
	private boolean bugging;
	private boolean hugLeft; //is wall on left of robot
	private Direction hugDir; //direction from cur to wall
	
	public Mover(RobotController rc) {
		this.rc = rc;
		this.bugging = false;
	}
	
	public void startBug(MapLocation target) {
		this.cur = rc.getLocation();
		this.from = this.cur;
		this.to = target;
	}
	
	public Direction getNextMove() {
		rc.setIndicatorString(0, String.valueOf(bugging));
		Direction desiredDir = this.cur.directionTo(this.to);
		
		if (desiredDir == Direction.NONE || desiredDir == Direction.OMNI) {
			return desiredDir;
		}
		
		Direction goToDir = getGoodMove(desiredDir);
		
		if (bugging) {
			if (goToDir != null) {
				//don't need to bug anymore: 
				if (rc.canMove(goToDir) && cur.add(goToDir).distanceSquaredTo(to)<cur.distanceSquaredTo(to)) {
					bugging = false;
					return goToDir;
				}
			}
			return bug();
		}
		
		else {
			if (goToDir!=null) {
				return goToDir;
			}
			//switch to bugging: 
			bugging = true;
			from = cur;
			hugDir = desiredDir;
			hugLeft = true;
			return bug();
		}
	}
	
	public Direction bug() {
		Direction tryDir;
		int i;
		if (hugLeft) {
			tryDir = hugDir.rotateRight();
			for (i = 8; i>0 && !rc.canMove(tryDir); i--) {
				tryDir = tryDir.rotateRight();
			}
		}
		else {
			tryDir = hugDir.rotateLeft();
			for (i = 8; i>0 && !rc.canMove(tryDir); i--) {
				tryDir = tryDir.rotateLeft();
			}
		}
		
		if (i==0) { //didn't find anywhere to go:
			return Direction.NONE;
		} else {
			return tryDir;
		}
	}
	
	public Direction getGoodMove(Direction testDir) {
		if (rc.canMove(testDir)) {
			return testDir;
		}
		//find left or right direction closest to target:
		Direction leftDir = testDir.rotateLeft();
		Direction rightDir = testDir.rotateRight();
		boolean leftIsBetter = cur.add(leftDir).distanceSquaredTo(to) <= cur.add(rightDir).distanceSquaredTo(to);
		if (leftIsBetter) {
			if (rc.canMove(leftDir)) {
				return leftDir;
			}
			else if (rc.canMove(rightDir)){
				return rightDir;
			}
		}
		
		else {
			if (rc.canMove(rightDir)) {
				return rightDir;
			}
			else if (rc.canMove(leftDir)) {
				return leftDir;
			}
		}
		
		return null;
	}
	
}