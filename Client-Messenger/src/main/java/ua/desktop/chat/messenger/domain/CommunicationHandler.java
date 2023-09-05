package ua.desktop.chat.messenger.domain;

import ua.desktop.chat.messenger.models.Message;
import ua.desktop.chat.messenger.models.User;
import ua.desktop.chat.messenger.parser.ParserJSON;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class CommunicationHandler implements Runnable {
	private Boolean isActive = true;
	private Socket s = new Socket();
	private PrintWriter s_out = null;
	private BufferedReader s_in = null;
	private Client client;
	private Boolean isConnected = false;
	
	public void run(){
		try {

			Client client = new Client(this);
			Thread thread = new Thread(client);
			thread.start();

			while(isActive){
				System.out.println(isConnected);
				if(isConnected){
					System.out.println("isConnected is true");
					String response;
					while((response = s_in.readLine()) != null){
						System.out.println("response: "+ response);
						if(response.equals("/GAU")){
							String uList;
							while((uList = s_in.readLine()) != null){
								System.out.println("TEST Userlist");
								ArrayList<String> userList = setUserList(uList);
								System.out.println("Response:" + userList);
								client.updateUserListChatGUI(userList);
						    	if(uList.contains("[END]")){
						    		break;
						    	}
							}
						}
						else if(response.equals("/M")){
							String messageResponse;
							while((messageResponse = s_in.readLine()) != null){
								Object parsedObject = ParserJSON.convertStringToObject(messageResponse);
								System.out.println(parsedObject);
								if (parsedObject instanceof Message) {
									System.out.println("parsedObject->Message");
									Message message = (Message) parsedObject;
									client.updateMessageChatGUI(message);
								} else if (parsedObject instanceof String) {
									System.out.println("parsedObject->String");
									String textMessage = (String) parsedObject;
									client.updateMessageChatGUI(textMessage);
								}
								System.out.println("parsedObject->Object");
								break;
							}
							System.out.println("Message Test");
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> setUserList(String rspUserList){
		ArrayList<String> parts = new ArrayList<>(Arrays.asList(rspUserList.split(":")));
		parts.remove("[END]"); // Arraylist
		return parts;
	}
	
	public synchronized Boolean getIsConnected() {
		return isConnected;
	}

	public synchronized void setIsConnected(Boolean isConnected) {
		this.isConnected = isConnected;
	}

	public synchronized void setActive(Boolean active) {
		isActive = active;
	}

	public synchronized Socket getS() {
		return s;
	}

	public synchronized void setS(Socket s) {
		this.s = s;
	}
	
	public synchronized PrintWriter getS_out() {
		return s_out;
	}

	public synchronized void setS_out(PrintWriter s_out) {
		this.s_out = s_out;
	}

	public synchronized BufferedReader getS_in() {
		return s_in;
	}

	public synchronized void setS_in(BufferedReader s_in) {
		this.s_in = s_in;
	}
	
	public synchronized Client getClient() {
		return client;
	}

	public synchronized void setClient(Client client) {
		this.client = client;
	}
}
