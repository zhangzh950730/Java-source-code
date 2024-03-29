package com.zzh.concurrent.locks;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class DemoLockTest {
    private static int count = 0;
    private static final DemoLock lock = new DemoLock();

    @Test
    void testLock() throws InterruptedException {
        // 10个线程, 执行内容都是调用increment方法
        List<Thread> threads = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            threads.add(new Thread(this::increment));
        }

        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }

        // main线程等待所有线程结束
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("count = " + count);
    }

    private void increment() {
        for (int i = 0; i < 100000; i++) {
            try {
                lock.lock();
                count++;
            } finally {
                lock.unlock();
            }
        }
    }

}