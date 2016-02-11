package izettle.cassandra;

import com.datastax.driver.mapping.EnumType;
import com.datastax.driver.mapping.annotations.Enumerated;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(name = "user", keyspace = "test")
public class User {

    public enum Gender {
        MALE, FEMALE
    }

    @PartitionKey
    private final int id;

    private final String email;

    @Enumerated(EnumType.STRING)
    private final Gender gender;

    public User(int id, String email, Gender gender) {
        this.id = id;
        this.email = email;
        this.gender = gender;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Gender getGender() {
        return gender;
    }

    @Override
    public String toString() {
        return "izettle.cassandra.User{" +
            "id=" + id +
            ", email='" + email + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id != user.id) return false;
        if (!email.equals(user.email)) return false;
        return gender == user.gender;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + email.hashCode();
        result = 31 * result + gender.hashCode();
        return result;
    }
}
