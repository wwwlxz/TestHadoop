package com.lxz.moviesrecommend;

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
 * 按用户分组，计算所有物品出现的组合列表，得到用户对物品的评分矩阵
 * 数据输入形式：
1	101,5.0
1	102,3.0
1	103,2.5
2	101,2.0
2	102,2.5
2	103,5.0
2	104,2.0
。。。
 * 数据输出形式：
 * 1	101:5.0,102:3.0,103:2.5
 * 2	101:2.0,102:2.5,103:5.0,104:2.0
 * ...
 * @author xz
 * 
 */
public class Step1 {
	public static class Step1_Mapper extends
			Mapper<Object, Text, Text, Text> {
		//将输入的数据行按制表符分割，存储用户ID
		private static Text userID = null;
		//存储项目及评分
		private static Text ItemScore = new Text();

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			String[] strs = value.toString().split("\t");
			userID = new Text(strs[0]);
			ItemScore.set(strs[1].replace(",", ":"));
			context.write(userID, ItemScore);
		}
	}

	public static class Step1_Reducer extends
			Reducer<Text, Text, Text, Text> {
		//将key:listOfValues转化成key:values
		public void reduce(Text key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			Text ItemScores = new Text();
			for (Text val : values) {
//				ItemScores.set(val.toString() + ",");
				ItemScores = new Text(ItemScores.toString()+val.toString() + ",");
			}
			context.write(key, ItemScores);
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
				"hdfs://192.168.192.100:9000/user/root/input_moviestep1",
				"hdfs://192.168.192.100:9000/user/root/output_moviestep1" };
		String[] otherArgs = new GenericOptionsParser(conf, ars)
				.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: wordcount <in> <out>");
			System.exit(2);
		}

		Job job = new Job(conf, "movie recommend step1");
		// And add this statement. XXX
		((JobConf) job.getConfiguration()).setJar(jarFile.toString());

		job.setJarByClass(Step1.class);
		job.setMapperClass(Step1_Mapper.class);
//		job.setCombinerClass(Step1_Reducer.class);
		job.setReducerClass(Step1_Reducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
