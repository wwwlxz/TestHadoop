package com.lxz.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Json {
	public static String STR = "{\"response\":{\"data\":[{\"address\":\"南京市游乐园\",\"province\":\"江苏\",\"district\":\"玄武区\",\"city\":\"南京\"}]},\"status\":\"ok\"}";
	public static String STR1 = "{\"content\":\"孔子曰：“政者正也”，国家公务员治事理政，贵在一个公字，难在一个公字。这包含两层意思：其一，公则公，公则不为私利所惑，正则不为邪所媚。有公在心，则权色难侵。其二，其身正，不令而行；其身不正，虽令不从。这说明国家公务员治事理政应具有（&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;）。（1）办事公道的职业道德（2）爱岗敬业的职业道德（3）坚持真理的职业道德（4）无私奉献的职业道德\",\"choices\":[\"（1）（3）\",\"（1）（2）\",\"（3）（4）\",\"（1）（4）\"],\"attrs\":{\"comment\":\"<p class=\\\"item-p\\\">题干中所阐释的“公”第一层意思是不谋私利，不被私利和邪恶所迷惑，心中要有公正、公道，即公务员要具备做人公正、做事公道的职业道德，第二层意思是指当管理者自身端正，作出榜样和表率时，不需下命令，被管理者也就会跟着行动起来；同样，如果管理者自身不端正，而要求被管理者端正，即使三令五申，被管理者也不会服从的，由此可见公务员应该坚持真理，做好表率的榜样。综上所述，只有（1），（3）符合题意，（2）和（4）与本题表达意思无关。故正确答案为A</p>\",\"comment_detail\":\"\",\"comment_author\":\"刘静\",\"techniques\":\"<p class=\\\"item-p\\\">根据题干中所表达的两层意思，用关键词法，考生较容易选定（1），排除（4），其次，（2）的爱岗敬业在本题中体现较少，所以只有（3）较符合，由此可选出答案</p>\",\"extension\":\"<p class=\\\"item-p\\\">公务员职业道德的主要内容：坚持为人民服务的宗旨，遵循集体主义原则，爱岗敬业，勤政为民，塑造个人修养。公务员职业道德的价值取向和功能有：基础价值取向——谋求公共利益最大化，核心价值取向——建立健全责任监控机制，根本价值取向——维护社会公正，目标价值取向——培育高尚人格。公民道德建设纲要：爱国守法，明礼诚信，团结友善，勤俭自强，敬业奉献。社会主义职业道德内容：爱岗敬业、诚实守信、办事公道、服务群众、奉献社会</p>\",\"from\":\"吉林（乙卷）\",\"from_year\":\"2012\",\"from_location\":\"586\",\"knowledge_point_name\":\"管理\",\"knowledge_point_id\":\"433\",\"knowledge_point_code\":\"\\u0001\\u0007\\u0001\"},\"answer\":\"A\",\"answerNum\":1,\"score\":0.0,\"tombstone\":0,\"updateTime\":1422274072,\"id\":39438}";
	public static String STR2 = "{}{%QUES%}{\"content\":\"孔子曰：“政者正也”，国家公务员治事理政，贵在一个公字，难在一个公字。这包含两层意思：其一，公则公，公则不为私利所惑，正则不为邪所媚。有公在心，则权色难侵。其二，其身正，不令而行；其身不正，虽令不从。这说明国家公务员治事理政应具有（&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;）。（1）办事公道的职业道德（2）爱岗敬业的职业道德（3）坚持真理的职业道德（4）无私奉献的职业道德\",\"choices\":[\"（1）（3）\",\"（1）（2）\",\"（3）（4）\",\"（1）（4）\"],\"attrs\":{\"comment\":\"<p class=\\\"item-p\\\">题干中所阐释的“公”第一层意思是不谋私利，不被私利和邪恶所迷惑，心中要有公正、公道，即公务员要具备做人公正、做事公道的职业道德，第二层意思是指当管理者自身端正，作出榜样和表率时，不需下命令，被管理者也就会跟着行动起来；同样，如果管理者自身不端正，而要求被管理者端正，即使三令五申，被管理者也不会服从的，由此可见公务员应该坚持真理，做好表率的榜样。综上所述，只有（1），（3）符合题意，（2）和（4）与本题表达意思无关。故正确答案为A</p>\",\"comment_detail\":\"\",\"comment_author\":\"刘静\",\"techniques\":\"<p class=\\\"item-p\\\">根据题干中所表达的两层意思，用关键词法，考生较容易选定（1），排除（4），其次，（2）的爱岗敬业在本题中体现较少，所以只有（3）较符合，由此可选出答案</p>\",\"extension\":\"<p class=\\\"item-p\\\">公务员职业道德的主要内容：坚持为人民服务的宗旨，遵循集体主义原则，爱岗敬业，勤政为民，塑造个人修养。公务员职业道德的价值取向和功能有：基础价值取向——谋求公共利益最大化，核心价值取向——建立健全责任监控机制，根本价值取向——维护社会公正，目标价值取向——培育高尚人格。公民道德建设纲要：爱国守法，明礼诚信，团结友善，勤俭自强，敬业奉献。社会主义职业道德内容：爱岗敬业、诚实守信、办事公道、服务群众、奉献社会</p>\",\"from\":\"吉林（乙卷）\",\"from_year\":\"2012\",\"from_location\":\"586\",\"knowledge_point_name\":\"管理\",\"knowledge_point_id\":\"433\",\"knowledge_point_code\":\"\\u0001\\u0007\\u0001\"},\"answer\":\"A\",\"answerNum\":1,\"score\":0.0,\"tombstone\":0,\"updateTime\":1422274072,\"id\":39438}";
	
//	public static void main(String[] args) throws JSONException{//STR
//		JSONObject dataJson = new JSONObject(STR);
//		JSONObject response = dataJson.getJSONObject("response");
//		String status = (String)dataJson.get("status");
//		JSONArray data = response.getJSONArray("data");
//		JSONObject info = data.getJSONObject(0);
//		String address = info.getString("address");
//		System.out.println(response.toString());
//		System.out.println(data.toString());
//		System.out.println(address);
//		System.out.println(status);
//	}
	
	public static void main(String[] args) throws JSONException{//STR1
		JSONObject dataJson = new JSONObject(STR1);
		JSONObject attrs = dataJson.getJSONObject("attrs");
		String knowledge_point_name = (String)attrs.get("knowledge_point_name");
		System.out.println(knowledge_point_name);
		int id = (int)dataJson.get("id");
		System.out.println(id);
	}
	
//	public static void main(String[] args) throws JSONException{//STR2
//		String[] strs = STR2.split("\\{%QUES%\\}");
//		System.out.println(strs[0]);
//		System.out.println(strs[1]);
//	}
}
