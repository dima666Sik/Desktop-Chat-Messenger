package ua.desktop.chat.messenger.main;

import ua.desktop.chat.messenger.client.service.CommunicationHandler;
import ua.desktop.chat.messenger.core.service.impl.ChatSystemHandlerImpl;
import ua.desktop.chat.messenger.core.service.impl.MessageSystemHandlerImpl;

public class MainClient {

	public static void main(String[] args) {
		Runnable cH = new CommunicationHandler(new ChatSystemHandlerImpl(), new MessageSystemHandlerImpl());
		Thread thread = new Thread(cH);
		thread.start();
	}
	
}
