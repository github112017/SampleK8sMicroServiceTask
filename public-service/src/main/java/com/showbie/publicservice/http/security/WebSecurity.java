package com.showbie.publicservice.http.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Extend web request security by requiring JWT token validation for each request.
 */
@Configuration
public class WebSecurity extends WebSecurityConfigurerAdapter {
    private NonAuthenticatedRequestEntryPoint nonAuthenticatedRequestEntryPoint;
    private TokenValidationFilter tokenValidationFilter;

    @Autowired
    public void setNonAuthenticatedRequestEntryPoint(NonAuthenticatedRequestEntryPoint nonAuthenticatedRequestEntryPoint) {
        this.nonAuthenticatedRequestEntryPoint = nonAuthenticatedRequestEntryPoint;
    }

    @Autowired
    public void setTokenValidationFilter(TokenValidationFilter tokenValidationFilter) {
        this.tokenValidationFilter = tokenValidationFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // ensure all requests are authenticated
        http.authorizeRequests().anyRequest().authenticated();

        // non-authenticated requests return a 401
        http.exceptionHandling().authenticationEntryPoint(nonAuthenticatedRequestEntryPoint);

        // add our token validation filter into the authentication chain
        http.addFilterBefore(tokenValidationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}

