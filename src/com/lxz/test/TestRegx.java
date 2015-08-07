package com.lxz.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegx {
	public static String WORD = "31611	{\"本文\":0.6262472746793588,\"的\":0.02158603557280141,\"主旨\":0.5550873798104059,\"是\":0.06292623817335649}";
	public static String JSON = "[{\"name\":\"zhangsan\",\"sex\":\"男\"},{\"name\":\"lisi\",\"sex\":\"女\"},{\"name\":\"wangwu\",\"sex\":\"男\"}]";
	public static String STRS = "30848	{\"一项\"=0.03881201205537848, \"划线\"=0.1033758387975329, \"规模\"=0.0904973843725464, \"填入\"=0.047860396139448945, \"其\"=0.058763890603474554, \"之大\"=0.15805751596575832, \"古塔\"=0.1504013742676852, \"最\"=0.039926889572691054, \"处\"=0.05874425215263683, \"建筑\"=0.10102473939811683, \"分布\"=0.11177673382545751, \"的\"=0.011262279429287692, \"广\"=0.13051540920142846, \"之\"=0.0659446739995078, \"之多\"=0.15461485309412246, \"古代\"=0.09226544639758706, \"在\"=0.030879006104468587, \"恰当\"=0.045023866437654485, \"是\"=0.02188738719073269, \"数量\"=0.08334005123239857, \"我国\"=0.05706450410983132, \"中\"=0.032121593466212434, \"建造\"=0.11636006412571077}";
	public static String STR1 = "{\"33671\":{\"qid\":\"33671\",\"kid\":\"435\",\"answer\":\"A\",\"is_right\":\"-1\"},\"33673\":{\"qid\":\"33673\",\"kid\":\"435\",\"answer\":\"B\",\"is_right\":\"-1\"}}";
	
	public static void main(String[] args) {
		Pattern pattern = Pattern.compile("\"([^\"]+)\":\\{");
		Matcher ma = pattern.matcher(STR1);
		String[] strs = null;
		while(ma.find()){
			//strs = ma.group();
			System.out.println(ma.group(1));
		}
		
//		Pattern pattern = Pattern.compile("\"([^\"]+)\"=([^,}]+)");
////		Pattern pattern = Pattern.compile("\"([^\"]+)\"=([^,}]+)[^,}]");
//		Matcher ma = pattern.matcher(STRS);
//		String[] strs = null;
//		while(ma.find()){
//			strs = ma.group().split("=");
//			System.out.println(ma.group());
//			if (strs.length == 2) {
//				System.out.println(strs[0].replace("\"", "").trim());
//				System.out.println(strs[1].replace("\"", "").trim());
//			}
//		}
		
	}
}
