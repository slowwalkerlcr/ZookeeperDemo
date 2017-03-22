package com.zookeeper.zkclient;

import java.io.UnsupportedEncodingException;

import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;


/**
 * 
 * TODO 基于ZkClient数据的转码 
 * @Date  2017年3月22日 下午10:23:06   	 
 * @author Administrator  
 * @version
 */
public class MyZkSerializer implements ZkSerializer{

	public Object deserialize(byte[] bytes) throws ZkMarshallingError {
		// TODO Auto-generated method stub
		try {
			return new String(bytes,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public byte[] serialize(Object data) throws ZkMarshallingError {
		
		try {
			return String.valueOf(data).getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
