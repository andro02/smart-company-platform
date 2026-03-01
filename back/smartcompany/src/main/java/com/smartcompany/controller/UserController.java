package com.smartcompany.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.smartcompany.config.UserAuthenticationProvider;
import com.smartcompany.dto.CredentialsDto;
import com.smartcompany.dto.SignUpDto;
import com.smartcompany.dto.UserDto;
import com.smartcompany.service.UserService;

import jakarta.validation.Valid;

@RestController
public class UserController {

    @Autowired
	private UserService userService;
	@Autowired
	private UserAuthenticationProvider userAuthenticationProvider;
	
	@GetMapping(value="/users/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserDto> getUser(@PathVariable("username") String username) {
		UserDto user = userService.findByUsername(username);

		if (user == null) {
			return new ResponseEntity<UserDto>(HttpStatus.NOT_FOUND);
		}
		
		user.setToken("");
		
		return new ResponseEntity<UserDto>(user, HttpStatus.OK);
	}
	
	@PostMapping("/login")
	public ResponseEntity<UserDto> login(@RequestBody CredentialsDto credentialsDto) {
		UserDto userDto = userService.login(credentialsDto);
		userDto.setToken(userAuthenticationProvider.createToken(userDto));
        return ResponseEntity.ok(userDto);
	}
	
	@PostMapping("/register/customer")
    public ResponseEntity<UserDto> register(@RequestBody @Valid SignUpDto user) {
        UserDto createdUser;
		try {
			createdUser = userService.registerCustomer(user);
		} catch (Exception e) {
			return new ResponseEntity<UserDto>(HttpStatus.BAD_REQUEST);
		}
        createdUser.setToken(userAuthenticationProvider.createToken(createdUser));
        return ResponseEntity.created(URI.create("/api/users/" + createdUser.getUsername())).body(createdUser);
    }
	
	@PutMapping(value = "/account/{username}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserDto> update(@RequestBody SignUpDto signUpDto, @PathVariable String username)
			throws Exception {
		UserDto updatedUser = userService.update(signUpDto, username);
		updatedUser.setToken(userAuthenticationProvider.createToken(updatedUser));
		return ResponseEntity.ok(updatedUser);
	}
	
	// @DeleteMapping(value = "/account/{username}")
	// public ResponseEntity<UserDTO> delete(@PathVariable("username") String emaiusernamel) {
	// 	userService.delete(username);
	// 	return new ResponseEntity<UserDTO>(HttpStatus.NO_CONTENT);
	// }
    
}
