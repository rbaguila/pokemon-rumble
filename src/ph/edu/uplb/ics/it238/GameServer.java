package ph.edu.uplb.ics.it238;

import java.awt.Point;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import sun.awt.windows.ThemeReader;

/**
 * The main game server. It just accepts the messages sent by one player to
 * another player
 * @author Roinand B. Aguila
 *
 */

public class GameServer implements Runnable, Constants{
	/**
	 * Placeholder for the data received from the player
	 */	 
	String playerData;
	
	/**
	 * The number of currently connected player
	 */
	int playerCount=0;
	
	/**
	 * The socket
	 */
    DatagramSocket serverSocket = null;
    
    /**
     * The current game state
     */
	GameState game;

	/**
	 * The current game stage
	 */
	int gameStage=WAITING_FOR_PLAYERS;
	
	/**
	 * Number of players
	 */
	int numPlayers;
	
	/**
	 * The main game thread
	 */
	Thread t = new Thread(this);
	
	
	//////////////////////////////////////////////////////
	
	// Enemy Information
	List<Enemy> enemies = new ArrayList<Enemy>();
	
	
	// Pokeball Information
	Point pokeballCoords = new Point(400,300);
	final int pokeballRadius = 20;
	final int pokeballDiameter = pokeballRadius * 2;
	
	// Player info
	final int playerRadius = 20;
	final int playerDiameter = pokeballRadius * 2;
	
	
	/**
	 * Simple constructor
	 */
	public GameServer(int numPlayers){
		this.numPlayers = numPlayers;
		try {
            serverSocket = new DatagramSocket(PORT);
			serverSocket.setSoTimeout(100);
		} catch (IOException e) {
            System.err.println("Could not listen on port: "+PORT);
            System.exit(-1);
		}catch(Exception e){}
		//Create the game state
		game = new GameState();
		
		System.out.println("Game created...");
		
		//Start the game thread
		t.start();
	}
	
	/**
	 * Helper method for broadcasting data to all players
	 * @param msg
	 */
	public void broadcast(String msg){
		for(Iterator ite=game.getPlayers().keySet().iterator();ite.hasNext();){
			String name=(String)ite.next();
			NetPlayer player=(NetPlayer)game.getPlayers().get(name);			
			send(player,msg);	
		}
	}


	/**
	 * Send a message to a player
	 * @param player
	 * @param msg
	 */
	public void send(NetPlayer player, String msg){
		DatagramPacket packet;	
		byte buf[] = msg.getBytes();		
		packet = new DatagramPacket(buf, buf.length, player.getAddress(),player.getPort());
		try{
			serverSocket.send(packet);
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
	
	/**
	 * The juicy part
	 */
	public void run(){
		while(true){
						
			// Get the data from players
			byte[] buf = new byte[256];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try{
     			serverSocket.receive(packet);
			}catch(Exception ioe){}
			
			/**
			 * Convert the array of bytes to string
			 */
			playerData=new String(buf);
			
			//remove excess bytes
			playerData = playerData.trim();
		
			// process
			switch(gameStage){
				  case WAITING_FOR_PLAYERS:
						//System.out.println("Game State: Waiting for players...");
						if (playerData.startsWith("CONNECT")){
							String tokens[] = playerData.split(" ");
							NetPlayer player=new NetPlayer(tokens[1],packet.getAddress(),packet.getPort());
							player.setX(1000); player.setY(1000);
							System.out.println("Player connected: "+tokens[1]);
							game.update(tokens[1].trim(),player);
							broadcast("CONNECTED "+tokens[1]);
							playerCount++;
							if (playerCount==numPlayers){
								gameStage=GAME_START;
							}
						}
					  break;	
				  case GAME_START:
					  System.out.println("Game State: START");
					  broadcast("START");
					  gameStage=IN_PROGRESS;
					  break;
				  case IN_PROGRESS:
					  //System.out.println("Game State: IN_PROGRESS");
					  
					  //Player data was received!
					  if (playerData.startsWith("PLAYER")){
						  //Tokenize:
						  //The format: PLAYER <player name> <x> <y>
						  String[] playerInfo = playerData.split(" ");					  
						  String pname =playerInfo[1];
						  Point pCoords = new Point(Integer.parseInt(playerInfo[2].trim()),Integer.parseInt(playerInfo[3].trim()));
						  //int pStatus = Integer.parseInt(playerInfo[5].trim());
						  
						  //Get the player from the game state
						  NetPlayer player=(NetPlayer)game.getPlayers().get(pname);					  
						  //player.setStatus(pStatus);
						  // if alive, set player
						  if (player.getStatus() != 1) {
							  player.setX(pCoords.x);
							  player.setY(pCoords.y);
						  }
						  else {
							  player.setX(1000);
							  player.setY(1000);
						  }
						  game.update(pname, player);	  
					  }
					  
					  for (Enemy e : enemies) {
						  e.move();
						   Map playerMap = game.getPlayers();
						   for(Iterator ite=playerMap.keySet().iterator();ite.hasNext();){
								String name=(String)ite.next();
								NetPlayer thePlayer=(NetPlayer)playerMap.get(name);
								Point p = new Point(thePlayer.getX(),thePlayer.getY());
								if (intersectsPlayer(e, p)) {
									thePlayer.setStatus(1);	//TODO: 1 is gameOver
								}
							}
					  }
					  
					  // check if player scored
					  Map playerMap = game.getPlayers();
					   for(Iterator ite=playerMap.keySet().iterator();ite.hasNext();){
							String name=(String)ite.next();
							NetPlayer netPlayer=(NetPlayer)playerMap.get(name);
							Point p = new Point(netPlayer.getX(),netPlayer.getY());
							if (intersectsPokeball(p)) {
								netPlayer.addScore();	//TODO: 1 is gameOver
								enemies.add(new Enemy());

								 //Update the game state
								// randomize pokeball
									pokeballCoords.x = (new Random()).nextInt(800 - pokeballRadius);
									pokeballCoords.y = (new Random()).nextInt(600 - pokeballRadius);

								
							}
						}
					  
					  String msg = game.toString();
					  
					   
					  // send a Enemy Data
					   
					  if (enemies.size() > 0) {
						  String enemyData = "";
						  for (Enemy e : enemies) {
							  enemyData +=  e.toString()+":";
						  }
						msg += "-" + enemyData;
					}
					  // send pokeball
					  msg += ("-POKEBALL "  + pokeballCoords.x + " " + pokeballCoords.y);
					     
					  //Send to all the updated game state
					  broadcast(msg);
					  
					  break;
			}
			try {
				Thread.sleep(2L);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	
	private boolean intersectsPlayer(Enemy object1, Point p) {
		java.awt.geom.Ellipse2D ellipseArea = new java.awt.geom.Ellipse2D.Float((float)object1.getX(),(float)object1.getY(),(float)object1.getRadius(),(float)object1.getRadius());
		
		return ellipseArea.intersects(p.getX(),p.getY(),playerDiameter,playerDiameter);
	}
	
	private boolean intersectsPokeball(Point object1) {
		java.awt.geom.Ellipse2D ellipseArea = new java.awt.geom.Ellipse2D.Float((float)object1.getX(),(float)object1.getY(),(float)playerRadius,(float)playerRadius);
		
		return ellipseArea.intersects(pokeballCoords.getX(),pokeballCoords.getY(),pokeballDiameter,pokeballDiameter);
	}
	
	public static void main(String args[]){
		if (args.length != 1){
			System.out.println("Usage: java -jar GameServer <number of players>");
			System.exit(1);
		}
		int p = Integer.parseInt(args[0]);
		if (p > 4) {
			System.out.println("Maximum of 4 players only");
			System.exit(1);
		}
			
		new GameServer(p);
	}
}

