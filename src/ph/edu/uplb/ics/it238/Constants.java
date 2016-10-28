package ph.edu.uplb.ics.it238;

/**
 * Important game constants
 * @author Roinand B. Aguila
 *
 */
public interface Constants {
	public static final String APP_NAME="Circle Wars 0.01";
	
	/**
	 * Game states.
	 */
	public static final int GAME_START=0;
	public static final int IN_PROGRESS=1;
	public final int GAME_END=2;
	public final int WAITING_FOR_PLAYERS=3;
	public static int PLAYER_ALIVE=0;
	public static int PLAYER_DEAD=1;
	public static int WHITE=0;
	public static int BLUE=1;
	public static int RED=2;
	public static int BLACK=3;
	
	/**
	 * Game port
	 */
	public static final int PORT=4445;
}
