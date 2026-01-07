package com.ziminpro.ums.dao;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ziminpro.ums.dtos.Constants;
import com.ziminpro.ums.dtos.LastSession;
import com.ziminpro.ums.dtos.Roles;
import com.ziminpro.ums.dtos.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcUmsRepository implements UmsRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcUmsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userWithRolesRowMapper = (rs, rowNum) -> {
        UUID userId = DaoHelper.bytesArrayToUuid(rs.getBytes("users.id"));
        String name = rs.getString("users.name");
        String email = rs.getString("users.email");
        String password = rs.getString("users.password");
        String githubId = rs.getString("users.github_id");
        String avatarUrl = rs.getString("users.avatar_url");
        Integer created = rs.getInt("users.created");

        LastSession lastSession = null;
        Integer lastVisitIn = (Integer) rs.getObject("last_visit.in");
        Integer lastVisitOut = (Integer) rs.getObject("last_visit.out");
        if (lastVisitIn != null && lastVisitOut != null) {
            lastSession = new LastSession(lastVisitIn, lastVisitOut);
        }

        Roles role = null;
        byte[] roleIdBytes = rs.getBytes("roles.id");
        if (roleIdBytes != null) {
            role = new Roles(
                    DaoHelper.bytesArrayToUuid(roleIdBytes),
                    rs.getString("roles.name"),
                    rs.getString("roles.description")
            );
        }

        User user = new User();
        user.setId(userId);
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setGithubId(githubId);
        user.setAvatarUrl(avatarUrl);
        user.setCreated(created);
        user.setLastSession(lastSession);

        if (role != null) {
            user.setRoles(Arrays.asList(role));
        }

        return user;
    };

    @Override
    public Map<UUID, User> findAllUsers() {
        Map<UUID, User> users = new HashMap<>();

        List<User> userList = jdbcTemplate.query(Constants.GET_ALL_USERS, userWithRolesRowMapper);

        for (User user : userList) {
            if (!users.containsKey(user.getId())) {
                User newUser = new User();
                newUser.setId(user.getId());
                newUser.setName(user.getName());
                newUser.setEmail(user.getEmail());
                newUser.setPassword(user.getPassword());
                newUser.setGithubId(user.getGithubId());
                newUser.setCreated(user.getCreated());
                newUser.setAvatarUrl(user.getAvatarUrl());
                newUser.setLastSession(user.getLastSession());
                users.put(user.getId(), newUser);
            }
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                users.get(user.getId()).addRole(user.getRoles().getFirst());
            }
        }
        return users;
    }

    @Override
    public User findUserByID(UUID userId) {
        User user = new User();
        List<User> users = jdbcTemplate.query(
                Constants.GET_USER_BY_ID_FULL,
                userWithRolesRowMapper,
                userId.toString()
        );

        for (User u : users) {
            if (user.getId() == null) {
                user.setId(u.getId());
                user.setName(u.getName());
                user.setEmail(u.getEmail());
                user.setPassword(u.getPassword());
                user.setGithubId(u.getGithubId());
                user.setAvatarUrl(u.getAvatarUrl());
                user.setTokenVersion(u.getTokenVersion());
                user.setCreated(u.getCreated());
                user.setLastSession(u.getLastSession());
            }
            if (u.getRoles() != null && !u.getRoles().isEmpty()) {
                user.addRole(u.getRoles().getFirst());
            }
        }
        return user;
    }

    @Override
    public User findUserByGithubId(String githubId) {
        User user = new User();
        List<User> users = jdbcTemplate.query(
                Constants.GET_USER_BY_GITHUB_ID,
                userWithRolesRowMapper,
                githubId
        );

        for (User u : users) {
            if (user.getId() == null) {
                user.setId(u.getId());
                user.setName(u.getName());
                user.setEmail(u.getEmail());
                user.setPassword(u.getPassword());
                user.setGithubId(u.getGithubId());
                user.setAvatarUrl(u.getAvatarUrl());
                user.setTokenVersion(u.getTokenVersion());
                user.setCreated(u.getCreated());
                user.setLastSession(u.getLastSession());
            }
            if (u.getRoles() != null && !u.getRoles().isEmpty()) {
                user.addRole(u.getRoles().getFirst());
            }
        }
        return user;
    }

    @Override
    public User findUserByEmail(String email) {
        User user = new User();
        List<User> users = jdbcTemplate.query(
                Constants.GET_USER_BY_EMAIL,
                userWithRolesRowMapper,
                email
        );

        for (User u : users) {
            if (user.getId() == null) {
                user.setId(u.getId());
                user.setName(u.getName());
                user.setEmail(u.getEmail());
                user.setPassword(u.getPassword());
                user.setGithubId(u.getGithubId());
                user.setAvatarUrl(u.getAvatarUrl());
                user.setTokenVersion(u.getTokenVersion());
                user.setCreated(u.getCreated());
                user.setLastSession(u.getLastSession());
            }
            if (u.getRoles() != null && !u.getRoles().isEmpty()) {
                user.addRole(u.getRoles().getFirst());
            }
        }
        return user;
    }

    @Override
    public UUID createUser(User user) {
        long timestamp = Instant.now().getEpochSecond();
        Map<String, Roles> roles = this.findAllRoles();
        UUID userId = UUID.randomUUID();

        try {
            jdbcTemplate.update(Constants.CREATE_USER, userId.toString(), user.getName(), user.getEmail(),
                    user.getPassword(), timestamp, null);

            if (user.getRoles() != null) {
                for (Roles role : user.getRoles()) {
                    Roles existingRole = roles.get(role.getRole());
                    if (existingRole != null) {
                        jdbcTemplate.update(Constants.ASSIGN_ROLE, userId.toString(),
                                existingRole.getRoleId().toString());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return userId;
    }

    @Override
    public UUID createOrUpdateGithubUser(User user) {
        User existingUser = findUserByGithubId(user.getGithubId());

        if (existingUser.getId() != null) {
            try {
                jdbcTemplate.update(
                        Constants.UPDATE_USER_BY_GITHUB_ID,
                        user.getName(),
                        user.getEmail(),
                        user.getAvatarUrl(),
                        user.getGithubId()
                );
                return existingUser.getId();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            UUID userId = UUID.randomUUID();
            long timestamp = Instant.now().getEpochSecond();

            try {
                jdbcTemplate.update(
                        Constants.CREATE_USER_WITH_GITHUB,
                        userId.toString(),
                        user.getName(),
                        user.getEmail(),
                        user.getGithubId(),
                        user.getAvatarUrl(),
                        timestamp,
                        user.getTokenVersion()
                );

                Roles subscriberRole = findRoleByName("SUBSCRIBER");
                if (subscriberRole != null) {
                    jdbcTemplate.update(
                            Constants.ASSIGN_ROLE,
                            userId.toString(),
                            subscriberRole.getRoleId().toString()
                    );
                }

                Roles producerRole = findRoleByName("PRODUCER");
                if (producerRole != null) {
                    jdbcTemplate.update(
                            Constants.ASSIGN_ROLE,
                            userId.toString(),
                            producerRole.getRoleId().toString()
                    );
                }

                return userId;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @Override
    public boolean incrementUserTokenVersion(UUID userId) {
        var sql = "UPDATE users SET token_version = COALESCE(token_version, 0) + 1, WHERE id = ?";
        int result = jdbcTemplate.update(sql,
                (Object) DaoHelper.uuidToBytesArray(userId)
        );
        return result == 1;
    }

    @Override
    public Integer getUserTokenVersion(UUID userId) {
        var sql = "SELECT token_version FROM users WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class,
                    (Object) DaoHelper.uuidToBytesArray(userId));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public int deleteUser(UUID userId) {
        return jdbcTemplate.update(Constants.DELETE_USER, userId.toString());
    }

    @Override
    public Map<String, Roles> findAllRoles() {
        Map<String, Roles> roles = new HashMap<>();
        jdbcTemplate.query(Constants.GET_ALL_ROLES, rs -> {
            Roles role = new Roles(
                    DaoHelper.bytesArrayToUuid(rs.getBytes("roles.id")),
                    rs.getString("roles.name"),
                    rs.getString("roles.description")
            );
            roles.put(rs.getString("roles.name"), role);
        });
        return roles;
    }

    private Roles findRoleByName(String roleName) {
        try {
            List<Roles> roles = jdbcTemplate.query(
                    Constants.GET_ROLE_BY_NAME,
                    (rs, rowNum) -> new Roles(
                            DaoHelper.bytesArrayToUuid(rs.getBytes("id")),
                            rs.getString("name"),
                            rs.getString("description")
                    ),
                    roleName
            );
            return roles.isEmpty() ? null : roles.getFirst();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
