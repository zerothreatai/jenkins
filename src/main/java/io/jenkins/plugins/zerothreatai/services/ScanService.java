package io.jenkins.plugins.zerothreatai.services;

import hudson.util.Secret;
import net.sf.json.JSONObject;

import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import io.jenkins.plugins.zerothreatai.models.ScanResponse;

public class ScanService {
    private static final String SCAN_API_URL = "https://api.zerothreat.ai/api/scan/devops";
    private static final String ZT_TOKEN_HEADER_KEY = "zt-token";

    public static ScanResponse initiateScan(Secret token) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(SCAN_API_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty(ZT_TOKEN_HEADER_KEY, Secret.toString(token));
            conn.setDoOutput(true);

            try (Scanner scanner = new Scanner(conn.getInputStream()).useDelimiter("\\A")) {
                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();

                JSONObject jsonResponse = JSONObject.fromObject(response);
                ScanResponse scanResponse = new ScanResponse();
                scanResponse.Status = jsonResponse.getInt("status");
                scanResponse.Message = jsonResponse.getString("message");
                scanResponse.Code = jsonResponse.getString("code");
                scanResponse.ScanStatus = jsonResponse.getInt("scanStatus");
                scanResponse.Url = jsonResponse.getString("url");
                scanResponse.TimeStamp = jsonResponse.getString("timeStamp");
                return scanResponse;
            }
        } catch (Exception e) {
            String message = e.getMessage();
            return new ScanResponse(message);
        }
    }

    public static boolean pollScanStatus(Secret token, String code, PrintStream logger) {
        int status = 1;
        while (status < 4) {
            try {
                TimeUnit.SECONDS.sleep(300);
                HttpURLConnection conn = (HttpURLConnection) new URL(SCAN_API_URL + "/" + code).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty(ZT_TOKEN_HEADER_KEY, Secret.toString(token));

                Scanner scanner = new Scanner(conn.getInputStream()).useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();

                JSONObject jsonResponse = JSONObject.fromObject(response);
                status = jsonResponse.getInt("scanStatus");
                var timeStamp = jsonResponse.getString("timeStamp");

                logger.println("Scan is in progress...   [ " + timeStamp + " ]");

            } catch (Exception e) {
                logger.println("Error polling scan status: " + e.getMessage());
                return false;
            }
        }
        return true;
    }

}
