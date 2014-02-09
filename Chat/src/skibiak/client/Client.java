package skibiak.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import skibiak.server.Room;

public class Client implements Runnable {
	private final static Logger logger = Logger.getLogger(Room.class);
	private final int port;
	private final String host;
	private String nickname;
	
	private BufferedReader scanner;
	private PrintWriter out;
	private BufferedReader in;
	private boolean active = true;

	public Client(int port, String host) {
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
		try {
			
			String s =  in.readLine();
			return InputProcessing.processInput(s, this.nickname);
			
		} catch (IOException e) {
			logger.info("connection close");
			return "Connection Closed";
		}
	}

	public void login() throws IOException {
		System.out.println(readServerMessages());
		boolean nickCorrect = false;
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
			while (active) {
				String message = scanner.readLine();
				if (message.equals("#exit")){					
					active = false;
				}
				sendClientMessage(message);
			}
		}
	}

	@Override
	public void run() {
		while (active) {
			try {
				Thread.sleep(100);
				String message = this.readServerMessages();
				if (message != null) {
					System.out.println(message);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws IOException {
		try {
			int port = Integer.parseInt(args[1]);
			if (port > 1024 && port < 65535) {
				logger.info("Server is listening at port " + port);
			}
			new Client(port, args[0]).startClient();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			logger.error(e);
		}
	}
}
