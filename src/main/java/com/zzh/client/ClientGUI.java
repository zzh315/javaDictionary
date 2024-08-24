package com.zzh.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * The ClientGUI class provides a graphical user interface for a dictionary client.
 * It allows users to perform actions such as CREATE, READ, UPDATE, DELETE, and APPEND
 * on a dictionary server using a TCP connection.
 *
 * <p>
 * Created by Zhonghe Zheng, Student ID: 825612.
 * </p>
 */
public class ClientGUI extends JDialog {
    private JPanel panel;
    private JTextArea dicDisplayText;
    private JComboBox choseAction;
    private JLabel choseActionLabel;
    private JButton submitButton;
    private JTextField wordKeyText;
    private JTextField meaningText;
    private JTextField specialMeaningText;
    private JLabel wordKeyLabel;
    private JLabel meaningLabel;
    private JLabel specialMeaningLabel;
    private JLabel dicDisplayLabel;
    private String actionType;

    public static void main(String[] args) {
        // Start Server GUI---
        new ClientGUI(null, args);
    }

    /**
     * Constructs a ClientGUI object and initializes the GUI components.
     *
     * @param parent the parent frame(Null for this project)
     * @param args   command-line arguments (server IP and port)
     */
    public ClientGUI(Frame parent, String[] args) {
        super(parent);
        setTitle("Dictionary Client");
        setContentPane(panel);
        setModal(true);
        pack();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent);

        String[] validatedArgs = validateArguments(args);
        String ip = validatedArgs[0];
        int port = Integer.parseInt(validatedArgs[1]);

        // Set up the submit button action
        submitButton.addActionListener(e -> handleSubmitAction(ip, port));

        // Set up the action type combo box listener
        choseAction.addActionListener(e -> handleActionSelection());
        setVisible(true);
    }


    /**
     * Handles the submit action when the submit button is clicked.
     * Validates the input fields, constructs a JSON request, and sends it to the server.
     * Displays the server response in the text area or shows an error dialog if something goes wrong.
     *
     * @param ip   the IP address of the server
     * @param port the port number to connect to on the server
     */
    private void handleSubmitAction(String ip, int port) {
        String word = wordKeyText.getText();
        String meaning = meaningText.getText();
        String specialMeaning = specialMeaningText.getText();
        String actionType = choseAction.getSelectedItem().toString();

        // Client-side validation
        if (word == null || word.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "Word Can Not Be Empty", "Illegal Input",
                                          JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (actionType.equals("APPEND") || actionType.equals("UPDATE") || actionType.equals("CREATE")) {
            if (meaning == null || meaning.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Meaning Can Not Be Empty", "Illegal Input",
                                              JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        if (actionType.equals("UPDATE") && (specialMeaning == null || specialMeaning.isEmpty())) {
            JOptionPane.showMessageDialog(panel, "Special Meaning Can Not Be Empty", "Illegal Input",
                                          JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create JSON for submission to the server
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("word", word.toLowerCase());
        requestMap.put("meaning", meaning);
        requestMap.put("specialMeaning", specialMeaning);
        requestMap.put("action", actionType);

        // Create a new TCP client socket
        ClientSocket client;
        try {
            client = new ClientSocket(ip, port);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(panel, "Server Can Not Be Connected, Please Try Again Later And Make Sure Client and Check IP Address or Port Number Are Correct", "Connection Error",
                                          JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Send request and receive response from the server
        HashMap<String, String> response;
        try {
            String requestJSON = new ObjectMapper().writeValueAsString(requestMap);
            response = client.sendRequest(requestJSON);
        } catch (JsonProcessingException ex) {
            JOptionPane.showMessageDialog(panel, "Trouble Parsing Inputs to JSON, Please Try Again",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(panel, "Trouble Closing Socket", "Error",
                                          JOptionPane.ERROR_MESSAGE);
            return;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(panel, "Trouble Reading/Writing to Server, Exiting Client",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        String responseCode = response.get("code");
        String responseMsg = response.get("msg");
        if (responseCode.equals("SUCCESS")) {
            // Update TextField
            dicDisplayText.setText(responseMsg);
        } else {
            JOptionPane.showMessageDialog(panel, responseMsg, "Try Again",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * Handles the selection of an action type from the combo box.
     * Prevent user from inputting unrelated text box.
     * Adjusts the visibility of input fields based on the selected action type.
     */
    private void handleActionSelection() {
        String actionType = choseAction.getSelectedItem().toString();
        meaningLabel.setText("Meaning");
        clearTextFields();
        specialMeaningLabel.setVisible(false);
        specialMeaningText.setVisible(false);

        switch (actionType) {
            case "CREATE":
                meaningText.setVisible(true);
                meaningLabel.setVisible(true);
                break;
            case "READ":
                meaningText.setVisible(false);
                meaningLabel.setVisible(false);
                break;
            case "UPDATE":
                meaningLabel.setText("Old Meaning");
                meaningText.setVisible(true);
                meaningLabel.setVisible(true);
                specialMeaningText.setVisible(true);
                specialMeaningLabel.setVisible(true);
                break;
            case "DELETE":
                meaningText.setVisible(false);
                meaningLabel.setVisible(false);
                break;
            case "APPEND":
                meaningText.setVisible(true);
                meaningLabel.setVisible(true);
                break;
        }
    }


    /**
     * Iterates over all components in the panel and clears the text
     * for any JTextField or JTextArea found.
     */
    private void clearTextFields() {
        for (Component component : this.panel.getComponents()) {
            if (component instanceof JTextField || component instanceof JTextArea) {
                ((JTextComponent) component).setText("");  // Clear the text field
            }
        }
    }


    /**
     * Validates the command-line arguments and returns an array containing the server IP and port.
     * If no arguments are provided, it returns default values. This method also handles
     * basic validation for the IP address and port number, showing an error dialog if necessary.
     *
     * @param args command-line arguments (server IP and port)
     * @return a string array containing the validated IP and port
     */
    private String[] validateArguments(String[] args) {
        // Default values
        String ip = "localhost";
        int port = 8080;

        // Check if the correct number of arguments are passed
        if (args.length == 0) {
            return new String[]{ip, String.valueOf(port)};
        }
        if (args.length != 2) {
            showErrorDialog("Usage: java -jar DictionaryClient.jar <server-address> <server-port>",
                            "Invalid Arguments");
        }

        // Validate the IP address
        if (!isValidIPAddress(args[0])) {
            showErrorDialog("Error: Invalid IP address or hostname: " + args[0], "Invalid IP Address");
        }

        // Validate the port number
        try {
            port = Integer.parseInt(args[1]);
            if (port < 1025 || port > 65535) {
                throw new NumberFormatException("Port out of range");
            }
        } catch (NumberFormatException e) {
            showErrorDialog("Error: Invalid port number. Must be an integer between 1025 and 65535.",
                            "Invalid Port Number");
        }

        return new String[]{ip, String.valueOf(port)};
    }


    /**
     * Displays an error dialog with the specified message and title, and then exits the application.
     *
     * @param message the error message to display
     * @param title   the title of the error dialog
     */
    private void showErrorDialog(String message, String title) {
        JOptionPane.showMessageDialog(panel, message, title, JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }


    /**
     * Validates whether the given IP address or hostname is valid.
     * This method allows localhost, valid hostnames, and valid IP addresses.
     *
     * @param ip the IP address or hostname to validate
     * @return true if the IP address or hostname is valid, false otherwise
     */
    private boolean isValidIPAddress(String ip) {
        // Basic validation for an IP address or hostname
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        // Allow localhost or any valid hostname/IP address
        return ip.equals("localhost") || ip.matches("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$");
    }
}
