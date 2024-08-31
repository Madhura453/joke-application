package com.jokes_application.infrastructure.adapter;


import com.jokes_application.domain.entity.Joke;
import com.jokes_application.domain.exception.RateLimitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class JokeRequestServiceImpl {

    @Autowired
    private WebClient jokeWebClient;

    @Value("${retryCount}")
    private long retryCount;

    @Value("${retryFixedDelayInSec}")
    private long retryFixedDelayInSec;

    public Mono<Joke> getJoke() {
        return jokeWebClient
                .get()
                .retrieve()
                .onStatus(
                        status -> HttpStatus.valueOf(status.value()).isError(),
                        response -> Mono.error(new WebClientResponseException(
                                response.statusCode().value(),
                                HttpStatus.valueOf(response.statusCode().value()).getReasonPhrase(), // Reason phrase fix
                                null,
                                null,
                                null)))
                .bodyToMono(new ParameterizedTypeReference<Joke>() {})
                .retryWhen(
                        Retry.fixedDelay(retryCount, Duration.ofSeconds(retryFixedDelayInSec))
                                .filter(this::is5xxServerErrorOrTooManyRequests)
                                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                                        new Exception(retrySignal.failure().getMessage())))
                .doOnError(e ->
                        {
                            log.error("MethodName: {} | Error receiving while calling joke endpoint ", e);
                            throw new RateLimitException(429,"Too Many Requests Please try again later",
                                    HttpStatus.TOO_MANY_REQUESTS);
                        }

                );
    }

    private boolean is5xxServerErrorOrTooManyRequests(Throwable throwable) {
        return (throwable instanceof WebClientResponseException
                && (((WebClientResponseException) throwable).getStatusCode().is5xxServerError()
                || ((WebClientResponseException) throwable).getStatusCode().equals(HttpStatus.TOO_MANY_REQUESTS)))
                || (throwable instanceof TimeoutException);
    }
}

