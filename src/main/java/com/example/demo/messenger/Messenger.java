package com.example.demo.messenger;

import lombok.extern.slf4j.Slf4j;
import org.jgroups.*;
import org.jgroups.blocks.locking.LockService;
import org.jgroups.util.Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Slf4j
@Component
public class Messenger extends ReceiverAdapter {

    private final List<String> state = new LinkedList<>();

    private JChannel channel;

    private View lastView;

    private LockService lockService;

    @Value("${server.port}")
    private String serverPort;

    @PostConstruct
    public void init() {
        log.info("Start Messenger");

        String username = System.getenv("COMPUTERNAME") + ":" + serverPort;
        String clusterName = "cluster";

        try {
            channel = new JChannel("udp.xml");
            channel.setReceiver(this);
            channel.name(username);
            channel.setDiscardOwnMessages(true);
            channel.connect(clusterName);
            channel.getState(null, TimeUnit.SECONDS.toMillis(10));
            lockService = new LockService(channel);
        } catch (Exception e) {
            log.error("Registering the channel in JMX failed: {}", e);
        }
    }

    public void close() {
        channel.close();
    }

    public void send(String message) {
        try {
            if (tryLock()) {
                channel.send(null, message);
                log.info("Sending message: {}", message);
                // if expired the tryLock only one node will get the lock
                Thread.sleep(TimeUnit.SECONDS.toMillis(10));
                unlock();
            }
        } catch (Exception e) {
            log.error("Sending message failed: {}", e);
        }
    }

    public boolean tryLock() {
        Lock lock = lockService.getLock("lock-cluster"); // gets a cluster-wide lock
        boolean acquiredLocked = false;
        try {
            acquiredLocked = lock.tryLock(5, TimeUnit.SECONDS);
            if (acquiredLocked) {
                log.info(lock.toString());
            }
        } catch (Exception e) {
            log.error("Trying to get lock failed: {}", e);
        }

        return acquiredLocked;
    }

    public void unlock() {
        Lock lock = lockService.getLock("lock-cluster"); // gets a cluster-wide lock
        lock.unlock();
        log.info(lock.toString());
    }

    public void viewAccepted(View newView) {
        log.info("** view: {}", newView);

        // Save view if this is the first
        if (lastView == null) {
            log.info("Received initial view:");
            newView.forEach(address -> log.info(address.toString()));
        } else {
            // Compare to last view
            log.info("Received new view: ");

            List<Address> newMembers = View.newMembers(lastView, newView);
            log.info("New members: ");
            newMembers.forEach(address -> log.info(address.toString()));

            List<Address> exMembers = View.leftMembers(lastView, newView);
            log.info("Exited members: ");
            exMembers.forEach(address -> log.info(address.toString()));
        }

        lastView = newView;
    }

    public void receive(Message msg) {
        log.info("Received: {} - {}", msg.getSrc(), msg.getObject());
        synchronized (state) {
            String line = msg.getSrc() + ": " + msg.getObject();
            state.add(line);
        }
    }

    public void getState(OutputStream output) throws Exception {
        synchronized (state) {
            Util.objectToStream(state, new DataOutputStream(output));
        }
    }

    public void setState(InputStream input) throws Exception {
        List<String> list = Util.objectFromStream(new DataInputStream(input));

        synchronized (state) {
            state.clear();
            state.addAll(list);
        }

        log.info("{} messages in state history", list.size());
        list.forEach(msg -> log.info(msg));
    }
}
