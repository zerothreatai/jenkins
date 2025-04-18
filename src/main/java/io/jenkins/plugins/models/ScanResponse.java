package io.jenkins.plugins.models;


import javax.annotation.Nullable;

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
