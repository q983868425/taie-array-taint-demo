package org.joychou.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * SQL Injection
 *
 * @author JoyChou @2018.08.22
 */

@SuppressWarnings("Duplicates")
@RestController
@RequestMapping("/sqli")
public class SQLIDemo {

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public void test(String user) {
        String[] users = user.split("@");
        String username = users[0];
        sink(username);
    }

    private void sink(String username) {

    }
}
