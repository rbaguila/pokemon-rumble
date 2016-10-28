package ph.edu.uplb.ics.it238;

import java.awt.Point;
import java.util.Random;

public class Enemy extends Point {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3343504933682926258L;
	private static final double MAX_SPEED = 3d;
	private double speed = 0;
	private double[] directionVector = new double[2];
	private int radius;
	public int getRadius() {
		return radius;
	}

	private int diameter;
	private int type;
	public Enemy() {
		super();
		
		radius = 20;
		diameter = radius * 2;
	
		setRandomLocation();
		setSpeed();
		setDirection();
		
		type = (new Random()).nextInt(4);
	}
	
	private void setRandomLocation() {
		//while (x - radius <= 0 || x + radius >= 800)
			x = (new Random()).nextInt(800 - radius);
		
		//while (y - radius <=0 || y + radius >= 600)
			y = (new Random()).nextInt(600 - radius);

	}

	private void setSpeed() {
		while ( (int)speed <= 0) {
			speed = (new Random()).nextDouble()*MAX_SPEED;
		}
	}
	
	private void setDirection() {
		int angle = (new Random()).nextInt(360);
		
		// rotate direction vector
		directionVector[0] = Math.cos(angle) - Math.sin(angle);
		directionVector[1] = Math.cos(angle) + Math.sin(angle);
		
	}
	
	public synchronized void move() {
		translate((int)(directionVector[0]*speed),(int)(directionVector[1]*speed));
		
		if (getY() - radius < 0 || getY() + radius > 600)
			directionVector[1] = directionVector[1]*(-1);
		if (getX() - radius < 0 || getX() + radius > 800)
			directionVector[0] = directionVector[0]*(-1);
	}

	public int getDiameter() {
		return diameter;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}
	
	public String toString(){
		String retval="";
		retval+="ENEMY ";
		retval+=x+" ";
		retval+=y+" ";
		retval+=type;
		return retval;
	}
}