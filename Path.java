package testing;

public class Path { // always 7 steps in both directions
	
	String path;
	int value;
	int x;
	int y;
	boolean finished;

//	3 types of steps -- 0-up/down,1-diag combo,2-left/right
	public Path(){
		path = "";
		value = 0;
		x = 0;
		y = 0;
		finished = false;
	}
	
	public Path(String p, int v, int x, int y){
		path = p;
		value = v;
		this.x = x;
		this.y = y;
		finished = false;
		if(x == 5 && y == 5){
			finished = true;
		}
	}
	
	public Path add(int p){
		if(p == 0){
			return new Path(path + p, value, x + 1, y);
		}
		else if(p == 1){
			return new Path(path + p, value, x + 1, y + 1);
		}
		else if(p == 2){
			return new Path(path + p, value, x, y + 1);
		}
		return null;
	}
	
	
	
}
