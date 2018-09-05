package pl.ezgolda.springproject.data.objects;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue
    private Integer id;
    @NotNull
    private String model;
    @NotNull
    private String engine;
    @NotNull
    private Integer price;
    @NotNull
    private boolean isAvailable;
    @OneToMany(mappedBy = "car")
    private Collection<Order> orders;


    public Car() {
    }

    public Car(@NotNull String model, @NotNull String engine, @NotNull Integer price, @NotNull boolean isAvailable) {
        this.model = model;
        this.engine = engine;
        this.price = price;
        this.isAvailable = isAvailable;
        this.orders = new ArrayList<>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }


}
