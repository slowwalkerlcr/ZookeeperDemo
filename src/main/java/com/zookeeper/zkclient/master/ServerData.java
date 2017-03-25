package com.zookeeper.zkclient.master;

import java.io.Serializable;

/**
 * 
 * TODO 每台服务器自身属性
 * 
 * @Date 2017年3月25日 下午6:53:17
 * @author Administrator
 * @version
 */
public class ServerData implements Serializable {

	/**
	 * serialVersionUID:TODO(用一句话描述这个变量表示什么).
	 */
	private static final long serialVersionUID = -2921113233104656597L;

	private int serverId; //serverId

	private String serverName; //serverName

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

}
