package bg.sofia.uni.fmi.mjt.spotify.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Object representation of the system user. Two users are considered to be
 * equal if their emails are equal. It is not possible for 2 users to have the same email.
 *
 * @author angel.beshirov
 */
public class User implements Serializable {

    private static final long serialVersionUID = -4479739480268021769L;
    private String email;
    private String password;


    public User() {
        this.email = null;
        this.password = null;
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

}
