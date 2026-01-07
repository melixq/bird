package com.ziminpro.ums.mappers;

import com.ziminpro.ums.dtos.PrivateUserResponse;
import com.ziminpro.ums.dtos.PublicUserResponse;
import com.ziminpro.ums.dtos.Roles;
import com.ziminpro.ums.dtos.User;

import java.util.List;

public class UserMapper {
    private UserMapper() {}

    public static PublicUserResponse toPublic(User user) {
        return PublicUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .avatarUrl(user.getAvatarUrl())
                .roles(extractRoles(user))
                .build();
    }

    public static PrivateUserResponse toPrivate(User user) {
        return PrivateUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .created(user.getCreated())
                .lastSession(user.getLastSession())
                .roles(extractRoles(user))
                .build();
    }

    private static List<String> extractRoles(User user) {
        return user.getRoles() == null
                ? List.of()
                : user.getRoles().stream()
                .map(Roles::getRole)
                .toList();
    }
}
