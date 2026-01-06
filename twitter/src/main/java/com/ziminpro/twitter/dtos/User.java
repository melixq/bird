package com.ziminpro.twitter.dtos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private UUID id;
    private String name;
    private String email;
    private String password;
    private String githubId;
    private String avatarUrl;
    private Integer created;
    private List<Roles> roles = new ArrayList<>();
    private Integer tokenVersion;
    private LastSession lastSession;

    public void addRole(Roles role) {
        this.roles.add(role);
    }

    public boolean hasRole(String s) {
        for (Roles role : roles) {
            if(role.getRole().equalsIgnoreCase(s)) return true;
        }
        return false;
    }
}