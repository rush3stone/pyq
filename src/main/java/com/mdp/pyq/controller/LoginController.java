package com.mdp.pyq.controller;

import com.mdp.pyq.result.Result;
import com.mdp.pyq.pojo.User;
import com.mdp.pyq.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.Objects;


@Controller
public class LoginController {

    @Autowired
    UserService userService;

    @CrossOrigin
    @PostMapping(value = "api/login") // pyq:这是数据对接位置, 在前端项目的main.js中有定义总位置为./api,
    @ResponseBody                     // 具体的跳转页面'/index'在login.vue中进行了追加
    public Result login (@RequestBody User requestUser) {
    // 对html标签进行转义, 防止XSS攻击
        String username = requestUser.getUsername();
        username = HtmlUtils.htmlEscape(username);

        User user = userService.get(username, requestUser.getPassword());
        if (null == user) {
            return new Result(400);
        } else {
            return new Result(200);
        }

//        // 没有数据库的登录方式
//        if (!Objects.equals("admin", username) || !Objects.equals("123", requestUser.getPassword())){
//            String message = "账号密码错误";
//            System.out.println("test");
//            return new Result(400);
//        } else {
//            return new Result(200);
//        }

    }//Result login


}//LoginController
