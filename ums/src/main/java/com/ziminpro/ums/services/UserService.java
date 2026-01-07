package com.ziminpro.ums.services;

import com.ziminpro.ums.dao.UmsRepository;
import com.ziminpro.ums.dtos.UpdateUserRequest;
import com.ziminpro.ums.dtos.User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class UserService {
    private final UmsRepository repository;

    public UserService(UmsRepository repository) {
        this.repository = repository;
    }

    public Map<UUID, User> getAllUsers() {
        return repository.findAllUsers();
    }

    public User getUserById(UUID userId) {
        return repository.findUserByID(userId);
    }

    public User updateUser(
            UUID authenticatedUserId,
            boolean isAdmin,
            UUID targetUserId,
            UpdateUserRequest request
    ) {
        if (!isAdmin && !authenticatedUserId.equals(targetUserId)) {
            throw new SecurityException("Forbidden");
        }

        User existing = repository.findUserByID(targetUserId);

        if (existing.getId() == null) {
            throw new IllegalStateException("User not found");
        }

        existing.setName(request.getName());
        existing.setAvatarUrl(request.getAvatarUrl());

        repository.updateUser(existing);
        return repository.findUserByID(targetUserId);
    }

    public void deleteUser(
            UUID authenticatedUserId,
            boolean isAdmin,
            UUID targetUserId
    ) {
        if (!isAdmin && !authenticatedUserId.equals(targetUserId)) {
            throw new SecurityException("Forbidden");
        }

        repository.deleteUser(targetUserId);
        repository.incrementUserTokenVersion(targetUserId);
    }
}
