package ua.desktop.chat.messenger.domain;

public class Main {

	public static void main(String[] args) {
		Runnable cH = new CommunicationHandler();
		Thread thread = new Thread(cH);
		thread.start();
	}
	
}
