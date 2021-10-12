package org.ehrbase.client.openehrclient.authentication;

public interface AuthorizationCredential {
    String getUserAuthorizationHeader();
    String getAdminAuthorizationHeader();
}
