package org.example.server;

import org.example.db.DatabaseManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer {
    private static final int PORT = 1234;



    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("--- Wallet Wilderness Server Started ---");
            System.out.println("Listening on port: " + PORT);

            while (true) {

                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket); // Create a ClientHandler instance

                Thread clientThread = new Thread(handler); // Create a new Thread with the handler

                clientThread.start(); // Start the thread to handle client communication
            }
        } catch (IOException e) {
            System.err.println("Server Error: " + e.getMessage());
        }
    }
}