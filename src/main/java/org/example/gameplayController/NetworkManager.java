package org.example; // Adjust package name as needed

import java.io.*;
import java.net.Socket;

public class NetworkManager {
    private static final String HOST = "localhost";
    private static final int PORT = 1234;

    public String sendRequest(String request) {
        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(request);
            return in.readLine(); // Returns SUCCESS|... or ERROR|... from server

        } catch (IOException e) {
            e.printStackTrace();
            return "ERROR|Connection failed";
        }
    }
}