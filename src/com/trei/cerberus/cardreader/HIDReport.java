package com.trei.cerberus.cardreader;

public class HIDReport extends Report{
	//private final int hidReportSize;
	private int status;
	private Code code;
	//private char[] data;
	private String data;
	
	/**
	 * Default constructor
	 */
	public HIDReport(){
		//hidReportSize = 64; 
		//data = new char[hidReportSize * 2 + 1];
	}
	/*
	public HIDReport(int hidReportSize){
		this.hidReportSize = hidReportSize; 
		//data = new char[hidReportSize * 2 + 1];
	}*/

	public void setStatus(int status){this.status = status;}
	public void setCode(Code code){this.code = code;}
	public void setData(String data){this.data = data;}

    public int  getStatus(){return status;}
	public Code getCode(){return code;}
	public String getData(){return data;}
}
