package de.kel0002.buildai.util;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;


public class RequestHandler {
    public static String dorequest(String url_string, Dictionary<String, Object> payload) {
        return dorequest(url_string, payload, null);
    }

    public static String dorequest(String url_string, Dictionary<String, Object> payload, String apiKey) {
        try {

            String jsonPayload = createJsonPayloadFromDictionary(payload);

            URL url = new URL(url_string);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                String trimmedApiKey = apiKey.trim();
                if (trimmedApiKey.regionMatches(true, 0, "Bearer ", 0, 7)) {
                    connection.setRequestProperty("Authorization", trimmedApiKey);
                } else {
                    connection.setRequestProperty("Authorization", "Bearer " + trimmedApiKey);
                }
            }
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            InputStream responseStream = responseCode == 200
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream, StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line.trim());
            }
            reader.close();

            return response.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static String createJsonPayloadFromDictionary(Dictionary<String, Object> payload) {
        StringBuilder jsonBuilder = new StringBuilder("{");
        Enumeration<String> keys = payload.keys();

        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            Object value = payload.get(key);

            jsonBuilder.append(toJsonString(key)).append(":").append(toJsonValue(value));

            if (keys.hasMoreElements()) {
                jsonBuilder.append(",");
            }
        }

        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    private static String toJsonValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return toJsonString((String) value);
        }
        if (value instanceof Boolean || value instanceof Number) {
            return String.valueOf(value);
        }
        if (value instanceof Dictionary) {
            StringBuilder jsonBuilder = new StringBuilder("{");
            Dictionary<?, ?> dictionary = (Dictionary<?, ?>) value;
            Enumeration<?> keys = dictionary.keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                jsonBuilder.append(toJsonString(String.valueOf(key))).append(":")
                        .append(toJsonValue(dictionary.get(key)));
                if (keys.hasMoreElements()) {
                    jsonBuilder.append(",");
                }
            }
            jsonBuilder.append("}");
            return jsonBuilder.toString();
        }
        if (value instanceof Map) {
            StringBuilder jsonBuilder = new StringBuilder("{");
            boolean first = true;
            for (Object entryObject : ((Map<?, ?>) value).entrySet()) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) entryObject;
                if (!first) {
                    jsonBuilder.append(",");
                }
                jsonBuilder.append(toJsonString(String.valueOf(entry.getKey()))).append(":")
                        .append(toJsonValue(entry.getValue()));
                first = false;
            }
            jsonBuilder.append("}");
            return jsonBuilder.toString();
        }
        if (value instanceof Collection) {
            StringBuilder jsonBuilder = new StringBuilder("[");
            boolean first = true;
            for (Object item : (Collection<?>) value) {
                if (!first) {
                    jsonBuilder.append(",");
                }
                jsonBuilder.append(toJsonValue(item));
                first = false;
            }
            jsonBuilder.append("]");
            return jsonBuilder.toString();
        }
        return toJsonString(String.valueOf(value));
    }

    private static String toJsonString(String value) {
        StringBuilder escaped = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
            }
        }
        escaped.append("\"");
        return escaped.toString();
    }

}
