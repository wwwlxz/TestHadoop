package com.lxz.ikanalyzer;

import java.io.IOException;
import java.io.StringReader;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

public class Test001 {
	public static void main(String[] args) throws IOException{
		String text = "基于java语言开发的轻量级的中文分词工具包";
		StringReader sr = new StringReader(text);
		IKSegmenter ik = new IKSegmenter(sr, true);
		Lexeme lex = null;
		while((lex = ik.next()) != null){
			System.out.print(lex.getLexemeText() + "|");
		}
	}
}
