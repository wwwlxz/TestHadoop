package com.lxz.examclusterandrecommend;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import com.lxz.examclustering.DocTool;
import com.lxz.test.EJob;

/**
 * 将中心点向量从hdfs读入内存中，然后将其保存为sequence格式到hdfs中
 * 
 * @author xz
 * 
 */
public class Kmeans {
	// private static final String DICT_PATH =
	// "hdfs://192.168.192.100:9000/user/root/input/seqFile.seq";
	// private static final String HDFS_PATH = "hdfs://192.168.192.100:9000";
	// private static final String DIR_PATH = "";
	// private static final String FILE_PATH = "";
	public static final String CENT_PATH = "hdfs://192.168.192.100:9000/user/root/input_cents/examcents.seq";

	public static class KmeansMapper extends
			Mapper<LongWritable, Text, Text, Text> {
		private static Map<String, Map<String, Double>> centers = new HashMap<String, Map<String, Double>>();
		/*
		 * 将文档中心从hdfs中加载至内存
		 */
		protected void setup(Context context) throws IOException,
				InterruptedException {// 读取中心点向量数据
			Configuration conf = context.getConfiguration();
			Path cents = new Path(CENT_PATH);
			// FileSystem fs = FileSystem.get(conf);
			FileSystem fs = cents.getFileSystem(conf);

			SequenceFile.Reader reader = new SequenceFile.Reader(fs, cents,
					conf);
			Text key = new Text();// 读取题号
			Text value = new Text();// 读取题号对应的单词=TFIDF，单词=TFIDF
			while (reader.next(key, value)) {
				Map<String, Double> tfidfAndword = new HashMap<String, Double>();// 存储TFIDF和单词
				String[] strs = null;
				Pattern p = Pattern.compile("\"([^\"]+)\"=([^,}]+)");// 正则匹配取出：单词和TFIDF
				Matcher m = p.matcher(value.toString());
				while (m.find()) {
					strs = m.group().split("=");
					if (strs.length == 2) {
						tfidfAndword.put(strs[0].replace("\"", "").trim(),
								Double.parseDouble(strs[1].replace("}", "")
										.trim()));
					}
				}
				centers.put(key.toString(), tfidfAndword);
			}
			reader.close();
			super.setup(context);
		}

		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {// 读取题目数据，并将题目数据与中心向量数据进行比对
			String nearestNum = null;

			// 对原始数据进行分割，取出题目编号，单词1：TFIDF值，单词2：TFIDF值
			// 1、取出题目编号
			String[] strs = value.toString().split("\t");
			key = new LongWritable(Long.parseLong(strs[0].trim()));// 取出题目编号

			Map<String, Double> doc = new HashMap<String, Double>();// 读取每个题目编号对应的单词TFIDF向量
			Pattern p = Pattern.compile("\"([^\"]+)\":([^,}]+)");// 正则匹配取出：单词和TFIDF
			Matcher m = p.matcher(value.toString());
			strs = null;
			while (m.find()) {
				strs = m.group().split(":");
				if (strs.length == 2) {
					doc = new HashMap<String, Double>();
					doc.put(strs[0].replaceAll("\"", "").trim(),
							Double.parseDouble(strs[1].trim().replaceAll("\"",
									"")));
				}
			}
			nearestNum = DocTool.returnNearestCentNum(doc, centers);// 计算当前题目与哪个中心点最近，并返回最近的中心点的编号
			context.write(new Text(nearestNum), value);
		}
	}

	public static class KmeansReducer extends Reducer<Text, Text, Text, Text> {
		// 借助词表大小计算value中所有网页向量的均值作为新中心点向量
		private static final DecimalFormat DF = new DecimalFormat(
				"###.########");
		private static Map<String, Long> dictWords = new HashMap<String, Long>();
		private Text cendroidTfidf = new Text();

		/**
		 * 在得到一个文档集合后重新计算这个集合的中心
		 * 
		 * @param context
		 *            输入 key:文档中心 value:属于文档中心所在类的文档<文档1，文档2...文档n> 输出
		 *            key:新的文档中心 value:文档中心对应的文档
		 */
		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			// 首先取出中心点向量的每个单词及其对应的TFIDF值，values中存放的数据形式是[单词=TFIDF，单词=TFIDF...][单词=TFIDF，单词=TFIDF...]...
			// 1、对读取的values进行单词TFIDF切割
			int belongNum = 0;// 存储归属该中心点的题目的个数
			Map<String, Double> wordAndtfidf = new HashMap<String, Double>();// 存储单词和对应的tfidf
			for (Text var : values) {
				Pattern p = Pattern.compile("\"([^\"]+)\":([^,}]+)");// 正则匹配取出：单词和TFIDF
				Matcher m = p.matcher(var.toString());
				String[] strs = null;
				while (m.find()) {
					strs = m.group().split(":");
					if (strs.length == 2) {
						if (wordAndtfidf.containsKey("\""
								+ strs[0].replaceAll("\"", "").trim() + "\"")) {// 判断该单词是否曾经出现过
							Double sum = wordAndtfidf.get("\""
									+ strs[0].replaceAll("\"", "").trim()
									+ "\"")
									+ Double.parseDouble(strs[1].trim()
											.replaceAll("\"", ""));
							wordAndtfidf.put("\""
									+ strs[0].replaceAll("\"", "").trim()
									+ "\"", sum);
						} else {
							wordAndtfidf.put("\""
									+ strs[0].replaceAll("\"", "").trim()
									+ "\"", Double.parseDouble(strs[1].trim()
									.replaceAll("\"", "")));
						}
					}
				}
				belongNum++;
			}
			Set set = wordAndtfidf.entrySet();
			Iterator itr = set.iterator();
			// List<String> rm = new ArrayList<>();
			while (itr.hasNext()) {
				Map.Entry<String, Double> entry1 = (Map.Entry<String, Double>) itr
						.next();
				if (entry1.getValue() / belongNum < 10E-4) {
					itr.remove();
					wordAndtfidf.remove(entry1.getKey());
					// rm.add(entry1.getKey());
				} else {
					wordAndtfidf.put(entry1.getKey(), entry1.getValue()
							/ belongNum);
				}
			}

			context.write(key, new Text(wordAndtfidf.toString()));
		}
	}

	public static class LastKmeansMapper extends
			Mapper<LongWritable, Text, LongWritable, Text> {
		private static Map<String, Map<String, Double>> centers = new HashMap<String, Map<String, Double>>();

		/*
		 * 将用户中心从hdfs中加载至内存 数据格式:用户编号 题号:值@题号:值@题号:值
		 */
		protected void setup(Context context) throws IOException,
				InterruptedException {// 读取中心点向量数据
			Configuration conf = context.getConfiguration();
			Path cents = new Path(CENT_PATH);
			// FileSystem fs = FileSystem.get(conf);
			FileSystem fs = cents.getFileSystem(conf);

			SequenceFile.Reader reader = new SequenceFile.Reader(fs, cents,
					conf);
			Text key = new Text();// 读取用户编号
			Text value = new Text();// 读取用户编号对应的题号=值，题号=值
			while (reader.next(key, value)) {
				Map<String, Double> qidAndvalue = new HashMap<String, Double>();// 存储题号和值
				// String[] strs = null;
				// 题号:值@题号:值@题号:值
				String[] strs = value.toString().split("@");
				for (int i = 0; i < strs.length; i++) {
					qidAndvalue.put(strs[i].split(":")[0].trim(), 1.0);
					// qidAndvalue.put(strs[i].split(":")[0].trim(),
					// Double.parseDouble(strs[i].split(":")[1].trim()));
				}
				centers.put(key.toString(), qidAndvalue);
			}
			reader.close();

			super.setup(context);
		}

		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {// 读取题目数据，并将题目数据与中心向量数据进行比对
			String nearestNum = null;

			// 对原始数据进行分割，取出用户编号，题号1@题号2@题号3
			// 1、取出用户编号
			String[] strs = value.toString().split("\t");
			key = new LongWritable(Long.parseLong(strs[0].trim()));// 取出用户编号

			Map<String, Integer> doc = new HashMap<String, Integer>();// 读取每个用户编号对应的题号向量
			String[] qids = strs[1].split("@");
			for (int i = 0; i < qids.length; i++) {
				doc.put(qids[i], 1);
			}
			nearestNum = UserTool.returnNearestCentNum(doc, centers);// 计算当前用户与哪个中心点最近，并返回最近的中心点的编号
			context.write(key, new Text(nearestNum));
		}
	}

	public static class LastKmeansReducer extends
			Reducer<LongWritable, Text, Text, Text> {

		public void reduce(LongWritable key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			super.reduce(key, values, context);
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
				"hdfs://192.168.192.100:9000/user/root/input_examuser",
				"hdfs://192.168.192.100:9000/user/root/output_examuser" };
		String[] otherArgs = new GenericOptionsParser(conf, ars)
				.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: wordcount <in> <out>");
			System.exit(2);
		}
		// conf.set("DICTPATH", DICT_PATH);

		Job job = new Job(conf, "word count");// 新建MapReduce作业
		// And add this statement. XXX
		((JobConf) job.getConfiguration()).setJar(jarFile.toString());

		job.setJarByClass(Kmeans.class);// 设置作业启动类

		// job.setMapperClass(KmeansMapper.class);
		// // job.setCombinerClass(IntSumReducer.class);
		// job.setReducerClass(KmeansReducer.class);
		// job.setOutputKeyClass(Text.class);
		// job.setOutputValueClass(Text.class);

		job.setMapperClass(LastKmeansMapper.class);
		// job.setReducerClass(LastKmeansReducer.class);
		job.setOutputKeyClass(LongWritable.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}