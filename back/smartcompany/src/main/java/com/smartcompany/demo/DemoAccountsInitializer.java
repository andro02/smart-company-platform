package com.smartcompany.demo;

import java.nio.CharBuffer;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.smartcompany.model.user.User;
import com.smartcompany.model.user.UserType;
import com.smartcompany.repository.user.UserRepository;

/**
 * Creates the demo accounts (or puts them back in order) every time the
 * application starts, e.g. after the nightly reset of the database.
 */
@Component
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoAccountsInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CacheManager cacheManager;
    private final String password;

    public DemoAccountsInitializer(UserRepository userRepository,
                                   PasswordEncoder passwordEncoder,
                                   CacheManager cacheManager,
                                   @Value("${app.demo.password:demo1234}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cacheManager = cacheManager;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        Cache users = cacheManager.getCache("users");
        for (Map.Entry<String, UserType> account : DemoAccounts.ACCOUNTS.entrySet()) {
            String username = account.getKey();
            User user = userRepository.findByUsername(username).orElseGet(User::new);
            user.setUsername(username);
            user.setType(account.getValue());
            user.setPassword(passwordEncoder.encode(CharBuffer.wrap(password)));
            user.setActive(true);
            user.setMustChangePassword(false);
            user.setEmail(username + "@demo.local");
            user.setActivationToken(null);
            userRepository.save(user);
            if (users != null) users.evict(username);
        }
    }
}
