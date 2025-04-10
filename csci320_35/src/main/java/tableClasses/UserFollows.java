package tableClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserFollows {
    private Connection connection;

    public UserFollows(Connection connection) {
        this.connection = connection;
    }

    /**
     * Adds a follow relationship between two users.
     *
     * @param followerId ID of the user who is following.
     * @param followedId ID of the user being followed.
     * @return true if the follow was successful, false otherwise.
     */
    public boolean followUser(int followerId, int followedId) {
        String sql = "INSERT INTO user_follows (User_Follower, User_Followed) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, followerId);
            stmt.setInt(2, followedId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Removes a follow relationship between two users.
     *
     * @param followerId ID of the user who is unfollowing.
     * @param followedId ID of the user being unfollowed.
     * @return true if the unfollow was successful, false otherwise.
     */
    public boolean unfollowUser(int followerId, int followedId) {
        String sql = "DELETE FROM user_follows WHERE User_Follower = ? AND User_Followed = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, followerId);
            stmt.setInt(2, followedId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if a user is following another user.
     *
     * @param followerId ID of the user who might be following.
     * @param followedId ID of the user who might be followed.
     * @return true if the follower is following the followed user, false otherwise.
     */
    public boolean isFollowing(int followerId, int followedId) {
        String sql = "SELECT 1 FROM user_follows WHERE User_Follower = ? AND User_Followed = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, followerId);
            stmt.setInt(2, followedId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     *  Returns the number of followers a user has
     */
    public int followers(int userId) {
        String sql = "SELECT * FROM user_follows WHERE User_Followed = " + userId;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.getFetchSize();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

     /*
      *  Fetches all user followers and returns the number
      */
}
