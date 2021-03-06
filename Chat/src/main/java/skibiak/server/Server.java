package main.java.skibiak.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Server implements Runnable {
	private static Logger logger = Logger.getLogger(Server.class);
	static {
		PropertyConfigurator.configure("main/resources/log4j.properties");
		logger.setLevel(Level.INFO);
	}
	
	private final Map<String, Room> rooms;
	private final ServerSocket serverSocket;
	private boolean active = true;

	public boolean isActive() {
		return active;
	}

	public Map<String, Room> getRooms() {
		return rooms;
	}
	
	public Server(int port) throws IOException {
		this.serverSocket = new ServerSocket(port);
		this.rooms = Collections.synchronizedMap(new HashMap<String, Room>());
		try {
			rooms.put("MainRoom", RoomFactory.getInstance(this, "MainRoom",
					"public", "Everything about Everyone"));
		} catch (ClassNotFoundException e) {
			logger.error(e);
			throw new RuntimeException();
		}
	}

	@Override
	public void run() {
		new Thread(new ServerController()).start();
		while (active) {
			try {
				Socket clientSocket = serverSocket.accept();
				new Thread(new LogUser(clientSocket)).start();
			} catch (SocketException e) {
				logger.info("Socket closed");
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

	public boolean addRoomToServer(Room room) {
		String roomName = room.getRoomName();
		if (!rooms.containsKey(roomName)) {
			rooms.put(room.getRoomName(), room);
			return true;
		}
		return false;
	}

	public boolean containsUser(String username) {
		for (Room room : rooms.values()) {
			if (room.containUser(username)) {
				return true;
			}
		}
		return false;
	}

	public boolean addUserToRoom(ClientConnectionAdapter client, String roomName) {
		if (rooms.containsKey(roomName)) {
			Room room = rooms.get(roomName);
			room.annouceMessage(">User " + client.getUsername() + " entered the room");
			client.setPresentRoom(room);
			room.addClient(client);
			logger.info("User " + client.getUsername() + " added to room "
					+ roomName);
			return true;
		} else {
			logger.info("User " + client.getUsername()
					+ " has selected a room that does not exist [" + roomName
					+ "]");
			return false;
		}
	}

	public void closeServer() throws IOException {
		for (Room room : rooms.values()) {
			room.annouceMessage(">GoodBye!");
		}
		active = false;
		serverSocket.close();
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			try {
				int port = Integer.parseInt(args[0]);
				logger.info("Server is listening at port " + port);
				new Thread(new Server(port)).start();
				System.out.println("The Chat server is listniening at port "
						+ port + "\nType \"#exit\" to exit the server");
			} catch (NumberFormatException e) {
				System.out.println("Port number should be integer value.");
			} catch (IOException e) {
				logger.error(e);
			}
		} else {
			System.out.println("Usage:\njava -jar server.jar portNumber");
		}		
	}
	
	/**
	 * ServerController allows server administrators to shut down the server by
	 * typing #exit command.
	 */
	
	private class ServerController implements Runnable {
		
		@Override
		public void run() {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				while (active) {
					String message = reader.readLine();
					if (message != null && message.equals("#exit")) {
						closeServer();
					}
				}
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	/**
	 * LogUser responsibility is to properly log a user into chat. 
	 * By default, user is placed into MainRoom.
	 */
	
	private class LogUser implements Runnable {
		
		private static final String USERNAME_PATTERN = "^[a-z0-9_-]{3,15}$";
		private final ClientConnectionAdapter client;

		private LogUser(Socket clientSocket) throws IOException {
			client = new ClientConnectionAdapter(clientSocket, rooms.get("MainRoom"));
		}

		private boolean validUsername(String username) {
			if (username != null) {
				Pattern pattern = Pattern.compile(USERNAME_PATTERN, Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(username);
				return matcher.matches();
			} else {
				return false;
			}
		}

		private boolean syncUserLogin(String username) {
			if (!containsUser(username)) {
				synchronized (rooms.get("MainRoom")) {
					if (!containsUser(username)) {
						client.setUsername(username);
						addUserToRoom(client, "MainRoom");
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public void run() {
			Boolean nickFree = false;
			client.sendMessage(">Please choose a username: ");
			try {
				while (!nickFree) {
					String username = client.readMessage();
					if (validUsername(username)) {
						nickFree = syncUserLogin(username);
						if (!nickFree) {
							client.sendMessage(">Username " + username
									+ " is already in use. Try something else.");
						} else {
							client.sendMessage(">Welcome " + username + "!");
							client.sendMessage(">Type #help to familiarize"
									+ " yourself with the chat capabilities");
							logger.info("User " + username + " has logged");
						}
					} else {
						client.sendMessage(">Valid login consists of 3 to 15 "
								+ "alphanumeric (plus _-) symbols");
					}
				}
			} catch (IOException e) {
				logger.error("User left the chat before choosing username", e);
			}
		}
	}

}
