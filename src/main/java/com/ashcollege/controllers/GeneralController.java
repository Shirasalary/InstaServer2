package com.ashcollege.controllers;

import com.ashcollege.entities.Post;
import com.ashcollege.entities.User;
import com.ashcollege.responses.*;
import com.ashcollege.utils.DbUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.ashcollege.utils.Errors.*;

@RestController
public class
GeneralController {


    @Autowired
    private DbUtils dbUtils;


    @RequestMapping("/")
    public String test () {
        return "Hello From Server";
    }

    @RequestMapping("/login")
    public LoginResponse checkUser (String username, String password) {

        String token = null;
        Integer errorCode = null;

         if ((username!=null&&password!=null) ||(!username.isEmpty() && !password.isEmpty())){
             token = dbUtils.login(username, password);

             if(token == null){
                 errorCode = ERROR_NOT_EXIST_USER;
             }
         }else {
             errorCode = ERROR_MISSING_FIELDS;
         }

        return new LoginResponse(token!=null ,errorCode ,token);
    }


    //TODO להוסיף בלקוח שיכניס גם תומנה ואז לשנות פה את הבנאי של היוזר
    @RequestMapping("/signUp")
    public LoginResponse signUp(String username, String password, String repeat) {
        Integer errorCode = null;
       String token =null;
        if (username != null) {
            if (password != null) {
                if (password.equals(repeat)) {
                    if (usernameAvailable(username).isAvailable()) {
                        User user = new User(username,password);
                      token =  dbUtils.registerUser(user);

                    } else {
                        errorCode = ERROR_USERNAME_NOT_AVAILABLE;
                    }
                } else {
                    errorCode = ERROR_PASSWORDS_DONT_MATCH;
                }
            } else {
                errorCode = ERROR_MISSING_PASSWORD;
            }
        } else {
            errorCode = ERROR_MISSING_USERNAME;
        }
        return new LoginResponse(token!=null, errorCode, token);
    }

    @RequestMapping("/username-available")
    public UsernameAvailableResponse usernameAvailable (String username) {
        boolean success = false;
        Integer errorCode = null;
        boolean available = false;
        if (username != null) {
            available = dbUtils.usernameAvailable(username);
            success = true;
        } else {
            errorCode = ERROR_MISSING_USERNAME;
        }
        return new UsernameAvailableResponse(success, errorCode, available);

    }

    @RequestMapping("get-user-by-token")
    public UserResponse getUserByToken (String token){
        Integer error = null;
         User user = this.dbUtils.getUser(token);
         if (user==null){
             error = ERROR_NOT_EXIST_USER;
         }
         return new UserResponse(user!=null,error,user );
    }

    @RequestMapping("get-user-all-posts")
    public ListGenericResponse<Post> getAllUserPosts (String token) {
        Integer error = null;
        List<Post> posts = this.dbUtils.getAllUserPosts(token);
        if (posts == null) {
            error = ERROR_NOT_EXIST_USER;
        }
        return new ListGenericResponse<Post>(posts != null, error, posts);
    }

//צריך להשתמש במחלקה הגנרית
//    @RequestMapping("get-all-users")
//    public AllUsersResponse getAllUsers () {
//        List<User> allUsers = dbUtils.getAllUsers();
//        return new AllUsersResponse(allUsers);
//    }



}
