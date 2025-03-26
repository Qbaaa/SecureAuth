package com.qbaaa.secure.auth.service.strategy;

import com.qbaaa.secure.auth.dto.AuthRequest;
import com.qbaaa.secure.auth.dto.RefreshTokenRequest;
import com.qbaaa.secure.auth.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("refreshToken")
@RequiredArgsConstructor
public class RefreshTokenStrategyService extends AuthStrategy {


    @Override
    public TokenResponse authenticate(String domainName, String baseUrl, AuthRequest request) {
        var refreshToken = (RefreshTokenRequest) request;
        return null;//"Refreshing token for: " + refreshToken.getToken();
    }

}
