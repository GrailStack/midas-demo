package com.itgrail.midas.demo.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import com.itgrail.midas.demo.model.User;

@FeignClient(name = "midas-demo-old-provider")
public interface UserService {

    @PostMapping(value = "/user/add")
    String addUser(@RequestBody User user);

    @RequestMapping(value = "/user/update", method = RequestMethod.POST)
    public String updateUser(@RequestBody User user);

}
