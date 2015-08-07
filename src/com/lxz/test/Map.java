package com.lxz.test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Map {
	public static void main(String[] args) {
		// Map读取键值对，Java遍历Map的两种实现方法
		// 根据map的keyset（）方法来获取key的set集合，然后遍历map取得value的值
		HashMap map = new HashMap();

		map.put("a", "aaaa");
		map.put("a", "hello world");
		map.put("b", "bbbb");
		map.put("c", "cccc");
		map.put("d", "dddd");
		System.out.println(map);
		Set set = map.keySet();

		for (Iterator iter = set.iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			String value = (String) map.get(key);
			System.out.println(key + "====" + value);
		}
		System.out.println(map.size());
		System.out.println(10E-6);
		
		
	}
}
