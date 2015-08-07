package com.lxz.examrecommend;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

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
 * 对物品组合列表进行计数，建立物品的同现矩阵
 * 
 * 
 */
public class Step2 {
	public static class Step2_Mapper extends
			Mapper<Object, Text, Text, IntWritable> {
		//读取每一行数据，按制表符分割
		private Text itemToItem = null;
		private final static IntWritable one = new IntWritable(1);

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			String[] strs = value.toString().split("\t");
			//102:3.0,103:2.5,101:5.0,
			String[] ItemAndScores = strs[1].split(",");
			for(int i = 0; i < ItemAndScores.length ; i++){
				String itemID1 = ItemAndScores[i].split(":")[0];
				for(int j = 0; j < ItemAndScores.length ; j++){
					String itemID2 = ItemAndScores[j].split(":")[0];
					itemToItem = new Text(itemID1 + ":" + itemID2);
					context.write(itemToItem, one);
				}
			}
		}
	}

	public static class Step2_Reducer extends
			Reducer<Text, IntWritable, Text, IntWritable> {
		private IntWritable result = new IntWritable();

		public void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			result.set(sum);
			context.write(key, result);
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
				"hdfs://192.168.192.100:9000/user/root/output_examstep1",
				"hdfs://192.168.192.100:9000/user/root/output_examstep2" };
		String[] otherArgs = new GenericOptionsParser(conf, ars)
				.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: wordcount <in> <out>");
			System.exit(2);
		}

		Job job = new Job(conf, "word count");
		// And add this statement. XXX
		((JobConf) job.getConfiguration()).setJar(jarFile.toString());

		job.setJarByClass(Step2.class);
		job.setMapperClass(Step2_Mapper.class);
//		job.setCombinerClass(Step2_Reducer.class);
		job.setReducerClass(Step2_Reducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
