package com.lxz.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonRegx {
	public static String WORD = "31611	{\"本文\":0.6262472746793588,\"的\":0.02158603557280141,\"主旨\":0.5550873798104059,\"是\":0.06292623817335649}";

	public static void main(String[] args) {
		String s = "[{\"name\":\"zhangsan\",\"sex\":\"男\"},{\"name\":\"lisi\",\"sex\":\"女\"},{\"name\":\"wangwu\",\"sex\":\"男\"}]";
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Map<String, String> map = null;
		//Pattern p = Pattern.compile("(\"\\w+\"):(\"[^\"]+\")");
		//Matcher m = p.matcher(s);
		
		Pattern p = Pattern.compile("\"([^\"]+)\":([^,]+)");
		Matcher m = p.matcher(WORD);
		String[] str = WORD.split("\t");
		System.out.println(Integer.parseInt(str[0].trim()));
		String[] strs = null;
		while (m.find()) {
			System.out.println(m.group());
			strs = m.group().split(":");
			if (strs.length == 2) {
				map = new HashMap<String, String>();
				// System.out.println(_strs[0].replaceAll("\"", "").trim() + "="
				// + _strs[1].trim().replaceAll("\"", ""));
				map.put(strs[0].replaceAll("\"", "").trim(), strs[1].trim()
						.replaceAll("\"", ""));
				list.add(map);
			}
		}
		System.out.println(list);
	}
}
