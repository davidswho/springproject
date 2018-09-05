package pl.ezgolda.springproject.data;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.ezgolda.springproject.data.objects.Car;

import java.util.Optional;


public interface CarRepository extends JpaRepository<Car, Integer> {

    Optional<Car> findByModel(String model);

//    @Transactional
//    @Modifying(clearAutomatically = true)
//    @Query("UPDATE Car a SET a.user = :user, a.rentStartDate = :startDate, a.rentEndDate = :endDate WHERE a.id = :carId")
//    void orderCarByUserId(@Param("user") User user, @Param("carId") Integer carId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

}
