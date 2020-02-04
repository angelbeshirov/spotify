package bg.sofia.uni.fmi.mjt.spotify.server;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Object representation of the system user. Two users are considered to be
 * equal if their IDs are equal. It is possible for 2 users to have the same email and password and
 * not be equal (currently the system allows multiple users to have the same email)
 *
 * @author angel.beshirov
 */
public class User implements Serializable {

    private static final long serialVersionUID = -4479739480268021769L;
    private static final AtomicInteger GLOBAL_ID = new AtomicInteger(0);
    private final int id;
    private String email;
    private String password;


    public User() {
        this.id = GLOBAL_ID.getAndAdd(1);
        this.email = null;
        this.password = null;
    }

    public User(String email, String password) {
        this.id = GLOBAL_ID.getAndAdd(1);
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
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
