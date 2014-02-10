package skibiak.server;

import java.io.IOException;

import com.beust.jcommander.ParameterException;

public class PublicRoom extends Room {
	public PublicRoom(Server server, String roomName, String chatTopic) {
		super(server, roomName, chatTopic);
		this.startRoom();
	}

	@Override
	public void run() {
		while (server.isActive()) {
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
		System.out.println("MESSAGE [" + getRoomName() + "] "
				+ message);
		for (ClientConnectionAdapter connection : this.getClients()) {
			connection.sendMessage(message);
		}
	}

	@Override
	public void readMessages() throws IOException {
		for (ClientConnectionAdapter connection : this.getClients()) {
			if (connection.containMessage()) {
				String message = connection.readMessage();
				if (!message.startsWith("#")) {
					annouceMessage(connection.getUsername() + ": "+ message);
				} else {
					try {
						new ClientRequestHandler(server, connection)
								.executeCommand(message);
					} catch (ParameterException e) {
						connection.sendMessage(">No such option, use #help to check correct syntax");
					}
				}
			}
		}
	}

}
