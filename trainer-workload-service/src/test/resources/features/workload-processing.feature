@focus
Feature: Workload Message Processing
  As a message consumer
  I want to process training workload messages
  So that trainer workload data is updated in MongoDB

  Background:
    Given the trainer workload service is running
    And MongoDB is available

  Scenario: Message causes unexpected error and goes to DLQ
    When a workload message is received with:
      | trainerUsername  | maria.ramirez |
      | trainerFirstName | Maria         |
      | trainerLastName  | Ramirez       |
      | isActive         | true          |
      | trainingDate     | invalid-date  |
      | trainingDuration | 60            |
      | actionType       | ADD           |
    Then the message should fail processing
    And the message should be sent to the DLQ


