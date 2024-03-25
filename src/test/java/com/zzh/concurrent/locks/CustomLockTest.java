package com.zzh.concurrent.locks;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class CustomLockTest {
    private static int count = 0;
    private static final CustomLock lock = new CustomLock();

    @Test
    void testLock() throws InterruptedException {
        List<Thread> threads = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            threads.add(new Thread(this::increment));
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("count = " + count);
    }

    private void increment() {
        for (int i = 0; i < 10000; i++) {
            try {
                lock.lock();
                count++;
            } finally {
                lock.unlock();
            }
        }
    }

}