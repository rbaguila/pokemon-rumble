package ph.edu.uplb.ics.it238;
import java.net.InetAddress;

/**
 * This class encapsulates a network players
 * @author Roinand B. Aguila
 *
 */

public class NetPlayer {
	/**
	 * The network address of the player
	 */
	private InetAddress address;
	
	/**
	 * The port number of  
	 */
	private int port;
	
	/**
	 * The name of the player
	 */
	private String name;
	
	/**
	 * The position of player
	 */
	private int x,y;

	private int score;

	public int getScore() {
		return score;
	}

	public void addScore() {
		this.score += 5;
	}
	
	public void resetScore() {
		this.score = 0;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	private int status;

	/**
	 * Constructor
	 * @param name
	 * @param address
	 * @param port
	 */
	public NetPlayer(String name,InetAddress address, int port){
		this.address = address;
		this.port = port;
		this.name = name;
	}

	/**
	 * Returns the address
	 * @return
	 */
	public InetAddress getAddress(){
		return address;
	}

	/**
	 * Returns the port number
	 * @return
	 */
	public int getPort(){
		return port;
	}

	/**
	 * Returns the name of the player
	 * @return
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Sets the X coordinate of the player
	 * @param x
	 */
	public void setX(int x){
		this.x=x;
	}
	
	
	/**
	 * Returns the X coordinate of the player
	 * @return
	 */
	public int getX(){
		return x;
	}
	
	
	/**
	 * Returns the y coordinate of the player
	 * @return
	 */
	public int getY(){
		return y;
	}
	
	/**
	 * Sets the y coordinate of the player
	 * @param y
	 */
	public void setY(int y){
		this.y=y;		
	}

	/**
	 * String representation. used for transfer over the network
	 */
	public String toString(){
		String retval="";
		retval+="PLAYER ";
		retval+=name+" ";
		retval+=x+" ";
		retval+=y + " ";
		retval+=score + " ";
		retval+=status;
		return retval;
	}	
}
