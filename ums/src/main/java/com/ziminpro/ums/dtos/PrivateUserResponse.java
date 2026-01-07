package com.ziminpro.ums.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrivateUserResponse {
    private UUID id;
    private String name;
    private String email;
    private String avatarUrl;
    private List<String> roles;
    private Integer created;
    private LastSession lastSession;
}
