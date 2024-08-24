package com.zzh.server;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

//not necessary since dictionary hashmap is not accessed outside of this class
//import java.util.concurrent.ConcurrentHashMap;


/**
 * The Dictionary class manages a collection of words and their meanings.
 * It allows for creating, reading, updating, deleting, and appending word meanings in the dictionary.
 * The dictionary is stored as a JSON file, and this class provides synchronized methods
 * to ensure thread-safe operations.
 *
 * <p>
 * Created by Zhonghe Zheng, Student ID: 825612.
 * </p>
 */
public class Dictionary {
    private HashMap<String, String> dictionary;
    private String filePath;


    /**
     * Constructs a Dictionary object by reading the dictionary from a JSON file.
     *
     * @param filePath the path to the JSON file containing the dictionary data
     * @throws IOException if there is an error reading the file
     */
    public Dictionary(String filePath) throws IOException {
        this.dictionary = readJSONDictionary(filePath);
        this.filePath = filePath;
    }


    /**
     * Creates a new word entry in the dictionary.
     * If the word already exists, it returns an error response.
     *
     * @param key     the word to be added
     * @param meaning the meaning of the word
     * @return a response indicating success or failure of the operation
     */
    public synchronized HashMap<String, String> createWord(String key, String meaning) {
        HashMap<String, String> response = new HashMap<>();
        response.put("code", "ERROR");
        if (dictionary.containsKey(key)) {
            response.put("msg",
                         "(" + key + ") already exist in the Dictionary! You can try update or append new meaning(s).");
        } else if (meaning == null || meaning.isEmpty()) {
            response.put("msg", "(" + key + ") Word meaning(s) cannot be null or empty.");
        } else {
            dictionary.put(key, meaning);
            response.put("msg", "(" + key + ") Word meaning(s) successfully created.");
            response.put("code", "SUCCESS");

            HashMap<String, String> writeResponse = this.writeJSONDictionary();
            if (writeResponse != null) return writeResponse;
        }
        return response;
    }


    /**
     * Reads the meaning of a word from the dictionary.
     * If the word does not exist, it returns an error response.
     *
     * @param key the word to be read
     * @return a response containing the word's meaning or an error message
     */
    public synchronized HashMap<String, String> readWord(String key) {
        //Delay request to test for concurrent access
        for (int i = 0; i < 2; i++) {
            try {
                System.out.println(Thread.currentThread().getName() + " is Reading, time to wait:" + i + " seconds");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        HashMap<String, String> response = new HashMap<>();
        response.put("code", "ERROR");
        String value = this.dictionary.get(key);
        if (value == null || value.isEmpty()) {
            response.put("msg", "(" + key + ") does not exist in the Dictionary!");
        } else {
            response.put("msg", value);
            response.put("code", "SUCCESS");
        }
        return response;
    }


    /**
     * Updates the meaning of an existing word in the dictionary.
     * If the word or current meaning does not exist, it returns an error response.
     *
     * @param key            the word to be updated
     * @param curMeaning     the current meaning of the word
     * @param updatedMeaning the new meaning to replace the current meaning
     * @return a response indicating success or failure of the operation
     */
    public synchronized HashMap<String, String> updateWord(String key, String curMeaning, String updatedMeaning) {
        HashMap<String, String> response = new HashMap<>();
        response.put("code", "ERROR");
        //Server side validation for word and meanings
        if (!dictionary.containsKey(key)) {
            response.put("msg", "(" + key + ") does not exist in the Dictionary!");
        } else if (curMeaning == null || updatedMeaning == null || curMeaning.isEmpty() || updatedMeaning.isEmpty()) {
            response.put("msg", "Word meaning(s) cannot be empty.");
        } else {
            String originalString = this.dictionary.get(key);
            if (!originalString.contains(curMeaning)) {
                response.put("msg", "Existing meaning not found for the word specified!");
            } else {
                dictionary.put(key, originalString.replace(curMeaning, updatedMeaning));
                response.put("msg", "(" + key + ") has been updated successfully!");
                response.put("code", "SUCCESS");

                HashMap<String, String> writeResponse = this.writeJSONDictionary();
                if (writeResponse != null) return writeResponse;
            }
        }
        return response;
    }


    /**
     * Deletes a word from the dictionary.
     * If the word does not exist, it returns an error response.
     *
     * @param key the word to be deleted
     * @return a response indicating success or failure of the operation
     */
    public synchronized HashMap<String, String> deleteWord(String key) {
        HashMap<String, String> response = new HashMap<>();
        response.put("code", "ERROR");
        if (!dictionary.containsKey(key)) {
            response.put("msg", "(" + key + ") does not exist in the Dictionary!");
        } else {
            dictionary.remove(key);
            response.put("msg", "(" + key + ") has been deleted successfully!");
            response.put("code", "SUCCESS");

            HashMap<String, String> writeResponse = this.writeJSONDictionary();
            if (writeResponse != null) return writeResponse;
        }
        return response;
    }


    /**
     * Appends a new meaning to an existing word in the dictionary.
     * If the word does not exist or the meaning is empty, it returns an error response.
     *
     * @param key        the word to be appended
     * @param newMeaning the new meaning to add to the word
     * @return a response indicating success or failure of the operation
     */
    public synchronized HashMap<String, String> appendWord(String key, String newMeaning) {
        HashMap<String, String> response = new HashMap<>();
        response.put("code", "ERROR");

        if (!dictionary.containsKey(key)) {
            response.put("msg", "(" + key + ") does not exist in the Dictionary!");
        } else if (newMeaning == null || newMeaning.isEmpty()) {
            response.put("msg", "New word meaning cannot be empty.");
        } else {
            String originalString = this.dictionary.get(key);
            if (originalString.contains(newMeaning)) {
                response.put("msg", "Meaning already exist for the word specified!");
            } else {
                originalString += newMeaning;
                this.dictionary.put(key, originalString);
                response.put("msg", "New word meaning has been added to (" + key + ") successfully!");
                response.put("code", "SUCCESS");

                HashMap<String, String> writeResponse = this.writeJSONDictionary();
                if (writeResponse != null) return writeResponse;
            }
        }
        return response;
    }


    /**
     * Reads the dictionary from a JSON file.
     *
     * @param filePath the path to the JSON file
     * @return a HashMap representing the dictionary
     * @throws IOException if there is an error reading the file
     */
    private HashMap<String, String> readJSONDictionary(String filePath) throws IOException {
        return new ObjectMapper().readValue(new File(filePath), HashMap.class);
    }


    /**
     * Writes the current state of the dictionary to a JSON file.
     *
     * @return null if successful, otherwise a HashMap with an error code and message
     */
    private HashMap<String, String> writeJSONDictionary() {
        try {
            new ObjectMapper().writeValue(new File(this.filePath), dictionary);
            return null;
        } catch (IOException e) {
            HashMap<String, String> response = new HashMap<>();
            response.put("code", "ERROR");
            response.put("msg", "Server error in writing Dictionary file");
            e.printStackTrace();
            return response;
        }
    }


    @Override
    public String toString() {
        return "Dictionary{" +
                "dictionary=" + dictionary +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
