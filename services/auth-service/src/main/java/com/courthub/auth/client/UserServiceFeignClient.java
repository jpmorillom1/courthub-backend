package com.courthub.auth.client;

import com.courthub.common.dto.CreateUserDto;
import com.courthub.common.dto.UserDto;
import com.courthub.common.dto.ValidateCredentialsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "USER-SERVICE")
public interface UserServiceFeignClient {

    @GetMapping("/users/email/{email}")
    UserDto getUserByEmail(@PathVariable("email") String email);

    @GetMapping("/users/{userId}")
    UserDto getUserById(@PathVariable("userId") UUID userId);

    @PostMapping("/users")
    UserDto createUser(@RequestBody CreateUserDto createUserDto);

    @PostMapping("/users/validate-credentials")
    Map<String, Object> validateCredentials(@RequestBody ValidateCredentialsDto requestDto);
}
