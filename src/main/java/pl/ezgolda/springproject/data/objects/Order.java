package pl.ezgolda.springproject.data.objects;

import lombok.ToString;
import org.joda.time.LocalDate;

import javax.persistence.*;

@Entity
@Table(name = "orders")
@ToString
public class Order {

    @Id
    @GeneratedValue
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;

    private LocalDate rentStartDate;
    private LocalDate rentEndDate;

    public Order() {
    }

    public Order(User user, Car car, LocalDate rentStartDate, LocalDate rentEndDate) {
        this.user = user;
        this.car = car;
        this.rentStartDate = rentStartDate;
        this.rentEndDate = rentEndDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public LocalDate getRentStartDate() {
        return rentStartDate;
    }

    public void setRentStartDate(LocalDate rentStartDate) {
        this.rentStartDate = rentStartDate;
    }

    public LocalDate getRentEndDate() {
        return rentEndDate;
    }

    public void setRentEndDate(LocalDate rentEndDate) {
        this.rentEndDate = rentEndDate;
    }
}
