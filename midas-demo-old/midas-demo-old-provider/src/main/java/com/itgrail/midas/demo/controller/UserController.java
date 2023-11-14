package com.itgrail.midas.demo.controller;

import com.itgrail.midas.demo.model.User;
import com.itgrail.midas.demo.service.UserService;
import org.springframework.web.bind.annotation.RestController;


@RestController("/test")
public class UserController implements UserService {

	@Override
	public String addUser(User user){
		return "hello,"+user.getName();
	}

	@Override
	public String updateUser(User user){
		return "hello,"+user.getName();
	}

}
