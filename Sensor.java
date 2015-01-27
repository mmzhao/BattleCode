package launcherStratPlusSoldiers;

import battlecode.common.*;

public class Sensor {
	
	private RobotController rc;
	private Team team; 
	private Team enemy;
	
	public int attackRadiusSq;
	public int[] nearbyAllies = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	//soldiers, miners, drones, tanks, launchers, commanders; first 6 indices within attacking range, last 6 withing sensing range
	public int[] nearbyEnemies = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	
	public Sensor(RobotController rc) {
		this.rc = rc;
		this.team = rc.getTeam();
		this.enemy = team.opponent();
		this.attackRadiusSq = rc.getType().attackRadiusSquared;
		
		
	}
	

	public void init() {
		MapLocation cur = rc.getLocation();
		RobotInfo[] allies = rc.senseNearbyRobots(cur, 35, team);
		RobotInfo ally;
		for (int i = allies.length-1 ; --i>=0;) {
			ally = allies[i];
			switch (ally.type) {
				case SOLDIER:
					if (ally.location.distanceSquaredTo(cur) <= attackRadiusSq) 
						nearbyAllies[0]++;
					else
						nearbyAllies[6]++;
					break;
				case MINER:
					if (ally.location.distanceSquaredTo(cur) <= attackRadiusSq) 
						nearbyAllies[1]++;
					else
						nearbyAllies[7]++;
					break;
				case DRONE:
					if (ally.location.distanceSquaredTo(cur) <= attackRadiusSq) 
						nearbyAllies[2]++;
					else
						nearbyAllies[8]++;
					break;
				case TANK:
					if (ally.location.distanceSquaredTo(cur) <= attackRadiusSq) 
						nearbyAllies[3]++;
					else
						nearbyAllies[9]++;
					break;
				case LAUNCHER:
					if (ally.location.distanceSquaredTo(cur) <= attackRadiusSq) 
						nearbyAllies[4]++;
					else
						nearbyAllies[10]++;
					break;
				case COMMANDER:
					if (ally.location.distanceSquaredTo(cur) <= attackRadiusSq) 
						nearbyAllies[5]++;
					else
						nearbyAllies[11]++;
					break;
				default: break;
			}
		}
		
		RobotInfo[] enemies = rc.senseNearbyRobots(cur, 35, this.enemy);
		RobotInfo enemy;
		for (int i = enemies.length-1 ; --i>=0;) {
			enemy = enemies[i];
			switch (enemy.type) {
				case SOLDIER:
					if (enemy.location.distanceSquaredTo(cur) <= attackRadiusSq) 
						nearbyAllies[0]++;
					else
						nearbyAllies[6]++;
					break;
				case MINER:
					if (enemy.location.distanceSquaredTo(cur) <= attackRadiusSq) 
						nearbyAllies[1]++;
					else
						nearbyAllies[7]++;
					break;
				case DRONE:
					if (enemy.location.distanceSquaredTo(cur) <= attackRadiusSq) 
						nearbyAllies[2]++;
					else
						nearbyAllies[8]++;
					break;
				case TANK:
					if (enemy.location.distanceSquaredTo(cur) <= attackRadiusSq) 
						nearbyAllies[3]++;
					else
						nearbyAllies[9]++;
					break;
				case LAUNCHER:
					if (enemy.location.distanceSquaredTo(cur) <= attackRadiusSq) 
						nearbyAllies[4]++;
					else
						nearbyAllies[10]++;
					break;
				case COMMANDER:
					if (enemy.location.distanceSquaredTo(cur) <= attackRadiusSq) 
						nearbyAllies[5]++;
					else
						nearbyAllies[11]++;
					break;
				default: break;
			}
		}
	}
	
	public boolean enemyHasLaunchers() {
		if (nearbyEnemies[4] > 0 || nearbyEnemies[10] > 0)
			return true;
		return false;
	}
	
}
