package com.epam.gym.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPassword() { return password; }
    public String getUsername() { return username; }
    public Boolean isActive() { return isActive; }
    public Role getRole() { return role; }

    protected User() {}

    private User(Builder builder) {
        this.id = builder.id;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.username = builder.username;
        this.password = builder.password;
        this.isActive = builder.isActive;
        this.role = builder.role;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        private Long id;
        private String firstName;
        private String lastName;
        private String username;
        private String password;
        private Boolean isActive;
        private Role role;

        public Builder() {}

        public Builder(User user) {
            this.id = user.id;
            this.firstName = user.firstName;
            this.lastName = user.lastName;
            this.username = user.username;
            this.password = user.password;
            this.isActive = user.isActive;
            this.role = user.role;
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }

    public enum Role {
        USER("ROLE_USER"),
        ADMIN("ROLE_ADMIN"),
        TRAINEE("ROLE_TRAINEE"),
        TRAINER("ROLE_TRAINER");

        private final String authority;

        Role(String authority) {
            this.authority = authority;
        }

        public String getAuthority() {
            return authority;
        }

        // Helper method to get role without ROLE_ prefix
        public String getRoleName() {
            return authority.substring(5); // Remove "ROLE_" prefix
        }

        // Static method to get Role from authority string
        public static Role fromAuthority(String authority) {
            for (Role role : Role.values()) {
                if (role.authority.equals(authority)) {
                    return role;
                }
            }
            throw new IllegalArgumentException("Unknown authority: " + authority);
        }
    }
}
