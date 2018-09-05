package pl.ezgolda.springproject.data;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.ezgolda.springproject.data.objects.Order;
import pl.ezgolda.springproject.data.objects.User;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    Optional<List<Order>> findAllByCar_Id(Integer carId);

    Optional<List<Order>> findAllByUser(User user);

}
