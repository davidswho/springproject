package pl.ezgolda.springproject.data.objects;

import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Collection;

@Entity
public class User implements UserDetails {

    @NotEmpty
    private Collection<@NotNull GrantedAuthority> authorities;
    @Id
    @GeneratedValue
    private Integer id;
    @NotNull
    @Length(min = 4, max = 16)
    private String username;
    @NotNull
    @Length(min = 4, max = 16)
    private String password;
    @NotNull
    @Email
    private String email;

    public User(@NotNull @Length(min = 4, max = 16) String username,
                @NotNull @Length(min = 4, max = 16) String password,
                @NotNull @Email String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public User() {
        //empty - constructor for JPA to inject
    }

    public boolean addAuthority(@NotNull GrantedAuthority authority) {
        return authorities.add(authority);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getId() {
        return id;
    }

}
