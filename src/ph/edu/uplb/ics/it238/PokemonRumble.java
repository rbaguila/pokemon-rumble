package ph.edu.uplb.ics.it238;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * 
 * @author Roinand B. Aguila
 *
 */
 
 //point coordinated for pokeball
 //enemy
 

public class PokemonRumble extends JPanel implements Runnable, Constants{
	
//	ImageIcon[] pIcons = new ImageIcon[4];
//	ImageIcon bg = new ImageIcon("images/FIELD.jpg");
//	ImageIcon poke = new ImageIcon("images/pokeball.png");
//	ImageIcon title = new ImageIcon("images/TITLE.jpg");
//	ImageIcon [] eIcons = new ImageIcon[4];
//	ImageIcon gameOver = new ImageIcon("images/GAMEOVER.png");
	
	BufferedImage gameOver;
	BufferedImage title;
	BufferedImage bg;
    BufferedImage poke;
    BufferedImage[] pIcons;
    BufferedImage[] eIcons;
    
	
	Point[] players;
	int playerRadius = 20;
	int[] pScore;
	int[] pStats;

	//Point[] eCoords;
	Point[] enemy;
	int enemyRadius = 20;
	int[] e_type;

	Point pokeballCoords = new Point();
	int pokeballRadius = 20;
	
	/**
	 * Main window
	 */
	JFrame frame= new JFrame();
	
	/**
	 * Player position, speed etc.
	 */
	int x=10,y=10,z=0,my_stats=PLAYER_ALIVE,xspeed=2,yspeed=2,prevX,prevY;
	
	/**
	 * Game timer, handler receives data from server to update game state
	 */
	Thread t=new Thread(this);
	NetPlayer nplayer;
	
	/**
	 * Nice name!
	 */
	String name="Player1";
	
	/**
	 * Player name of others
	 */
	//String pname;	//TODO: replace this with array  version
	String[] playerNames;
	
	/**
	 * Server to connect to
	 */
	String server="localhost";

	/**
	 * Flag to indicate whether this player has connected or not
	 */
	boolean connected=false;
	/**
	 * get a datagram socket
	 */
    DatagramSocket socket = new DatagramSocket();

	
    /**
     * Placeholder for data received from server
     */
	String serverData;
	private boolean serverConnected = false;
	
	public void initImages() {
		try {
			bg = ImageIO.read(this.getClass().getResource("images/FIELD.jpg"));
			poke = ImageIO.read(this.getClass().getResource("images/pokeball.png"));
			gameOver = ImageIO.read(this.getClass().getResource("images/GAMEOVER.png"));
			title = ImageIO.read(this.getClass().getResource("images/TITLE.jpg"));
	        pIcons = new BufferedImage[4];
	        pIcons[0] = ImageIO.read(this.getClass().getResource("images/player1.png"));
	        pIcons[1] = ImageIO.read(this.getClass().getResource("images/player2.png"));
	        pIcons[2] = ImageIO.read(this.getClass().getResource("images/player3.png"));
	        pIcons[3] = ImageIO.read(this.getClass().getResource("images/player4.png"));
	        
	        eIcons = new BufferedImage[4];
	        eIcons[RED] = ImageIO.read(this.getClass().getResource("images/enemy1_40x40.png"));
	        eIcons[BLUE] = ImageIO.read(this.getClass().getResource("images/enemy2_40x40.png"));
	        eIcons[BLACK] = ImageIO.read(this.getClass().getResource("images/enemy3_40x40.png"));
	        eIcons[WHITE] = ImageIO.read(this.getClass().getResource("images/enemy4_40x40.png"));
		} catch (IOException ex) {
            Logger.getLogger(PokemonRumble.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
	/**
	 * Basic constructor
	 * @param server
	 * @param name
	 * @throws Exception
	 */
	
//	public void initImages() {
//		pIcons[0] = new ImageIcon("images/player1.png");
//		pIcons[1] = new ImageIcon("images/player2.png");
//		pIcons[2] = new ImageIcon("images/player3.png");
//		pIcons[3] = new ImageIcon("images/player4.png");
//	
//		eIcons[RED] = new ImageIcon("images/enemy1_40x40.png");
//		eIcons[BLUE] = new ImageIcon("images/enemy2_40x40.png");
//		eIcons[BLACK] = new ImageIcon("images/enemy3_40x40.png");
//		eIcons[WHITE] = new ImageIcon("images/enemy4_40x40.png");
//	}
	
	public PokemonRumble(String server,String name) throws Exception{
		this.server=server;
		this.name=name;
		initImages();
		frame.setTitle(APP_NAME+":"+name);
		//set some timeout for the socket
		socket.setSoTimeout(100);
		
		frame.getContentPane().add(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(815, 635);
		frame.setVisible(true);
			
		//Some gui stuff again...
		frame.addMouseMotionListener(new MouseMotionHandler());

		//time to play
		t.start();		
	}
	
	/**
	 * Helper method for sending data to server
	 * @param msg
	 */
	public void send(String msg){
		try{
			byte[] buf = msg.getBytes();
        	InetAddress address = InetAddress.getByName(server);
        	DatagramPacket packet = new DatagramPacket(buf, buf.length, address, PORT);
        	socket.send(packet);
        }catch(Exception e){}
		
	}
	
	public void run(){
		while(true){
			try{
				Thread.sleep(15L);
			}catch(Exception ioe){}
						
			// send data
			if (prevX != x || prevY != y){
				send("PLAYER "+name+" "+x+" "+y);
			}		
			//Get the data from players
			byte[] buf = new byte[1024];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try{
     			socket.receive(packet);
			}catch(Exception ioe){/*lazy exception handling :)*/}
			
			serverData=new String(buf);
			serverData=serverData.trim();
			
			System.out.println(serverData);
			
			if (!connected && serverData.startsWith("CONNECTED")){
				connected=true;
				System.out.println("Connected.");
				
			}else if (!connected){
				System.out.println("Connecting..");	
				serverConnected=true;			
				send("CONNECT "+name);
			}else if (connected){
				
				String [] s_objects = serverData.split("-");
				
				for(String s_object : s_objects){
					if (s_object.startsWith("PLAYER")){
						String[] playersInfo = s_object.split(":");
						players = new Point[playersInfo.length];
						playerNames = new String[playersInfo.length];
						pScore = new int[playersInfo.length];
						pStats = new int[playersInfo.length];
						for (int i=0;i<playersInfo.length;i++){
							String[] playerInfo = playersInfo[i].split(" ");
							playerNames[i] =playerInfo[1];
							players[i] = new Point();
							players[i].x = Integer.parseInt(playerInfo[2]);
							players[i].y = Integer.parseInt(playerInfo[3]);		
							
							pScore[i] = Integer.parseInt(playerInfo[4]);	
							pStats[i] = Integer.parseInt(playerInfo[5]);	
						} //end for	
					} //end if - PLAYER
					
					//enemy 
					else if (s_object.startsWith("ENEMY")){
						String[] enemyInfo = s_object.split(":");
						enemy = new Point[enemyInfo.length];
						e_type = new int[enemyInfo.length];
						for (int i=0;i<enemyInfo.length;i++){
							
							String[] enemyInfo1 = enemyInfo[i].split(" ");
							enemy[i] = new Point();
							enemy[i].x = Integer.parseInt(enemyInfo1[1]);
							enemy[i].y = Integer.parseInt(enemyInfo1[2]);	
						    e_type[i] = Integer.parseInt(enemyInfo1[3]);	
						}
					}
					
					//pokeball
					else if (s_object.startsWith("POKEBALL")){
							String[] pokeballInfo = s_object.split(" ");
							pokeballCoords.x = Integer.parseInt(pokeballInfo[1]);
							pokeballCoords.y = Integer.parseInt(pokeballInfo[2]);
					}
			
				}
					//show the changes		
					frame.repaint();
				
				}			
			}			
		}
	
	/**
	 * Repainting method
	 */
	
//	public void paintComponent(Graphics g){
//		
//		if (!connected){
//			title.paintIcon(this, g, 0, 0);
//		}
//		else
//		{
//			bg.paintIcon(this, g, 0, 0);
//		}
//		
//		if (connected)
//			poke.paintIcon(this, g, pokeballCoords.x - pokeballRadius, pokeballCoords.y - pokeballRadius);	
//	
//		if (enemy != null) {
//			for (int i = 0; i < enemy.length; i++) {
//				eIcons[e_type[i]].paintIcon(this, g, enemy[i].x - enemyRadius, enemy[i].y - enemyRadius);
//			}
//		}
//		
//		if (players != null){
//		for (int i = 0; i < players.length; i++) {
//			pIcons[i].paintIcon(this, g, players[i].x - playerRadius, players[i].y - playerRadius);
//			if (pStats[i] == 1 && playerNames[i].equals(name)){
//				gameOver.paintIcon(this, g, 100, 40);
//			}
//		}
//		}
//		
//		if (connected)
//			paintScore(g);
//	}
	
	public void paintComponent(Graphics g){
		   g.drawImage(bg, 0, 0, this.getWidth(), this.getHeight(), this);
	       g.drawImage(poke, pokeballCoords.x, pokeballCoords.y, this);
//	       for(String playerDta: playerData.keySet()){
//	           NetPlayer netPlayer = playerData.get(playerDta);
//	           g.drawImage(pIcons[netPlayer.getPicNumber()], netPlayer.getX()-20, netPlayer.getY()-20, this);
//	           if (netPlayer.getStatus()  == 1 && netPlayer.getName().equals(name)){
//	        	   g.drawImage(gameOver, 100, 40, this);
//	           }
////	           if (netPlayer.getStatus()  == 2 && netPlayer.getName().equals(name)){
////	        	   g.drawImage(youWon, 100, 40, this);
////	          }
//	           
//	       }
	       if (players != null){
	   		for (int i = 0; i < players.length; i++) {
	   			g.drawImage(pIcons[i], players[i].x - playerRadius, players[i].y - playerRadius, this);
//	   			pIcons[i].paintIcon(this, g, players[i].x - playerRadius, players[i].y - playerRadius);
	   			if (pStats[i] == 1 && playerNames[i].equals(name)){
	   				g.drawImage(gameOver, 100, 40, this);
	   			}
	   		}
	       }
	       
	       if (enemy != null) {
				for (int i = 0; i < enemy.length; i++) {
					g.drawImage(eIcons[e_type[i]], enemy[i].x - enemyRadius, enemy[i].y - enemyRadius, this);
	           }
	       }
	       paintScore(g);
		}
	
	
	private void paintScore(Graphics g) {
		
		Font font1 = new Font("ARIAL", Font.PLAIN,  18);
		
		g.setColor(new Color(0f, 0f, 0f, .5f));
		g.fillRect(35, 540, 715, 45);
		
		g.setColor(Color.WHITE);
		
		int size = 180;
		for (int i = 0; i < players.length; i++){
			g.setFont(font1);
			g.drawString(playerNames[i] + " : " + pScore[i], -135 + size, 570);
			size=size + 200;
		}
	}
	
	
	class MouseMotionHandler extends MouseMotionAdapter{
		public void mouseMoved(MouseEvent me){
			x=me.getX();y=me.getY();
		}
	}

	static int playerNo = 0;
	public static void main(String args[]) throws Exception{
		if (args.length != 2){
			System.out.println("Usage: java -jar PokemonRumble <host> <playername>");
			System.exit(1);
		}

		new PokemonRumble(args[0],args[1]);
	}
}
