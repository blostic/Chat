package skibiak.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import skibiak.server.Room;

public class Client implements Runnable {
	private static Logger logger = Logger.getLogger(Room.class);
	private int port;
	private String host;
	private BufferedReader scanner;
	private PrintWriter out;
	private BufferedReader in;

	public Client(String nick, int port, String host) {
		super();
		this.port = port;
		this.host = host;
		PropertyConfigurator.configure("log4j.properties");
		logger.setLevel(Level.INFO);
	}

	public void sendClientMessage(String message) throws IOException {
		if (!out.checkError()) {
			out.write(message + "\n");
			out.flush();
		} else {
			logger.error("Connection closed by remote host");
			System.exit(1);
		}
	}

	public String readServerMessages() throws IOException {
		return in.readLine();
	}

	public void login() throws IOException {
		System.out.println(readServerMessages());
		boolean nickCorrect = false;
		String nickname = "";
		while (!nickCorrect) {
			nickname = scanner.readLine();
			sendClientMessage(nickname);
			String serverResponse = readServerMessages();
			System.out.println(serverResponse);
			nickCorrect = (serverResponse.split(" ")[0].equals("Welcome"));
		}
	}

	public void startClient() throws UnknownHostException, IOException {
		try (Socket socket = new Socket(this.host, this.port)) {
			this.scanner = new BufferedReader(new InputStreamReader(System.in));
			out = new PrintWriter(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			login();
			new Thread(this).start();
			while (true) {
				String message = scanner.readLine();
				sendClientMessage(message);
			}
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(100);
				String message = this.readServerMessages();
				if (message != null && message != "") {
					System.out.println(message);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws UnknownHostException,
			IOException {
		new Client("test" + new Random().nextInt(100), 4000, "localhost")
				.startClient();
	}
}
