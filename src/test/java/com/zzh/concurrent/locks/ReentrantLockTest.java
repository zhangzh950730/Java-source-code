package com.zzh.concurrent.locks;

import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockTest {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition noFull = lock.newCondition();
    private final Condition noEmpty = lock.newCondition();
    private final Queue<Integer> queue = new LinkedList<>();
    private final Integer max = 10;
    private int num = 0;

    @Test
    public void test() throws InterruptedException {
        Thread consumer = new Thread(this::consumer);
        Thread producer = new Thread(this::producer);
        consumer.start();
        producer.start();
        consumer.join();
        producer.join();
    }

    private void producer() {
        while (true) {
            lock.lock(); // 加锁
            try {
                if (queue.size() == max) {
                    noFull.await(); // 等待队列不满
                }
                System.out.println("offer: " + num);
                queue.offer(num++);
                Thread.sleep(1000);
                noEmpty.signal(); // 通知队列不空
            } catch (InterruptedException e) {
                System.out.println("InterruptedException: " + e);
            } finally {
                lock.unlock(); // 解锁
                Thread.yield();
            }
        }
    }

    private void consumer() {
        while (true) {
            lock.lock(); // 加锁
            try {
                if (queue.isEmpty()) {
                    noEmpty.await(); // 等待队列不空
                }
                System.out.println("poll: " + queue.poll());
                Thread.sleep(500);
                noFull.signal(); // 通知队列不满
            } catch (InterruptedException e) {
                System.out.println("InterruptedException: " + e);
            } finally {
                lock.unlock(); // 解锁
                Thread.yield();
            }
        }
    }
}
