package com.example.data.controller;

import com.example.data.dto.CategoryCode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@RestController
@RequestMapping("/api")
public class ForecastController {
    @Value("${openApi.serviceKey}")
    private String serviceKey;

    @Value("${openApi.callBackUrl}")
    private String callBackUrl;

    @Value("${openApi.dataType}")
    private String dataType;

    @GetMapping("/forecast")
    public ResponseEntity<String> callForecastApi(
            @RequestParam(value="base_time") String baseTime,
            @RequestParam(value="base_date") String baseDate,
            @RequestParam(value="beach_num") String beachNum
    ){
        HttpURLConnection urlConnection = null;
        InputStream stream = null;
        String result = null;

        String urlStr = callBackUrl +
                "/getUltraSrtFcstBeach" +
                "?serviceKey=" + serviceKey +
                "&dataType=" + dataType +
                "&base_date=" + baseDate +
                "&base_time=" + baseTime +
                "&beach_num=" + beachNum;

        try {
            URL url = new URL(urlStr);

            urlConnection = (HttpURLConnection) url.openConnection();
            stream = getNetworkConnection(urlConnection);
            result = readStreamToString(stream);

            if (stream != null) stream.close();
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        // JSON 데이터 파싱 및 변환
        return new ResponseEntity<>(parseAndTransformResult(result), HttpStatus.OK);
    }

    private InputStream getNetworkConnection(HttpURLConnection urlConnection) throws IOException {
        urlConnection.setConnectTimeout(3000);
        urlConnection.setReadTimeout(3000);
        urlConnection.setRequestMethod("GET");
        urlConnection.setDoInput(true);

        if(urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code : " + urlConnection.getResponseCode());
        }

        return urlConnection.getInputStream();
    }

    private String readStreamToString(InputStream stream) throws IOException{
        StringBuilder result = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

        String readLine;
        while((readLine = br.readLine()) != null) {
            result.append(readLine + "\n\r");
        }

        br.close();
        return result.toString();
    }

    // JSON 데이터를 변환하는 로직 추가
    private String parseAndTransformResult(String result) {
        JSONObject jsonObject = new JSONObject(result);
        JSONObject response = jsonObject.getJSONObject("response");
        JSONObject body = response.getJSONObject("body");
        JSONObject items = body.getJSONObject("items");
        JSONArray itemArray = items.getJSONArray("item");

        JSONArray transformedItems = new JSONArray();
        for (int i = 0; i < itemArray.length(); i++) {
            JSONObject item = itemArray.getJSONObject(i);
            JSONObject transformedItem = new JSONObject();

            String category = item.getString("category");
            String value = item.getString("fcstValue");

            // 카테고리 이름과 단위를 가져옵니다.
            CategoryCode categoryCode = CategoryCode.valueOf(category);
            String categoryName = categoryCode.getName();
            String unit = categoryCode.getUnit();

            // 카테고리 값 변환
            String transformedValue = CategoryCode.getCodeValue(category, value) + unit;

            transformedItem.put("categoryName", categoryName);
            transformedItem.put("fcstValue", transformedValue);
            transformedItem.put("beachNum", item.getString("beachNum"));
            transformedItem.put("baseDate", item.getString("baseDate"));
            transformedItem.put("baseTime", item.getString("baseTime"));
            transformedItem.put("fcstDate", item.getString("fcstDate"));
            transformedItem.put("fcstTime", item.getString("fcstTime"));
            transformedItem.put("nx", item.getInt("nx"));
            transformedItem.put("ny", item.getInt("ny"));

            transformedItems.put(transformedItem);
        }

        // 결과 JSON으로 변환
        JSONObject resultJson = new JSONObject();
        resultJson.put("item", transformedItems);
        return resultJson.toString();
    }
}
