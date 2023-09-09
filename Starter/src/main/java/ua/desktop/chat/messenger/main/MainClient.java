package ua.desktop.chat.messenger.main;

import ua.desktop.chat.messenger.domain.CommunicationHandler;
import ua.desktop.chat.messenger.domain.impl.ChatSystemHandlerImpl;
import ua.desktop.chat.messenger.domain.impl.MessageSystemHandlerImpl;

public class MainClient {

	public static void main(String[] args) {
		Runnable cH = new CommunicationHandler(new ChatSystemHandlerImpl(), new MessageSystemHandlerImpl());
		Thread thread = new Thread(cH);
		thread.start();
	}
	
}
