Feature: Workload Message Processing
  As a message consumer
  I want to process training workload messages
  So that trainer workload data is updated in MongoDB

  Background:
    Given the trainer workload service is running
    And MongoDB is available
    And the message queue is empty

  Scenario: Process ADD training message for new trainer
    When a workload message is received with:
      | trainerUsername  | maria.ramirez |
      | trainerFirstName | Maria         |
      | trainerLastName  | Ramirez       |
      | isActive         | true          |
      | trainingDate     | 2024-10-15    |
      | trainingDuration | 60            |
      | actionType       | ADD           |
    Then the message should be processed successfully
    And a trainer summary should be created in MongoDB for "maria.ramirez"
    And the summary should contain year 2024 and month 10
    And the month duration should be 60

  Scenario: Process ADD training message for existing trainer same month
    Given a trainer summary exists in MongoDB for "maria.ramirez" with:
      | firstName | Maria   |
      | lastName  | Ramirez |
      | status    | true    |
      | year      | 2024    |
      | month     | 10      |
      | duration  | 60      |
    When a workload message is received with:
      | trainerUsername  | maria.ramirez |
      | trainerFirstName | Maria         |
      | trainerLastName  | Ramirez       |
      | isActive         | true          |
      | trainingDate     | 2024-10-20    |
      | trainingDuration | 45            |
      | actionType       | ADD           |
    Then the message should be processed successfully
    And the trainer summary for "maria.ramirez" should be updated
    And the month 10 of year 2024 should have duration 105

  Scenario: Process ADD training message for existing trainer new month
    Given a trainer summary exists in MongoDB for "maria.ramirez" with:
      | firstName | Maria   |
      | lastName  | Ramirez |
      | status    | true    |
      | year      | 2024    |
      | month     | 9       |
      | duration  | 100     |
    When a workload message is received with:
      | trainerUsername  | maria.ramirez |
      | trainerFirstName | Maria         |
      | trainerLastName  | Ramirez       |
      | isActive         | true          |
      | trainingDate     | 2024-10-15    |
      | trainingDuration | 60            |
      | actionType       | ADD           |
    Then the message should be processed successfully
    And the trainer summary for "maria.ramirez" should contain month 10
    And the month 10 of year 2024 should have duration 60
    And the month 9 of year 2024 should still have duration 100

  Scenario: Process DELETE training message
    Given a trainer summary exists in MongoDB for "maria.ramirez" with:
      | firstName | Maria   |
      | lastName  | Ramirez |
      | status    | true    |
      | year      | 2024    |
      | month     | 10      |
      | duration  | 120     |
    When a workload message is received with:
      | trainerUsername  | maria.ramirez |
      | trainerFirstName | Maria         |
      | trainerLastName  | Ramirez       |
      | isActive         | true          |
      | trainingDate     | 2024-10-15    |
      | trainingDuration | 50            |
      | actionType       | DELETE        |
    Then the message should be processed successfully
    And the month 10 of year 2024 should have duration 70

  Scenario: Process DELETE training message that would go negative
    Given a trainer summary exists in MongoDB for "maria.ramirez" with:
      | firstName | Maria   |
      | lastName  | Ramirez |
      | status    | true    |
      | year      | 2024    |
      | month     | 10      |
      | duration  | 30      |
    When a workload message is received with:
      | trainerUsername  | maria.ramirez |
      | trainerFirstName | Maria         |
      | trainerLastName  | Ramirez       |
      | isActive         | true          |
      | trainingDate     | 2024-10-15    |
      | trainingDuration | 50            |
      | actionType       | DELETE        |
    Then the message should be processed successfully
    And the month 10 of year 2024 should have duration 0

  Scenario: Process message with invalid data
    When a workload message is received with:
      | trainerUsername  |               |
      | trainerFirstName | Maria         |
      | trainerLastName  | Ramirez       |
      | isActive         | true          |
      | trainingDate     | 2024-10-15    |
      | trainingDuration | 60            |
      | actionType       | ADD           |
    Then the message should fail validation
    And no data should be saved to MongoDB

  Scenario: Update trainer status through message
    Given a trainer summary exists in MongoDB for "maria.ramirez" with:
      | firstName | Maria   |
      | lastName  | Ramirez |
      | status    | true    |
      | year      | 2024    |
      | month     | 10      |
      | duration  | 60      |
    When a workload message is received with:
      | trainerUsername  | maria.ramirez |
      | trainerFirstName | Maria         |
      | trainerLastName  | Ramirez       |
      | isActive         | false         |
      | trainingDate     | 2024-10-20    |
      | trainingDuration | 30            |
      | actionType       | ADD           |
    Then the message should be processed successfully
    And the trainer status should be updated to false