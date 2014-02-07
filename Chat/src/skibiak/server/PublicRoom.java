package skibiak.server;

import java.io.IOException;

import com.beust.jcommander.ParameterException;

public class PublicRoom extends Room {
	public PublicRoom(Server server, String roomName, String chatTopic,
			String roomMaster) {
		super(server, roomName, chatTopic, roomMaster);
		this.startRoom();
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(100);
				this.readMessages();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void annouceMessage(String message) {
		System.out.println("RECEIVER MESSAGE [room " + getRoomName() + "] "
				+ message);
		for (ClientConnectionAdapter connection : clients) {
			connection.sendMessage(message);
		}
	}

	@Override
	public void readMessages() throws IOException {
		for (ClientConnectionAdapter connection : clients) {
			if (connection.containMessage()) {
				String message = connection.readMessage();
				if (!message.startsWith("#")) {
					annouceMessage(connection.getNickname() + ": " + message);
				} else {
					new ClientRequestHandler(server, connection).parseCommand(message);
				}
		}
		}
	}

}
