package com.company.opl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.mfa")
public class MfaProperties {

    private boolean required = true;
    private String issuer = "Project Issue Hub";
    private int digits = 6;
    private int stepSeconds = 30;
    private int window = 1;

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public int getDigits() {
        return digits;
    }

    public void setDigits(int digits) {
        this.digits = digits;
    }

    public int getStepSeconds() {
        return stepSeconds;
    }

    public void setStepSeconds(int stepSeconds) {
        this.stepSeconds = stepSeconds;
    }

    public int getWindow() {
        return window;
    }

    public void setWindow(int window) {
        this.window = window;
    }
}
