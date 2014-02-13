package main.java.skibiak.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

import org.ahocorasick.trie.Token;
import org.ahocorasick.trie.Trie;

public class CensoredRoom extends Room {

	/**
	 * Trie class provides the implementation of the Aho-Corasick algorithm,
	 * which is used to check whether the user has used curses.
	 */

	private final Trie trie;

	public CensoredRoom(Server server, String roomName, String chatTopic) {
		super(server, roomName, chatTopic);
		this.trie = new Trie().removeOverlaps().onlyWholeWords().caseInsensitive();
		try {
			this.addBannedWords("main/resources/wordsToAvoid.txt");
		} catch (IOException e) {
			logger.warn("No default file with swears",e);
		}
		this.startRoom();
	}

	public final void addBannedWords(String filePath) throws IOException {
		try (BufferedReader reader = new BufferedReader(
				new FileReader(filePath))) {
			String line = reader.readLine();
			while (line != null) {
				trie.addKeyword(line);
				line = reader.readLine();
			}
		}
	}

	private final String toAsterisks(String word){
		return word.replaceAll(".", "*");
	}

	public String censureMessage(String message) {
		Collection<Token> tokens = trie.tokenize(message);
		StringBuffer result = new StringBuffer();
		for (Token token : tokens) {
			if (token.isMatch()) {
				result.append(toAsterisks(token.getFragment()));
			} else {
				result.append(token.getFragment());
			}
		}
		return result.toString();
	}

	@Override
	public void annouceMessage(String message) {
		System.out.println(" MESSAGE [" + getRoomName() + "] " + message);
		message = censureMessage(message);
		for (ClientConnectionAdapter connection : this.getClients()) {
			connection.sendMessage(message);
		}
	}

}
