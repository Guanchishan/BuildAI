package de.kel0002.buildai.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;

public class ResponseFormatter {
    public static String extractResponseField(String jsonResponse) {
        try {
            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();

            String legacyResponse = getString(root, "response");
            if (legacyResponse != null) {
                return legacyResponse;
            }

            JsonArray choices = root.getAsJsonArray("choices");
            if (choices == null || choices.size() == 0) {
                return null;
            }

            JsonObject firstChoice = choices.get(0).getAsJsonObject();
            JsonObject message = firstChoice.getAsJsonObject("message");
            if (message != null) {
                String content = getString(message, "content");
                if (content != null) {
                    return content;
                }
            }

            return getString(firstChoice, "content");
        } catch (Exception e) {
            return null;
        }
    }

    private static String getString(JsonObject object, String fieldName) {
        if (object == null || !object.has(fieldName)) {
            return null;
        }
        JsonElement element = object.get(fieldName);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            String value = element.getAsString();
            return value == null || value.isEmpty() ? null : value;
        }
        return null;
    }

    public static ArrayList<String> extractResponseLines(String responseString) {
        String[] lines = responseString.split("[/\\n\\r]+");
        ArrayList<String> linesList = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();

            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("/")) {
                line = line.substring(1).trim();
            }

            while (line.endsWith("\\n") || !endsWithAny(line, getLettersAndNumbers()) && !(line.isEmpty())) {
                if (line.endsWith("\\n")) {
                    line = line.substring(0, line.length() - 2).trim();
                } else {
                    line = line.substring(0, line.length() - 1).trim();
                }
            }
            linesList.add(line);
        }

        return linesList;
    }

    public static ArrayList<String> getLettersAndNumbers() {
        ArrayList<String> list = new ArrayList<>();

        for (char c = 'a'; c <= 'z'; c++) {
            list.add(String.valueOf(c));
        }

        for (char c = 'A'; c <= 'Z'; c++) {
            list.add(String.valueOf(c));
        }

        for (char c = '0'; c <= '9'; c++) {
            list.add(String.valueOf(c));
        }

        return list;
    }

    public static boolean endsWithAny(String line, ArrayList<String> characters) {
        for (String character : characters) {
            if (line.endsWith(character)) {
                return true;
            }
        }
        return false;
    }
}
