package ua.desktop.chat.messenger.closable.ifaces;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.net.Socket;

public interface Closable {
    void closeBufferReader(BufferedReader bufferedReader);
    void closeBufferWriter(PrintWriter printWriter);
    void closeSocket(Socket socket);
}
