package com.zookeeper.zkclient;

import java.io.IOException;
import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

/**
 * 
 * TODO Zkclient演示demo
 * 
 * @Date 2017年3月22日 下午10:56:05
 * @author Administrator
 * @version
 */
public class ZkclientDemo {

	 private static String CONNECT_STRING="120.77.22.187:2181,120.77.22.187:2182,120.77.22.187:2183";

	    private static int TIMEOUT=3000;

	    public static void main(String[] args) {
	        ZkClient zkClient=new ZkClient(CONNECT_STRING,TIMEOUT,TIMEOUT,new MyZkSerializer());
	        try {
	            zkClient.subscribeChildChanges("/configuration", new IZkChildListener() {
	                public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
	                    System.out.println("触发事件："+parentPath);
	                    for(String str:currentChilds){
	                        System.out.println(str);
	                    }
	                }
	            });
	            System.in.read();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            zkClient.close();
	        }
	    }
	    /**
	     * 
	     * TODO(创建节点)  
	     * @param zk 
	     * @author Administrator  
	     * @Date 2017年3月22日下午11:10:37
	     */
	    private static void create(ZkClient zk){
	    	//持久节点
	        zk.createPersistent("/node_11/node_11_1/node_11_1_1",true); //递归创建节点
	        //临时节点
	        zk.createEphemeral("/ephemeral_node1");
	    }

	    private static void update(ZkClient zk){
	        zk.writeData("/node_11","zyz");
	    }

	    private static void delete(ZkClient zk){
	    	//删除节点
	    	zk.delete("/node1");
	    	//递归删除
	        boolean bool=zk.deleteRecursive("/node_11");
	        System.out.println(bool);
	    }
	    
	    /**
	     * 
	     * TODO(创建节点 并创建watcher 即监听)  
	     * @param zk 
	     * @author Administrator  
	     * @Date 2017年3月22日下午11:12:24
	     */
	    private static void subWatch(ZkClient zk){
	        if(!zk.exists("/node_11")) {
	            zk.createPersistent("/node_11");
	        }
	        //数据订阅事件
	        zk.subscribeDataChanges("/node_11", new IZkDataListener() {
	            public void handleDataChange(String dataPath, Object data) throws Exception {
	                System.out.println("触发事件："+dataPath+"->"+data);
	            }

	            public void handleDataDeleted(String dataPath) throws Exception {
	                System.out.println("触发删除事件:"+dataPath);
	            }
	        });
	    }

}
