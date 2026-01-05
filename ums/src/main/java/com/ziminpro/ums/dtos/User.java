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
    UUID id;
    String name;
    String email;
    String password;
    private String githubId;
    int created;
    List<Roles> roles = new ArrayList<>();
    LastSession lastSession;

    public void addRole(Roles role) {
        this.roles.add(role);
    }

    public boolean hasRole(Roles role) { return this.roles != null && this.roles.contains(role); }
}