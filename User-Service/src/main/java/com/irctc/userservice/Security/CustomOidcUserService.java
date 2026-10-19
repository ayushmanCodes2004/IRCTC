package com.irctc.userservice.Security;

import com.irctc.userservice.Entity.User;
import com.irctc.userservice.Service.OAuth2UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOidcUserService extends OidcUserService {

    private final OAuth2UserService oAuth2UserService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String subject = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String name = oidcUser.getClaimAsString("name");

        String firstName = name;
        String lastName = "";
        if (name != null && name.contains(" ")) {
            String[] nameParts = name.split(" ", 2);
            firstName = nameParts[0];
            lastName = nameParts[1];
        }

        log.info("OAuth2 login: provider={}, subject={}, email={}", provider, subject, email);

        User user = oAuth2UserService.createOrUpdateOAuthUser(provider, subject, email, firstName, lastName);
        log.info("User saved/updated: id={}, email={}", user.getId(), user.getEmail());

        return oidcUser;
    }
}
