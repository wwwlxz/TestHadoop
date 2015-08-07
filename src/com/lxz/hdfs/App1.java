package com.lxz.hdfs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

public class App1 {
	public static final String HDFS_PATH = "hdfs://192.168.192.100:9000";
	public static final String DIR_PATH = "abc";
	public static final String FILE_PATH = "abc/haha";
	
	public static void main(String[] args) throws Exception{
		
		final FileSystem fileSystem = FileSystem.get(new URI(HDFS_PATH),new Configuration());
		//创建文件夹
		makeDirectory(fileSystem);
		
		//上传文件
		uploadData(fileSystem);
		
		//下载文件
		downloadData(fileSystem);
		
		//删除文件
		deleteFile(fileSystem);
	}

	private static void downloadData(final FileSystem fileSystem)
			throws IOException {
		final FSDataInputStream in = fileSystem.open(new Path(FILE_PATH));
		IOUtils.copyBytes(in, System.out, 1024, true);
	}

	private static void deleteFile(final FileSystem fileSystem)
			throws IOException {
		fileSystem.delete(new Path(FILE_PATH), true);
	}

	private static void uploadData(final FileSystem fileSystem)
			throws IOException, FileNotFoundException {
		final FSDataOutputStream out = fileSystem.create(new Path(FILE_PATH));
		final FileInputStream in = new FileInputStream("d://file1.txt");
		IOUtils.copyBytes(in, out , 1024, true);
	}

	private static void makeDirectory(final FileSystem fileSystem)
			throws IOException {
		fileSystem.mkdirs(new Path(DIR_PATH));
	}
}
