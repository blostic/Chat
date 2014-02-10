package skibiak.server;

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
	private final Map<String, Room> rooms;

	private boolean active = true;
	private final ServerSocket serverSocket;

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
			throw new RuntimeException();
		}
	}
	
	@Override
	public void run() {
		new Thread(new ServerController()).start();
		while (active) {
			Socket clientSocket;
			try {
				clientSocket = serverSocket.accept();
				new Thread(new LogUser(clientSocket)).start();
			} catch (SocketException e) {
				logger.info("Socket closed");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean addRoomToServer(ClientConnectionAdapter client, Room room) {
		String roomName = room.getRoomName();
		if (!rooms.containsKey(roomName)) {
			rooms.put(room.getRoomName(), room);
			client.sendMessage(">Created room " + roomName);
			return true;
		} else {
			logger.warn("User " + client.getUsername()
					+ " tried to add room which already exist [" + roomName
					+ "]");
			client.sendMessage(">Room already exist");
			return false;
		}
	}

	public boolean containUser(String username) {
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
			room.annouceMessage(">User " + client.getUsername()
					+ " entered the room");
			client.setPresentRoom(room);
			room.addClient(client);
			logger.info("User " + client.getUsername() + " added to room "
					+ roomName);
			return true;
		} else {
			logger.warn("User " + client.getUsername()
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
		PropertyConfigurator.configure("log4j.properties");
		try {
			logger.setLevel(Level.INFO);
			int port = Integer.parseInt(args[0]);
			if (port > 1024 && port < 65535) {
				logger.info("Server is listening at port " + port);
				new Thread(new Server(port)).start();
			} else {
				logger.error("Uncorect port");
			}
		} catch (NumberFormatException e) {
			logger.error("No port provided");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private class ServerController implements Runnable {

		@Override
		public void run() {
			try {
				while (active) {
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(System.in));
					String message = reader.readLine();
					if (message.equals("#exit")) {
						closeServer();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class LogUser implements Runnable {
		
		private final ClientConnectionAdapter client;
		private static final String USERNAME_PATTERN = "^[a-z0-9_-]{3,15}$";

		private LogUser(Socket clientSocket) {
			this.client = new ClientConnectionAdapter(clientSocket,
					rooms.get("MainRoom"));
		}

		private boolean validUsername(String username) {
			Pattern pattern = Pattern.compile(USERNAME_PATTERN, Pattern.CASE_INSENSITIVE);
			System.out.println(username);
			Matcher matcher = pattern.matcher(username);
			return matcher.matches();
		}

		private boolean syncUserLogin(String username) {
			if (!containUser(username)) {
				synchronized (rooms.get("MainRoom")) {
					if (!containUser(username)) {
						client.setUsername(username);
						client.sendMessage(">Welcome " + username + "!");
						logger.info("User " + username + " has logged");
						addUserToRoom(client, "MainRoom");
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public void run() {
			Boolean userFree = false;
			client.sendMessage(">Please choose a username: ");
			try {
				while (!userFree) {
					String username = client.readMessage();
					if (username != null && validUsername(username)) {
						userFree = syncUserLogin(username);
						if (!userFree) {
							client.sendMessage(">Username " + username
									+ " is already in use. Try something else.");
						}
					} else {
						client.sendMessage(">Valid login consists of 3 to 15 alphanumeric (plus _-) symbols");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("User left the chat before choosing username");
			}
		}
	}

}
