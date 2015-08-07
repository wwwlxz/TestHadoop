package com.lxz.test;

import java.io.IOException;
import java.io.StringReader;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

public class TestFrequence {
	public static class TokenizerMapper extends
			Mapper<LongWritable, Text, Text, IntWritable> {
		// 匹配中文正则表达式
		private static final Pattern PATTERN = Pattern.compile("[\u4e00-\u9fa5]");
		// 记录单词
		private Text word = new Text();
		// 记录出现次数
		private IntWritable singleCount = new IntWritable();

		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			Matcher m = PATTERN.matcher(value.toString());
			//记录一条记录中所有中文
			StringBuilder valueBuilder = new StringBuilder();
			//过滤中文
			while(m.find()){
				String matchkey = m.group();
				valueBuilder.append(matchkey);
			}
			String text = valueBuilder.toString();
			//使用IKAnalyzer工具包分词以及去除停用词
			StringReader retext = new StringReader(text);
			IKSegmenter ikseg = new IKSegmenter(retext, false);
			Lexeme lex =null;
			while((lex = ikseg.next()) != null){
				//在每个单词后面加上对应的所在题号
				this.word.set(lex.getLexemeText() + "@" + key.toString());
				context.write(this.word, this.singleCount);
			}
			valueBuilder.setLength(0);
		}
	}

	public static class IntSumReducer extends
			Reducer<Text, IntWritable, Text, IntWritable> {
		//记录词语出现次数
		private IntWritable wordSum = new IntWritable();
		
		public void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			//统计词语出现次数
			int sum = 0;
			for(IntWritable val : values){
				sum += val.get();
			}
			this.wordSum.set(sum);
			context.write(key, this.wordSum);
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		conf.set("mapred.job.tracker", "192.168.192.100:9001");
		String[] ars = new String[] {
				"hdfs://192.168.192.100:9000/user/root/input_wordfrequence",
				"hdfs://192.168.192.100:9000/user/root/output_wordfrequence" };
		String[] otherArgs = new GenericOptionsParser(conf, ars)
				.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: wordcount  ");
			System.exit(2);
		}
		Job job = new Job(conf, "word count");
		job.setJarByClass(TestFrequence.class);
		job.setMapperClass(TokenizerMapper.class);
		job.setCombinerClass(IntSumReducer.class);
		job.setReducerClass(IntSumReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
