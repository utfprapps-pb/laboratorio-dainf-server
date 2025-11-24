package br.com.utfpr.gerenciamento.server.security.social;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GoogleTokenVerifier {

  private static final HttpTransport transport = new NetHttpTransport();
  private static final JsonFactory jsonFactory = new GsonFactory().getDefaultInstance();

  @Value("${google.clientId}")
  private String CLIENT_ID;

  public Payload verify(String idTokenString) throws GeneralSecurityException, IOException {
    return verifyToken(idTokenString);
  }

  private Payload verifyToken(String idTokenString) throws GeneralSecurityException, IOException {
    final GoogleIdTokenVerifier verifier =
        new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
            .setIssuers(Arrays.asList("https://accounts.google.com", "accounts.google.com"))
            .setAudience(Collections.singletonList(CLIENT_ID))
            .build();

    // System.out.println("validating:" + idTokenString);
    GoogleIdToken idToken = null;
    try {
      idToken = verifier.verify(idTokenString);
    } catch (IllegalArgumentException e) {
      // means token was not valid and idToken
      // will be null
    }

    if (idToken == null) {
      throw new RuntimeException("idToken is invalid");
    }

    return idToken.getPayload();
  }
}
