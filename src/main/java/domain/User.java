package domain;

import java.time.LocalDate;

public class User {
    private int id;
    private String name;
    private String userName;
    private String password;
    private Role role;
    private boolean isActive;
    private LocalDate createdAt;

    public enum Role {
        ADMIN,
        ASSISTANT
    }

    public User(int id, String name, String userName, String password, Role role, boolean isActive, LocalDate createdAt) {
        this.id = id;
        this.name = name;
        this.userName = userName;
        this.password = password;
        this.role = role;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public User() {
    }

    public User(String name, String userName, String password, Role role) {
        this.name = name;
        this.userName = userName;
        this.password = password;
        this.role = role;
        this.isActive = true;
        this.createdAt = LocalDate.now();
    }

    public User(int id, String name, String userName, Role role) {
        this.id = id;
        this.name = name;
        this.userName = userName;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }
}

