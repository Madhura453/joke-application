package com.jokes_application.port.incoming;

import com.jokes_application.domain.entity.Joke;

import java.util.List;

public interface JokeService {
    List<Joke> getJokes(int totalCount);
}
