Feature: Training Management
  As a gym administrator
  I want to manage training sessions
  So that I can track trainer workloads

  Background:
    Given the gym system is running
    And a trainer exists with username "maria.ramirez"
    And a trainee exists with username "john.doe"
    And I am authenticated as an admin

  Scenario: Successfully create a new training session
    When I create a training with following details:
      | traineeUsername  | john.doe      |
      | trainerUsername  | maria.ramirez |
      | name             | Cardio Session|
      | date             | 2024-10-15    |
      | duration         | 60            |
    Then the training should be created successfully
    And a workload message should be sent to the message queue
    And the message should contain trainer username "maria.ramirez"
    And the message should contain duration 60
    And the message should have action type "ADD"

  Scenario: Create training with invalid trainer
    When I create a training with following details:
      | traineeUsername  | john.doe          |
      | trainerUsername  | invalid.trainer   |
      | name             | Cardio Session    |
      | date             | 2024-10-15        |
      | duration         | 60                |
    Then the training creation should fail
    And an error message should indicate "Trainer not found"
    And no message should be sent to the queue

  Scenario: Create training with invalid trainee
    When I create a training with following details:
      | traineeUsername  | invalid.trainee   |
      | trainerUsername  | maria.ramirez     |
      | name             | Cardio Session    |
      | date             | 2024-10-15        |
      | duration         | 60                |
    Then the training creation should fail
    And an error message should indicate "Trainee not found"
    And no message should be sent to the queue

  Scenario: Create training with missing required fields
    When I create a training with following details:
      | traineeUsername  | john.doe      |
      | trainerUsername  | maria.ramirez |
      | name             |               |
      | date             | 2024-10-15    |
      | duration         | 60            |
    Then the training creation should fail
    And a validation error should be returned

  Scenario: Successfully delete a training session
    Given a training exists with id 1 for trainer "maria.ramirez" with duration 60
    When I delete the training with id 1
    Then the training should be deleted successfully
    And a workload message should be sent to the message queue
    And the message should contain trainer username "maria.ramirez"
    And the message should contain duration 60
    And the message should have action type "DELETE"

  Scenario: Delete non-existent training
    When I delete the training with id 99999
    Then the training deletion should fail
    And an error message should indicate "Training not found"
    And no message should be sent to the queue

  Scenario: Create multiple trainings for same trainer
    When I create a training with following details:
      | traineeUsername  | john.doe      |
      | trainerUsername  | maria.ramirez |
      | name             | Morning Session|
      | date             | 2024-10-15    |
      | duration         | 45            |
    And I create a training with following details:
      | traineeUsername  | john.doe      |
      | trainerUsername  | maria.ramirez |
      | name             | Evening Session|
      | date             | 2024-10-15    |
      | duration         | 60            |
    Then both trainings should be created successfully
    And 2 workload messages should be sent to the message queue