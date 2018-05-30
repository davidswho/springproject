package pl.ezgolda.springproject.data.objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Role {
    @Id
    @GeneratedValue
    private Integer id;
    @NotNull
    private String name;
    @ManyToMany(mappedBy = "roles")
    private Set<User> users;

    public Role(@NotNull String name, Set<User> users) {
        this.name = name;
        this.users = users;
    }

    public Role(@NotNull String name) {
        this.name = name;
        this.users = new HashSet<>();
    }

    public Role() {
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<User> getUsers() {
        return users;
    }

    public boolean addAllUsers(Collection<User> users) {
        return this.users.addAll(users);
    }

    public boolean addUser(User user) {
        return users.add(user);
    }
}
