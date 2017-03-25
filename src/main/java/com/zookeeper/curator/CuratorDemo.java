package com.zookeeper.curator;

import java.util.Collection;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.Stat;

public class CuratorDemo {
	 private static String CONNECT_STRING="192.168.148.130:2181,192.168.148.130:2182,192.168.148.130:2183";

	 private static int TIMEOUT=3000;
	
	 

	public static void main(String[] args) {
		try{
		//连接Zookeeper
		 CuratorFramework framework=CuratorFrameworkFactory.
	                newClient(CONNECT_STRING, TIMEOUT, TIMEOUT, new ExponentialBackoffRetry(1000, 10));
		//启动连接
		framework.start();
		//获取连接状态
		System.out.println(framework.getState());
		//节点的各种操作
		create(framework);
//		update(framework);
//		delete(framework);
		/*listener(framework);
		listener2(framework);
		System.in.read();*/
		//获取连接状态
		System.out.println(framework.getState());
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	/**
	 * 
	 * TODO(curator创建节点demo)  
	 * @param framework 
	 * @author Administrator  
	 * @Date 2017年3月25日下午2:54:55
	 */
	private static void create(CuratorFramework framework) {
		try {
			//递归创建节点
			//String rtn =framework.create().creatingParentsIfNeeded().forPath("/curator_node/node1/node_1_1","curator_value".getBytes());
			//System.out.println(rtn);
			//异步创建 持久节点  需要在inbackground中设置回调，这里不做演示
			 String rtn2=framework.create().withMode(CreateMode.EPHEMERAL).inBackground().forPath("/curator_node/node_2","background_value".getBytes());
	         System.out.println(rtn2);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			framework.close();
		}
	}
	
   /**
    * 	
    * TODO(更新节点)  
    * @param cf
    * @throws Exception 
    * @author Administrator  
    * @Date 2017年3月25日下午3:57:54
    */
   private static void update(CuratorFramework cf) throws Exception {
        Stat stat=cf.setData().forPath("/curator/node_13/node_13_1","curator_value2".getBytes());
        System.out.println(stat);
        cf.close();
    }
   /**
    * 
    * TODO(删除节点)  
    * @param cf 
    * @author Administrator  
    * @Date 2017年3月25日下午3:59:03
    */
   private static void delete(CuratorFramework cf){
       try {
    	  //递归删除的话，则输入父节点（/node1）  会删除/node1和/node1/node2节点
    	  //如果输入多个节点(/node1/node2) 只会删除node2节点 
           cf.delete().deletingChildrenIfNeeded().forPath("/node_2"); 
       } catch (Exception e) {
           e.printStackTrace();
       }finally{
           cf.close();
       }
   }
	
   
   /**
    * 
    * TODO(事务演示)  
    * 下面方法 1、创建节点node2 2、创建节点node3 3、删除节点node99 由于删除节点node99会报错，所以node2和node3都会回滚，即创建失败
    * @param cf 
    * @author Administrator  
    * @Date 2017年3月25日下午4:00:45
    */
   private static void transaction(CuratorFramework cf){
       try {
           //事务处理， 事务会自动回滚
           Collection<CuratorTransactionResult> results=cf.inTransaction().create().
                   forPath("/node_2").and().create().forPath("/node_3").and().delete().forPath("/node99").and().commit();
           for(CuratorTransactionResult result:results){
               System.out.println(result.getResultStat()+"->"+result.getForPath()+"->"+result.getType());
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
   }
   /**
    * 
    * TODO(监听节点事件)  、
    * 这种方式只会监听一次事件,即执行监听后，第一次对节点做操作(增删改),会触发监听,第二次和以后再对节点做操作时候，不会触发这个监听
    * @param cf   
    * @author Administrator  
    * @Date 2017年3月25日下午4:03:35
    */
   private static void listener(CuratorFramework cf){
       try {
           cf.getData().usingWatcher(new CuratorWatcher() {
               public void process(WatchedEvent event) throws Exception {
                   System.out.println("触发事件"+event.getType());
               }
           }).forPath("/node_3"); //通过CuratorWatcher 去监听指定节点的事件， 只监听一次

       } catch (Exception e) {
           e.printStackTrace();
       }finally {
    	   //记得不能关闭连接，否则监听还没执行就已经关闭连接了
          // cf.close();
       }
   }
   
   /**
    * 
    * TODO(轮询多次监听节点操作，每次操作都会触发监听)  
    * @param cf
    * @throws Exception 
    * @author Administrator  
    * @Date 2017年3月25日下午4:05:56
    */
   private static void listener2(CuratorFramework cf) throws Exception {
	   //1、NodeCacheListenner 监听当前节点	    
       //2、PathChildrenCache 子节点监听  监听当前节点的子节点
       PathChildrenCache childrenCache=new PathChildrenCache(cf,"/node_3",true);
       
       //NodeCache nodeCache;
       childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
       childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
           public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
               System.out.println(event.getType()+"事件监听2");
           }
       });
   }


}
