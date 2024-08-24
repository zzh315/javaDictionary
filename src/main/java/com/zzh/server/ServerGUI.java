package com.zzh.server;

import com.zzh.server.pool.DicPool;

import javax.net.ServerSocketFactory;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * The ServerGUI class provides a graphical user interface for managing a server
 * that handles dictionary operations using a thread pool. The server can be started
 * and stopped through this interface, and the logs of server activity are displayed.
 *
 * <p>
 * Created by Zhonghe Zheng, Student ID: 825612.
 * </p>
 */
public class ServerGUI extends JDialog {
    private JButton closeServer;
    private JButton startServer;
    private JPanel panel;
    private JTextField threadWorkerText;
    private JLabel threadWorkerLabel;
    private JTextField queueText;
    private JLabel queueLabel;
    private JTextArea logs;
    private static int counter = 0;


    public static void main(String[] args) {
        //Default port and filePath
        int port = 8080;
        String dicFilePath = "dictionary.json";

        // Validate command-line arguments if provided
        if (args.length > 0) {
            String[] validatedArgs = validateArguments(args);
            port = Integer.parseInt(validatedArgs[0]);
            dicFilePath = validatedArgs[1];
        }

        // Initialize the dictionary
        Dictionary dictionary = null;
        try {
            dictionary = new Dictionary(dicFilePath);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                                          "Dictionary File Is Not In Correct JSON Format, Please Specify A New File",
                                          "Error",
                                          JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }


        //Start Server GUI
        new ServerGUI(null, port,dictionary);
    }


    /**
     * Constructor for ServerGUI object and initializes the GUI components.
     * Sets up action listeners for the start and close server buttons.
     *
     * @param parent the parent frame
     * @param port   server port
     * @param dictionary Dictionary Object
     */
    public ServerGUI(Frame parent, int port, Dictionary dictionary) {
        super(parent);
        setTitle("Server Thread Pool Control");
        setContentPane(panel);
        setModal(true);
        pack();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent);
        setMinimumSize(new Dimension(100, 100));

        //Set Border Title
        TitledBorder title = BorderFactory.createTitledBorder("Server Thread Pool Control");
        title.setTitleJustification(TitledBorder.LEFT);  // You can set the title to be centered
        title.setTitlePosition(TitledBorder.TOP); // Set the position of the title
        panel.setBorder(title);

        // Set up the start server button action
        startServer.addActionListener(e -> handleStartServer(port,dictionary));

        // Set up the close server button action
        closeServer.addActionListener(e -> System.exit(0));

        setVisible(true);
    }


    /**
     * Handles the action of starting the server when the start button is clicked.
     * Validates input fields, initializes the thread pool, and starts the server in a new thread.
     *
     * @param port   server port
     * @param dictionary Dictionary Object
     */
    private void handleStartServer(int port, Dictionary dictionary) {
        startServer.setEnabled(false);
        closeServer.setEnabled(true);
        threadWorkerText.setEditable(false);
        queueText.setEditable(false);
        setTitle("Server Running");

        appendLogs("Server Running\n");

        int workerNum;
        int queueNum;
        try {
            workerNum = Integer.parseInt(threadWorkerText.getText());
            queueNum = Integer.parseInt(queueText.getText());
            if (workerNum < 1 || queueNum < 1) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(panel,
                                          "Please Enter a Valid Number for Worker Thread or Queue Size",
                                          "Error",
                                          JOptionPane.ERROR_MESSAGE);
            return;
        }

        DicPool dicPool = new DicPool(workerNum, queueNum, this);
        new Thread(() -> startServer(port,dictionary, dicPool)).start();
    }


    /**
     * Starts the server and continuously listens for client connections.
     * Creates a new thread from the pool for each incoming client connection.
     *
     * @param port   server port
     * @param dictionary Dictionary Object
     * @param dicPool the thread pool for handling client connections
     */
    private void startServer(int port,Dictionary dictionary, DicPool dicPool) {
        // Create a ServerSocketFactory instance to create the ServerSocket
        ServerSocketFactory factory = ServerSocketFactory.getDefault();

        // Try to create a ServerSocket on the specified port, automatically close
        try (ServerSocket server = factory.createServerSocket(port)) {
            System.out.println("Waiting for client connection-");

            // Continuously wait for client connections
            while (true) {
                // Accept a client connection; returns a Socket object representing the client
                Socket clientSocket = server.accept();
                counter++;
                System.out.println("Reqeuest: " + counter + " Connected");

                // Handle the client connection using the custom thread pool
                try {
                    dicPool.execute(new SocketRunner(counter, dictionary, clientSocket));
                } catch (IllegalStateException e) {
                    JOptionPane.showMessageDialog(null, "A New Client Connection Rejected: Exceeded Maximum ServerLoad",
                                                  "Warning",
                                                  JOptionPane.ERROR_MESSAGE);
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            // Handle any IOExceptions that may occur
            JOptionPane.showMessageDialog(null, "Error In Socket Creation/Connection, Please Try Again", "Error",
                                          JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


    /**
     * Validates the command-line arguments for server port and dictionary file path.
     * Ensures that the port is within the valid range and the dictionary file exists.
     *
     * @param args command-line arguments (server port and dictionary file path)
     * @return a string array containing the validated port and file path
     */
    private static String[] validateArguments(String[] args) {
        int port = 8080;
        String dicFilePath;
        // Check if the correct number of arguments are passed
        if (args.length != 2) {
            showErrorDialog("Usage: java -jar DictionaryServer.jar <port> <dictionary-file>", "Invalid Arguments");
        }

        // Validate the port number
        try {
            port = Integer.parseInt(args[0]);
            if (port < 1025 || port > 65535) {
                throw new NumberFormatException("Port out of range");
            }
        } catch (NumberFormatException e) {
            showErrorDialog("Error: Invalid port number. Must be an integer between 1025 and 65535.",
                            "Invalid Port Number");
        }

        // Validate the dictionary file path
        dicFilePath = args[1];
        File file = new File(dicFilePath);
        if (!file.exists() || file.isDirectory()) {
            showErrorDialog("Error: Dictionary file does not exist or is a directory: " + dicFilePath,
                            "Invalid File Path");
        }

        return new String[]{String.valueOf(port), dicFilePath};
    }


    /**
     * Displays an error dialog with the specified message and title, and then exits the application.
     * This method is used to handle critical errors where the application cannot continue running.
     *
     * @param message the error message to display
     * @param title   the title of the error dialog
     */
    private static void showErrorDialog(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }


    /**
     * Appends the given text to the logs text area in the GUI.
     *
     * @param logs the text to append to the logs
     */
    public void appendLogs(String logs) {
        this.logs.setText(this.logs.getText() + logs);
    }


    //Getter and Setters
    public JTextArea getLogs() {
        return logs;
    }

    public JPanel getPanel() {
        return panel;
    }
}
