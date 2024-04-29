package com.example.picturesearch;

import com.example.picturesearch.database.ImageItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ImageSearchHelper {

    private static final String API_KEY = BuildConfig.API_KEY;
    private static final String CX = BuildConfig.CX;

    public List<ImageItem> searchImages(String query, int pageNumber) {
        List<ImageItem> imageItems = new ArrayList<>();
        try {
            int startIndex = calculateStartIndex(pageNumber);

            String urlString = buildQueryString(query, startIndex);
            String jsonResponse = fetchJsonResponse(urlString);

            imageItems = parseJsonResponse(jsonResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageItems;
    }

    private int calculateStartIndex(int pageNumber) {
        int RESULTS_PER_PAGE = 10;
        return (pageNumber - 1) * RESULTS_PER_PAGE + 1;
    }

    private String buildQueryString(String query, int startIndex) {
        return "https://www.googleapis.com/customsearch/v1?key=" + API_KEY +
                "&cx=" + CX +
                "&q=" + query +
                "&searchType=image" +
                "&start=" + startIndex;
    }

    private String fetchJsonResponse(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }

        conn.disconnect();

        return response.toString();
    }

    private List<ImageItem> parseJsonResponse(String jsonResponse) throws Exception {
        List<ImageItem> imageItems = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray items = jsonObject.getJSONArray("items");

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            String link = item.getString("link");
            String title = item.optString("title", "");

            JSONObject imageObject = item.getJSONObject("image");
            String contextLink = imageObject.optString("contextLink", "");
            int height = imageObject.optInt("height", 0);
            int width = imageObject.optInt("width", 0);
            int byteSize = imageObject.optInt("byteSize", 0);

            ImageItem imageItem = new ImageItem(link, title, contextLink, height, width, byteSize);
            imageItems.add(imageItem);
        }
        return imageItems;
    }
}

