package com.lxz.hdfs;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class TestHDFS {
	static Configuration conf = new Configuration();
	static FileSystem hdfs;
	static{
		String path = "/home/hadoop/hadoop-1.0.0/conf";
		conf.addResource(new Path(path + "core-stie.xml"));
		conf.addResource(new Path(path + "hdfs-site.xml"));
		conf.addResource(new Path(path + "mapred-site.xml"));
		try{
			hdfs = FileSystem.get(conf);
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void createDir(String dir) throws IOException{
		Path path = new Path(dir);
		hdfs.mkdirs(path);
		System.out.println("new dir \t" + conf.get("fs.default.name") + dir);
	}
	
	public static void main(String[] args) throws IOException{
		TestHDFS hdfs = new TestHDFS();
		System.out.println("\n==========create dir===========");
		String dir = "/test";
		hdfs.createDir(dir);
	}
}
