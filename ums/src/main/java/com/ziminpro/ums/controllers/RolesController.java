package com.ziminpro.ums.controllers;

import java.util.ArrayList;
import java.util.Map;

import com.ziminpro.ums.dao.UmsRepository;
import com.ziminpro.ums.dtos.ApiResponse;
import com.ziminpro.ums.dtos.Constants;
import com.ziminpro.ums.dtos.Roles;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController()
@RequestMapping("/roles")
public class RolesController {
    private final UmsRepository umsRepository;

    public RolesController(UmsRepository umsRepository) {
        this.umsRepository = umsRepository;
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<?>>> getAllRoles() {
        Map<String, Roles> roles = umsRepository.findAllRoles();

        if (roles == null) {
            return Mono.just(ResponseEntity.ok()
                    .header(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON)
                    .header(Constants.ACCEPT, Constants.APPLICATION_JSON)
                    .body(ApiResponse.error(500, "Roles have not been retrieved", new ArrayList<>())));
        }

        return Mono.just(ResponseEntity.ok()
                .header(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON)
                .header(Constants.ACCEPT, Constants.APPLICATION_JSON)
                .body(ApiResponse.success(new ArrayList<>(roles.values()))));
    }
}
