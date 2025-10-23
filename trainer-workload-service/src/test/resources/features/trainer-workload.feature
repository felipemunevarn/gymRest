Feature: Trainer workload update and summary retrieval

  Background:
    Given the application is running

  Scenario: Save workload and retrieve summary
    Given a workload message for trainer "john" with 60 minutes on "2025-01-12" and messageId "msg-1"
    When the message is sent to the trainer-workload queue
    Then the system should store the trainer summary for "john"
    When I call GET /api/v1/trainers/summary/john
    Then the response status should be 200
    And the response should contain trainerUsername "john"
    And the month 1 of year 2025 should contain 60 total minutes
