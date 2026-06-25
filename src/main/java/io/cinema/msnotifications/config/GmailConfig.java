package io.cinema.msnotifications.config;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import io.cinema.domain.enumerated.CinemaExceptionTypes;
import io.cinema.domain.exceptions.CinemaException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GmailConfig {
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private final EmailProperties emailProperties;

    @Bean
    public Gmail gmailService() throws Exception {
        log.info("Initializing Gmail API client...");

        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        InputStream in = getClass().getResourceAsStream(emailProperties.getCredentialsFilePath());

        if (in == null) {
            throw new CinemaException(
                    "Resource not found " + emailProperties.getCredentialsFilePath(),
                    CinemaExceptionTypes.TECHNICAL_ERROR
            );
        }

        GoogleClientSecrets secrets = GoogleClientSecrets.load(
                GsonFactory.getDefaultInstance(),
                new InputStreamReader(in)
        );

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                GsonFactory.getDefaultInstance(),
                secrets,
                Collections.singletonList(GmailScopes.GMAIL_SEND)
        )
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        var localServerReceiver = new LocalServerReceiver.Builder().setPort(8888).build();
        var credential = new AuthorizationCodeInstalledApp(flow, localServerReceiver).authorize("user");

        return new Gmail.Builder(httpTransport, GsonFactory.getDefaultInstance(), credential)
                .setApplicationName("cinema-notifications")
                .build();
    }
}
