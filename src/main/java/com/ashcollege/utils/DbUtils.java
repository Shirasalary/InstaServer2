package com.ashcollege.utils;

import com.ashcollege.entities.Post;
import com.ashcollege.entities.User;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Component
public class DbUtils {

    private  Connection connection = null;

    @PostConstruct
    public Connection createConnection () {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/new_data", Constants.DB_USERNAME, Constants.DB_PASSWORD);
            System.out.println("Connection success");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    public List<Post> getAllUserPosts (String token) {
        List<Post> allPosts = null;
        try {
            User user = getUser(token);
            if (user != null){
                allPosts = new ArrayList<>();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT * FROM posts WHERE user_id=?"
                );
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    Post post = new Post(resultSet.getInt("id"),
                            resultSet.getInt("user_id"),
                            resultSet.getString("text"));
                    allPosts.add(post);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return allPosts;
    }


    public User getUser (String token) {
        try {
            User result = null;
            PreparedStatement preparedStatement =
                    connection.prepareStatement(
                            "SELECT * FROM users WHERE token=?");
            preparedStatement.setString(1, token);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                result.setId(resultSet.getInt("id"));
                result.setUsername(resultSet.getString("username"));
                result.setPassword(null); //אין צורך בסיסמא,אבטחת מידע
                result.setToken(resultSet.getString("token"));
                result.setPictureUrl(resultSet.getString("pictureUrl"));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String registerUser (User user) {
        try {
            String token =getToken();
            PreparedStatement preparedStatement =
                    connection.prepareStatement(
                            "INSERT INTO users (username, password,token) VALUE (?, ?,?)");
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, token);
            preparedStatement.executeUpdate();
            return token;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String login(String username, String password) {
        String token = null;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT id FROM users WHERE username = ? AND password = ?");
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()){

                token = getToken();
                int userId = resultSet.getInt("id");
                updateToken(userId,token);
            }
            return token;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private String getToken(){
        return String.valueOf(new Random().nextInt());
    }



    private void updateToken(int id, String token) {
        try {

            PreparedStatement preparedStatement = connection.prepareStatement(
                    "UPDATE users SET token = ? WHERE id = ?");

            preparedStatement.setString(1, token);
            preparedStatement.setInt(2, id);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean usernameAvailable (String username) {
        try {
            PreparedStatement preparedStatement =
                    connection.prepareStatement(
                            "SELECT users.username " +
                                    "FROM users WHERE username = ? ");
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            return !resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<User> getAllUsers () {
        List<User> allUsers = null;
        try {
            allUsers = new ArrayList<>();
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT id, username, password FROM users"
            );
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                User user = new User();
                user.setId(resultSet.getInt("id"));
                user.setUsername(resultSet.getString("username"));
                user.setPassword(resultSet.getString("password"));
                allUsers.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return allUsers;
    }

}
