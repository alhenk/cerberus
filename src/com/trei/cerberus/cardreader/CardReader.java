package com.trei.cerberus.cardreader;

public abstract class CardReader {
	
	public CardReader(){
		super();
	}
	
	public abstract Object openById() throws CardReaderException;
	public abstract void close() throws CardReaderException;
	
	public abstract Report getUID() throws CardReaderException;

}

