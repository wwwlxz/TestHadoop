package com.lxz.examrecommend;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

/*
 * 生成用户评分矩阵，并将其存储为sequenceText格式
 */
public class New_Step2_1 {
	/*
	 * 将用户试题数据存成sequenceText格式 
	 * 数据输入形式: 
	 * 14235 55119@55120@33316@54469@36351@
	 * 17413 47223@33568@33569@33570@33577@
	 */
	public static class New_Step2_1_Mapper extends
			Mapper<LongWritable, Text, Text, Text> {
		public static final String HDFS_PATH = "hdfs://192.168.192.100:9000";
		public static final String SEQFILE_PATH = "/user/root/output/examuser.seq";
		Map<String, String> map = new HashMap<String, String>();


		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			// 将行按制表符分割
			String[] strs = value.toString().split("\t");
			String user_key = "";
			String user_value = "";
			user_key = strs[0];
			user_value = strs[1];
			map.put(user_key, user_value);
			context.write(new Text(user_key), new Text(user_value));
		}

		protected void cleanup(Context context) throws IOException,
				InterruptedException {
			// 将内存中的文本数据转为sequenceText数据存储到hdfs中
			Configuration conf = new Configuration();
			try {
				FileSystem fs = FileSystem.get(new URI(HDFS_PATH), conf);
				Path seqFile = new Path(SEQFILE_PATH);
				// Writer内部类用于文件的写操作，假设key和value都为Text类型
				SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf,
						seqFile, Text.class, Text.class);
				// 通过writer向文档中写入记录
				Set set = map.keySet();
				for (Iterator iter = set.iterator(); iter.hasNext();) {
					String key = (String) iter.next();
					String value = (String) map.get(key);
					writer.append(new Text(key), new Text(value));
				}
				IOUtils.closeStream(writer);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
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
				"hdfs://192.168.192.100:9000/user/root/output_examnewstep2_1" };
		String[] otherArgs = new GenericOptionsParser(conf, ars)
				.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: wordcount <in> <out>");
			System.exit(2);
		}

		Job job = new Job(conf, "word count");
		// And add this statement. XXX
		((JobConf) job.getConfiguration()).setJar(jarFile.toString());

		job.setJarByClass(New_Step2_1.class);
		job.setMapperClass(New_Step2_1_Mapper.class);
		// job.setCombinerClass(Step2_1Reducer.class);
		//job.setReducerClass(New_Step2_1_Reducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
