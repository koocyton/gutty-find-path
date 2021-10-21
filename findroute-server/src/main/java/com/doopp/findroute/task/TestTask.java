package com.doopp.findroute.task;

import com.doopp.findroute.service.SocketService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TestTask implements Runnable {

    @Inject
    private SocketService socketService;

    public synchronized void run() {
        // socketService.cleanEmptyRoom();
    }
}
