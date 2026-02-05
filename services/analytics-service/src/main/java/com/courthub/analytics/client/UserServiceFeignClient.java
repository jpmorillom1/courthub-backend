package com.courthub.analytics.client;

import com.courthub.common.dto.analytics.UserInternalDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "USER-SERVICE")
public interface UserServiceFeignClient {

    @GetMapping("/users/internal/users/all")
    List<UserInternalDTO> getAllUsers();
}
