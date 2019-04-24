package com.example.demo.controller;

import com.example.demo.messenger.Messenger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping
@RestController
public class Controller {

    @Autowired
    private Messenger messenger;

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public ResponseEntity send(@RequestBody String message) {
        messenger.send(message);
        return ResponseEntity.ok().build();
    }
}
