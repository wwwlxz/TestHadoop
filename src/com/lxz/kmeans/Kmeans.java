package com.lxz.kmeans;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class Kmeans {
	private static final String DICT_PATH = "hdfs://192.168.192.100:9000/user/root/input/seqFile.seq";
	
	public static class KmeansMapper extends
			Mapper<Object, Text, IntWritable, Text> {
		
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			String line = value.toString();
			String[] fields = line.split(" ");
			List<ArrayList<Float>> centers = Assistance.getCenters(context.getConfiguration().get("centerpath"));
			int k = Integer.parseInt(context.getConfiguration().get(""));
			float minDist = Float.MAX_VALUE;
			int centerIndex = k;
			//计算样本点到各个中心的距离，并把样本聚类到距离最近的中心点所属的类
			for(int i = 0; i<k; ++i){
				float currentDist = 0;
				for(int j = 0; j<fields.length; ++j){
					float tmp = Math.abs(centers.get(i).get(j + 1) - Float.parseFloat(fields[j]));
					currentDist += Math.pow(tmp, 2);
				}
				if(minDist > currentDist){
					minDist = currentDist;
					centerIndex = i;
				}
			}
			context.write(new IntWritable(centerIndex), new Text(value));
		}
	}

	public static class KmeansReducer extends
			Reducer<IntWritable, Text, IntWritable, Text> {

		public void reduce(IntWritable key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			List<ArrayList<Float>> assistList = new ArrayList<ArrayList<Float>> ();
			String tmpResult = "";
			for(Text val:values){
				String line = val.toString();
				String[] fields = line.split(" ");
				List<Float> tmpList = new ArrayList<Float>();
				for(int i = 0; i<fields.length; ++i){
					tmpList.add(Float.parseFloat(fields[i]));
				}
				assistList.add((ArrayList<Float>) tmpList);
			}
			//计算新的聚类中心
			for(int i = 0; i<assistList.get(0).size(); ++i){
				float sum = 0;
				for(int j = 0; j<assistList.size(); ++j){
					sum += assistList.get(j).get(i);
				}
				float tmp = sum / assistList.size();
				if(i == 0){
					tmpResult += tmp;
				}else{
					tmpResult += " " + tmp;
				}
			}
			Text result = new Text(tmpResult);
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
				"hdfs://192.168.192.100:9000/user/root/input",
				"hdfs://192.168.192.100:9000/user/root/output" };
		String[] otherArgs = new GenericOptionsParser(conf, ars)
				.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: wordcount <in> <out>");
			System.exit(2);
		}
		conf.set("DICTPATH", DICT_PATH);

		Job job = new Job(conf, "word count");// 新建MapReduce作业
		// And add this statement. XXX
		((JobConf) job.getConfiguration()).setJar(jarFile.toString());

		job.setJarByClass(Kmeans.class);// 设置作业启动类
		job.setMapperClass(KmeansMapper.class);
		//job.setCombinerClass(KmeansReducer.class);
		job.setReducerClass(KmeansReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
