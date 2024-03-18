package com.ashcollege.utils;

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


    public void registerUser (User user) {
        try {
            PreparedStatement preparedStatement =
                    connection.prepareStatement(
                            "INSERT INTO users (username, password) VALUE (?, ?)");
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.executeUpdate();
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

                token = String.valueOf(new Random().nextInt());
                int userId = resultSet.getInt("id");
                updateToken(userId,token);
            }
            return token;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
