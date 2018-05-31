package pl.ezgolda.springproject.data;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.ezgolda.springproject.data.objects.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String name);
}
