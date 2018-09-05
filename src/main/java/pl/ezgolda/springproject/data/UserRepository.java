package pl.ezgolda.springproject.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.ezgolda.springproject.data.objects.User;

import javax.transaction.Transactional;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM User a WHERE a.id = :id")
    void deleteUserById(@Param("id") Integer id);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User a SET a.username = :username, a.email = :email WHERE a.id = :id")
    void updateUser(@Param("id") Integer id, @Param("username") String username, @Param("email") String email);


}
