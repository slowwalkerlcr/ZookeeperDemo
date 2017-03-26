package com.zookeeper.lock.curator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.RetryNTimes;

/**
 * 
 * TODO 通过curator去实现分布式锁 
 * @Date  2017年3月26日 下午2:23:16   	 
 * @author Administrator  
 * @version
 */
public class DistributeCuratorLockDemo {
	 //server链接字符串
    private static final String CONNECTION_STRING="192.168.148.130:2181,192.168.148.130:2182,192.168.148.130:2183";

    private static final int SESSION_TIMEOUT=5000; //超时时间

    private static final String CURATOR_LOCK_ROOT="/curator_lock";

    public static void main(String[] args) {

        ExecutorService service= Executors.newCachedThreadPool();
        Semaphore semaphore=new Semaphore(10);
        for(int i=0;i<10;i++) {
            Runnable runnable=()->{
                try {
                    CuratorFramework framework=CuratorFrameworkFactory.newClient(CONNECTION_STRING,new RetryNTimes(10,5000));
                    framework.start();
                    semaphore.acquire();
                    doLock(framework);
                    semaphore.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };
            service.execute(runnable);
        }
        service.shutdown();
    }

    private static void doLock(CuratorFramework client) {
        System.out.println(Thread.currentThread().getName()+" try to get lock");
        InterProcessMutex mutex=new InterProcessMutex(client,CURATOR_LOCK_ROOT);
        try {
            if(mutex.acquire(5, TimeUnit.SECONDS)) {
                System.out.println(Thread.currentThread().getName() + " hold lock");
                Thread.sleep(5000);
            }
        }catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                mutex.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        }
}
