package ua.desktop.chat.messenger.main;

import ua.desktop.chat.messenger.domain.CommunicationHandler;

public class MainClient {

	public static void main(String[] args) {
		Runnable cH = new CommunicationHandler();
		Thread thread = new Thread(cH);
		thread.start();
	}
	
}
