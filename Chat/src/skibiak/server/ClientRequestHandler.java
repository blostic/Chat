package skibiak.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class ClientRequestHandler {
	private static Logger logger = Logger.getLogger(Server.class);
	private ClientConnectionAdapter client;
	private String command;
	private Server server;

	@Parameter(names = { "-n", "-name" }, description = "Name of room to switch / to create")
	private String roomName = "";

	@Parameter(names = { "-t", "-type" }, description = "Type of created room")
	private String roomType = "";

	@Parameter(names = { "-T", "-topic" }, description = "Topic of created topic")
	private String roomTopic = "";

	public ClientRequestHandler(Server server, ClientConnectionAdapter client) {
		PropertyConfigurator.configure("log4j.properties");
		logger.setLevel(Level.INFO);
		this.server = server;
		this.client = client;
	}

	private List<String> commandList = Arrays.asList("createRoom",
			"switchRoom", "changeTopic", "showTopic", "showRooms", "showUsers",
			"help", "exit");

	private void help() {
		client.sendMessage("#createRoom -name roomName "
				+ "-topic roomTopic -type [public|censored]\n"
				+ "#switchRoom -name roomName\n"
				+ "#changeTopic -topic roomTopic\n" + "#showTopic\n"
				+ "#showRooms\n" + "#showUsers\n" + "#help\n" + "#exit\n");
	}

	private void createRoom() {
		try {
			if (!roomName.equals("") && !roomType.equals("") && !roomTopic.equals("")) {
				Room room = RoomFactory.getInstance(server, roomName, roomType, roomTopic);
				if (server.addRoomToServer(room)) {
					room.addClient(client);
					client.setPresentRoom(room);
					client.sendMessage(">Created room " + roomName);
					logger.info(">" + client.getUsername() + " added room " + roomName);
				} else {
					client.sendMessage(">Room already exist");
				}
			} else {
				client.sendMessage("You should provide all required parameters");
			}
		} catch (ClassNotFoundException e) {
			client.sendMessage("No such type of room [" + roomType + "]");
		}
	}

	private void switchRoom() {
		if( server.addUserToRoom(client, roomName )){
			client.sendMessage(">Switched to " + roomName);
		} else {
			client.sendMessage("Room doesn't exist");
		}
	}

	private void changeTopic() {
		client.getPresentRoom().setChatTopic(roomTopic);
		client.getPresentRoom().annouceMessage(
				">" + client.getUsername() + " changed topic to: " + roomTopic);
		logger.info(client.getUsername() + " changed topic to: " + roomTopic);
	}

	private void showTopic() {
		client.sendMessage(">Topic: " + client.getPresentRoom().getChatTopic());
	}

	private void showUsers() {
		client.sendMessage(">Users in room "
				+ client.getPresentRoom().getRoomName());
		int counter = 1;
		for (ClientConnectionAdapter connection : client.getPresentRoom().getClients()) {
			client.sendMessage(">" + counter + ".\t" + connection.getUsername());
			counter++;
		}
	}

	private void showRooms() {
		for (Room room : server.getRooms().values()) {
			client.sendMessage(">Room name: " + room.getRoomName()
					+ ", Topic: " + room.getChatTopic());
		}
	}

	private void exit() {
		Room room = client.getPresentRoom();
		room.removeClient(client);
		room.annouceMessage(">User: " + client.getUsername() + " left the chat");
		client.sendMessage(">GoodBye!");
	}

	public String replaceQuotation(String options) {
		Pattern p = Pattern.compile("\".*?\"");
		Matcher m = p.matcher(options);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String replacement = m.group().replace(" ", "#");
			m.appendReplacement(sb, replacement);
		}
		m.appendTail(sb);
		return sb.toString();
	}

	private void parseCommand(String args) throws ParameterException {
		args = replaceQuotation(args);

		String[] paramaters = args.split(" ");
		command = paramaters[0].substring(1);
		try {
			new JCommander(this, Arrays.copyOfRange(paramaters, 1,
					paramaters.length));
			roomName = roomName.replace("#", " ");
			roomTopic = roomTopic.replace("#", " ");
		} catch (ParameterException e) {
			logger.warn("Unknow option provided by " + client.getUsername());
			throw new ParameterException(e);
		}
	}

	public void executeCommand(String clientMessage) throws ParameterException {
		parseCommand(clientMessage);
		if (commandList.contains(command)) {
			try {
				Method method = ClientRequestHandler.class.getDeclaredMethod(command);
				method.invoke(this);
			} catch (NoSuchMethodException | SecurityException
					| IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				logger.error("Reflective invocation failed", e);
			}
		} else {
			client.sendMessage(">No such command, use #help to check correct syntax");
		}
	}
}
