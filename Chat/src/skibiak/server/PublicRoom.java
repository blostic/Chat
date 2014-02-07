package skibiak.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.ParameterException;

public class PublicRoom extends Room {
	public PublicRoom(Server server, String roomName, String chatTopic, String roomMaster) {
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
		System.out.println("RECEIVER MESSAGE [room " + this.getRoomName() + "] " + message);
		for (ClientConnectionAdapter clientConnection : this.clients) {
			clientConnection.sendMessage(message);
		}
	}

	@Override
	public void readMessages() throws IOException {
		for (ClientConnectionAdapter clientConnection : this.clients) {
			if (clientConnection.containMessage()) {
				String message = clientConnection.readMessage();
				if (!message.startsWith("#")){
					annouceMessage(clientConnection.getNickname() + ": " +message);					
				}else{
					try {
						new ClientRequestHandler(server, clientConnection)
								.parseCommand(message);
					} catch (ParameterException e) {
						clientConnection
								.sendMessage("No such option(s), use #HELP to check correct syntax");
					}
				}
			}
		}
	}

	@Override
	public void removeUnresponsiveClients() {
		List<ClientConnectionAdapter> clientsToRemove = new ArrayList<ClientConnectionAdapter>();
		for (ClientConnectionAdapter clientConnection : this.clients) {
			if (clientConnection.clientDisconected()) {
				clientsToRemove.add(clientConnection);
			}
		}
		this.clients.removeAll(clientsToRemove);

	}
}
