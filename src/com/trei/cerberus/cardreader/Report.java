package com.trei.cerberus.cardreader;

public abstract class Report {
	Report(){
		super();
	}
	
	public abstract int  getStatus();
	public abstract Code getCode();
	public abstract String getData();
	
}
