package com.lxz.examclustering;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DocTool {
	/**
	 * 返回余弦距离下与文档距离最近的中心点类标号
	 * @param doc 文档
	 * @param centers 所有中心点
	 * @param dictSize 词典大小
	 * @return 文档所属类标号
	 */
	public static String returnNearestCentNum(Map<String, Double> doc,
			Map<String, Map<String, Double>> centers) {
		//最近中心点
		String nearestCendroid = "null";
		//最小距离
		double nearestDistance = 0;
		//文档向量长度
		double docLength = 0;
		//中心点向量长度
		double centLength = 0;
		//文档向量与中心点向量内积
		double innerProduct = 0;
		
		//计算文档向量长度
		Iterator<Entry<String, Double>> docIter = doc.entrySet().iterator();
		while (docIter.hasNext()) {
			Map.Entry<String, Double> entry = (Map.Entry<String, Double>) docIter
					.next();
			docLength += Math.pow(entry.getValue(), 2.0);
		}
		docLength = Math.sqrt(docLength);//文档向量长度
		
		//计算文档与所有中心点的余弦距离
		Iterator<Entry<String, Map<String, Double>>> allCendroids = centers
				.entrySet().iterator();//取出每个中心点
		while (allCendroids.hasNext()) {
			Map.Entry<String, Map<String, Double>> entry = (Entry<String, Map<String, Double>>) allCendroids
					.next();
			Set docSet = doc.keySet();//取得所有doc值，依次判断
			for (Iterator iter = docSet.iterator(); iter.hasNext();) {
				String word = (String)iter.next();
				if(entry.getValue().containsKey(word)){//判断中心点和文档有没有共同的词
					innerProduct += entry.getValue().get(word) * doc.get(word);//计算文档向量和中心点向量的内积
				}
//				String key = (String) iter.next();
//				Double value = (Double) doc.get(key);
//				System.out.println(key + "====" + value);
			}
			Set centSet = entry.getValue().keySet();
			for(Iterator iter = centSet.iterator(); iter.hasNext();){
				centLength += Math.pow(entry.getValue().get(iter.next()), 2.0);
			}
			
//			for (long i = 0; i < dictSize; i++) {
//				if (entry.getValue().containsKey(i)) {
//					centLength += Math.pow(entry.getValue().get(i), 2.0);//计算中心点向量长度
//					if (doc.containsKey(i))
//						innerProduct += entry.getValue().get(i) * doc.get(i);//计算文档向量和中心点向量的内积
//				}
//			}
			
			//计算余弦距离并更新最近中心点内积
			centLength = Math.sqrt(centLength);
			if (innerProduct / (docLength * centLength) > nearestDistance){
				nearestDistance = innerProduct / (docLength * centLength);
				nearestCendroid = entry.getKey();
			}
			centLength = 0;
			innerProduct = 0;
		}

		return nearestCendroid;
		//return "abc";
	}
	
	public static void main(String[] args){
		Map<String, Double> doc = new HashMap<String, Double>();
		doc.put("你好", 0.123);
		doc.put("他好", 0.1232);
		doc.put("ds", 0.1123);
		doc.put("aa", 0.1223);
		doc.put("dsd", 0.1323);
		
		Map<String, Map<String, Double>> centers = null;
		Map<String, Double> center = new HashMap<String, Double>();
		//center.put("你好", 0.256);
		center.put("你ds好", 0.256);
		//center.put("dsd", 0.256);
		center.put("ffdg", 0.256);
		//center.put("他好", 0.256);
		Map<String, Double> center1 = new HashMap<String, Double>();
		center1.put("你好", 0.123);
		center1.put("你ds好", 0.256);
		center1.put("dsd", 0.256);
		center1.put("ffdg", 0.256);
		center1.put("他好", 0.256);
		Map<String, Double> center2 = new HashMap<String, Double>();
		center2.put("你好", 0.123);
		center2.put("他好", 0.1232);
		center2.put("ds", 0.1123);
		center2.put("aa", 0.1223);
		center2.put("dsd", 0.1323);
		//centers.put("5369", center);
		centers = new HashMap<String,Map<String,Double>>();
		centers.put("5369", center);
		centers.put("656941", center1);
		//centers.put("941", center2);
		
		System.out.println(returnNearestCentNum(doc, centers));
	}
}
