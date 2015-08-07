package com.lxz.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regx {
	public static String STR1 = "2277760	{\"64157\":{\"qid\":\"64157\",\"kid\":\"392\",\"answer\":\"B\",\"is_right\":\"-1\"},\"64163\":{\"qid\":\"64163\",\"kid\":\"392\",\"answer\":\"B\",\"is_right\":\"-1\"}}";
	public static String STR2 = "";
	
	public static void main(String[] args){
		Pattern p = Pattern.compile("\"qid\":\"[^,]*");
		Matcher m = p.matcher(STR1);
		while(m.find()){
			String[] mistake = m.group().replace("\"", "").trim().split(":");
			System.out.println(mistake[1]);
		}
	}
}
