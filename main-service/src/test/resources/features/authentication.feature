Feature: User Authentication
  As a user of the gym system
  I want to authenticate with my credentials
  So that I can access the system securely

  Background:
    Given the gym system is running

  Scenario: Successful login with valid credentials
    Given a user exists with username "john.trainer" and password "password123"
    When the user attempts to login with username "john.trainer" and password "password123"
    Then the authentication should be successful
    And a JWT token should be returned
    And the token should contain username "john.trainer"

  Scenario: Failed login with invalid password
    Given a user exists with username "john.trainer" and password "password123"
    When the user attempts to login with username "john.trainer" and password "wrongpassword"
    Then the authentication should fail
    And no JWT token should be returned
    And an error message should indicate "Invalid credentials"

  Scenario: Failed login with non-existent user
    When the user attempts to login with username "nonexistent.user" and password "password123"
    Then the authentication should fail
    And no JWT token should be returned
    And an error message should indicate "User not found"

  Scenario: Login attempt with empty credentials
    When the user attempts to login with username "" and password ""
    Then the authentication should fail
    And a validation error should be returned