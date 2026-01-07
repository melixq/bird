package com.ziminpro.ums.controllers;

import java.util.*;

import com.ziminpro.ums.dtos.*;
import com.ziminpro.ums.mappers.UserMapper;
import com.ziminpro.ums.services.UserService;
import com.ziminpro.ums.utils.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<PublicUserResponse>>>> getAllUsers() {
        List<PublicUserResponse> users =
                userService.getAllUsers().values().stream()
                        .map(UserMapper::toPublic)
                        .toList();

        return Mono.just(ResponseEntity.ok(ApiResponse.success(users)));
    }

    @GetMapping("/{userId}")
    public Mono<ResponseEntity<ApiResponse<?>>> getUser(@PathVariable UUID userId) {
        User user = userService.getUserById(userId);

        if (user.getId() == null) {
            return Mono.just(
                    ResponseEntity.status(404)
                            .body(ApiResponse.error(404, "User not found"))
            );
        }

        return Mono.zip(
                SecurityUtils.currentUserId(),
                SecurityUtils.isAdmin()
        ).map(tuple -> {
            boolean isSelf = tuple.getT1().equals(userId);
            boolean isAdmin = tuple.getT2();

            if (isSelf || isAdmin) {
                return ResponseEntity.ok(
                        ApiResponse.success(UserMapper.toPrivate(user))
                );
            }

            return ResponseEntity.ok(
                    ApiResponse.success(UserMapper.toPublic(user))
            );
        });
    }

    @PutMapping("/{userId}")
    public Mono<ResponseEntity<ApiResponse<PrivateUserResponse>>> updateUser(
            @PathVariable UUID userId,
            @RequestBody UpdateUserRequest request
    ) {
        return Mono.zip(
                SecurityUtils.currentUserId(),
                SecurityUtils.isAdmin()
        ).map(tuple -> {
            User updated = userService.updateUser(
                    tuple.getT1(),
                    tuple.getT2(),
                    userId,
                    request
            );
            return ResponseEntity.ok(
                    ApiResponse.success(UserMapper.toPrivate(updated))
            );
        });
    }

    @PutMapping("/me")
    public Mono<ResponseEntity<ApiResponse<User>>> updateSelf(@RequestBody UpdateUserRequest update) {
        return Mono.zip(
                SecurityUtils.currentUserId(),
                SecurityUtils.isAdmin()
        ).map(tuple -> {
            User updated = userService.updateUser(
                    tuple.getT1(),
                    tuple.getT2(),
                    tuple.getT1(),
                    update
            );
            return ResponseEntity.ok(ApiResponse.success(updated));
        });
    }

    @DeleteMapping("/{userId}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteUser(@PathVariable UUID userId) {
        return Mono.zip(
                SecurityUtils.currentUserId(),
                SecurityUtils.isAdmin()
        ).doOnNext(tuple ->
                userService.deleteUser(
                        tuple.getT1(),
                        tuple.getT2(),
                        userId
                )
        ).thenReturn(
                ResponseEntity.ok(ApiResponse.success(null))
        );
    }

    @DeleteMapping("/me")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteSelf() {
        return Mono.zip(
                SecurityUtils.currentUserId(),
                SecurityUtils.isAdmin()
        ).doOnNext(tuple ->
                userService.deleteUser(
                        tuple.getT1(),
                        tuple.getT2(),
                        tuple.getT1()
                )
        ).thenReturn(
                ResponseEntity.ok(ApiResponse.success(null))
        );
    }
}