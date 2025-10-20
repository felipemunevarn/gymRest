Feature: User Authentication
  As a user of the gym system
  I want to authenticate with my credentials
  So that I can access the system securely

  Background:
    Given the gym system is running

  Scenario: Successful login with valid credentials for trainer
    When the user attempts to login with username "maria.ramirez" and password "password"
    Then the authentication should be successful
    And a JWT token should be returned
    And the token should contain username "maria.ramirez"

  Scenario: Successful login with valid credentials for trainee
    When the user attempts to login with username "antonia.neira" and password "password"
    Then the authentication should be successful
    And a JWT token should be returned
    And the token should contain username "antonia.neira"

  Scenario: Successful login with valid credentials for admin
    When the user attempts to login with username "felipe.munevar" and password "password"
    Then the authentication should be successful
    And a JWT token should be returned
    And the token should contain username "felipe.munevar"

  Scenario: Failed login with invalid password
    When the user attempts to login with username "maria.ramirez" and password "wrongpassword"
    Then the authentication should fail
    And no JWT token should be returned

  Scenario: Failed login with non-existent user
    When the user attempts to login with username "nonexistent.user" and password "password"
    Then the authentication should fail
    And no JWT token should be returned

  Scenario: Login attempt with empty credentials
    When the user attempts to login with username "" and password ""
    Then the authentication should fail