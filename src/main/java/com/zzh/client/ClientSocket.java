package com.zzh.client;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

/**
 * The ClientSocket class handles the client's connection to a server using a socket.
 * It sends a request to the server and receives a response in the form of a JSON string,
 * which is then converted into a HashMap.
 *
 * <p>
 * Created by Zhonghe Zheng, Student ID: 825612.
 * </p>
 */
public class ClientSocket {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private int clientNumber;


    /**
     * Constructs a ClientSocket object with the specified IP address and port.
     * Initializes the input and output streams for communication with the server.
     *
     * @param ip   the IP address of the server
     * @param port the port number to connect to on the server
     * @throws IOException if an I/O error occurs when creating the socket
     */
    public ClientSocket(String ip, int port) throws IOException {
        this.socket = new Socket(ip, port);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        //Get thread number as User Number/ Relic of Thread per Connection Implementation
        this.clientNumber = reader.read();
        if (clientNumber == -1) {
            throw new IOException();
        }
    }


    /**
     * Sends a request to the server and receives a response.
     * The response is expected to be a JSON string, which is converted into a HashMap.
     *
     * @param request the request string to be sent to the server
     * @return a HashMap representing the server's JSON response
     * @throws IOException if an I/O error occurs during communication with the server
     */
    public HashMap<String, String> sendRequest(String request) throws IOException {
        //Send Request to Server
        writer.write(request + "\n");
        writer.flush();

        //Receive Response From Server
        String responseJSON = reader.readLine();
        HashMap<String, String> response = new ObjectMapper().readValue(responseJSON, HashMap.class);

        //Close Socket to save resources
        socket.close();

        return response;
    }


    @Override
    public String toString() {
        return "ClientSocket{" +
                "socket=" + socket +
                ", reader=" + reader +
                ", writer=" + writer +
                '}';
    }
}
