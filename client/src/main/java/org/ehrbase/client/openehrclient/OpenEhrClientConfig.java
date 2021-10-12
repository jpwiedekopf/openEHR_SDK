/*
 *  Copyright (c) 2019  Stefan Spiska (Vitasystems GmbH) and Hannover Medical School
 *  This file is part of Project EHRbase
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ehrbase.client.openehrclient;

import java.net.URI;

import org.apache.http.HttpHeaders;
import org.apache.http.client.fluent.Request;
import org.ehrbase.client.flattener.DefaultValuesProvider;
import org.ehrbase.client.openehrclient.authentication.AuthorizationCredential;
import org.ehrbase.client.openehrclient.authentication.BasicAuthorizationCredential;
import org.ehrbase.client.openehrclient.authentication.BearerAuthorizationCredential;

public class OpenEhrClientConfig {

  private final URI baseUri;
  private final AuthorizationType authorizationType;
  private final AuthorizationCredential authorizationCredential;
  private CompositionFormat compositionFormat = CompositionFormat.JSON;
  private DefaultValuesProvider defaultValuesProvider;

  public OpenEhrClientConfig(URI baseUri) {
    this.baseUri = baseUri;
    this.authorizationType = AuthorizationType.NONE;
    this.authorizationCredential = null;
  }

  public OpenEhrClientConfig(URI baseUri, AuthorizationType authorizationType, AuthorizationCredential authorizationCredential) {
    this.baseUri = baseUri;
    switch (authorizationType) {
      case NONE:
        if (authorizationCredential != null) throw new IllegalArgumentException("When using NONE authorization, don't provide a credential");
        break;
      case BASIC:
        if (!(authorizationCredential instanceof BasicAuthorizationCredential)) throw new IllegalArgumentException("Provide a BasicAuthorizationCredential for BASIC");
        break; //condition also catches null instances
      case OAUTH2:
        if (!(authorizationCredential instanceof BearerAuthorizationCredential)) throw new IllegalArgumentException("Provide a BearerAuthorizationCredential for OAUTH2");
        break;
    }
    this.authorizationType = authorizationType;
    this.authorizationCredential = authorizationCredential;
  }

  public URI getBaseUri() {
    return baseUri;
  }

  public CompositionFormat getCompositionFormat() {
    return compositionFormat;
  }

  public void setCompositionFormat(CompositionFormat compositionFormat) {
    this.compositionFormat = compositionFormat;
  }

  public DefaultValuesProvider getDefaultValuesProvider() {
    return defaultValuesProvider;
  }

  public void setDefaultValuesProvider(DefaultValuesProvider defaultValuesProvider) {
    this.defaultValuesProvider = defaultValuesProvider;
  }

  public void addAuthorizationHeader(Request request) {
    if (this.authorizationType == AuthorizationType.NONE) return;
    if (this.authorizationCredential != null)
      request.addHeader(HttpHeaders.AUTHORIZATION, authorizationCredential.getUserAuthorizationHeader());
    else throw new IllegalStateException("No authorizationCredential was set.");
  }

  public enum AuthorizationType {
    NONE, BASIC, OAUTH2
  }
}
