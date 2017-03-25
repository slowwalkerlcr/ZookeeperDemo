package com.zookeeper.zkclient.master;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

/**
 * 
 * TODO 通过zkclient实现master选举
 * @Date  2017年3月25日 下午6:54:03   	 
 * @author Administrator  
 * @version
 */
public class MasterServer {
	
	 //表示这是zkclient客户端
    private ZkClient zkClient;

    // 表示这是一个争抢的一个master节点
    private final String MASTER_NODE="/master";

    //server链接字符串
    private static String CONNECT_STRING="192.168.148.130:2181,192.168.148.130:2182,192.168.148.130:2183";

    private static final int SESSION_TIMEOUT=5000; //超时时间

    private ServerData serverData; //争抢master节点的服务器

    private ServerData masterData; //争抢到的master节点的服务器信息

    private volatile boolean running=false; //服务是否启动状态

    IZkDataListener dataListener; //master节点的监听事件

    private ScheduledExecutorService scheService= Executors.newScheduledThreadPool(1);
    
    
    public MasterServer(ZkClient zkClient,ServerData serverData){
        this.zkClient=zkClient;
        this.serverData=serverData; //表示当前来争取master节点的服务
        dataListener=new IZkDataListener() { //初始化一个监听
            public void handleDataChange(String dataPath, Object data) throws Exception {}

            public void handleDataDeleted(String dataPath) throws Exception {
                //监听节点删除事件
                scheService.schedule(new Runnable() {
                    public void run() {
                        takeMaster();
                    }
                },5, TimeUnit.SECONDS);

            }
        };
    }

    //开始争抢master的方法
    public void start(){
        if(running){
            throw new RuntimeException("服务已经启动了");
        }
        running=true;
        zkClient.subscribeDataChanges(MASTER_NODE,dataListener);
        takeMaster();
    }

    //停止
    public void stop(){
        if(!running){
            throw new RuntimeException("服务已经停止了");
        }
        running=false;
        zkClient.subscribeDataChanges(MASTER_NODE,dataListener);
        releaseMaster();
    }

    //争抢master节点的具体实现
    private void takeMaster(){
        if(!running){
            return;
        }
        System.out.println(serverData.getServerName()+" 来抢master节点");
        try {
            zkClient.createEphemeral(MASTER_NODE,serverData); //用到了一个临时节点的特性
            masterData=serverData;
            System.out.println(masterData.getServerName()+"成功抢到master节点");
            scheService.schedule(new Runnable() {
                public void run() {
                    if(checkMaster()) {
                        zkClient.delete(MASTER_NODE);
                    }
                }
            },5,TimeUnit.SECONDS); //纯粹为了演示效果，所以每5秒钟释放一次master
        }catch (ZkNodeExistsException e){
            //如果节点创建过程中提示节点已经存在的异常，那这个时候意味着master节点是存在的（特性，节点唯一性）
            ServerData serverData=zkClient.readData(MASTER_NODE); //读取当前master节点的服务器信息
            if(serverData==null){
                //在读取过程中，发现master节点已经被释放
                takeMaster();
            }else{
                masterData=serverData;
            }
        }
    }

    //释放master
    private void releaseMaster(){
        if(checkMaster()){
            zkClient.delete(MASTER_NODE);
        }
    }

    //校验当前的服务器是不是master
    private boolean checkMaster(){
        try {
            //读取当前master节点的数据，并赋值给masterdata
            ServerData ms = zkClient.readData(MASTER_NODE);
            masterData = ms;
            //这个时候，如果master节点的数据和当前过来争抢master节点的服务器的数据是一样的话，那么意味
            //当前的serverData就是master
            if (masterData.getServerName().equals(serverData.getServerName())) {
                return true;
            }
            return false;
        }catch (ZkNoNodeException e){
            return false;
        }catch (ZkInterruptedException e){
            return checkMaster();
        }catch (ZkException e){
            return false;
        }
    }

    public static void main(String[] args) {
        ExecutorService service=Executors.newCachedThreadPool();
         final Semaphore semaphore=new Semaphore(10);
        for(int i=0;i<10;i++){
            final int idx=i;
            Runnable runnable=new Runnable() {
                public void run() {
                    try {
                        semaphore.acquire();
                        //初始化一个zkclient的连接
                        ZkClient zk=new ZkClient(CONNECT_STRING,
                                SESSION_TIMEOUT,SESSION_TIMEOUT,
                                new SerializableSerializer());
                        //定义一台争抢master节点的服务器
                        ServerData serverData=new ServerData();
                        serverData.setServerId(idx);
                        serverData.setServerName("#server-"+idx);
                        //初始化一个争抢master节点的服务
                        MasterServer ms=new MasterServer(zk,serverData);
                        ms.start();
                        semaphore.release();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            service.execute(runnable);
        }
        service.shutdown();
    }
	
}
