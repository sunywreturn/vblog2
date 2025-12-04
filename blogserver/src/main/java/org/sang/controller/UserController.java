package org.sang.controller;

import org.sang.bean.RespBean;
import org.sang.bean.User;
import org.sang.service.UserService;
import org.sang.utils.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sang on 2017/12/24.
 */
@RestController
public class UserController {

    @Autowired
    UserService userService;

    @RequestMapping("/currentUserName")
    public String currentUserName() {
        return Util.getCurrentUser().getNickname();
    }

    @RequestMapping("/currentUserId")
    public Long currentUserId() {
        return Util.getCurrentUser().getId();
    }

    @RequestMapping("/currentUserEmail")
    public String currentUserEmail() {
        return Util.getCurrentUser().getEmail();
    }

    @RequestMapping("/isAdmin")
    public Boolean isAdmin() {
        List<GrantedAuthority> authorities = Util.getCurrentUser().getAuthorities();
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().contains("超级管理员")) {
                return true;
            }
        }
        return false;
    }

    @RequestMapping(value = "/updateUserEmail",method = RequestMethod.PUT)
    public RespBean updateUserEmail(String email) {
        if (userService.updateUserEmail(email) == 1) {
            return new RespBean("success", "开启成功!");
        }
        return new RespBean("error", "开启失败!");
    }

    @RequestMapping(value = "/user/profile", method = RequestMethod.PUT)
    public RespBean updateUserProfile(@RequestBody Map<String, String> profileData) {
        try {
            String nickname = profileData.get("nickname");
            String email = profileData.get("email");

            User currentUser = Util.getCurrentUser();
            if (nickname != null && !nickname.trim().isEmpty()) {
                userService.updateUserNickname(nickname, currentUser.getId());
            }
            if (email != null && !email.trim().isEmpty()) {
                userService.updateUserEmail(email, currentUser.getId());
            }

            return new RespBean("success", "个人信息更新成功!");
        } catch (Exception e) {
            return new RespBean("error", "个人信息更新失败!");
        }
    }

    @RequestMapping("/user/statistics")
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        try {
            Long currentUserId = Util.getCurrentUser().getId();
            statistics = userService.getUserStatistics(currentUserId);
        } catch (Exception e) {
            // 返回默认统计数据
            statistics.put("articleCount", 0);
            statistics.put("viewCount", 0);
            statistics.put("draftCount", 0);
        }
        return statistics;
    }
}
