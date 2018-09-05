package pl.ezgolda.springproject.service;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.ezgolda.springproject.data.CarRepository;
import pl.ezgolda.springproject.data.OrderRepository;
import pl.ezgolda.springproject.data.RoleRepository;
import pl.ezgolda.springproject.data.UserRepository;
import pl.ezgolda.springproject.data.objects.Car;
import pl.ezgolda.springproject.data.objects.Order;
import pl.ezgolda.springproject.data.objects.Role;
import pl.ezgolda.springproject.data.objects.User;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class InitializationService implements ApplicationListener<ContextRefreshedEvent> {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CarRepository carRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrderRepository orderRepository;
    private boolean alreadySetup = false;

    @Autowired
    public InitializationService(UserRepository userRepository, RoleRepository roleRepository, CarRepository carRepository, PasswordEncoder passwordEncoder, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.carRepository = carRepository;
        this.passwordEncoder = passwordEncoder;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup)
            return;

        Role adminRole = createRole("ROLE_ADMIN");
        Role userRole = createRole("ROLE_USER");

        User user = new User("test", passwordEncoder.encode("test"), "test@test.pl");
        User admin = new User("admin", passwordEncoder.encode("admin"), "admin@admin.pl");

        Car nubira = new Car("Daewoo Nubira", "2.0", 700, true);
        Car lanos = new Car("Daewoo Lanos", "1.6", 500, true);
        Car matiz = new Car("Daewoo Matiz", "0.9", 200, true);
        Car tico = new Car("Daewoo Tico", "0.7", 100, true);

        Order firstOrder = new Order(user, lanos, new LocalDate(), new LocalDate(2018, 9, 22));
//        Order orderToChange = orderRepository.findById(firstOrder.getId()).get();
//        orderToChange.setRentEndDate(new Date());

        user.addRole(userRole);
        admin.addRole(adminRole);
        userRepository.save(user);
        userRepository.save(admin);

        carRepository.save(nubira);
        carRepository.save(lanos);
        carRepository.save(matiz);
        carRepository.save(tico);

        orderRepository.save(firstOrder);


        alreadySetup = true;

    }

    @Transactional
    protected Role createRole(String name) {
        Optional<Role> role = roleRepository.findByName(name);

        return role.orElseGet(() -> roleRepository.save(new Role(name)));
    }

}
