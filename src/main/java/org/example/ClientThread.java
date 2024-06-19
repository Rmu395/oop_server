package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;

public class ClientThread extends Thread {
    Socket client;
    Server server;
    PrintWriter writer;
    String clientName;

    public ClientThread(Socket client, Server server) {
        this.client = client;
        this.server = server;
    }

    public String getClientName() {
        return clientName;
    }

    public void run() {
        try {
            InputStream input = client.getInputStream();
            OutputStream output = client.getOutputStream();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input)
            );
            writer = new PrintWriter(output,true);

            String rawMessage;
            // "{"type": "", "content": ""}"

            while((rawMessage = reader.readLine()) != null) {
                Message message = new ObjectMapper()
                        .readValue(rawMessage, Message.class);

                switch (message.type) {
                    case Broadcast -> server.broadcast(message, this);
                    case Login -> login(message.content);
                    case Command -> commands(message);
                }
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(Message message) throws JsonProcessingException {
        String rawMessage = new ObjectMapper()
                .writeValueAsString(message);
        writer.println(rawMessage);
    }

    public void login(String name) throws JsonProcessingException {
        clientName = name;
        Message message = new Message(MessageType.Broadcast, name + " has joined the chat!");
        server.broadcast(message, this);
    }

    public void commands(Message command) throws JsonProcessingException {
        // /online          ->  /o
        // /w [receiver]    ->  /w
        // /disconnect      ->  /d
        switch (command.content.substring(0,2)) {
            case "/o" -> {
                if (command.content.equals("/online")) {    // to można (a nawet bym radził) usunąć lecz w zadaniu ewidentnie potrzeba "/online" więc dla tego to tu jest
                    server.clientList(this);
                }
            }
            case "/w" -> {
                int startPosition = 3;
                int endPosition = command.content.indexOf(' ', startPosition);
                String receiver = command.content.substring(startPosition, endPosition);
//                System.out.println("receiver -> ]" + receiver + "[");           //check

                command.content = command.content.substring(endPosition + 1);
//                System.out.println("content -> ]" + command.content + "[");    //check

                command.type = MessageType.Broadcast;
                server.direct(command, this, receiver);
            }
            case "/d" -> {
                if (command.content.equals("/disconnect")) {
                    Message disconnectMessage = new Message(MessageType.Broadcast, clientName + " has disconnected from the chat!");
                    server.broadcast(disconnectMessage, this);

                }
            }
        }
    }

}