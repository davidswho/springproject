package pl.ezgolda.springproject.data;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.ezgolda.springproject.data.objects.User;

public interface UserRepository extends JpaRepository<User, Integer> {
}
