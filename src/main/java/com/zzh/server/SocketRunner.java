package com.zzh.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


/**
 * The SocketRunner class implements Runnable and handles the communication between the server
 * and a single client. It processes incoming requests from the client, interacts with the
 * Dictionary, and sends responses back to the client.
 *
 * <p>
 * Created by Zhonghe Zheng, Student ID: 825612.
 * </p>
 */
public class SocketRunner implements Runnable {
    private int requestCounter;
    private Dictionary dictionary;
    private Socket clientSocket;


    /**
     * Constructs a SocketRunner with the specified thread number, dictionary, and client socket.
     *
     * @param requestCounter the number identifying this thread
     * @param dictionary   the Dictionary instance to be used for processing requests
     * @param clientSocket the client socket for communication
     */
    public SocketRunner(int requestCounter, Dictionary dictionary, Socket clientSocket) {
        this.requestCounter = requestCounter;
        this.dictionary = dictionary;
        this.clientSocket = clientSocket;
    }


    /**
     * The main run method that handles client-server communication.
     * It reads the client's request, processes it using the Dictionary, and sends back a response.
     * This method also manages I/O streams and ensures resources are properly closed.
     */
    @Override
    public void run() {
        // Create ObjectMapper instance for JSON parsing
        ObjectMapper objectMapper = new ObjectMapper();

        BufferedWriter writer = null;
        BufferedReader reader = null;
        Map<String, String> requestMap = null;

        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                                          "Thread-" + requestCounter + ": Thread IO Stream Error",
                                          "Error",
                                          JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            //Send thread number as client(User) Number
            writer.write(this.requestCounter);
            writer.flush();

            // Read JSON from the client
            String JSONLine = "";
            String JSONRequest = "";
            while ((JSONLine = reader.readLine()) != null) {
                JSONRequest += JSONLine;
                if (JSONLine.trim().endsWith("}")) {
                    break;
                }
            }

            // Convert the received data to a Map
            requestMap = objectMapper.readValue(JSONRequest, HashMap.class);
            HashMap<String, String> responseMap = handleRequest(requestMap);
            String responseJSON = new ObjectMapper().writeValueAsString(responseMap);

            // Send the response back to the client in string representation of JSON
            writer.write(responseJSON + "\n");
            writer.flush();
        } catch (JsonProcessingException e) {
            JOptionPane.showMessageDialog(null,
                                          "Error In Processing JSON, Please Make Sure File Has Correct Dictionary Structure",
                                          "Error",
                                          JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                                          "Error In Client Server Communication! Client Number Affected: " + requestCounter,
                                          "Error",
                                          JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            // Close Resources and Socket
            try {
                if (reader != null) reader.close();
                if (writer != null) writer.close();
                if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
                System.out.println("Request Number: " + requestCounter + " Disconnected");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                                              "Error closing resources for thread " + requestCounter,
                                              "Error",
                                              JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }


    /**
     * Processes the client's request by determining the action (e.g., CREATE, READ, UPDATE, DELETE, APPEND)
     * and interacting with the Dictionary accordingly. Returns the response as a HashMap.
     *
     * @param requestMap the request data sent by the client
     * @return a HashMap containing the response data
     */
    private HashMap<String, String> handleRequest(Map<String, String> requestMap) {
        String action = requestMap.get("action");
        String key = requestMap.get("word");
        String meaning = requestMap.get("meaning");
        String specialMeaning = requestMap.get("specialMeaning");

        HashMap<String, String> responseMap = new HashMap<>();
        responseMap.put("code", "ERROR");
        responseMap.put("msg", "There is something wrong, please try again");

        //Server side validation for request action and word key
        if (action == null || key == null || action.isEmpty() || key.isEmpty()) {
            responseMap.put("msg", "Word can not be empty, please try again");
            return responseMap;
        }


        responseMap = switch (action) {
            case "CREATE" -> this.dictionary.createWord(key, meaning);
            case "READ" -> this.dictionary.readWord(key);
            case "UPDATE" -> this.dictionary.updateWord(key, meaning, specialMeaning);
            case "DELETE" -> this.dictionary.deleteWord(key);
            case "APPEND" -> this.dictionary.appendWord(key, meaning);
            default -> responseMap;
        };

        return responseMap;
    }

    @Override
    public String toString() {
        return "SocketRunner{" +
                "requestCounter='" + requestCounter + '\'' +
                ", dictionary=" + dictionary +
                ", clientSocket=" + clientSocket +
                '}';
    }
}
