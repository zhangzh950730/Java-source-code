package com.zzh.concurrent.atomic;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public class AtomicIntegerFieldUpdaterTest {
    private volatile int count = 0;

    @Test
    public void test() {
        AtomicIntegerFieldUpdater<AtomicIntegerFieldUpdaterTest> count
                = AtomicIntegerFieldUpdater.newUpdater(AtomicIntegerFieldUpdaterTest.class, "count");
        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                int ans = count.addAndGet(this, 1);
                System.out.println(ans);
            }).start();
        }

    }
}
