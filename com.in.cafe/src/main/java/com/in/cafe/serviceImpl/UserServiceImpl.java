package com.in.cafe.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.in.cafe.JWT.CustomerUsersDetailsService;
import com.in.cafe.JWT.JwtFilter;
import com.in.cafe.JWT.JwtUtil;
import com.in.cafe.POJO.User;
import com.in.cafe.constents.CafeConstants;
import com.in.cafe.dao.UserDao;
import com.in.cafe.service.UserService;
import com.in.cafe.utils.CafeUtils;
import com.in.cafe.utils.EmailUtils;
import com.in.cafe.wrapper.UserWrapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserDao userDao;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    CustomerUsersDetailsService customerUsersDetailsService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    EmailUtils emailUtils;

    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        log.info("Inside signup {}", requestMap);
        try {
            if (validateSignUpMap(requestMap)) {
                User user = userDao.findByEmailId(requestMap.get("email"));
                if (Objects.isNull(user)) {
                    userDao.save(getUserFromMap(requestMap));
                    return CafeUtils.getResponseEntity("Successfully Registered.", HttpStatus.OK);
                } else {
                    return CafeUtils.getResponseEntity("Email already exists.", HttpStatus.BAD_REQUEST);
                }
            } else {
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateSignUpMap(Map<String, String> requestMap) {
        return requestMap.containsKey("name") && requestMap.containsKey("contactNumber")
                && requestMap.containsKey("email") && requestMap.containsKey("password");
    }

    private User getUserFromMap(Map<String, String> requestMap) {

        User user = new User();
        user.setName(requestMap.get("name"));
        user.setContactNumber(requestMap.get("contactNumber"));
        user.setEmail(requestMap.get("email"));
        user.setPassword(requestMap.get("password"));
        user.setStatus("false");
        user.setRole("user");

        return user;
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        log.info("Inside login");

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password")));

            if (customerUsersDetailsService.getUserDetail().getStatus().equalsIgnoreCase("true")) {
                return new ResponseEntity<String>(
                        "{\"token\":\"" + jwtUtil.generateToken(customerUsersDetailsService.getUserDetail().getEmail(),
                                customerUsersDetailsService.getUserDetail().getRole()) + "\"}",
                        HttpStatus.OK);
            } else {
                return CafeUtils.getResponseEntity("{\"message\":\"Wait for the admin approval.\"}",
                        HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            log.info("{} {}", e.getMessage(), e);
        }

        return CafeUtils.getResponseEntity("Bad credentials.", HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUsers() {
        log.info("Inside getAllUsers");
        try {
            if (jwtFilter.isAdmin()) {
                return new ResponseEntity<>(userDao.getAllUser(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        log.info("Inside update {}", requestMap);
        try {
            if (jwtFilter.isAdmin()) {
                Optional<User> optional = userDao.findById(Integer.parseInt(requestMap.get("id")));
                if (optional.isPresent()) {
                    userDao.updateStatus(Integer.parseInt(requestMap.get("id")), requestMap.get("status"));
                    sendMailToAllAdmin(requestMap.get("status"), optional.get().getEmail(), userDao.getAllAdmin());
                    return CafeUtils.getResponseEntity("User status updated successfully.", HttpStatus.OK);
                } else {
                    return CafeUtils.getResponseEntity("User not found.", HttpStatus.BAD_REQUEST);
                }
            } else {
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void sendMailToAllAdmin(String status, String user, List<String> allAdmin) {
        log.info("Inside Mail sent to all admins");
        allAdmin.remove(jwtFilter.getCurrentUser());
        if (status != null && status.equalsIgnoreCase("true")) {
            String message = "USER:- " + user + " account is approved by \nADMIN:- " + jwtFilter.getCurrentUser();
            emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(), "Account Approved", message, allAdmin);
        } else {
            String message = "USER:- " + user + " account is disabled by \nADMIN:- " + jwtFilter.getCurrentUser();
            emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(), "Account Disabled", message, allAdmin);
        }
    }

    @Override
    public ResponseEntity<String> checkToken() {
        return CafeUtils.getResponseEntity("true", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        log.info("Inside changePassword.");
        try {
            User userObj = userDao.findByEmailId(jwtFilter.getCurrentUser());
            if (userObj != null) {
                if (userObj.getPassword().equals(requestMap.get("oldPassword"))) {
                    if (Objects.equals(
                            requestMap.get("oldPassword") != null ? requestMap.get("oldPassword").toString() : "",
                            requestMap.get("newPassword") != null ? requestMap.get("newPassword").toString() : "")) {
                        return CafeUtils.getResponseEntity("Old password and new password should not be same.",
                                HttpStatus.BAD_REQUEST);
                    } else {
                        userObj.setPassword(requestMap.get("newPassword"));
                        userDao.save(userObj);
                        return CafeUtils.getResponseEntity("Password changed successfully.", HttpStatus.OK);
                    }
                }
                return CafeUtils.getResponseEntity("Old password is incorrect.", HttpStatus.BAD_REQUEST);
            }
            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
        log.info("Inside forgotPassword.");
        try {
            User userObj = userDao.findByEmailId(requestMap.get("email"));
            if (userObj != null) {
                // emailUtils.forgotPasswordMail(userObj.getEmail(), "Credentials by Cafe
                // Management System",
                // userObj.getPassword());
                // return CafeUtils.getResponseEntity("Password sent to your email.",
                // HttpStatus.OK);
                String message = "Your password is:- " + userObj.getPassword();
                return CafeUtils.getResponseEntity(message, HttpStatus.OK);
            }
            return CafeUtils.getResponseEntity("Check your mail for credentials.", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
