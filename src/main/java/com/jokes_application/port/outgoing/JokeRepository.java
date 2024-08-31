package com.jokes_application.port.outgoing;

import com.jokes_application.domain.entity.Joke;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JokeRepository extends  JpaRepository<Joke, Long> {

}