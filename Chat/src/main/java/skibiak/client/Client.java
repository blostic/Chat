package main.java.skibiak.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import main.java.skibiak.server.Room;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Client implements Runnable {
	private final static Logger logger = Logger.getLogger(Room.class);
	
	private final int serverPort;
	private final String serverHost;
	private String nickname;
	private boolean active = true;
	
	private BufferedReader userInputReader;
	private PrintWriter clientOut;
	private BufferedReader clientIn;

	public String getNickname(){
		return nickname;
	}
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	public Client(int port, String host) {
		this.serverPort = port;
		this.serverHost = host;
		
		PropertyConfigurator.configure("main/resources/log4j.properties");
		logger.setLevel(Level.INFO);
	}

	public void sendClientMessage(String message) throws IOException {
		if (!clientOut.checkError()) {
			if (message != null) {
				clientOut.write(message + "\n");
				clientOut.flush();
			}
		} else {
			logger.error("Connection closed by remote host");
			System.out.println("Connection closed by remote host");
			System.exit(1);
		}
	}

	public String readServerMessages() {
		try {
			String message = clientIn.readLine();
			if (message != null && message.equals(">GoodBye!")) {
				setActive(false);
			}
			return OutputProcessing.processInput(message, nickname);
		} catch (IOException e) {
			logger.info("connection closed");
			setActive(false);
			return "Connection Closed";
		}
	}

	public void logUser() throws IOException {
		System.out.println(readServerMessages());
		boolean isNickCorrect = false;
		while (!isNickCorrect) {
			nickname = userInputReader.readLine();
			sendClientMessage(nickname);
			String serverResponse = readServerMessages();
			System.out.println(serverResponse);
			isNickCorrect = (serverResponse.split(" ")[0].equals(">Welcome"));
		}
	}

	public void startClient() throws UnknownHostException, IOException {
		try (Socket socket = new Socket(serverHost, serverPort)) {
			userInputReader = new BufferedReader(new InputStreamReader(System.in));
			clientOut = new PrintWriter(socket.getOutputStream());
			clientIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			logUser();
			new Thread(this).start();
			while (isActive()) {
				String message = userInputReader.readLine();
				if (message != null && message.equals("#exit")) {
					setActive(false);
				}
				sendClientMessage(message);
			}
		}
	}

	@Override
	public void run() {
		while (isActive()) {
			try {
				Thread.sleep(10);
				String message = this.readServerMessages();
				if (message != null) {
					System.out.println(message);
				}
			} catch (InterruptedException e) {
				logger.error(e);
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			try {
				int port = Integer.parseInt(args[1]);
				logger.info("Client selected port:" + port + "host:" + args[0]);
				new Client(port, args[0]).startClient();
			} catch (NumberFormatException e) {
				System.out.println("Port number should be integer value.");
			} catch (IOException e) {
				System.out.println("Problem with server connection. "
						+ "Please contact the server administrator "
						+ "to resolve this issue.");
				logger.error(e);
			}
		} else {
			System.out.println("Usage:\njava -jar client.jar host portNumber");
		}
	}

}
