package com.zzh.concurrent.locks;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class DemoLock {

    public void lock() {
        // 代理给sync的acquire方法
        sync.acquire(1);
    }

    public void unlock() {
        // 代理给sync的release方法
        sync.release(0);
    }

    private static final Sync sync = new Sync();

    private static class Sync extends AbstractQueuedSynchronizer {

        @Override
        protected boolean tryAcquire(int arg) {
            return compareAndSetState(0, 1);
        }

        @Override
        protected boolean tryRelease(int arg) {
            setState(0);
            return true;
        }
    }

}
