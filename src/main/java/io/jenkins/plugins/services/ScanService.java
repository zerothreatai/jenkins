package io.jenkins.plugins.services;

import io.jenkins.plugins.models.ScanResponse;
import net.sf.json.JSONObject;

import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ScanService {
    private static final String SCAN_API_URL = "https://api.zerothreat.ai/api/scan/devops";

    public static ScanResponse initiateScan(String token, PrintStream logger) throws Exception {
        try {
            String requestBody = "{\"token\":\"" + token + "\"}";
            HttpURLConnection conn = (HttpURLConnection) new URL(SCAN_API_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.getOutputStream().write(requestBody.getBytes());

            Scanner scanner = new Scanner(conn.getInputStream()).useDelimiter("\\A");
            String response = scanner.hasNext() ? scanner.next() : "";
            scanner.close();

            JSONObject jsonResponse = JSONObject.fromObject(response);
            ScanResponse scanResponse = new ScanResponse();
            scanResponse.Status = jsonResponse.getInt("status");
            scanResponse.Message = jsonResponse.getString("message");
            scanResponse.Code = jsonResponse.getString("code");
            scanResponse.ScanStatus = jsonResponse.getInt("scanStatus");
            scanResponse.Url = jsonResponse.getString("url");
            return scanResponse;
        } catch (Exception e) {
            return new ScanResponse("Error initiating scan: " + e.getMessage());
        }
    };

    public static boolean pollScanStatus(String token, PrintStream logger) {
        int status = 1;
        while (status < 4) {
            try {
                TimeUnit.SECONDS.sleep(300);
                HttpURLConnection conn = (HttpURLConnection) new URL(SCAN_API_URL + "/" + token).openConnection();
                conn.setRequestMethod("GET");

                Scanner scanner = new Scanner(conn.getInputStream()).useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();

                JSONObject jsonResponse = JSONObject.fromObject(response);
                status = jsonResponse.getInt("scanStatus");

                logger.println("Scan is inprogress... [ " + new Date() + " ]");

            } catch (Exception e) {
                logger.println("Error polling scan status: " + e.getMessage());
                return false;
            }
        }
        return true;
    }

}
