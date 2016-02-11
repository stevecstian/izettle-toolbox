package izettle.cassandra;

import com.izettle.cassandra.DataRow;
import com.izettle.cassandra.RowMapper;

public class UserMapper implements RowMapper<User> {
    @Override
    public User map(DataRow row) {
        return new User(row.getInt("id"), row.getString("email"), User.Gender.valueOf(row.getString("gender")));
    }
}
