package com.smartcompany.demo;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.smartcompany.exception.AppException;
import com.smartcompany.model.user.UserType;

/**
 * Public demo accounts, one per role, for visitors of the live demo
 * (app.demo.enabled=true). They are created on startup with a known password
 * (DemoAccountsInitializer), and nobody can change their password, block or
 * delete them, so they keep working for everyone until the nightly reset.
 */
@Component
public class DemoAccounts {

    public static final Map<String, UserType> ACCOUNTS = new LinkedHashMap<>();
    static {
        ACCOUNTS.put("demo-admin", UserType.SuperAdmin);
        ACCOUNTS.put("demo-manager", UserType.Manager);
        ACCOUNTS.put("demo-customer", UserType.Customer);
    }

    private final boolean enabled;

    public DemoAccounts(@Value("${app.demo.enabled:false}") boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isDemoAccount(String username) {
        return enabled && ACCOUNTS.containsKey(username);
    }

    /** Stops changes that would lock other visitors out of a demo account. */
    public void ensureCanBeChanged(String username) {
        if (isDemoAccount(username)) {
            throw new AppException("Demo accounts can't be changed", HttpStatus.FORBIDDEN);
        }
    }
}
