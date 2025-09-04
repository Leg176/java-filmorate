package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class UserRepository extends BaseRepository<User> {
     private static final String FIND_ALL_FRIENDS_QUERY = "SELECT f.idUserFriends FROM Friends f WHERE f.idUser = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM Users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM Users WHERE idUser = ?";
    private static final String FIND_BY_EMAIL_QUERY = "SELECT * FROM Users WHERE email = ?";
    private static final String INSERT_QUERY = "INSERT INTO Users(email, login, name, birthday)" +
            "VALUES (?, ?, ?, ?) returning idUser";
    private static final String ADD_FRIEND_QUERY = "INSERT INTO Friends(idUser, idUserFriends) VALUES (?, ?)";
    private static final String FIND_JOIN_FRIENDS_QUERY = "SELECT f1.idUserFriends FROM Friends f1 WHERE f1.idUser = ? " +
                    "AND f1.idUserFriends IN ( SELECT f2.idUserFriends FROM Friends f2 WHERE f2.idUser = ?)";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM Friends WHERE idUser = ? AND idUserFriends = ?";
    private static final String UPDATE_QUERY = "UPDATE Users SET email = ?, login = ?, name = ?, birthday = ?" +
            " WHERE idUser = ?";

    public UserRepository(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    public List<User> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<User> findByEmail(String email) {
        return findOne(FIND_BY_EMAIL_QUERY, email);
    }

    public List<Long> findAllFriends(long idUser) {
        return jdbc.queryForList(FIND_ALL_FRIENDS_QUERY, Long.class, idUser);
    }

    public void addFriend(long idUser, long friendId) {
        insert(ADD_FRIEND_QUERY, idUser, friendId);
        insert(ADD_FRIEND_QUERY, friendId, idUser);
    }

    public void deleteFriends(long idUser, long friendId) {
        jdbc.update(DELETE_FRIEND_QUERY, idUser, friendId);
        jdbc.update(DELETE_FRIEND_QUERY, friendId, idUser);
    }

    public List<User> findJointFriendsUsers(Long idUser, Long otherId) {
        return jdbc.queryForList(FIND_JOIN_FRIENDS_QUERY, Long.class, idUser, otherId).stream()
                .map(this::getUser)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public Optional<User> getUser(long idUser) {
        return findOne(FIND_BY_ID_QUERY, idUser);
    }

    public User save(User user) {
        long id = insert(
                INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
        user.setIdUser(id);
        return user;
    }

    public User update(User user) {
        update(
                UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getIdUser()
        );
        return user;
    }
}
