package com.jokes_application.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Joke {

    @Id
    public Long id;

    public String setup;

    public String type;

    @Column(nullable = false)
    public String punchline;
}