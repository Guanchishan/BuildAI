package de.kel0002.buildai.util;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Dictionary;

public class RequestHandler {
    private static final Gson GSON = new Gson();
    private static final int DEFAULT_TIMEOUT_SECONDS = 90;

    public static RequestResult dorequest(String urlString, Dictionary<String, Object> payload) {
        return dorequest(urlString, payload, null, DEFAULT_TIMEOUT_SECONDS);
    }

    public static RequestResult dorequest(String urlString, Dictionary<String, Object> payload, String apiKey) {
        return dorequest(urlString, payload, apiKey, DEFAULT_TIMEOUT_SECONDS);
    }

    public static RequestResult dorequest(String urlString, Dictionary<String, Object> payload, String apiKey, int timeoutSeconds) {
        int effectiveTimeoutSeconds = Math.max(1, timeoutSeconds);
        if (urlString == null || urlString.trim().isEmpty()) {
            return RequestResult.failure("Endpoint is not configured.");
        }

        HttpURLConnection connection = null;
        try {
            String jsonPayload = GSON.toJson(payload);

            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            int timeoutMillis = effectiveTimeoutSeconds * 1000;

            connection.setConnectTimeout(timeoutMillis);
            connection.setReadTimeout(timeoutMillis);
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
            InputStream responseStream = responseCode >= 200 && responseCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String body = readResponse(responseStream);

            if (responseCode >= 200 && responseCode < 300) {
                return RequestResult.success(responseCode, body);
            }
            return RequestResult.httpError(responseCode, body);
        } catch (SocketTimeoutException e) {
            return RequestResult.failure("Request timed out after " + effectiveTimeoutSeconds + " seconds.");
        } catch (Exception e) {
            String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getClass().getSimpleName() + ": " + e.getMessage();
            return RequestResult.failure(message);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String readResponse(InputStream responseStream) throws java.io.IOException {
        if (responseStream == null) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line.trim());
            }
            return response.toString();
        }
    }

    public static class RequestResult {
        private final boolean success;
        private final int statusCode;
        private final String body;
        private final String errorMessage;

        private RequestResult(boolean success, int statusCode, String body, String errorMessage) {
            this.success = success;
            this.statusCode = statusCode;
            this.body = body == null ? "" : body;
            this.errorMessage = errorMessage == null ? "" : errorMessage;
        }

        public static RequestResult success(int statusCode, String body) {
            return new RequestResult(true, statusCode, body, "");
        }

        public static RequestResult httpError(int statusCode, String body) {
            return new RequestResult(false, statusCode, body, "");
        }

        public static RequestResult failure(String errorMessage) {
            return new RequestResult(false, -1, "", errorMessage);
        }

        public boolean isSuccess() {
            return success;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getBody() {
            return body;
        }

        public String describeFailure() {
            if (statusCode > 0) {
                return "HTTP " + statusCode + ": " + body;
            }
            return errorMessage;
        }
    }
}
