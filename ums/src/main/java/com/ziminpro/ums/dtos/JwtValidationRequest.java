package com.ziminpro.ums.dtos;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtValidationRequest {
    private String token;
}
