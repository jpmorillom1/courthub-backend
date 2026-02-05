package com.courthub.notification.client;

import com.courthub.common.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "USER-SERVICE")
public interface UserServiceFeignClient {

    @GetMapping("/users/{userId}")
    UserDto getUserById(@PathVariable("userId") UUID userId);
}
