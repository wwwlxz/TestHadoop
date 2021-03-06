package com.lxz.moviesrecommend;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
/**
 * 计算评分矩阵
 * @author xz
 *
 */
public class Step3 {
	/*
	 * 读取两张表中的数据，这两张表中的数据格式分别如下所示：
	 * 表1 用户项目评分表
	 * 1	101,5.0
	 * 1	102,3.0
	 * 1	103,2.5
     * 2	101,2.0 
     * ...
	 * 表2 项目同现矩阵
	 * 101:101	5
	 * 101:102	3
	 * 101:103	4
	 * 101:104	4
	 * 101:105	2
	 * 101:106	2
	 * 101:107	1
	 * ...
	 * 输出格式为：
	 * 1@	101,5.0:102,3.0:103,2.5
	 * ...
	 * 101	101,5:102,3:103,4:105,2:106,2:107,1
	 * ...
	 */
	public static class Step3_Mapper extends
			Mapper<LongWritable, Text, Text, Text> {
		private final static Text k = new Text();
		private final static Text v = new Text();
		
		private final static Map<String, List> cooccurrenceMatrix = new HashMap<String, List>();
		
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			//将行按制表符分割
			String[] strs = value.toString().split("\t");
			if(strs[0].contains(":")){//cooccurrence同现矩阵
				String itemID1 = strs[0].split(":")[0];
				String itemID2 = strs[0].split(":")[1];
				int num = Integer.parseInt(strs[1]);
				
				List list = null;
				if(!cooccurrenceMatrix.containsKey(itemID1)){
					list = new ArrayList();
				}else{
					list = cooccurrenceMatrix.get(itemID1);
				}
				list.add(new Cooccurrence(itemID1, itemID2, num));
				cooccurrenceMatrix.put(itemID1, list);
			}else{//userVector评分矩阵
				String userID = strs[0];
				String itemID = strs[1].split(",")[0];
				double pref = Double.parseDouble(strs[1].split(",")[1]);
				k.set(userID);
//				for(Cooccurrence co : cooccurrenceMatrix.get(itemID)){
//					v.set(co.getItemID2() + "," + pref * co.getNum());
//					context.write(k, v);
//				}
			}
			//String[] v1 = tokens[0].split("");
		}
	}

	public static class Step3_Reducer extends
			Reducer<Text, Text, Text, Text> {

		public void reduce(Text key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			String str = "";
			for(Text val : values){
				str = str + val.toString() + ":";
			}
			context.write(key, new Text(str));
		}
	}

//	public class Cooccurrence{
//		private String itemID1;
//		private String itemID2;
//		private int num;
//		
//		public Cooccurrence(String itemID1,String itemID2,int num){
//			super();
//			this.itemID1 = itemID1;
//			this.itemID2 = itemID2;
//			this.num = num;
//		}
//		
//		public String getItemID1(){
//			return itemID1;
//		}
//		
//		public void setItemID1(String itemID1){
//			this.itemID1 = itemID1;
//		}
//		
//		public String getItemID2(){
//			return itemID2;
//		}
//		
//		public void setItemID2(String itemID2){
//			this.itemID2 = itemID2;
//		}
//		
//		public int getNum(){
//			return num;
//		}
//		
//		public void setNum(int num){
//			this.num = num;
//		}
//	}
	
	public static void main(String[] args) throws Exception {
		// Add these statements. XXX
		File jarFile = EJob.createTempJar("bin");
		EJob.addClasspath("/home/hadoop/hadoop-1.0.0/conf");
		ClassLoader classLoader = EJob.getClassLoader();
		Thread.currentThread().setContextClassLoader(classLoader);

		Configuration conf = new Configuration();
		conf.set("mapred.job.tracker", "192.168.192.100:9001");
		// String[] ars=new String[]{"input","newout"};
		String[] ars = new String[] {
				"hdfs://192.168.192.100:9000/user/root/input_moviestep3",
				"hdfs://192.168.192.100:9000/user/root/output_moviestep3" };
		String[] otherArgs = new GenericOptionsParser(conf, ars)
				.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: wordcount <in> <out>");
			System.exit(2);
		}

		Job job = new Job(conf, "word count");
		// And add this statement. XXX
		((JobConf) job.getConfiguration()).setJar(jarFile.toString());

		job.setJarByClass(Step3.class);
		job.setMapperClass(Step3_Mapper.class);
//		job.setCombinerClass(Step3_Reducer.class);
		job.setReducerClass(Step3_Reducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
