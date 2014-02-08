package skibiak.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Server {
	private static Logger logger = Logger.getLogger(Server.class);
	private Map<String, Room> rooms;
	private int port;

	public Map<String, Room> getRooms() {
		return rooms;
	}

	public Server(int port) throws ClassNotFoundException {
		this.port = port;
		this.rooms = Collections.synchronizedMap(new HashMap<String, Room>());
		rooms.put("MainRoom", RoomFactory.getInstance(this, "MainRoom",
				"public", "Everything about Everyone", "bb"));
	}

	public void runServer() throws IOException {
		try (ServerSocket socket = new ServerSocket(port)) {
			while (true) {
				Socket clientSocket = socket.accept();
				new Thread(new LogUser(clientSocket)).start();
			}
		}
	}

	public void addRoomToServer(ClientConnectionAdapter client, Room room) {
		String roomName = room.getRoomName();
		if (!rooms.containsKey(roomName)) {
			rooms.put(room.getRoomName(), room);
			client.sendMessage("Created room " + roomName);
		} else {
			logger.warn("User " + client.getUsername()
					+ " tried to add room which already exist [" + roomName
					+ "]");
			client.sendMessage("Room already exist");
		}
	}

	public boolean containUser(String username) {
		for (Room room : rooms.values()) {
			for (ClientConnectionAdapter clientConnection : room.clients) {
				if (clientConnection.getUsername().equals(username)) {
					return true;
				}
			}
		}
		return false;
	}

	public void addUserToRoom(ClientConnectionAdapter client, String roomName) {
		if (rooms.containsKey(roomName)) {
			Room room = rooms.get(roomName);
			room.annouceMessage(">User " + client.getUsername()
					+ " entered the room");
			client.setPresentRoom(room);
			logger.info("User " + client.getUsername() + " added to room "
					+ roomName);
		} else {
			logger.warn("User " + client.getUsername()
					+ " has selected a room that does not exist [" + roomName
					+ "]");
			client.sendMessage("Room doesn't exist");
		}
	}

	public void removeUnresponsiveClients() {
		for (Room room : rooms.values()) {
			List<ClientConnectionAdapter> clientsToRemove = new ArrayList<ClientConnectionAdapter>();
			for (ClientConnectionAdapter clientConnection : room.clients) {
				if (clientConnection.clientDisconected()) {
					clientsToRemove.add(clientConnection);
				}
			}
			room.clients.removeAll(clientsToRemove);
		}
	}

	public static void main(String[] args) {
		try {
			PropertyConfigurator.configure("log4j.properties");
			logger.setLevel(Level.INFO);

			int port = 4000;
			logger.info("Server is listening at port " + port);
			new Server(port).runServer();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private class LogUser implements Runnable {
		private final ClientConnectionAdapter client;

		private static final String USERNAME_PATTERN = "^[a-z0-9_-]{3,15}$";
		private final Pattern pattern = Pattern.compile(USERNAME_PATTERN,
				Pattern.CASE_INSENSITIVE);

		private LogUser(Socket clientSocket) {
			this.client = new ClientConnectionAdapter(clientSocket,
					rooms.get("MainRoom"));
		}

		private boolean validUsername(String username) {
			Matcher matcher = pattern.matcher(username);
			return matcher.matches();
		}

		private boolean syncUserLogin(String username) {
			if (!containUser(username)) {
				synchronized (rooms.get("MainRoom")) {
					if (!containUser(username)) {
						client.setUsername(username);
						client.sendMessage("Welcome " + username + "!");
						logger.info("User" + username + " has logged");
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
			client.sendMessage("Please choose a username: ");
			try {
				while (!userFree) {
					String username = client.readMessage();
					if (validUsername(username)) {
						userFree = syncUserLogin(username);
						if (!userFree) {
							client.sendMessage("Username " + username
									+ " is already in use. Try something else.");
						}
					} else {
						client.sendMessage("Valid login consists of 3 to 15 alphanumeric (plus _-) symbols");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("User left the chat before choosing username");
			}
		}
	}

}
