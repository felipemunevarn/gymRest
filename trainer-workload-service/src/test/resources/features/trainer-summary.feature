@focus
Feature: Trainer Summary Query
  As a system user
  I want to query trainer training summaries
  So that I can view trainer workload information

  Background:
    Given the trainer workload service is running
    And MongoDB is available

  Scenario: Successfully retrieve trainer summary
    Given a trainer summary exists in MongoDB for "maria.ramirez" with:
      | firstName | Maria        |
      | lastName  | Ramirez      |
      | status    | true         |
      | year      | 2024         |
      | month     | 10           |
      | duration  | 120          |
    When I request the summary for trainer "maria.ramirez"
    Then the response should be successful
    And the summary should contain trainer username "maria.ramirez"
    And the summary should contain first name "Maria"
    And the summary should contain last name "Ramirez"
    And the summary should contain year 2024
    And the summary should contain month 10 with duration 120

  Scenario: Query non-existent trainer summary
    When I request the summary for trainer "nonexistent.trainer"
    Then the response should be not found
    And no summary data should be returned

  Scenario: Retrieve trainer workload for specific month
    Given a trainer summary exists in MongoDB for "maria.ramirez" with:
      | firstName | Maria        |
      | lastName  | Ramirez      |
      | status    | true         |
      | year      | 2024         |
      | month     | 10           |
      | duration  | 150          |
    When I request the workload for trainer "maria.ramirez" for year 2024 and month 10
    Then the response should be successful
    And the workload duration should be 150

  Scenario: Retrieve workload for month with no data
    Given a trainer summary exists in MongoDB for "maria.ramirez" with:
      | firstName | Maria        |
      | lastName  | Ramirez      |
      | status    | true         |
      | year      | 2024         |
      | month     | 10           |
      | duration  | 150          |
    When I request the workload for trainer "maria.ramirez" for year 2024 and month 11
    Then the response should be successful
    And the workload duration should be 0

  Scenario: Query with invalid month number
    When I request the workload for trainer "maria.ramirez" for year 2024 and month 13
    Then the response should be bad request

  Scenario: Retrieve trainer with multiple years and months
    Given a trainer summary exists in MongoDB for "maria.ramirez" with multiple periods:
      | year | month | duration |
      | 2024 | 9     | 100      |
      | 2024 | 10    | 150      |
      | 2023 | 12    | 200      |
    When I request the summary for trainer "maria.ramirez"
    Then the response should be successful
    And the summary should contain 2 years
    And year 2024 should contain 2 months


  Scenario: Message with invalid JWT token
    When a workload message is received with invalid authentication
    Then the message should be rejected
    And no data should be saved to MongoDB
