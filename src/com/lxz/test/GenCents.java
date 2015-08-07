package com.lxz.test;

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
public class GenCents {
	private static final String DICT_PATH = "hdfs://192.168.192.100:9000/user/root/input/seqFile.seq";

	public static class TokenizerMapper extends
			Mapper<Object, Text, Text, Text> {

//		private final static IntWritable one = new IntWritable(1);
//		private Text word = new Text();
//		private static Map<String, String> dictWords = new HashMap<String, String>();
//
//		protected void setup(Context context) throws IOException,
//				InterruptedException {// 在map前读取数据
//
//			Configuration conf = context.getConfiguration();
//			Path dictPath = new Path(conf.get("DICTPATH"));// 保存词表
//			// FileSystem fs = FileSystem.get(conf);
//			FileSystem fs = dictPath.getFileSystem(conf);
//
//			SequenceFile.Reader reader1 = new SequenceFile.Reader(fs, dictPath,
//					conf);
//			Text key1 = new Text();
//			Text value1 = new Text();
//			while (reader1.next(key1, value1)) {
//				dictWords.put(key1.toString(), value1.toString());
//			}
//			reader1.close();
//			// System.out.println(dictWords.toString());
//
//			super.setup(context);
//		}

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			// 读取TextFile格式的中心点向量数据，对其进行正则处理后存到另一个文本中
			Map<String, Double> centsTfidf = new HashMap<String, Double>();
			String[] strs = value.toString().split("\t");
			Text num = new Text(strs[0].trim());// 取出题目号

			Pattern p = Pattern.compile("\"([^\"]+)\":([^,]+[^,}])");// 正则匹配取出：单词和TFIDF
			Matcher m = p.matcher(value.toString());
			strs = null;
			while (m.find()) {
				strs = m.group().split(":");
				if (strs.length == 2) {
					centsTfidf.put(strs[0].replace("\"", "").trim(), Double
							.parseDouble(strs[1].replace("\"", "").trim()));
				}
			}
			context.write(num, new Text(centsTfidf.toString()));
		}
	}

	public static class IntSumReducer extends
			Reducer<Text, Text, Text, Text> {

		public void reduce(Text key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			Text text = new Text();
			for(Text value : values){
				text = value;
			}
			context.write(key, text);
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
				"hdfs://192.168.192.100:9000/user/root/input_cents",
				"hdfs://192.168.192.100:9000/user/root/output_cents" };
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

		job.setJarByClass(GenCents.class);// 设置作业启动类
		job.setMapperClass(TokenizerMapper.class);
		job.setCombinerClass(IntSumReducer.class);
		job.setReducerClass(IntSumReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
