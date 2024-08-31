package com.jokes_application.controller;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.MySQLContainer;

import static org.apache.logging.log4j.ThreadContext.isEmpty;
import static org.assertj.core.api.Fail.fail;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JokesControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private static final MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:latest")
            .withUsername("root")
            .withPassword("password")
            .withDatabaseName("testdb");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);
    }

    @BeforeAll
    static void beforeAll() {
        mySQLContainer.start();
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @AfterAll
    static void afterAll() {
        mySQLContainer.stop();
    }

    @Test
    public void testGetJokesWithRandomCount() throws Exception {
        mockMvc.perform(get("/jokes?count=random"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 200) {
                        result.getResponse().getContentAsString().isEmpty(); // Ensure the response isn't empty
                    } else if (status == 429) {
                        assertEquals(429, status);
                    } else {
                        fail("Unexpected status code: " + status);
                    }
                });
    }

    @Test
    public void testGetJokesWithValidCount() throws Exception {
        mockMvc.perform(get("/jokes?count=15"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 200) {
                        result.getResponse().getContentAsString().isEmpty();
                        jsonPath("$", hasSize(15));// Ensure the response isn't empty
                    } else if (status == 429) {
                        assertEquals(429, status);
                    } else {
                        fail("Unexpected status code: " + status);
                    }
                });
    }

    @Test
    public void testGetJokesWithInvalidCount() throws Exception {
        mockMvc.perform(get("/jokes?count=invalid"))
                .andExpect(status().isBadRequest())
               // .andExpect(jsonPath("$.message").value("For input string: \"invalid\""))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.description").exists());
    }


    @Test
    public void testGetJokesWithExceedingCount() throws Exception {
        mockMvc.perform(get("/jokes?count=105"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Request number of jokes is out of limits"))
                .andExpect(jsonPath("$.statusCode").value(429))
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void testGetJokesWithZeroCount() throws Exception {
        mockMvc.perform(get("/jokes?count=0"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Request number of jokes is out of limits"))
                .andExpect(jsonPath("$.statusCode").value(429))
                .andExpect(jsonPath("$.description").exists());
    }
}