package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Server {
    private ServerSocket socket;
    List<ClientThread> clients = new ArrayList<>();

    public Server(int port) throws IOException {
        socket = new ServerSocket(port);
    }

    public void listen() throws IOException {
        while (true) {
            Socket client = socket.accept();
            ClientThread thread = new ClientThread(client, this);
            clients.add((thread));
            thread.start();
        }
    }

    public void broadcast(Message message, ClientThread sender) throws JsonProcessingException {
        message.content = sender.clientName + ": " + message.content;
        for (ClientThread client: clients) {
            if (client != sender) {
                client.send(message);
            }
        }
    }

    public void direct(Message message, ClientThread sender, String receiver) throws JsonProcessingException {
        boolean found = false;
        System.out.println("in direct");
        for (ClientThread client: clients) {
            System.out.println("in for loop");
            if (client.clientName.equals(receiver) && client != sender) {
                System.out.println("in if");
                message.content += " [direct form: " + sender.clientName + "]";
                client.send(message);
                found = true;
            }
        }
        if (!found) {
            System.out.println("in not found");
            message.content += " - [not sent, receiver offline]";
            sender.send(message);
        }
    }
}
