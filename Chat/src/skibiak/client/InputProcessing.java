package skibiak.client;

/**
 * Class is used to controll a console display
 * @author piotr
 *
 */

public class InputProcessing {

	private static char ESC = 0x1b;
	private static String CSI = "" + ESC + '[';

	public static String cursorUp(int n) {
		return CSI + n + 'A';
	}

	public static String cursorDown(int n) {
		return CSI + n + 'B';
	}
	
	public static String processInput(String message, String nickname){
		if(message!=null){
			String nick = message.split(":")[0];
			if (nick.equals(nickname)){
				return cursorUp(1) + message;
			} else {
				return message;
			}			
		}
		return null;
	}
	
}
