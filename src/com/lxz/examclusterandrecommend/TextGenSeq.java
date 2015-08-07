package com.lxz.examclusterandrecommend;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

/**
 * 将中心点向量从hdfs读入内存中，然后将其保存为sequence格式到hdfs中
 * 
 * @author xz
 * 
 */
public class TextGenSeq {
//	private static final String DICT_PATH = "hdfs://192.168.192.100:9000/user/root/input/seqFile.seq";
//	public static final String HDFS_PATH = "hdfs://192.168.192.100:9000";
//	public static final String SEQFILE_PATH = "/user/root/input_cents/cents.seq";
	public static final String CENT_PATH = "/user/root/input_cents/examcents.seq";

	public static class TokenizerMapper extends
			Mapper<Object, Text, Text, Text> {

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			//输入数据形式:用户编号	试题编号:值@试题编号:值@试题编号:值@
			String[] strs = value.toString().split("\t");
			Text num = new Text(strs[0].trim());// 取出用户编号
			Text qidAndPoint = new Text(strs[1].trim());
			context.write(num, qidAndPoint);
		}
	}

	public static class IntSumReducer extends
			Reducer<Text, Text, Text, Text> {
		private static Map<String, String> qidAndPoint = new HashMap<String, String>();
		
		public void reduce(Text key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			Text text = new Text();
			for(Text value : values){
				text = value;
			}
			qidAndPoint.put(key.toString().trim(), text.toString());
			context.write(key, text);
		}
		
		protected void cleanup(Context context) throws IOException, InterruptedException{
			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(conf);
			Path centorPath = new Path(CENT_PATH);
			fs.delete(centorPath, true);
			final SequenceFile.Writer out = SequenceFile.createWriter(fs, conf, centorPath, Text.class, Text.class);
			Iterator<Entry<String, String>> iterator = qidAndPoint.entrySet().iterator();
			Text allqid = new Text();
			while(iterator.hasNext()){
				Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator.next();
				allqid.set(entry.getValue());
				out.append(new Text(entry.getKey()), allqid);
			}
			out.close();
			super.cleanup(context);
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
				"hdfs://192.168.192.100:9000/user/root/input_examcents",
				"hdfs://192.168.192.100:9000/user/root/output_examcents" };
		String[] otherArgs = new GenericOptionsParser(conf, ars)
				.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: wordcount <in> <out>");
			System.exit(2);
		}

		Job job = new Job(conf, "word count");// 新建MapReduce作业
		// And add this statement. XXX
		((JobConf) job.getConfiguration()).setJar(jarFile.toString());

		job.setJarByClass(TextGenSeq.class);// 设置作业启动类
		job.setMapperClass(TokenizerMapper.class);
		//job.setCombinerClass(IntSumReducer.class);
		job.setReducerClass(IntSumReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
