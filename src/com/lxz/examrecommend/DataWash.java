package com.lxz.examrecommend;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
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
/*
 * 对数据进行清洗
 * 数据原始格式：
 * 158348 	33585@33163@ 
 * 1348 	3358@333@
 * 把数据变成如下格式
 * 用户编号	试题编号,1
 * 用户编号	试题编号,1
 * 用户编号	试题编号,1
 */
public class DataWash {
	public static class DataWashMapper extends
			Mapper<Object, Text, Text, Text> {
		// 记录用户的编号
		private Text userId = new Text();
		// 记录用户错题的编号
		private Text mistake = new Text();

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			String[] strs = value.toString().split("\t");
			userId = new Text(strs[0]);// 将用户ID取出来

			String[] mistakes = strs[1].split("@");
			for(int i = 0; i<mistakes.length; i++){
				context.write(userId, new Text(mistakes[i] + ",1.0"));
			}
			
//			Pattern pattern = Pattern.compile("\"([^\"]+)\":\\{");// 对出错题目进行正则匹配，取出出错题目编号
//			Matcher ma = pattern.matcher(strs[1]);
//			String mistakesTmp = "";
//			while (ma.find()) {
//				mistakesTmp = mistakesTmp + ma.group(1) + "@";
//			}
//			mistakes = new Text(mistakesTmp);
//			if (mistakesTmp != "") {
//				context.write(userId, mistakes);
//			}
		}
	}

	public static class DataWashReducer extends
			Reducer<Text, Text, Text, IntWritable> {

		public void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {

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
				"hdfs://192.168.192.100:9000/user/root/output_testcollaborative",
				"hdfs://192.168.192.100:9000/user/root/output_datawash" };
		String[] otherArgs = new GenericOptionsParser(conf, ars)
				.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: wordcount <in> <out>");
			System.exit(2);
		}

		Job job = new Job(conf, "DataWash");
		// And add this statement. XXX
		((JobConf) job.getConfiguration()).setJar(jarFile.toString());

		job.setJarByClass(DataWash.class);
		job.setMapperClass(DataWashMapper.class);
		// job.setCombinerClass(DataWashReducer.class);
		// job.setReducerClass(DataWashReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
