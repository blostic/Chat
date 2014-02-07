package skibiak.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

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
	private String roomName;

	@Parameter(names = { "-t", "-type" }, description = "Type of created room")
	private String roomType;

	@Parameter(names = { "-T", "-topic" }, description = "Topic of created room")
	private String roomTopic;

	public ClientRequestHandler(Server server, ClientConnectionAdapter client) {
		PropertyConfigurator.configure("log4j.properties");
		logger.setLevel(Level.INFO);
		this.server = server;
		this.client = client;

	}

	private List<String> commandList = Arrays.asList("creteRoom", "switchRoom",
			"changeTopic", "showTopic", "showRooms", "showUsers", "help",
			"exit");

	private void displayHelp() {
		client.sendMessage("#createRoom  [-n|-name] roomName [-T|-topic] roomTopic [-t|-type] roomType");
		client.sendMessage("#switchRoom  [-n|-name] roomName");
		client.sendMessage("#changeTopic [-T|-topic] roomTopic");
		client.sendMessage("#showTopic");
		client.sendMessage("#showRooms");
		client.sendMessage("#showUsers");
		client.sendMessage("#help");
		client.sendMessage("#exit");
		client.sendMessage("roomType = [public|censored]");
	}

	private void createRoom() {
		try {
			if (roomName.length() > 1 && roomType.length() > 1
					&& roomTopic.length() > 1) {
				Room room = RoomFactory.getInstance(server, roomName, roomType,
						roomTopic, client.getNickname());
				server.addRoomToServer(client, room);
				client.setPresentRoom(room);
				logger.info("User " + client.getNickname() + " added room "
						+ roomName);
			} else {
				logger.error(client.getNickname()
						+ " didn't provide all required parameters to create new room");
				client.sendMessage("You should provide all required parameters");
			}
		} catch (ClassNotFoundException e) {
			logger.error(client.getNickname()
					+ " tried to add room of unrecognizable type [" + roomType
					+ "]", e);
			client.sendMessage("No such type of room");
			
		}
	}

	private void switchRoom() {
		server.addUserToRoom(client, roomName);
	}

	private void changeTopic() {
		client.getPresentRoom().setChatTopic(roomTopic);
		client.getPresentRoom().annouceMessage(
				client.getNickname() + " changed topic to: " + roomTopic);
		logger.info(client.getNickname() + " changed topic to: " + roomTopic);
	}

	private void showTopic() {
		client.sendMessage("Ropic: " + client.getPresentRoom().getChatTopic());
	}

	private void showUsers() {
		client.sendMessage("Users in room " + roomName);
		for (ClientConnectionAdapter connection : client.getPresentRoom().clients) {
			client.sendMessage(connection.getNickname());
		}
	}

	private void showRooms() {
		for (Room room : server.getRooms().values()) {
			client.sendMessage("Room name: " + room.getRoomName()
					+ " | Topic: " + room.getChatTopic());
		}
	}

	private void help() {
		displayHelp();
	}

	private void exit() {
		client.getPresentRoom().removeClient(client);
		client.sendMessage("GoodBye!");
	}

	public void parseCommand(String args) throws ParameterException {
		String topic = args.replaceAll(".*\"(.+)\".*", "$1");
		args = args.replaceAll("\"(.*)\"", "topic");
		String[] paramaters = args.split(" ");
		command = paramaters[0].substring(1);
		try {
			new JCommander(this, Arrays.copyOfRange(paramaters, 1,
					paramaters.length));
			logger.info("Command from user " + client.getNickname()
					+ " parsed correctly");
			roomTopic = topic;
		} catch (ParameterException e) {
			logger.error("Unknow option provided by " + client.getNickname());
			client.sendMessage("No such option, use #help to check correct syntax");
			throw new ParameterException(e);
		}
		executeCommand();
	}

	public void executeCommand() {
		if (commandList.contains(command)) {
			try {
				Method method = ClientRequestHandler.class
						.getDeclaredMethod(command);
				method.invoke(this);
			} catch (NoSuchMethodException | SecurityException
					| IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				logger.error("Reflective invocation failed", e);
			}
		} else {
			client.sendMessage("No such command, use #help to check correct syntax");
		}
	}
}
