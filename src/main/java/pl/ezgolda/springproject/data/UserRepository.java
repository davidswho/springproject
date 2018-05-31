package pl.ezgolda.springproject.data;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.ezgolda.springproject.data.objects.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

}
