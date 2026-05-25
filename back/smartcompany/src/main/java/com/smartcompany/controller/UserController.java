package com.smartcompany.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartcompany.config.UserAuthenticationProvider;
import com.smartcompany.dto.ChangePasswordDto;
import com.smartcompany.dto.CredentialsDto;
import com.smartcompany.dto.SignUpDto;
import com.smartcompany.dto.UserDto;
import com.smartcompany.exception.AppException;
import com.smartcompany.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Users", description = "Upravljanje korisnicima")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserAuthenticationProvider userAuthenticationProvider;

    @Operation(summary = "Dohvata korisnika po korisničkom imenu")
    @GetMapping(value = "/users/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> getUser(@PathVariable("username") String username) {
        UserDto user = userService.findByUsername(username);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        user.setToken("");
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @Operation(summary = "Dohvata listu menadžera sa pretragom i paginacijom")
    @GetMapping(value = "/users/managers", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SuperAdmin')")
    public ResponseEntity<Page<UserDto>> getManagers(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());
        Page<UserDto> managers = userService.getManagers(search, pageable);
        return ResponseEntity.ok(managers);
    }

    @Operation(summary = "Blokira menadžera")
    @PutMapping(value = "/users/{username}/block")
    @PreAuthorize("hasRole('SuperAdmin')")
    public ResponseEntity<UserDto> blockManager(@PathVariable String username) {
        UserDto updated = userService.blockManager(username);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Odblokirava menadžera")
    @PutMapping(value = "/users/{username}/unblock")
    @PreAuthorize("hasRole('SuperAdmin')")
    public ResponseEntity<UserDto> unblockManager(@PathVariable String username) {
        UserDto updated = userService.unblockManager(username);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Prijava korisnika")
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody CredentialsDto credentialsDto) {
        UserDto userDto = userService.login(credentialsDto);
        userDto.setToken(userAuthenticationProvider.createToken(userDto));
        return ResponseEntity.ok(userDto);
    }

    @Operation(summary = "Registracija kupca")
    @PostMapping("/register/customer")
    public ResponseEntity<UserDto> register(@RequestBody @Valid SignUpDto user) {
        UserDto createdUser;
        try {
            createdUser = userService.registerCustomer(user);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        createdUser.setToken(userAuthenticationProvider.createToken(createdUser));
        return ResponseEntity.created(URI.create("/api/users/" + createdUser.getUsername())).body(createdUser);
    }

    @Operation(summary = "Registracija menadžera — samo SuperAdmin")
    @PostMapping("/register/manager")
    @PreAuthorize("hasRole('SuperAdmin')")
    public ResponseEntity<UserDto> registerManager(@RequestBody @Valid SignUpDto user) {
        UserDto createdUser;
        try {
            createdUser = userService.registerManager(user);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.created(URI.create("/api/users/" + createdUser.getUsername())).body(createdUser);
    }

    @Operation(summary = "Ažuriranje korisnika")
    @PutMapping(value = "/account/{username}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> update(@RequestBody SignUpDto signUpDto, @PathVariable String username)
            throws Exception {
        UserDto updatedUser = userService.update(signUpDto, username);
        updatedUser.setToken(userAuthenticationProvider.createToken(updatedUser));
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Promena lozinke")
    @PutMapping("/account/change-password")
    public ResponseEntity<UserDto> changePassword(@Valid @RequestBody ChangePasswordDto dto) {
        UserDto updatedUser = userService.changePasswordAndReturn(dto);
        String newToken = userAuthenticationProvider.createToken(updatedUser);
        updatedUser.setToken(newToken);
        return ResponseEntity.ok(updatedUser);
    }

	// @DeleteMapping(value = "/account/{username}")
	// public ResponseEntity<UserDTO> delete(@PathVariable("username") String emaiusernamel) {
	// 	userService.delete(username);
	// 	return new ResponseEntity<UserDTO>(HttpStatus.NO_CONTENT);
	// }
}