package main.java.skibiak.client;

/**
 * Class is used to controll a console display
 * @author piotr
 *
 */

public class OutputProcessing {

	private final static char ESC = 0x1b;
	private final static String CSI = "" + ESC + '[';

	public static String cursorUp(int n) {
		return CSI + n + 'A';
	}

	public static String cursorDown(int n) {
		return CSI + n + 'B';
	}

	public static String processInput(String message, String nickname) {
		if (message != null) {
			String nick = message.split(":")[0];
			if (nick != null && nick.equals(nickname)) {
				return cursorUp(1) + message;
			} else {
				return message;
			}
		}
		return null;
	}

}
