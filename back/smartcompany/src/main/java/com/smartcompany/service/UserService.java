package com.smartcompany.service;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.smartcompany.dto.CredentialsDto;
import com.smartcompany.dto.SignUpDto;
import com.smartcompany.dto.UserDto;
import com.smartcompany.exception.AppException;
import com.smartcompany.model.User;
import com.smartcompany.model.UserType;
import com.smartcompany.repository.UserRepository;
import com.smartcompany.service.ActivationTokenService;
import com.smartcompany.service.EmailService;
import com.smartcompany.service.UserService;

@Service
public class UserService {

    @Autowired
    private EmailService emailService;
    @Autowired
    private ActivationTokenService tokenService;
    @Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

    public boolean validateActivationToken(String username, String token) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return token.equals(user.get().getActivationToken()) && !user.get().isActive();
        }
        return false;
    }

    public void activateUser(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        user.ifPresent(u -> {
            u.setActive(true);
            u.setActivationToken(null);
            userRepository.save(u);
        });
    }

    public List<UserDto> getAll() {
        List<User> users = userRepository.findAll();
        return toUserDtos(users);
    }
	
    public UserDto login(CredentialsDto credentialsDto) {
        User user = userRepository.findByUsername(credentialsDto.username())
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

        if (passwordEncoder.matches(CharBuffer.wrap(credentialsDto.password()), user.getPassword())) {
        	// if (user.getIsBlocked() == true) 
            //     throw new AppException("User is blocked", HttpStatus.BAD_REQUEST);
            return toUserDto(user);
        }
        throw new AppException("Invalid password", HttpStatus.BAD_REQUEST);
    }
    
    public UserDto register(SignUpDto userDto) {
    	
    	Optional<User> optionalUser = userRepository.findByUsername(userDto.username());

        if (optionalUser.isPresent()) {
            throw new AppException("Username already exists", HttpStatus.BAD_REQUEST);
        }
        
        User user = signUpToUser(userDto);
        user.setPassword(passwordEncoder.encode(CharBuffer.wrap(userDto.password())));

        if (!user.isActive() && !user.getType().equals(UserType.SuperAdmin)) {
            String baseUrl = "http://localhost:4200";
            String token = tokenService.generateActivationToken();
    
            user.setActivationToken(token);
            String activationLink = baseUrl + "/activate?username=" + user.getUsername() + "&token=" + token;

            emailService.sendActivationEmail(user.getEmail(), activationLink);
        }

        User savedUser = userRepository.save(user);

        return toUserDto(savedUser);
    }

	public UserDto update(SignUpDto signUpDto, String username) throws Exception {
		User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
		
		User updatedUser = new User(signUpDto.username(), "", signUpDto.type(), signUpDto.isActive(), signUpDto.email(), signUpDto.image(), signUpDto.activationToken());

		if (signUpDto.password().length == 0) {
			updatedUser.setPassword(user.getPassword());
		}
		else {
			updatedUser.setPassword(passwordEncoder.encode(CharBuffer.wrap(signUpDto.password())));
		}

		delete(username);

        User savedUser = userRepository.save(updatedUser);

        return toUserDto(savedUser);
	}
	
	public void delete(String username) {
		userRepository.deleteById(username);
	}
    
	public UserDto findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
        
        return toUserDto(user);
    }
	
    public User toUser(UserDto userDto) {
        if (userDto == null) {
            return null;
        }

        User user = new User();

        user.setUsername( userDto.getUsername() );
    	user.setType(userDto.getType());
    	user.setActive(userDto.isActive());
    	user.setEmail( userDto.getEmail());
    	user.setImage( userDto.getImage());
    	user.setActivationToken( userDto.getActivationToken());

        return user;
    }

    public UserDto toUserDto(User user) {
        if (user == null) {
            return null;
        }

        String username = null;

        username = user.getUsername();
        String password = user.getPassword();
        UserType type = user.getType();
        boolean isActive = user.isActive();
        String email = user.getEmail();
        String image = user.getImage();
        String activationToken = user.getActivationToken();

        UserDto userDto = new UserDto(username, password, type, isActive, email, image, activationToken);

        return userDto;
    }

    public List<UserDto> toUserDtos(List<User> users) {
        if ( users == null ) {
            return null;
        }

        List<UserDto> list = new ArrayList<UserDto>( users.size() );
        for ( User user : users ) {
            list.add( toUserDto( user ) );
        }

        return list;
    }

    public void updateUser(User target, User source) {
        if ( source == null ) {
            return;
        }

        target.setUsername( source.getUsername() );
        target.setPassword( source.getPassword());
        target.setType( source.getType());
        target.setActive( source.isActive());
        target.setEmail( source.getEmail() );
        target.setImage( source.getImage() );
        target.setActivationToken( source.getActivationToken() );
    }
    
    public User signUpToUser(SignUpDto signUpDto) {
    	if (signUpDto == null) {
            return null;
        }

        User user = new User();

        user.setUsername( signUpDto.username() );
    	user.setType(signUpDto.type());
        user.setActive(signUpDto.isActive());
    	user.setEmail( signUpDto.email());
    	user.setImage( signUpDto.image());
    	user.setActivationToken( signUpDto.activationToken());

        return user;
    }
    
}
