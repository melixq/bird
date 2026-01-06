package com.ziminpro.ums.dtos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private UUID id;
    private String name;
    private String email;
    private String password;
    private String githubId;
    private Integer created;
    private List<Roles> roles;
    private Integer tokenVersion;
    private LastSession lastSession;

    public User(UUID id, String name, String email, String password, Integer created,
                List<Roles> roles, LastSession lastSession) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.created = created;
        this.roles = roles;
        this.lastSession = lastSession;
    }

    public boolean hasRole(Roles role) {
        return roles != null && roles.contains(role);
    }

    public void addRole(Roles role) {
        if (this.roles == null) {
            this.roles = new ArrayList<>();
        }
        if (!this.roles.contains(role)) {
            this.roles.add(role);
        }
    }
}