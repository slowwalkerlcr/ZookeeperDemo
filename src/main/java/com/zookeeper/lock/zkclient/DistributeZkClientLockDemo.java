package com.zookeeper.lock.zkclient;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
/**
 * 
 * TODO 实现分布式环境下同步锁的实现 
 * @Date  2017年3月26日 下午2:22:08   	 
 * @author Administrator  
 * @version
 */
public class DistributeZkClientLockDemo implements  Watcher{
	
	  ZooKeeper zk=null; //zookeeper原生api去实现一个分布式锁

	    private String root="/locks";

	    private String myZonode; //表示当前获取到的锁名称-也就是节点名称

	    private String waitNode; // 表示当前等待的节点

	    private CountDownLatch latch;

	    //server链接字符串
	    private static final String CONNECTION_STRING="192.168.148.130:2181,192.168.148.130:2182,192.168.148.130:2183";

	    private static final int SESSION_TIMEOUT=5000; //超时时间

	/**
	 * 构造函数初始化
	 * @param config  表示zookeeper连接串
	 */
	public DistributeZkClientLockDemo(String config){
	        try {
	            zk=new ZooKeeper(config,SESSION_TIMEOUT,this);
	            Stat stat=zk.exists(root,false); //判断是不是已经存在locks节点，不需要监听root节点
	            if(stat==null){ //如果不存在，则创建根节点
	                zk.create(root,new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        } catch (KeeperException e) {
	            e.printStackTrace();
	        }
	    }

	    public void process(WatchedEvent event) {
	        if(this.latch!=null){ //如果计数器不为空话话，释放计数器锁
	            this.latch.countDown();
	        }
	    }

	    /**
	     * 获取多的方法
	     */
	    public void lock(){
	        if(tryLock()){
	            System.out.println("Thread "+Thread.currentThread().getName()+" - hold lock!");
	            return;
	        }
	        try {
	            waitLock(waitNode,SESSION_TIMEOUT);
	        } catch (KeeperException e) {
	            e.printStackTrace();
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	        //等待并获取锁
	    }

	    /**
	     * 释放锁操作的方法
	     */
	    public void unlock(){
	        System.out.println("UnLock = "+ myZonode);
	        try {
	            zk.delete(myZonode,-1);
	            myZonode=null;
	            zk.close();
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        } catch (KeeperException e) {
	            e.printStackTrace();
	        }

	    }

	    private boolean tryLock(){
	        String splitStr="lock_"; //lock_0000000001
	        try {
	            //创建一个有序的临时节点，赋值给myznode
	            myZonode=zk.create(root+"/"+splitStr,new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
	            System.out.println(myZonode+" 创建成功");
	            List<String> subNodes=zk.getChildren(root,false);
	            Collections.sort(subNodes); //讲所有的子节点排序
	            if(myZonode.equals(root+"/"+subNodes.get(0))){
	                //当前客户端创建的临时有序节点是locks下节点中的最小的节点，表示当前的客户端能够获取到锁
	                return true;
	            }
	            //否则的话,监听比自己小的节点 locks/lock_0000000003
	            String subMyZnode=myZonode.substring((myZonode.lastIndexOf("/")+1));
	            waitNode=subNodes.get(Collections.binarySearch(subNodes,subMyZnode)-1);// 获取比当前节点小的节点
	        } catch (KeeperException e) {
	            e.printStackTrace();
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	        return false;
	    }

	    private boolean waitLock(String lower,long waitTime) throws KeeperException, InterruptedException {
	        Stat stat=zk.exists(root+"/"+lower,true); //获取节点状态，并添加监听
	        if(stat!=null){
	            System.out.println("Thread "+Thread.currentThread().getName()+" waiting for"+root+" /"+lower);
	            this.latch=new CountDownLatch(1); //实例化计数器，让当前的线程等待
	            this.latch.await(waitTime, TimeUnit.MILLISECONDS);
	            this.latch=null;
	        }
	        return true;
	    }

	    public static void main(String[] args) {
	        ExecutorService executorService= Executors.newCachedThreadPool();
	        final Semaphore semaphore=new Semaphore(10);
	        for(int i=0;i<10;i++){
	            Runnable runnable=()->{ //lambda的一种用法，在java8里面有。
	                try {
	                    semaphore.acquire();
	                    DistributeZkClientLockDemo distributeLockDemo=new DistributeZkClientLockDemo(CONNECTION_STRING);
	                    distributeLockDemo.lock();
	                    //业务代码
	                    Thread.sleep(3000);
	                    distributeLockDemo.unlock();
	                    semaphore.release();
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                }
	            };
	            executorService.execute(runnable);
	        }
	        executorService.shutdown();
	    }

}
