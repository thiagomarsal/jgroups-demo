package com.example.demo.cron;

import com.example.demo.messenger.Messenger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class Cron {

    @Autowired
    private Messenger messenger;

    @Value("${server.port}")
    private String serverPort;

    private int count;

    @Scheduled(fixedRate = 60000)
    public void start() {
        messenger.send("Hello " + ++count + " at " + LocalDateTime.now());
    }
}
