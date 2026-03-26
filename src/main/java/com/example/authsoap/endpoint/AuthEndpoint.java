package com.example.authsoap.endpoint;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.example.authsoap.LoginUserRequest;
import com.example.authsoap.LoginUserResponse;
import com.example.authsoap.RegisterUserRequest;
import com.example.authsoap.RegisterUserResponse;
import com.example.authsoap.ValidateTokenRequest;
import com.example.authsoap.ValidateTokenResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Endpoint
public class AuthEndpoint {

    private static final String NAMESPACE_URI = "http://example.com/authsoap";

    // In-memory storage
    private final Map<String, String> users = new HashMap<>();
    private final Map<String, String> tokens = new HashMap<>();

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "RegisterUserRequest")
    @ResponsePayload
    public RegisterUserResponse registerUser(@RequestPayload RegisterUserRequest request) {

        RegisterUserResponse response = new RegisterUserResponse();

        String username = request.getUsername();
        String password = request.getPassword();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            response.setMessage("Username or password cannot be empty");
            return response;
        }

        if (users.containsKey(username)) {
            response.setMessage("User already exists");
        } else {
            users.put(username, password);
            response.setMessage("User registered successfully");
        }

        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "LoginUserRequest")
    @ResponsePayload
    public LoginUserResponse loginUser(@RequestPayload LoginUserRequest request) {

        LoginUserResponse response = new LoginUserResponse();

        String username = request.getUsername();
        String password = request.getPassword();

        String storedPassword = users.get(username);

        if (storedPassword != null && storedPassword.equals(password)) {
            String token = UUID.randomUUID().toString();
            tokens.put(token, username);

            response.setToken(token);
            response.setMessage("Login successful");
        } else {
            response.setToken("");
            response.setMessage("Invalid credentials");
        }

        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "ValidateTokenRequest")
    @ResponsePayload
    public ValidateTokenResponse validateToken(@RequestPayload ValidateTokenRequest request) {

        ValidateTokenResponse response = new ValidateTokenResponse();

        boolean valid = tokens.containsKey(request.getToken());

        response.setValid(valid);

        if (valid) {
            response.setMessage("Token valid");
        } else {
            response.setMessage("Token invalid");
        }

        return response;
    }
}