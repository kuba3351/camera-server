package com.raspberry.camera;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    ConfigFileService configFileService;

    public static LocalDateTime appStartDate = LocalDateTime.now();

    @Autowired
    public void configureFlobal(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception
    {
        UsernameAndPasswordDTO usernameAndPasswordDTO = configFileService.getUsernameAndPasswordDTO();
        if(usernameAndPasswordDTO.getEnabled()) {
            authenticationManagerBuilder.inMemoryAuthentication()
                    .withUser(usernameAndPasswordDTO.getUsername())
                    .password(usernameAndPasswordDTO.getPassword()).roles("ADMIN");
        }
        else {
            authenticationManagerBuilder.inMemoryAuthentication()
                    .withUser("default")
                    .password("default").roles("ADMIN");
        }
    }
    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception
    {
        UsernameAndPasswordDTO usernameAndPasswordDTO = configFileService.getUsernameAndPasswordDTO();
        if(usernameAndPasswordDTO.getEnabled()) {
            httpSecurity.authorizeRequests().antMatchers("/api/**").authenticated()
                    .and().sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and().addFilterBefore(new AuthorizationFilter(usernameAndPasswordDTO), UsernamePasswordAuthenticationFilter.class)
                    .csrf().disable();
        }
        else {
            httpSecurity.authorizeRequests().antMatchers("/api/**").authenticated()
                    .and().sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and().addFilterBefore(new PermitAllFilter(), UsernamePasswordAuthenticationFilter.class)
                    .csrf().disable();
        }
    }

}

