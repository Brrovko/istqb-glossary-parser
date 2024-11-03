package istqb.parsing;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        String urlString = "https://api.glossary.istqb.org/v1/search"; // Заменено на нужный URL

        try {
            URI uri = new URI(urlString);
            URL url = uri.toURL();

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("term", "");
            jsonRequest.put("all_results", true);
            jsonRequest.put("page", 100);
            jsonRequest.put("limit", 20);
            jsonRequest.put("exact_matches_first", true);
            jsonRequest.put("second_language", "ru_RU");

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonRequest.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray dataArray = jsonResponse.getJSONArray("previous_results");

                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject dataObject = dataArray.getJSONObject(i);

                    String acceptLanguageName;
                    if (!dataObject.isNull("accept_language")) {
                        acceptLanguageName = dataObject.getJSONObject("accept_language").getString("name");
                    } else {
                        acceptLanguageName = "N/A";
                    }

                    String secondaryLanguageName;
                    if (!dataObject.isNull("secondary_language")) {
                        secondaryLanguageName = dataObject.getJSONObject("secondary_language").getString("name");
                    } else {
                        secondaryLanguageName = "N/A";
                    }

                    if (!secondaryLanguageName.equals("N/A") && !acceptLanguageName.equals("N/A")) {
                        System.out.println(acceptLanguageName + "; " + secondaryLanguageName);
                    }

                }
            } else {
                System.out.println("Request failed. Response code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}