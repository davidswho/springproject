package pl.ezgolda.springproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.ezgolda.springproject.data.RoleRepository;
import pl.ezgolda.springproject.data.UserRepository;
import pl.ezgolda.springproject.data.objects.Role;
import pl.ezgolda.springproject.data.objects.User;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class InitializationService implements ApplicationListener<ContextRefreshedEvent> {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private boolean alreadySetup = false;

    @Autowired
    public InitializationService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup)
            return;

        Role adminRole = createRole("ADMIN");
        Role userRole = createRole("USER");

        User user = new User("test", passwordEncoder.encode("test"), "test@test.pl");
        User admin = new User("admin", passwordEncoder.encode("admin"), "admin@admin.pl");
        user.addRole(userRole);
        admin.addRole(adminRole);
        userRepository.save(user);
        userRepository.save(admin);


        alreadySetup = true;

    }

    @Transactional
    protected Role createRole(String name) {
        Optional<Role> role = roleRepository.findByName(name);

        return role.orElseGet(() -> roleRepository.save(new Role(name)));
    }
}
