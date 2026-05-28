package de.kel0002.buildai.util;

import java.util.ArrayList;


public class ResponseFormatter {
    public static String extractResponseField(String jsonResponse) {
        try {
            String response = extractJsonStringField(jsonResponse, "response", 0);
            if (response != null) {
                return response;
            }

            int choicesIndex = jsonResponse.indexOf("\"choices\"");
            if (choicesIndex == -1) {
                return null;
            }

            int messageIndex = jsonResponse.indexOf("\"message\"", choicesIndex);
            if (messageIndex != -1) {
                response = extractJsonStringField(jsonResponse, "content", messageIndex);
                if (response != null) {
                    return response;
                }
            }

            return extractJsonStringField(jsonResponse, "content", choicesIndex);
        } catch (Exception e) {
            return null;
        }
    }

    private static String extractJsonStringField(String jsonResponse, String fieldName, int startSearch) {
        String field = "\"" + fieldName + "\"";
        int fieldIndex = jsonResponse.indexOf(field, startSearch);

        while (fieldIndex != -1) {
            int colonIndex = jsonResponse.indexOf(":", fieldIndex + field.length());
            if (colonIndex == -1) return null;

            int valueStart = colonIndex + 1;
            while (valueStart < jsonResponse.length() && Character.isWhitespace(jsonResponse.charAt(valueStart))) {
                valueStart++;
            }

            if (valueStart < jsonResponse.length() && jsonResponse.charAt(valueStart) == '"') {
                return readJsonString(jsonResponse, valueStart);
            }

            fieldIndex = jsonResponse.indexOf(field, fieldIndex + field.length());
        }
        return null;
    }

    private static String readJsonString(String jsonResponse, int quoteIndex) {
        StringBuilder result = new StringBuilder();
        for (int i = quoteIndex + 1; i < jsonResponse.length(); i++) {
            char c = jsonResponse.charAt(i);
            if (c == '"') {
                return result.toString();
            }
            if (c == '\\' && i + 1 < jsonResponse.length()) {
                char escaped = jsonResponse.charAt(++i);
                switch (escaped) {
                    case '"':
                    case '\\':
                    case '/':
                        result.append(escaped);
                        break;
                    case 'b':
                        result.append('\b');
                        break;
                    case 'f':
                        result.append('\f');
                        break;
                    case 'n':
                        result.append('\n');
                        break;
                    case 'r':
                        result.append('\r');
                        break;
                    case 't':
                        result.append('\t');
                        break;
                    case 'u':
                        if (i + 4 < jsonResponse.length()) {
                            String hex = jsonResponse.substring(i + 1, i + 5);
                            result.append((char) Integer.parseInt(hex, 16));
                            i += 4;
                        }
                        break;
                    default:
                        result.append(escaped);
                }
            } else {
                result.append(c);
            }
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
