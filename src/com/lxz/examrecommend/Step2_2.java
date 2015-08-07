package com.lxz.examrecommend;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

/*
 * 生成同现矩阵，并将其存储为sequenceFile格式
 */
public class Step2_2 {
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
		private Text userVector_key = null;
		private Text userVector_value = null;

		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			// 将行按制表符分割
			String[] strs = value.toString().split("\t");
			// 判断读取的行为同现矩阵还是评分矩阵
			userVector_key = new Text(strs[0] + "@");
			userVector_value = new Text(strs[1]);
			context.write(userVector_key, userVector_value);

		}
	}

	public static class Step2_1Reducer extends Reducer<Text, Text, Text, Text> {
		public static final String COOCCURRENCE_PATH = "hdfs://192.168.192.100:9000/user/root/output/examcooccurrence.seq";
		private static Map<String, Map<String, Integer>> cooccurrences = new HashMap<String, Map<String, Integer>>();
		/*
		 * 将同现矩阵从hdfs中加载至内存
		 */
		protected void setup(Context context) throws IOException,
				InterruptedException{//取出同现矩阵
			Configuration conf = context.getConfiguration();
			Path path = new Path(COOCCURRENCE_PATH);
			FileSystem fs = path.getFileSystem(conf);
			
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf);
			Text key = new Text();
			Text value = new Text();
			while(reader.next(key, value)){
				Map<String, Integer> itemScores = new HashMap<String, Integer>();
				String[] strs = value.toString().split(":");
				for(int i = 0; i<strs.length ; i++){
					itemScores.put(strs[i].split(",")[0], Integer.parseInt(strs[i].split(",")[1]));
				}
				cooccurrences.put(key.toString(), itemScores);
			}
			reader.close();
			super.setup(context);
		}
		
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {//同现矩阵*评分矩阵
			Map<String, Double> itemSumScores = new HashMap<String, Double>();//保存用户推荐项和推荐项的总评分
			Set co = cooccurrences.keySet();
			String value = "";
			for(Text val : values){
				value  = value + val.toString() + ":";//102,3.0:103,2.5:101,5.0:
			}
			for(Iterator iter = co.iterator(); iter.hasNext();){//计算用户对每项的评分 取出101 101,5:102,3:103,4:105,2:106,2:107,1
				String item = (String) iter.next();//101
				Map<String, Integer> itemNums = (Map<String, Integer>) cooccurrences.get(item);//项目编号和出现次数 101,5:102,3:103,4:105,2:106,2:107,1
				String[] itemScores = value.split(":");//切割出用户项和用户对项的评分 102,3.0:103,2.5:101,5.0:
				for(int i=0; i<itemScores.length; i++){
					if(itemNums.containsKey(itemScores[i].split(",")[0])){//判断用户评分项是否在同现矩阵中，如果不在表示该值在同现矩阵中出现次数为0
						if(itemSumScores.containsKey(item)){//判断101是否已被读过
							Double score = itemSumScores.get(item);
							score = score + itemNums.get(itemScores[i].split(",")[0])*Double.parseDouble(itemScores[i].split(",")[1]);
							itemSumScores.put(item, score);
						}else{
							Double score = itemNums.get(itemScores[i].split(",")[0])*Double.parseDouble(itemScores[i].split(",")[1]);
							itemSumScores.put(item, score);
						}
					}
				}
			}
			String text = "";
			Set set = itemSumScores.keySet();
			for(Iterator iter = set.iterator(); iter.hasNext();){
				String k = (String) iter.next();
				Double v = (Double) itemSumScores.get(k);
				text = text + k + "," + v + ":";
			}
			context.write(key, new Text(text));
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
				"hdfs://192.168.192.100:9000/user/root/output_datawash",
				"hdfs://192.168.192.100:9000/user/root/output_examstep2_2" };
		String[] otherArgs = new GenericOptionsParser(conf, ars)
				.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: wordcount <in> <out>");
			System.exit(2);
		}

		Job job = new Job(conf, "word count");
		// And add this statement. XXX
		((JobConf) job.getConfiguration()).setJar(jarFile.toString());

		job.setJarByClass(Step2_2.class);
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
