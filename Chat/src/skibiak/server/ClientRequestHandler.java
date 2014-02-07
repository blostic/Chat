package skibiak.server;

import java.util.Arrays;

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

	private void displayHelp() {
		client.sendMessage("#CreateRoom  [-n|-name] roomName [-T|-topic] roomTopic [-t|-type] roomType");
		client.sendMessage("#SwitchRoom  [-n|-name] roomName");
		client.sendMessage("#ChangeTopic [-T|-topic] roomTopic");
		client.sendMessage("#ShowTopic");
		client.sendMessage("#ShowRooms");
		client.sendMessage("#ShowUsersInRoom");
		client.sendMessage("#HELP");
		client.sendMessage("roomType = [public|censored]");
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
		} catch (ParameterException e) {
			logger.error("Unknow option provided by " + client.getNickname());
			throw new ParameterException(e);
		}

		switch (command.toLowerCase()) {
		case "createroom":
			try {
				if (roomName.length() > 1 && roomType.length() > 1
						&& topic.length() > 1) {
					Room room = RoomFactory.getInstance(server, roomName,
							roomType, topic, client.getNickname());
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
						+ " tried to add room of unrecognizable type ["
						+ roomType + "]");
				client.sendMessage("No such type of room");
			}
			break;
		case "switchroom":
			server.addUserToRoom(client, roomName);
			break;
		case "changetopic":
			client.getPresentRoom().setChatTopic(topic);
			logger.info(client.getNickname() + " changed topic to: " + topic);
			break;
		case "showtopic":
			client.sendMessage(client.getPresentRoom().getChatTopic());
			break;
		case "showusersinroom":
			for (ClientConnectionAdapter pClient : client.getPresentRoom().clients) {
				client.sendMessage(pClient.getNickname());
			}
			break;
		case "showrooms":
			for (Room room : server.getRooms().values()) {
				client.sendMessage(room.getRoomName() + ": "
						+ room.getChatTopic());
			}
			break;
		case "help":
			logger.info(client.getNickname() + " used help");
			displayHelp();
			break;
		default:
			client.sendMessage("No such command, use #HELP to check correct syntax");
			break;
		}

	}
}
