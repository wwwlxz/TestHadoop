package com.lxz.hdfs;

import java.io.DataInputStream;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

public class SequenceFileTest {
	public static final String HDFS_PATH = "hdfs://192.168.192.100:9000";
	public static final String FILE_PATH = "/user/root/input/file1.txt";
	public static final String SEQFILE_PATH = "/user/root/output/abc.seq";

	public static void main(String[] args) throws Exception {
		//从hdfs中读取文本数据到内存中
		final FileSystem fileSystem = FileSystem.get(new URI(HDFS_PATH),new Configuration());
		final FSDataInputStream in = fileSystem.open(new Path(FILE_PATH));
		String data = in.readLine();
		//IOUtils.copyBytes(in, System.out, 1024, true);//将读取的数据输出到控制台打印出来
		
		//将内存中的文本数据转为sequenceText数据存储到hdfs中
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(new URI(HDFS_PATH), conf);
//		Path seqFile = new Path("seqFile.seq");
		Path seqFile = new Path(SEQFILE_PATH);
		// Writer内部类用于文件的写操作，假设key和value都为Text类型
		SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf, seqFile,
				Text.class, Text.class);
		// 通过writer向文档中写入记录
		writer.append(new Text("key"), new Text(data));
		IOUtils.closeStream(writer);// 关闭write流
		
		// Reader内部类用于文件的读取操作
//		SequenceFile.Reader reader = new SequenceFile.Reader(fs, seqFile, conf);
//		// 通过reader从文档中读取记录
//		Text key = new Text();
//		Text value = new Text();
//		while (reader.next(key, value)) {
//			System.out.println(key);
//			System.out.println(value);
//		}
//		IOUtils.closeStream(reader);// 关闭read流
	}
}
