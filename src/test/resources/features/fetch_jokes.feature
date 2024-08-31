Feature: Fetch Jokes

  Scenario: Fetching a valid number of jokes
    Given the joke service is running
    When I request 20 jokes
    Then the service should return 20 jokes
    And the jokes should be saved in the database

  Scenario: Fetching an invalid number of jokes (too high)
    Given the joke service is running
    When I request 200 jokes
    Then the service should return an error with status 429
    And the error message should be "Request number of jokes is out of limits"

  Scenario: Fetching an invalid number of jokes (negative)
    Given the joke service is running
    When I request -5 jokes
    Then the service should return an error with status 429
    And the error message should be "Request number of jokes is out of limits"

  Scenario: Fetching a batch of jokes with API rate limit exceeded
    Given the joke service is running
    And the external joke API has reached its rate limit
    When I request 10 jokes
    Then the service should return an error with status 429
    And the error message should be "Too Many Requests Please try again later"

  Scenario: Fetching a batch of jokes with intermittent API errors
    Given the joke service is running
    And the external joke API occasionally returns errors
    When I request 15 jokes
    Then the service should retry the failed requests
    And the service should eventually return 15 jokes
    And the jokes should be saved in the database
