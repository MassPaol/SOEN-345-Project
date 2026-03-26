package com.team_one.soen_345_project.model.util;

import android.util.Log;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import com.team_one.soen_345_project.model.entity.Event;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class EmailHelper {

    private static final String EMAIL_JS_URL = "https://api.emailjs.com/api/v1.0/email/send";
    
    private static final String SERVICE_ID = "service_0cz1x9h";
    private static final String TEMPLATE_ID = "template_wtxq1fm";
    private static final String PUBLIC_KEY = "RhLeshM4exNNWIpRl";
    private static final String PRIVATE_KEY = "4-e9a64C_rOZ3V8Z7ARkl";

    /**
     * Sends a background email using the free EmailJS REST API.
     */
    public static void sendEventStatusEmail(String userEmail, Event event, boolean isCancellation) {
        if (userEmail == null || userEmail.isEmpty() || event == null) return;

        new Thread(() -> {
            try {
                URL url = new URL(EMAIL_JS_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");    
                conn.setDoOutput(true);

                // Create JSON Payload
                JSONObject payload = buildEmailPayload(userEmail, event, isCancellation);

                String jsonInputString = payload.toString();
                try(OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("EmailHelper", "Confirmation email sent successfully.");
                } else {
                    Log.e("EmailHelper", "Failed to send email. Response Code: " + responseCode);
                }
            } catch (Exception e) {
                Log.e("EmailHelper", "Error sending email", e);
            }
        }).start();
    }

    public static JSONObject buildEmailPayload(String userEmail, Event event, boolean isCancellation) throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("service_id", SERVICE_ID);
        payload.put("template_id", TEMPLATE_ID);
        payload.put("user_id", PUBLIC_KEY);
        // Required for Strict Mode in EmailJS
        payload.put("accessToken", PRIVATE_KEY);

        JSONObject templateParams = new JSONObject();
        templateParams.put("to_email", userEmail);
        templateParams.put("event_id", event.getEventId() != null ? event.getEventId() : "N/A");
        templateParams.put("event_name", event.getTitle() != null ? event.getTitle() : "Unknown Event");
        
        String timeStr = "TBD";
        if (event.getDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
            timeStr = sdf.format(event.getDate().toDate());
        }
        templateParams.put("event_time", timeStr);
        templateParams.put("event_loc", event.getLocation() != null ? event.getLocation() : "TBD");
        
        // Adjusting syntax to grammatically fit the template's sentences: "Notification: {action} for...", "Action Performed: {action}"
        templateParams.put("action", isCancellation ? "Reservation Cancellation" : "Event Registration");
        payload.put("template_params", templateParams);

        return payload;
    }
}
