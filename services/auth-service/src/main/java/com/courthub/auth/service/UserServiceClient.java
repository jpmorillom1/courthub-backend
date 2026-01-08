package com.courthub.auth.service;

import com.courthub.common.dto.CreateUserDto;
import com.courthub.common.dto.UserDto;
import com.courthub.common.dto.ValidateCredentialsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Component
public class UserServiceClient {

    private final RestTemplate restTemplate;
    private final String userServiceBaseUrl;

    @Autowired
    public UserServiceClient(RestTemplate restTemplate, @Qualifier("userServiceBaseUrl") String userServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.userServiceBaseUrl = userServiceBaseUrl;
    }

    public UserDto getUserByEmail(String email) {
        try {
            String url = userServiceBaseUrl + "/users/email/" + email;
            ResponseEntity<UserDto> response = restTemplate.getForEntity(url, UserDto.class);
            return response.getBody();
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    public UserDto getUserById(UUID userId) {
        try {
            String url = userServiceBaseUrl + "/users/" + userId;
            ResponseEntity<UserDto> response = restTemplate.getForEntity(url, UserDto.class);
            return response.getBody();
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    public UserDto createUser(CreateUserDto createUserDto) {
        String url = userServiceBaseUrl + "/users";
        HttpEntity<CreateUserDto> request = new HttpEntity<>(createUserDto);
        ResponseEntity<UserDto> response = restTemplate.postForEntity(url, request, UserDto.class);
        return response.getBody();
    }

    public UserDto createUserWithToken(CreateUserDto createUserDto, String accessToken) {
        String url = userServiceBaseUrl + "/users";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<CreateUserDto> request = new HttpEntity<>(createUserDto, headers);
        ResponseEntity<UserDto> response = restTemplate.exchange(url, HttpMethod.POST, request, UserDto.class);
        return response.getBody();
    }

    @SuppressWarnings("unchecked")
    public boolean validateCredentials(String email, String password) {
        String url = userServiceBaseUrl + "/users/validate-credentials";
        ValidateCredentialsDto requestDto = new ValidateCredentialsDto(email, password);
        HttpEntity<ValidateCredentialsDto> request = new HttpEntity<>(requestDto);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        Map<String, Object> body = response.getBody();
        return body != null && Boolean.TRUE.equals(body.get("valid"));
    }
}
