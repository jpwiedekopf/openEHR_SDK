package org.ehrbase.client.openehrclient;

import org.apache.http.client.fluent.Request;
import org.ehrbase.client.openehrclient.authentication.BasicAuthorizationCredential;
import org.ehrbase.client.openehrclient.authentication.BearerAuthorizationCredential;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class OpenEhrClientConfigTest {

    private static URI OPEN_EHR_URL = null;

    static {
        try {
            OPEN_EHR_URL = new URI("http://localhost:8080/ehrbase/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    BasicAuthorizationCredential basicAuthorizationCredential = new BasicAuthorizationCredential("username", "password", "admin", "adminPassword");
    BearerAuthorizationCredential bearerAuthorizationCredential = new BearerAuthorizationCredential("token");

    OpenEhrClientConfig basicAuthConfig = new OpenEhrClientConfig(OPEN_EHR_URL, OpenEhrClientConfig.AuthorizationType.BASIC, basicAuthorizationCredential);
    OpenEhrClientConfig bearerAuthConfig = new OpenEhrClientConfig(OPEN_EHR_URL, OpenEhrClientConfig.AuthorizationType.OAUTH2, bearerAuthorizationCredential);

    @Test
    public void testBasicAuthorizationHeader() {
        String encodedUserHeader = basicAuthorizationCredential.getUserAuthorizationHeader();
        assertEquals("Basic dXNlcm5hbWU6cGFzc3dvcmQ=", encodedUserHeader);
        String encodedAdminHeader = basicAuthorizationCredential.getAdminAuthorizationHeader();
        assertEquals("Basic YWRtaW46YWRtaW5QYXNzd29yZA==", encodedAdminHeader);
    }

    @Test
    public void testBearerAuthorizationHeader() {
        String encodedUserHeader = bearerAuthorizationCredential.getUserAuthorizationHeader();
        assertEquals("Bearer token", encodedUserHeader);
        String encodedAdminHeader = bearerAuthorizationCredential.getAdminAuthorizationHeader();
        assertEquals(encodedUserHeader, encodedAdminHeader);
    }

    @Test
    public void testAddingBasicAuth() throws NoSuchFieldException, IllegalAccessException {
        Request request = Request.Get("localhost");
        basicAuthConfig.addAuthorizationHeader(request);
        //TODO how to test? the internal http request is private in HC Fluent
        //either access the field using reflection, or use a mocking framework to verify that the header is added correctly
    }

    @Test
    public void invalidAuthThrows() {
        assertThrows(IllegalArgumentException.class, () -> new OpenEhrClientConfig(OPEN_EHR_URL, OpenEhrClientConfig.AuthorizationType.NONE, new BearerAuthorizationCredential("token")));
        assertThrows(IllegalArgumentException.class, () -> new OpenEhrClientConfig(OPEN_EHR_URL, OpenEhrClientConfig.AuthorizationType.BASIC, null));
        assertThrows(IllegalArgumentException.class, () -> new OpenEhrClientConfig(OPEN_EHR_URL, OpenEhrClientConfig.AuthorizationType.OAUTH2, null));
        assertThrows(IllegalArgumentException.class, () -> new OpenEhrClientConfig(OPEN_EHR_URL, OpenEhrClientConfig.AuthorizationType.BASIC, new BearerAuthorizationCredential("token")));
        assertThrows(IllegalArgumentException.class, () -> new OpenEhrClientConfig(OPEN_EHR_URL, OpenEhrClientConfig.AuthorizationType.OAUTH2, new BasicAuthorizationCredential("a", "b", "c", "d")));
    }

}