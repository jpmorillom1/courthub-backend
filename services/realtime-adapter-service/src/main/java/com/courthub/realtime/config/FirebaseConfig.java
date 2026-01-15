package com.courthub.realtime.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.database-url}")
    private String firebaseDatabaseUrl;

    @Value("${firebase.credentials-path:}")
    private String credentialsPath;

    @Bean
    public FirebaseApp firebaseApp(ResourceLoader resourceLoader) throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        GoogleCredentials credentials;

        if (credentialsPath != null && !credentialsPath.isEmpty()) {
            Resource resource = resourceLoader.getResource(credentialsPath);
            try (InputStream is = resource.getInputStream()) {
                credentials = GoogleCredentials.fromStream(is);
                log.info("Firebase initialized with credentials from: {}", credentialsPath);
            }
        } else {
            credentials = GoogleCredentials.getApplicationDefault();
            log.info("Firebase initialized with default application credentials");
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setDatabaseUrl(firebaseDatabaseUrl)
                .build();

        return FirebaseApp.initializeApp(options);
    }

    @Bean
    public FirebaseDatabase firebaseDatabase(FirebaseApp firebaseApp) {
        return FirebaseDatabase.getInstance(firebaseApp);
    }
}