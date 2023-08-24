package ua.desktop.chat.messenger.domain.server;

import ua.desktop.chat.messenger.domain.ifaces.Closable;
import ua.desktop.chat.messenger.domain.util.ClosableImpl;
import ua.desktop.chat.messenger.models.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

final public class Server {
    private final ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();

                ClientHandler clientHandler = new ClientHandler(socket);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            closeServerSocket();
            throw new RuntimeException(e);
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        try {
            ServerSocket sc = new ServerSocket(8086);
            Server server = new Server(sc);
            server.startServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    final private static class ClientHandler implements Runnable {
        private final static List<ClientHandler> clientHandlers = new ArrayList<>();
        private final Closable closable = new ClosableImpl();
        private final Socket socket;
        private BufferedReader bufferedReader;
        private PrintWriter printWriter;
        private final User user;

        public ClientHandler(Socket socket) {
            try {
                this.socket = socket;
                printWriter = new PrintWriter(socket.getOutputStream(), true);
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                user = readUser();

                clientHandlers.add(this);

                broadcastMessage("SERVER".concat(": ")
                        .concat(user.getUsername()
                                .concat(" was added into world chat! Welcome!!!"))
                );

                System.out.println("SERVER".concat(": ")
                        .concat(user.getUsername()
                                .concat(" was added into world chat!"))
                        .concat("\tTime added into chat: ")
                        .concat(String.valueOf(LocalDateTime.now())));

            } catch (IOException e) {
                removeClientHandler();
                closable.closeBufferReader(bufferedReader);
                closable.closeBufferWriter(printWriter);
                closable.closeSocket(socket);
                throw new RuntimeException(e);
            }
        }

        private void broadcastMessage(String message) {
            for (ClientHandler clientHandler : clientHandlers) {
                if (!clientHandler.user.getUsername().equals(this.user.getUsername())) {
                    clientHandler.printWriter.println(message);
                    clientHandler.printWriter.flush();
                }
            }
        }

        private User readUser() throws IOException {
            String username = bufferedReader.readLine();
//            System.out.print("Enter your email: ");
            String email = null;

//            System.out.print("Enter your password: ");
            String password = null;

            return new User(username, email, password);
        }

        @Override
        public void run() {
            String messageFromClient = null;

            try {
                while ((messageFromClient = bufferedReader.readLine()) != null) {
                    broadcastMessage(messageFromClient);
                }

            } catch (IOException e) {
                removeClientHandler();
                closable.closeBufferReader(bufferedReader);
                closable.closeBufferWriter(printWriter);
                closable.closeSocket(socket);
                throw new RuntimeException(e);
            }
        }

        private void removeClientHandler() {
            clientHandlers.remove(this);
            broadcastMessage(getClass().getSimpleName()
                    .concat(": ").concat(user.getUsername()
                            .concat(" was left the chat! Goodbye!")));
        }
    }
}
