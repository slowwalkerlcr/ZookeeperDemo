package com.zookeeper.lvcr;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;

/**
 * 
 * TODO zookeeperDemo演示
 * @Date  2017年3月19日 下午8:35:58   	 
 * @author Administrator  
 * @version
 */
public class ZookeeperDemo {
	/**
	 * session 过期时间
	 */
	private static final int SESSION_TIMEOUT = 3000;
	
	public static void main(String[] args) {
		ZooKeeper zk = null;
		
		try {
			zk = new ZooKeeper("192.168.148.130:2181", SESSION_TIMEOUT, new Watcher() {
				public void process(WatchedEvent event) {
					// TODO Auto-generated method stub
					System.out.println("触发事件:"+event.getType());
				}
			});
			//zk操作演示
//			createDemo(zk);
//			updateDemo(zk);
//			deleteDemo(zk);
//			aclDemo(zk);
			watcherDemo(zk);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(zk != null){
				try {
					zk.close();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 
	 * TODO(创建zk节点演示)  
	 * @param zk 
	 * @author Administrator  
	 * @throws InterruptedException 
	 * @throws KeeperException 
	 * @Date 2017年3月19日下午9:12:38
	 */
	private static void createDemo(ZooKeeper zk) throws KeeperException, InterruptedException{
		if((zk.exists("/node3", true))==null){
			zk.create("/node3", "node3_value".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		System.out.println(new String(zk.getData("/node3", true, null)));
	}
	/**
	 * 
	 * TODO(删除zk节点演示)  
	 * @param zk
	 * @throws KeeperException
	 * @throws InterruptedException 
	 * @author Administrator  
	 * @Date 2017年3月19日下午9:23:01
	 */
	private static void deleteDemo(ZooKeeper zk) throws KeeperException, InterruptedException{
		if((zk.exists("/node3", true))!=null){
			zk.delete("/node3", -1);
		}
		System.out.println(new String(zk.getData("/node3", true, null)));
	}
	
	/**
	 * 
	 * TODO(创建zk节点演示)  
	 * @param zk 
	 * @author Administrator  
	 * @throws InterruptedException 
	 * @throws KeeperException 
	 * @Date 2017年3月19日下午9:12:38
	 */
	private static void updateDemo(ZooKeeper zk) throws KeeperException, InterruptedException{
		if((zk.exists("/node3", true))!=null){
			zk.setData("/node3", "update_node3_value".getBytes(), -1);
		}
		System.out.println(new String(zk.getData("/node3", true, null)));
	}
	
	/**
	 * 
	 * TODO(zk权限演示demo)  
	 * @param zk
	 * @throws KeeperException
	 * @throws InterruptedException
	 * @throws NoSuchAlgorithmException 
	 * @author Administrator  
	 * @Date 2017年3月19日下午9:37:12
	 */
	private static void aclDemo(ZooKeeper zk) throws KeeperException, InterruptedException, NoSuchAlgorithmException{
		if((zk.exists("/node4", true))==null){
			//创建ACL权限，设置密码
			ACL acl = new ACL(ZooDefs.Perms.ALL, new Id("digest", DigestAuthenticationProvider.generateDigest("root:root")));
			List<ACL> acls = new ArrayList<ACL>();
			acls.add(acl);
			//zk.setACL("/node4", acls, version)
			//创建节点并设置权限，
			zk.create("/node4", "node4_acl_value".getBytes(), acls, CreateMode.PERSISTENT);
			System.out.println(new String(zk.getData("/node4",true,null)));
		}
		//添加auth,如果不设置，无权限访问
		zk.addAuthInfo("digest","root:root".getBytes());
		System.out.println(new String(zk.getData("/node4", true, null)));
	}
	
	
	/**
	 * 
	 * TODO(zk监听演示demo)  
	 * @param zk
	 * @throws KeeperException
	 * @throws InterruptedException
	 * @throws NoSuchAlgorithmException 
	 * @author Administrator  
	 * @Date 2017年3月19日下午9:39:00
	 */
	private static void watcherDemo(final ZooKeeper zk) throws KeeperException, InterruptedException, NoSuchAlgorithmException{
		if(zk.exists("/node5",true)==null) {
            zk.create("/node5", "node5_watcher_value".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
		
		byte[] rsByt = zk.getData("/node5", new Watcher() {
			public void process(WatchedEvent event) {
				// TODO Auto-generated method stub
				System.out.println("触发节点事件：" + event.getPath());
				
				 try {
					System.out.println("==="+new String(zk.getData(event.getPath(),true,null)));
				} catch (KeeperException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, null);
		
		
		  System.out.println(new String(rsByt));
	        zk.setData("/node5","node5_watcher_value_2".getBytes(),-1);
	        System.out.println(new String(zk.getData("/node5",true,null)));
		
	}
	
	

}
