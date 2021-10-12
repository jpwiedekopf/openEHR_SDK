package org.ehrbase.client.openehrclient.authentication;

public class BearerAuthorizationCredential implements AuthorizationCredential {

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    private String accessToken;

    public  BearerAuthorizationCredential(String accessToken) {
        this.accessToken = accessToken;
    }


    @Override
    public String getUserAuthorizationHeader() {
        return String.format("Bearer %s", accessToken);
    }

    @Override
    public String getAdminAuthorizationHeader() {
        return getUserAuthorizationHeader();
    }
}
