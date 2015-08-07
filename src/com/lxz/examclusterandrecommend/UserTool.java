package com.lxz.examclusterandrecommend;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class UserTool {
	/**
	 * 返回余弦距离下与用户距离最近的中心点类标号
	 * @param user 用户
	 * @param centers 所有中心点
	 * @return 用户所属类标号
	 */
	//输入数据格式:
	//用户编号	错题号1，错题号2，错题号3	
	public static String returnNearestCentNum(Map<String, Integer> user,
			Map<String, Map<String, Double>> centers) {
		//最近中心点
		String nearestCendroid = "null";
		//最小距离
		double nearestDistance = 0.0;
		//用户向量长度
		double userLength = 0.0;
		//中心点向量长度
		double centLength = 0.0;
		//用户向量与中心点向量内积
		double innerProduct = 0.0;
		
		//计算用户向量长度
		userLength = Math.sqrt(user.size());
		/*Iterator<Entry<String, Integer>> userIter = user.entrySet().iterator();
		while (userIter.hasNext()) {
			Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) userIter
					.next();
			userLength += Math.pow(entry.getValue(), 2.0);
		}
		userLength = Math.sqrt(userLength);*/ //用户向量长度
		
		//计算用户与所有中心点的余弦距离
		Iterator<Entry<String, Map<String, Double>>> allCendroids = centers
				.entrySet().iterator();//取出每个中心点
		while (allCendroids.hasNext()) {
			Map.Entry<String, Map<String, Double>> entry = (Entry<String, Map<String, Double>>) allCendroids
					.next();
			Set userSet = user.keySet();//取得所有user值，依次判断
			for (Iterator iter = userSet.iterator(); iter.hasNext();) {
				String point = (String)iter.next();
				if(entry.getValue().containsKey(point)){//判断中心点和用户有没有共同的题号
					innerProduct += entry.getValue().get(point) * user.get(point);//计算用户向量和中心点向量的内积
				}
			}
			Set centSet = entry.getValue().keySet();
			for(Iterator iter = centSet.iterator(); iter.hasNext();){
				centLength += Math.pow(entry.getValue().get(iter.next()), 2.0);
			}
			
			//计算余弦距离并更新最近中心点内积
			centLength = Math.sqrt(centLength);
			if (innerProduct / (userLength * centLength) > nearestDistance){
				nearestDistance = innerProduct / (userLength * centLength);
				nearestCendroid = entry.getKey();
			}
			centLength = 0;
			innerProduct = 0;
		}

		return nearestCendroid;
		//return "abc";
	}
	
	public static void main(String[] args){
		Map<String, Integer> user = new HashMap<String, Integer>();
		user.put("12354", 1);
		user.put("12345", 1);
		user.put("26645", 1);
		user.put("54574", 1);
		user.put("58512", 1);
		
		Map<String, Map<String, Double>> centers = null;
		Map<String, Double> center = new HashMap<String, Double>();
		center.put("45668", 0.256);
		center.put("54557", 0.256);
		center.put("45555", 0.256);
		center.put("54574", 0.256);
		center.put("55562", 0.256);
		Map<String, Double> center1 = new HashMap<String, Double>();
		center1.put("45455", 0.123);
		center1.put("44554", 0.256);
		center1.put("54574", 0.256);
		center1.put("85887", 0.256);
		center1.put("88842", 0.256);
		Map<String, Double> center2 = new HashMap<String, Double>();
		center2.put("75776", 0.123);
		center2.put("26643", 0.1232);
		center2.put("85757", 0.1123);
		center2.put("57755", 0.1223);
		center2.put("58512", 0.1323);
		//centers.put("5369", center);
		centers = new HashMap<String,Map<String,Double>>();
		centers.put("5369", center);
		centers.put("656941", center1);
		centers.put("941", center2);
		
		System.out.println(returnNearestCentNum(user, centers));
	}
}
