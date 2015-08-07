package com.lxz.ikanalyzer;

import java.io.IOException;
import java.io.StringReader;

import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.cfg.DefaultConfig;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

public class Test002 {
	public static void main(String[] args) throws IOException{
//		String keyWords = "2012年那个欧洲杯四强赛";
//		InputStreamReader isr = new InputStreamReader(new FileInputStream(new File("")));
//		IKSegmenter ikSegmenter = new IKSegmenter(isr, true);
//		Lexeme lexeme = null;
//		while((lexeme = ikSegmenter.next()) != null){
//			System.out.println(lexeme.getLexemeText());
//		}
		
		//String text = "老爹我们都爱您！";
		String text = "在百度的众多排名算法中，其中有一项是停用词。百度爬虫为了能够提高索引的速度和节省存储空间，当在索引页面内容时会对一些没有意义的词过过滤掉。也就是被爬虫停用到这些词，我们称被爬虫停掉的词称为停用词，英文叫stopword";
		//String text = "停止词,是由英文单词:stopword翻译过来的,原来在英语里面会遇到很多a,the,or等使用频率很多的字或词,常为冠词、介词、副词或连词等。如果搜索引擎要将这些词都...";
		org.wltea.analyzer.cfg.Configuration configuration = DefaultConfig.getInstance();
		configuration.setUseSmart(true);
		IKSegmenter ik = new IKSegmenter(new StringReader(text), configuration);
		Lexeme lexeme = null;
		while((lexeme = ik.next()) != null){
			System.out.println(lexeme.getLexemeText());
		}
	}
}
