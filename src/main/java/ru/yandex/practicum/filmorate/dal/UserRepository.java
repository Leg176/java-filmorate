package ru.yandex.practicum.filmorate.dal;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class UserRepository extends BaseRepository<User> {

    @Autowired
    private EntityManager entityManager;

    private static final String FIND_ALL_FRIENDS_QUERY = "SELECT idUserFriends FROM friends WHERE idUser = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String FIND_BY_EMAIL_QUERY = "SELECT * FROM users WHERE email = ?";
    private static final String INSERT_QUERY = "INSERT INTO users(email, login, name, birthday)" +
            "VALUES (?, ?, ?, ?)";
    private static final String ADD_FRIEND_QUERY = "INSERT INTO friends(idUser, idUserFriends) VALUES (?, ?)";
    private static final String FIND_JOIN_FRIENDS_QUERY = "SELECT f1.idUserFriends FROM friends f1 WHERE f1.idUser = ? " +
                    "AND f1.idUserFriends IN ( SELECT f2.idUserFriends FROM friends f2 WHERE f2.idUser = ?)";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friends WHERE idUser = ? AND idUserFriends = ?";
    private static final String DELETE_USER_QUERY = "DELETE FROM users WHERE id = ?";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ?" +
            " WHERE id = ?";
    private static final String FIND_FRIENDSHIP = "SELECT COUNT(*) FROM friends f WHERE f.idUser = :idUser " +
            "AND f.idUserFriends = :idUserFriends";

    public UserRepository(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    public boolean existsFriendship(Long idUser, Long idUserFriends) {
        long count = (long) entityManager.createNativeQuery(FIND_FRIENDSHIP, Long.class)
                .setParameter("idUser", idUser)
                .setParameter("idUserFriends", idUserFriends)
                .getSingleResult();
        return count > 0;
    }

    public List<User> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<User> findByEmail(String email) {
        return findOne(FIND_BY_EMAIL_QUERY, email);
    }

    public List<Long> findAllFriends(long id) {
        return jdbc.queryForList(FIND_ALL_FRIENDS_QUERY, Long.class, id);
    }

    public void addFriend(long idUser, long friendId) {
        insert(ADD_FRIEND_QUERY, "idUser", idUser, friendId);
    }

    public void deleteFriends(long idUser, long friendId) {
        jdbc.update(DELETE_FRIEND_QUERY, idUser, friendId);
    }

    public void deleteUser(long id) {
        jdbc.update(DELETE_USER_QUERY, id);
    }

    public List<User> findJointFriendsUsers(Long idUser, Long otherId) {
        return jdbc.queryForList(FIND_JOIN_FRIENDS_QUERY, Long.class, idUser, otherId).stream()
                .map(this::getUser)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public Optional<User> getUser(long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public User save(User user) {
            long id = insert(INSERT_QUERY, "id",
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    user.getBirthday()
            );
            user.setId(id);
            return user;
    }

    public User update(User user) {
        update(UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }
}
