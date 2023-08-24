package ua.desktop.chat.messenger.domain.client;

import ua.desktop.chat.messenger.domain.ifaces.Closable;
import ua.desktop.chat.messenger.domain.util.ClosableImpl;
import ua.desktop.chat.messenger.models.User;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

final public class Client {
    private final Closable closable = new ClosableImpl();
    private final Socket socket;
    private final User user;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;

    public Client(Socket socket) {
        try {
            this.socket = socket;
            this.user = createUser();
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printWriter = new PrintWriter(socket.getOutputStream(), true);

        } catch (IOException e) {
            closable.closeBufferReader(bufferedReader);
            closable.closeBufferWriter(printWriter);
            closable.closeSocket(socket);
            throw new RuntimeException(e);
        }
    }

    public void sendMessage() {

        printWriter.println(user.getUsername());
        printWriter.flush();

        Scanner scanner = new Scanner(System.in);
        while (socket.isConnected()) {
            if (scanner.hasNext()) {
                String messageClient = scanner.nextLine();
                printWriter.println(user.getUsername().concat(": ").concat(messageClient));
                printWriter.flush();
            }
        }
        scanner.close();
    }

    public void listenMessageFromClients() {
        new Thread(() -> {
            String messageFromClient = null;

            while (socket.isConnected()) {
                try {
                    messageFromClient = bufferedReader.readLine();
                    if (messageFromClient != null) {
                        System.out.println(messageFromClient);
                    }
                } catch (IOException e) {
                    closable.closeBufferReader(bufferedReader);
                    closable.closeBufferWriter(printWriter);
                    closable.closeSocket(socket);
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private User createUser() throws IOException {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your username: ");
            String username = scanner.nextLine();

//        System.out.print("Enter your email: ");
//        String email = scanner.nextLine();
//
//        System.out.print("Enter your password: ");
//        String password = scanner.nextLine();
            return new User(username, null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        try {
            Socket s = new Socket("localhost", 8086);
            Client client = new Client(s);
            client.listenMessageFromClients();
            client.sendMessage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
