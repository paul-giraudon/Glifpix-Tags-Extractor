package com.jamespot.glifpix.index;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;

public class ArrayTokenStream extends TokenStream {

	String[] strings;
	int currentPos;
	
	
	@Override
	public boolean incrementToken() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}
	

}
