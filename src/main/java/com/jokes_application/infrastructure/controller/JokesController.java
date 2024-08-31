package com.jokes_application.infrastructure.controller;


import com.jokes_application.domain.entity.Joke;
import com.jokes_application.service.JokeServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;


@RestController
@Slf4j
public class JokesController {

    @Autowired
    private JokeServiceImpl jokeService;


    @GetMapping("/jokes")
    public List<Joke> getJokes(@RequestParam(name = "count", required = false) String count) {
        int jokesCount;
        final Random random = new Random();

        if ("random".equalsIgnoreCase(count)) {
            jokesCount = random.nextInt(101);
        } else {
            try {
                jokesCount = Integer.parseInt(count);
            } catch (NumberFormatException e) {
              throw new NumberFormatException();
            }
        }
         log.info("Fetching jokes. Jokes count was {},",jokesCount);
        return jokeService.getJokes(jokesCount);
    }
}
