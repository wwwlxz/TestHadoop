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
 * 将用户试题数据从sequenceText中读出，并将其与同现矩阵相乘计算推荐结果
 */
public class New_Step2_2 {
	public static class New_Step2_2_Mapper extends
			Mapper<LongWritable, Text, Text, Text> {
		/*
		 * 数据输入形式如下：
		 * itemi:itemj	出现次数
		 * itemk:itemh	出现次数d
		 * 数据输出形式如下：
		 * itemi	itemj:出现次数@itemk:出现次数@
		 * itemk	itemh:出现次数@itemm:出现次数@
		 */
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			// 将行按制表符分割
			String[] strs = value.toString().split("\t");//itemi:itemj	出现次数
			String collaborative_key = null;
			String collaborative_value = null;
			//处理同现矩阵
			collaborative_key = strs[0].split(":")[0];///itemi:itemj
			collaborative_value = strs[0].split(":")[1] + ":" + strs[1];
			context.write(new Text(collaborative_key), new Text(collaborative_value));//itemi	itemj:出现次数
		}
	}

	public static class New_Step2_2_Reducer extends Reducer<Text, Text, Text, Text> {
		public static final String USER_PATH = "hdfs://192.168.192.100:9000/user/root/output/examuser.seq";
		private static Map<String, Map<String, Integer>>  users= new HashMap<String, Map<String, Integer>>();
		/*
		 * 将用户试题矩阵从hdfs中加载至内存
		 */
		protected void setup(Context context) throws IOException,
				InterruptedException{//取出同现矩阵
			Configuration conf = context.getConfiguration();
			Path path = new Path(USER_PATH);
			FileSystem fs = path.getFileSystem(conf);
			
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf);
			Text key = new Text();
			Text value = new Text();
			while(reader.next(key, value)){
				Map<String, Integer> itemScores = new HashMap<String, Integer>();
				String[] strs = value.toString().split("@");//55119@55120@33316@54469@
				for(int i = 0; i<strs.length ; i++){
					itemScores.put(strs[i], 1);
				}
				users.put(key.toString(), itemScores);//用户编号	试题编号：1@试题编号：1@试题编号：1@
			}
			reader.close();
			super.setup(context);
		}
		
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {//同现矩阵*评分矩阵
			/*
			 * 同现矩阵
			 * itemi	itemj:出现次数@itemk:出现次数@
			 * itemk	itemh:出现次数@itemm:出现次数@
			 * 评分矩阵
			 * 用户编号	试题编号：1@试题编号：1@试题编号：1@
			 */
			//同现矩阵的一行
			Map<String, Integer> itemAndNum = new HashMap<String, Integer>();//保存用户同现矩阵中错题和错题出错次数 
			Map<String, Integer> userSumScores = new HashMap<String, Integer>();//保存用户推荐项和推荐项的总评分
			String str = "";
			for(Text val : values){//itemj:出现次数
				itemAndNum.put(val.toString().split(":")[0], Integer.parseInt(val.toString().split(":")[1]));
			}
			
			//所有用户错题矩阵乘以同现矩阵的一行
			Set user = users.keySet();
			for(Iterator iter1 = user.iterator(); iter1.hasNext();){//取出一个用户
				String userNo = (String) iter1.next();//用户编号
				Map<String, Integer> itemScores = (Map<String, Integer>) users.get(userNo);//该用户对应的 试题编号：1@试题编号：1@试题编号：1@
				Set itemScore = itemScores.keySet();
				for(Iterator iter2 = itemScore.iterator(); iter2.hasNext();){
					String itemNo = (String) iter2.next();//试题编号
					if(itemAndNum.containsKey(itemNo)){//判断用户错题和同现矩阵的一行是否有相同的试题编号
						if(userSumScores.containsKey(userNo)){//判断该试题是否存在过
							Integer score = userSumScores.get(userNo);
							score = score + itemAndNum.get(itemNo);
							userSumScores.put(userNo, score);
						}else{
							Integer score = itemAndNum.get(itemNo);
							userSumScores.put(userNo, score);
						}
					}
				}
			}
			String text = "";
			Set set = userSumScores.keySet();
			for(Iterator iter = set.iterator(); iter.hasNext();){
				String k = (String) iter.next();
				Integer v = (Integer) userSumScores.get(k);
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
				"hdfs://192.168.192.100:9000/user/root/output_examnewstep2",
				"hdfs://192.168.192.100:9000/user/root/output_examnewstep2_2" };
		String[] otherArgs = new GenericOptionsParser(conf, ars)
				.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: wordcount <in> <out>");
			System.exit(2);
		}

		Job job = new Job(conf, "word count");
		// And add this statement. XXX
		((JobConf) job.getConfiguration()).setJar(jarFile.toString());

		job.setJarByClass(New_Step2_2.class);
		job.setMapperClass(New_Step2_2_Mapper.class);
		// job.setCombinerClass(Step2_1Reducer.class);
		job.setReducerClass(New_Step2_2_Reducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
