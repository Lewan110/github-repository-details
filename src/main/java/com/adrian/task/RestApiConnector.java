
package com.adrian.task;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import java.time.Duration;


@Service
public class RestApiConnector implements RepositoryDetails {

    private final String CONTENT_TYPE = "Content-Type";
    private final String APPLICATION_JSON = "application/json";
    private final String AUTHORIZATION = "authorization";
    private final String ACCEPT = "Accept";
    private final Duration TIMEOUT_FOR_API = Duration.ofSeconds(5);

    @Value("${github.token}")
    private String token;

    @Value("${github.url}")
    private String url;

    @Value("${github.api-version}")
    private String apiVersion;

    private WebClient webClient;

    @PostConstruct
    private void init() {
        if (token.isEmpty()) {
            this.webClient = WebClient.builder()
                    .defaultHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .defaultHeader(ACCEPT, apiVersion)
                    .baseUrl(url)
                    .build();
        } else {
            this.webClient = WebClient.builder()
                    .defaultHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .defaultHeader(ACCEPT, apiVersion)
                    .defaultHeader(AUTHORIZATION, "token " + token)
                    .baseUrl(url)
                    .build();
        }
    }

    @Override
    public RepositoryInfo getRepositoryDetails(String owner, String repositoryName) {
        return webClient.get()
                .uri(builder -> builder
                        .pathSegment(owner)
                        .pathSegment(repositoryName)
                        .build())
                .retrieve()
                .onStatus(httpStatus -> httpStatus.equals(HttpStatus.FORBIDDEN), clientResponse -> {
                    throw new ConnectorException(ErrorMessage.ERROR_403.toString());
                })
                .onStatus(httpStatus -> httpStatus.equals(HttpStatus.NOT_FOUND), clientResponse -> {
                    throw new ConnectorException(ErrorMessage.ERROR_404.toString());
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    throw new ConnectorException(ErrorMessage.ERROR_500.toString());
                })
                .bodyToMono(RepositoryInfo.class)
                .block(TIMEOUT_FOR_API);
    }
}
