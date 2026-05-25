package com.smartcompany.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.smartcompany.model.UserType;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UserDto implements UserDetails {

    private String username;
    private String token;
    private UserType type;
    @JsonProperty("isActive")
    private boolean isActive;
    @JsonProperty("mustChangePassword")
    private boolean mustChangePassword;
    private String email;
    private String image;
    private String activationToken;

    @Override
    @JsonDeserialize(using = CustomAuthorityDeserializer.class)
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new SimpleGrantedAuthority("ROLE_" + this.getType()));
        return list;
    }

    @Override
    public String getPassword() { return null; }

    @Override
    public String getUsername() { return username; }
}