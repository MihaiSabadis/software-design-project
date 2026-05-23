package com.andrei.demo.util;

import com.andrei.demo.model.Person;
import com.andrei.demo.model.Role;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.util.PasswordUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@AllArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final PersonRepository personRepository;
    private final PasswordUtil passwordUtil;

    private static final String ADMIN_EMAIL    = "admin@playworthy.com";
    private static final String ADMIN_PASSWORD = "Admin123!";
    private static final String ADMIN_NAME     = "System Admin";

    @Override
    public void run(String... args) {
        if (personRepository.existsByEmail(ADMIN_EMAIL)) {
            log.info("Admin account already exists, skipping seed.");
            return;
        }

        Person admin = new Person();
        admin.setRole(Role.ADMIN);
        admin.setName(ADMIN_NAME);
        admin.setEmail(ADMIN_EMAIL);
        admin.setAge(30);
        admin.setPassword(passwordUtil.hashPassword(ADMIN_PASSWORD));

        personRepository.save(admin);

        log.info("==============================================");
        log.info("  Default admin account created:");
        log.info("  Email   : {}", ADMIN_EMAIL);
        log.info("  Password: {}", ADMIN_PASSWORD);
        log.info("  CHANGE THIS PASSWORD IMMEDIATELY IN PRODUCTION");
        log.info("==============================================");
    }
}