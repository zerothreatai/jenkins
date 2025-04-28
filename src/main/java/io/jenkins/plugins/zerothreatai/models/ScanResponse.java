package io.jenkins.plugins.zerothreatai.models;

public class ScanResponse {
    public int Status = 0;
    public String Message = "";
    public String Code = "";
    public int ScanStatus = 0;
    public String Url = "";

    public ScanResponse() {
    }

    public ScanResponse(String message) {
        this.Message = message;
    }
}
