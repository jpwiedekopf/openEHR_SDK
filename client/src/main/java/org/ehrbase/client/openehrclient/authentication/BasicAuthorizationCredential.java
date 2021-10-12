package org.ehrbase.client.openehrclient.authentication;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class BasicAuthorizationCredential implements AuthorizationCredential {

    public String getUserUsername() {
        return userUsername;
    }

    public void setUserUsername(String userUsername) {
        this.userUsername = userUsername;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public BasicAuthorizationCredential(String userUsername, String userPassword, String adminUsername, String adminPassword) {
        this.userUsername = userUsername;
        this.userPassword = userPassword;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    private String userUsername;
    private String userPassword;
    private String adminUsername;
    private String adminPassword;

    private String formatCredential(String user, String password) {
        String formattedCredential = String.format("%s:%s", user, password);
        String encodedCredential = Base64.getEncoder().encodeToString(formattedCredential.getBytes(StandardCharsets.UTF_8));
        return String.format("Basic %s", encodedCredential);
    }

    @Override
    public String getUserAuthorizationHeader() {
        return formatCredential(userUsername, userPassword);
    }

    @Override
    public String getAdminAuthorizationHeader() {
        return formatCredential(adminUsername, adminPassword);
    }
}