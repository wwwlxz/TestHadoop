package com.lxz.examrecommend;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

/**
 * @author xz
 * 
 */
public class New_Step3 {
	/*
	 * 输入数据格式
	 * 试题编号	用户编号,得分:用户编号,得分:用户编号,得分:
	 * 30757	22624,51:20872,27:39222,39:
	 * 
	 * 输出数据格式
	 * 用户编号	试题编号,得分
	 * 用户编号	试题编号,得分
	 * 用户编号	试题编号,得分
	 * 
	 */
	public static class New_Step3_Mapper extends
			Mapper<Object, Text, Text, Text> {
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			String[] strs = value.toString().split("\t");
			String[] userAndScores = strs[1].split(":");
			for(int i = 1; i<userAndScores.length; i++){
				context.write(new Text(userAndScores[i].split(",")[0]), new Text(strs[0] + "," + userAndScores[i].split(",")[1]));
			}
		}
	}

	public static class New_Step3_Reducer extends
			Reducer<Text, Text, Text, Text> {
		/*
		 * 输入数据格式：
		 * 用户编号	试题编号,得分 试题编号,得分 试题编号,得分
		 * 用户编号	试题编号,得分 试题编号,得分 试题编号,得分
		 * 用户编号	试题编号,得分 试题编号,得分 试题编号,得分
		 * 
		 * 输出数据格式：
		 * 用户编号	试题编号,得分@试题编号,得分@试题编号,得分@
		 */
		//将key:listOfValues转化成key:values
		public void reduce(Text key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			String userNo = key.toString();//用户编号
			String itemAndScores = "";
			for(Text val : values){
				itemAndScores = itemAndScores + val.toString().split(",")[0] + "," + val.toString().split(",")[1] + "@";
			}
			context.write(new Text(userNo), new Text(itemAndScores));
		}
	}

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
				"hdfs://192.168.192.100:9000/user/root/output_examnewstep2_2",
				"hdfs://192.168.192.100:9000/user/root/output_examnewstep3" };
		String[] otherArgs = new GenericOptionsParser(conf, ars)
				.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: wordcount <in> <out>");
			System.exit(2);
		}

		Job job = new Job(conf, "movie recommend step1");
		// And add this statement. XXX
		((JobConf) job.getConfiguration()).setJar(jarFile.toString());

		job.setJarByClass(New_Step3.class);
		job.setMapperClass(New_Step3_Mapper.class);
//		job.setCombinerClass(Step1_Reducer.class);
		job.setReducerClass(New_Step3_Reducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
