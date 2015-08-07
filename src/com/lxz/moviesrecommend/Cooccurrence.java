package com.lxz.moviesrecommend;

public class Cooccurrence {
	private String itemID1;
	private String itemID2;
	private int num;
	
	public Cooccurrence(String itemID1,String itemID2,int num){
		super();
		this.itemID1 = itemID1;
		this.itemID2 = itemID2;
		this.num = num;
	}
	
	public String getItemID1(){
		return itemID1;
	}
	
	public void setItemID1(String itemID1){
		this.itemID1 = itemID1;
	}
	
	public String getItemID2(){
		return itemID2;
	}
	
	public void setItemID2(String itemID2){
		this.itemID2 = itemID2;
	}
	
	public int getNum(){
		return num;
	}
	
	public void setNum(int num){
		this.num = num;
	}
}
