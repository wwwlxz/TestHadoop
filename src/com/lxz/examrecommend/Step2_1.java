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
 * 生成同现矩阵，并将其存储为sequenceFile格式
 */
public class Step2_1 {
	/*
	 * 读取两张表中的数据，这两张表中的数据格式分别如下所示：
	 * 表1 用户项目评分表 
	 * 1 101,5.0 
	 * 1 102,3.0 
	 * 1 103,2.5 
	 * 2 101,2.0 
	 * ... 
	 * 表2 项目同现矩阵 
	 * 101:101 5 
	 * 101:102 3 
	 * 101:103 4 
	 * 101:104 4 
	 * 101:105 2
	 * 101:106 2 
	 * 101:107 1 
	 * ... 
	 * 输出格式为： 
	 * 1@ 101,5.0:102,3.0:103,2.5 
	 * ... 
	 * 101 101,5:102,3:103,4:105,2:106,2:107,1 ...
	 */
	public static class Step2_1Mapper extends
			Mapper<LongWritable, Text, Text, Text> {
		private Text coOccurrence_key = null;
		private Text coOccurrence_value = null;

		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			// 将行按制表符分割
			String[] strs = value.toString().split("\t");
			// 判断读取的行为同现矩阵还是评分矩阵
			String[] keys = strs[0].split(":");
			coOccurrence_key = new Text(keys[0]);// 将项目编号存到key中
			coOccurrence_value = new Text(keys[1] + "," + strs[1]);// 将项目编号及出现次数存到value中
			context.write(coOccurrence_key, coOccurrence_value);

		}
	}

	public static class Step2_1Reducer extends Reducer<Text, Text, Text, Text> {
		public static final String HDFS_PATH = "hdfs://192.168.192.100:9000";
		public static final String SEQFILE_PATH = "/user/root/output/examcooccurrence.seq";
		Map<String, String> map = new HashMap<String, String>();
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			String str = "";
			for (Text val : values) {
				str = str + val.toString() + ":";
			}
			map.put(key.toString(), str);
			context.write(key, new Text(str));
		}
		
		protected void cleanup(Context context)throws IOException,
				InterruptedException{
			//将内存中的文本数据转为sequenceText数据存储到hdfs中
			Configuration conf = new Configuration();
			try {
				FileSystem fs = FileSystem.get(new URI(HDFS_PATH), conf);
				Path seqFile = new Path(SEQFILE_PATH);
				//Writer内部类用于文件的写操作，假设key和value都为Text类型
				SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf, seqFile, Text.class, Text.class);
				//通过writer向文档中写入记录
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
				"hdfs://192.168.192.100:9000/user/root/output_examstep2",
				"hdfs://192.168.192.100:9000/user/root/output_examstep2_1" };
		String[] otherArgs = new GenericOptionsParser(conf, ars)
				.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: wordcount <in> <out>");
			System.exit(2);
		}

		Job job = new Job(conf, "word count");
		// And add this statement. XXX
		((JobConf) job.getConfiguration()).setJar(jarFile.toString());

		job.setJarByClass(Step2_1.class);
		job.setMapperClass(Step2_1Mapper.class);
		// job.setCombinerClass(Step2_1Reducer.class);
		job.setReducerClass(Step2_1Reducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
