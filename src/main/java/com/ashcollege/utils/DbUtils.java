package com.ashcollege.utils;

import com.ashcollege.entities.Post;
import com.ashcollege.entities.User;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


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

    public boolean isUserFollowing (int id,int followingId){
        boolean result = false;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM following WHERE user_id = ? AND following_id =?");
            preparedStatement.setInt(1, id);
            preparedStatement.setInt(2, followingId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                result = true;
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }
        return result;
    }

    public boolean addFollow(int id, int followingId) {
        boolean success = booleanUpdateResultSql(true,false,
                id,
                followingId,null,
                "INSERT INTO following (user_id, following_id) VALUE (?, ?)");
        if (success){
            success = booleanUpdateResultSql(true,false,
                    followingId,
                    id,null,
                    "INSERT INTO followers (user_id, follower_id) VALUE (?, ?)");
        }

        return success;
    }

    private <T> boolean booleanUpdateResultSql(boolean isIntType,boolean isThreeParameters , T parameter1,
                                               int parameter2, Timestamp parameter3, String sql){
        boolean success = true;
        try {

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            if (isIntType){
                preparedStatement.setInt(1, (Integer) parameter1);
            } else {
                preparedStatement.setString(1, (String) parameter1);
            }
            preparedStatement.setInt(2, parameter2);

            if (isThreeParameters){
                preparedStatement.setTimestamp(3, parameter3);
            }

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            success = false;
            e.printStackTrace();
        }
        return success;
    }

    public boolean updateUrlImage(int id, String url) {
       return booleanUpdateResultSql(false,false,url,id,null,"UPDATE users SET pictureUrl = ? WHERE id = ?");
    }

    public boolean addNewPost(int id, String text) {
        LocalDateTime now = LocalDateTime.now();
        Timestamp timestamp = Timestamp.valueOf(now);
        return booleanUpdateResultSql(false,true,text,id,timestamp,"INSERT INTO posts (text,user_id,date) VALUE (?,?,?)");
    }

    public List<User> getUsersByName(String prefix, int id) {

        List<User> users = new ArrayList<>();
        if (!prefix.isEmpty()){
            users = getListByCode(true, false, "SELECT id, username ,pictureUrl FROM users WHERE username LIKE ?",
                    prefix + "%", User.class);

            users = users.stream()
                    .filter(user -> user.getId() != id)
                    .collect(Collectors.toList());
        }
        return users;
    }


    public List<User> getAllUserFollowing (int id){
        return getListByCode(true,true,
                "SELECT id,username,pictureUrl FROM users WHERE id IN (SELECT following_id FROM following WHERE user_id = ?)",
                Integer.toString(id), User.class);
    }
    public List<User> getAllUserFollowers (int id) {

        List<User> followers = getListByCode(true,true,
                "SELECT id,username,pictureUrl FROM users WHERE id IN (SELECT follower_id FROM followers WHERE user_id = ?)"
                ,Integer.toString(id), User.class);
        return followers;
    }


    private  <T> List<T> getListByCode (boolean isUserClass,boolean isIntType,String sql,String parameter,Class<T> type) {
        List<T>  result = null;
        try {

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            if (isIntType){
                preparedStatement.setInt(1, Integer.parseInt(parameter));
            } else {
                preparedStatement.setString(1, parameter);
            }

            ResultSet resultSet = preparedStatement.executeQuery();
            result = new ArrayList<>();
            while (resultSet.next()) {
                T r = null;
                if (!isUserClass){
                     r = type.getConstructor(int.class,int.class, String.class,Timestamp.class)
                            .newInstance(resultSet.getInt("id"),resultSet.getInt("user_id"),
                                    resultSet.getString("text"),resultSet.getTimestamp("date"));

                }else{
                    r = type.getConstructor(int.class, String.class, String.class)
                            .newInstance(resultSet.getInt("id")
                                    , resultSet.getString("username")
                                    , resultSet.getString("pictureUrl"));

                }

                result.add(r);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return result;
    }


    public List<Post> getAllUserPosts (String token) {

        return getListPost("SELECT * FROM posts WHERE user_id=?",token);
    }

    public List<Post> getTwentyLastPosts (String token){

        return getListPost("SELECT * FROM posts WHERE user_id IN(SELECT following_id FROM following WHERE user_id=?) ORDER BY posts.date DESC LIMIT 20",
                token);

    }

    private List<Post> getListPost(String url ,String token){

        List<Post> allPosts = null;
        User user = getUser(token);
        if (user != null){
            allPosts =getListByCode(false,true,
                    url,
                    Integer.toString(user.getId() ), Post.class);
        }

        return allPosts;
    }


    public User getUser (String token) {
        try {
            User result = null;
            PreparedStatement preparedStatement =
                    connection.prepareStatement(
                            "SELECT id,username,token,pictureUrl FROM users WHERE token=?");
            preparedStatement.setString(1, token);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                result = new User(resultSet.getInt("id"),
                        resultSet.getString("username"),
                        null,
                        resultSet.getString("token"),
                        resultSet.getString("pictureUrl"));
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
        booleanUpdateResultSql(false,false,token,id,null,"UPDATE users SET token = ? WHERE id = ?");
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

}
