package com.ashcollege.controllers;

import com.ashcollege.entities.Post;
import com.ashcollege.entities.User;
import com.ashcollege.responses.*;
import com.ashcollege.utils.DbUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.ashcollege.utils.Constants.*;
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

    @RequestMapping("/signUp")
    public LoginResponse signUp(String username, String password, String repeat) {
        Integer errorCode = null;
       String token =null;
        if (username != null) {
            if (password != null) {
                if (password.equals(repeat)) {
                    if (usernameAvailable(username).isResult()) {
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
    public BooleanResponse usernameAvailable (String username) {
        boolean success = false;
        Integer errorCode = null;
        boolean available = false;
        if (username != null) {
            available = dbUtils.usernameAvailable(username);
            success = true;
        } else {
            errorCode = ERROR_MISSING_USERNAME;
        }
        return new BooleanResponse(success, errorCode, available);

    }

    @RequestMapping("/get-user-by-token")
    public UserResponse getUserByToken (String token){
        Integer error = null;
        boolean success = false;
        User user = null;
        if (token != null && !token.isEmpty()){
            user = this.dbUtils.getUser(token);
            if (user==null){
                error = ERROR_NOT_EXIST_USER;
            }else {
                success = true;
            }
        }else {
            error =ERROR_MISSING_TOKEN;
        }

         return new UserResponse(success,error,user );
    }

    @RequestMapping("/get-user-all-posts")
    public ListGenericResponse<Post> getAllUserPosts (String token) {
        return getPostsList(true,token);
    }
    @RequestMapping("/get-last-posts")
    public ListGenericResponse<Post> getLastPosts (String token) {
        return getPostsList(false,token);
    }

    private ListGenericResponse<Post> getPostsList (boolean isAllPosts,String token) {
        Integer error = null;
        List<Post> posts = null;
        if (token != null && !token.isEmpty()){
            if (isAllPosts){
                posts = this.dbUtils.getAllUserPosts(token);
            }else {
                posts = this.dbUtils.getTwentyLastPosts(token);
            }

            if (posts == null) {
                error = ERROR_NOT_EXIST_USER;
            }
        }else {
            error =ERROR_MISSING_TOKEN;
        }

        return new ListGenericResponse<Post>(posts != null, error, posts);
    }

    @RequestMapping("get-followers")
    public ListGenericResponse<User> getAllFollowers (Integer id) {
        return getUserList(FOLLOWERS,null,id,null);
    }

    @RequestMapping("get-following")
    public ListGenericResponse<User> getAllFollowing (Integer id) {
        return getUserList(FOLLOWING,null,id,null);
    }

    private ListGenericResponse<User> getUserList (int code,Integer errorCode,Integer id ,String prefix){
        Integer error = errorCode;
        List<User> users = null;
        if (error == null){
            if (id != null){
                if (code==FOLLOWERS){
                    users = this.dbUtils.getAllUserFollowers(id);
                }else if (code==FOLLOWING) {
                    users = this.dbUtils.getAllUserFollowing(id);
                } else if (code == USERS_BY_NAME) {
                    users =dbUtils.getUsersByName(prefix,id);
                }
            }else {
                error =ERROR_MISSING_USER_ID;
            }
        }

        ListGenericResponse<User> response = new ListGenericResponse<>(users != null, error, users);

        return response;
    }

    @RequestMapping("get-users-by-prefix")
    public ListGenericResponse<User> getUsersByPrefix (String prefix,int id) {
        Integer error = null;
        if (prefix == null){
            error = ERROR_MISSING_PREFIX;
        }

        return getUserList(USERS_BY_NAME,error,id,prefix);
    }

    @RequestMapping("save-image-url")
    public BasicResponse saveImage ( Integer id,String url){
        Integer error = null;
        boolean success = false;
        if (id != null ){
            if (url !=null && !url.isEmpty()){
                success = dbUtils.updateUrlImage(id, url);
            }else {
               error = ERROR_MISSING_URL_IMAGE;
            }
        }else {
            error = ERROR_MISSING_USER_ID;
        }

        return new BasicResponse(success,error);
    }


    @RequestMapping("save-post")
    public BasicResponse savePost ( Integer id,String text){
        Integer error = null;
        boolean success = false;
        if (id != null ){
            if (text !=null && !text.isEmpty()){
                success = dbUtils.addNewPost(id, text);
            }else {
                error = ERROR_MISSING_TEXT;
            }
        }else {
            error = ERROR_MISSING_USER_ID;
        }
        return new BasicResponse(success,error);
    }

    @RequestMapping("is-user-following")
    public BooleanResponse isUserFollowing (Integer id, Integer followingId){
        Integer error = null;
        boolean success = false;
        boolean result =false;
        if (id != null){
            if (followingId != null){
                result = dbUtils.isUserFollowing(id,followingId);
                success =true;
            }else {
                error =ERROR_FOLLOWING_ID;
            }
        }else {
            error = ERROR_MISSING_USER_ID;
        }
        return new BooleanResponse(success,error,result);
    }

    @RequestMapping("add-follow")
    public BasicResponse addFollow(Integer id, Integer followingId){
        Integer error = null;
        boolean success = false;
        if (id != null){
            if (followingId !=null){
                success = dbUtils.addFollow(id,followingId);
            }else {
                error =ERROR_FOLLOWING_ID;
            }
        }else {
            error = ERROR_MISSING_USER_ID;
        }

        return new BasicResponse(success,error);
    }




//צריך להשתמש במחלקה הגנרית
//    @RequestMapping("get-all-users")
//    public AllUsersResponse getAllUsers () {
//        List<User> allUsers = dbUtils.getAllUsers();
//        return new AllUsersResponse(allUsers);
//    }

    //    @PostMapping("/save-image-url")
//    public ResponseEntity<BasicResponse> saveImage(
//            @RequestParam("user") int userId,
//            @RequestParam("url") String imageUrl) {
//
//        boolean success = dbUtils.updateUrlImage(userId, imageUrl);
//
//        BasicResponse response = new BasicResponse(success, success ? null : NOT_SAVE_IMAGE);
//        return ResponseEntity.ok(response);
//    }


}
