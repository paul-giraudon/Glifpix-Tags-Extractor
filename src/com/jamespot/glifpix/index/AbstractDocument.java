package com.jamespot.glifpix.index;

import java.io.IOException;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;

import com.jamespot.glifpix.library.TagsExtractorImpl;

public class AbstractDocument extends StoredDocument{
	
	private AbstractDocument() {
		super();
	}

	public static AbstractDocument create(TagsExtractorImpl tagsExtractor, String content, String lng) throws IOException, NoSuchAlgorithmException {
		AbstractDocument toRet = new AbstractDocument();
		toRet.addContentField(tagsExtractor, content, lng);
		return toRet;
	}

	private void addContentField(TagsExtractorImpl tagsExtractor, String content, String lng)
	{
		String coolcontent = content.replaceAll("\\\"", "");
		coolcontent = replaceUnicodeStr(coolcontent);
		
		Set<String> tags = tagsExtractor.getTokens(coolcontent, lng);
		
		StringBuffer tagText = new StringBuffer();
		if (tags != null)
		{
			for (String tag : tags)
			{
				tagText.append(" "+tag);
			}
				
		}
		WhitespaceTokenizer tokenizer = new WhitespaceTokenizer(new StringReader(tagText.toString()));
		_luceneDocument.add(new Field("content", tokenizer));
	}
	
	public static float getTagWeight(IndexReader ir, String token) throws IOException
	{
		TermEnum te = ir.terms(new Term("content", token));
		Term term = te.term();
		if ((term != null) && term.field().equalsIgnoreCase("content") && term.text().equalsIgnoreCase(token)) {
			if (ir.docFreq(term) > 0)
			{
				double numDocs = Math.log(ir.numDocs());
				double termFreq = Math.log(ir.docFreq(term));
				return (float)( 1 - (termFreq / numDocs));
			}
			
		}
		return (float)(((double)1.0) / ((double)ir.numDocs()));
		
	}

	
}
