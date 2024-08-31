package com.jokes_application.service;

import com.jokes_application.domain.entity.Joke;
import com.jokes_application.domain.exception.RateLimitException;
import com.jokes_application.port.incoming.JokeService;
import com.jokes_application.port.outgoing.JokeRepository;
import com.jokes_application.infrastructure.adapter.JokeRequestServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class JokeServiceImpl implements JokeService {

    @Autowired
    private JokeRequestServiceImpl jokeRequestService;

    @Autowired
    private JokeRepository jokeRepository;

    @Override
    public List<Joke> getJokes(int totalCount) {

        if(totalCount<=0 || totalCount>100)
        {
            throw new RateLimitException(429,"Request number of jokes is out of limits",
                    HttpStatus.TOO_MANY_REQUESTS);
        }
        log.info("Fetching a total of {} jokes in batches of {}", totalCount, 10);

        int fullBatches = totalCount / 10;
        int remainingJokes = totalCount % 10;

        List<List<Joke>> batchJokes= new ArrayList<>();

        for (int i = 0; i < fullBatches; i++) {
            batchJokes.add(getJokesBatch(10));
        }

        if (remainingJokes > 0) {
            batchJokes.add(getJokesBatch(remainingJokes));
        }

        return batchJokes.stream().flatMap(jokes ->jokes.stream()).toList();
    }

    private List<Joke> getJokesBatch(int batchSize) {

        log.info("Fetching batch of {} jokes from Jokes API", batchSize);

        Mono<List<Joke>> jokesMono=Flux.range(1, batchSize)
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(i -> jokeRequestService.getJoke())
                .sequential()
                .collectList()
                .doOnError(e ->
                        log.error(" Error receiving while calling joke endpoint ",  e));

        List<Joke> jokes= jokesMono.block();
        jokeRepository.saveAll(jokes);
        return jokes;
    }


}