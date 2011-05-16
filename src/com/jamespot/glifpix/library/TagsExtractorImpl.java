package com.jamespot.glifpix.library;
/* ----------------------------------------------------------------------------------

 This file is part of GlifPix Tags Extractor.

 GlifPix Tags Extractor is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 GlifPix Tags Extractor is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with GlifPix Tags Extractor.  If not, see <http://www.gnu.org/licenses/>.

 Contact : paul<at>jamespot<dot>com

 ---------------------------------------------------------------------------------- */

import java.io.IOException;
import java.io.StringReader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import com.jamespot.glifpix.index.AbstractStore;
import com.jamespot.glifpix.index.ResourceStore;
import com.jamespot.glifpix.util.Utils;

public class TagsExtractorImpl implements TagsExtractor {

	static public Logger logger = Logger.getLogger(TagsExtractorImpl.class);
	private int _MaxExpressionLength = 15;
	private int _MinWordLength = 1;
	private Properties _props;
	private ContentAnalyzer _contentAnalyzer;
	private Map<String, ResourceStore> _resStores;
	private Map<String, AbstractStore> _abstractStores;
	private Map<String, Set<String>> _lngStopTags;

	public TagsExtractorImpl(Properties props) {
		_props = props;
		_contentAnalyzer = new ContentAnalyzer();
		_resStores = new HashMap<String, ResourceStore>();
		_abstractStores = new HashMap<String, AbstractStore>();
		_lngStopTags = new HashMap<String, Set<String>>();
		if (_props.getProperty("tagLib.maxExpressionLength") != null) {
			_MaxExpressionLength = Integer.parseInt(_props.getProperty("tagLib.maxExpressionLength"));
		}
		if (_props.getProperty("tagLib.minWordLengh") != null) {
			_MinWordLength = Integer.parseInt(_props.getProperty("tagLib.minWordLengh"));
		}
	}
	
	
	public void loadResources() throws CorruptIndexException, LockObtainFailedException, IOException {
		internalLoadResources();
		internalLoadAbstracts();
	}

	public void loadResourcesWithoutAbstracts() throws CorruptIndexException, LockObtainFailedException, IOException {
		internalLoadResources();
	}

	
	
	private void internalLoadResources() throws CorruptIndexException, LockObtainFailedException, IOException {
		String home = _props.getProperty("glifpix.home");
		String resourceBase = home + "/resources";
		String indices = resourceBase + "/indices";

		String[] lngs = _props.getProperty("res.lngs").split(",");

		for (String lng : lngs) {
			if (lng != null && _props.getProperty("res.lng." + lng) != null && _props.getProperty("res.lng." + lng).equals("true")) {
				logger.info("-- Loading " + lng + " resources");
				String indexPath = indices + "/" + lng;
				ResourceStore resStore = ResourceStore.open(indexPath);
				_resStores.put(lng, resStore);
			}
			if (lng != null && _props.getProperty("res.lng." + lng + ".stop") != null) {
				Set<String> stopTags = new HashSet<String>();
				String[] sTags = _props.getProperty("res.lng." + lng + ".stop").split(",");
				for (String tag : sTags) {
					stopTags.add(tag);
				}
				_lngStopTags.put(lng, stopTags);
			}
		}
	}

	private void internalLoadAbstracts() throws IOException {
		String home = _props.getProperty("glifpix.home");
		String resourceBase = home + "/resources";
		String indices = resourceBase + "/indices";

		String[] lngs = _props.getProperty("res.lngs").split(",");

		for (String lng : lngs) {
			if (lng != null && _props.getProperty("res.lng." + lng) != null && _props.getProperty("res.lng." + lng).equals("true")) {
				logger.info("-- Loading " + lng + " abstracts");
				String indexPath = indices + "/" + "abstracts_" + lng;
				AbstractStore absStore = AbstractStore.open(indexPath);
				_abstractStores.put(lng, absStore);
			}
		}
	}

	@Override
	public String[] getTags(String content, String lng, int maxTags) {

		Map<String, Float> tagsFreq = getWeightedTagsFreq(content, lng);
		SortedMap<String, Float> sortedTags = new TreeMap<String, Float>(new MapFloatValueComparer(tagsFreq));
		sortedTags.putAll(tagsFreq);

		int nbTagsToReturn = Math.min(maxTags, sortedTags.size());
		List<String> intermediateList = new Vector<String>();

		int i = 0;
		for (String tag : sortedTags.keySet()) {
			// Verify that a larger tag is not already in the response set
			boolean alreadyIn = false;
			for (int j = 0; j < i; j++) {
				if (intermediateList.get(j).toLowerCase().indexOf(tag.toLowerCase()) != -1)
				{
					alreadyIn = true;
				}
			}

			if (alreadyIn == false) {
				intermediateList.add(tag);
				i++;
			}

			if (i == nbTagsToReturn)
				break;
		}

		String[] toRet = intermediateList.toArray(new String[intermediateList.size()]);
		return toRet;

	}

	public Map<String, Integer> getTagsFreq(String content, String lng) {

		Map<String, Integer> items = new HashMap<String, Integer>();
		TokensArray tokArray = new TokensArray(_MaxExpressionLength);

		TokenStream ts = _contentAnalyzer.tokenStream("dummyField", new StringReader(content));
		TermAttribute termAttribute = ts.addAttribute(TermAttribute.class);

		try {
			while (ts.incrementToken()) {
				tokArray.pushString(termAttribute.term());
				Map<String, Integer> tagCandidates = tokArray.check(_resStores.get(lng).getCRCs(), _lngStopTags.get(lng));

				if (tagCandidates.size() > 0) {
					for (Map.Entry<String, Integer> s : tagCandidates.entrySet()) {
						String tag = _resStores.get(lng).getTag(s.getKey());
						if (tag != null && tag.length() >= _MinWordLength) {
							if (items.containsKey(tag)) {
								items.put(tag, items.get(tag) + s.getValue());
							} else {
								items.put(tag, s.getValue());
							}
						}
					}
				}
			}
			ts.end();
			ts.close();

		} catch (IOException e) {
			logger.error(e);
		}

		return items;
	}

	public Map<String, Float> getWeightedTagsFreq(String content, String lng) {

		Map<String, Float> items = new HashMap<String, Float>();
		TokensArray tokArray = new TokensArray(_MaxExpressionLength);

		TokenStream ts = _contentAnalyzer.tokenStream("dummyField", new StringReader(content));
		TermAttribute termAttribute = ts.addAttribute(TermAttribute.class);

		try {
			while (ts.incrementToken()) {
				tokArray.pushString(termAttribute.term());
				Map<String, Integer> tagCandidates = tokArray.check(_resStores.get(lng).getCRCs(), _lngStopTags.get(lng));

				if (tagCandidates.size() > 0) {
					for (Map.Entry<String, Integer> s : tagCandidates.entrySet()) {
						String tag = _resStores.get(lng).getTag(s.getKey());
						if (tag != null && tag.length() >= _MinWordLength) {
							if (items.containsKey(tag)) {
								items.put(tag, items.get(tag) + (s.getValue().floatValue()) * getTagWeight(s.getKey(), lng));
							} else {
								items.put(tag, (s.getValue().floatValue()) * getTagWeight(s.getKey(), lng));
							}
						}
					}
				}
			}
			ts.end();
			ts.close();

		} catch (IOException e) {
			logger.error(e);
		}

		return items;
	}

	private float getTagWeight(String token, String lng) throws IOException {
		return _abstractStores.get(lng).getTagWeight(token);
	}
	public Set<String> getTokens(String content, String lng) {

		Set<String> tokens = new HashSet<String>();
		TokensArray tokArray = new TokensArray(15);

		TokenStream ts = _contentAnalyzer.tokenStream("dummyField", new StringReader(content));
		TermAttribute termAttribute = ts.addAttribute(TermAttribute.class);

		try {
			while (ts.incrementToken()) {
				tokArray.pushString(termAttribute.term());
				Map<String, Integer> tagCandidates = tokArray.check(_resStores.get(lng).getCRCs(), _lngStopTags.get(lng));

				if (tagCandidates.size() > 0) {
					for (Map.Entry<String, Integer> s : tagCandidates.entrySet()) {
						tokens.add(s.getKey());
					}
				}
			}
			ts.end();
			ts.close();

		} catch (IOException e) {
			logger.error(e);
		}

		return tokens;
	}

	private class crcToken {
		int _size;
		StringBuffer _txt;
		String _crc;

		crcToken() {
			_txt = new StringBuffer("");
			_size = 0;
			_crc = "";
		}

		void push(String s) {
			_size += 1;
			_txt.append('_' + s);
			_crc = Utils.getCRC(getString());
		}
		String getString() {
			return Integer.toString(_size) + _txt.toString();
		}
	}

	private class TokensArray {
		crcToken[] tokens;
		int _size;

		TokensArray(int size) {
			_size = size;
			tokens = new crcToken[size];
			for (int i = 0; i < _size; i++) {
				tokens[i] = null;
			}
		}

		void pushString(String s) {
			for (int i = _size - 1; i != 0; i--) {
				tokens[i] = tokens[i - 1];
			}
			tokens[0] = new crcToken();

			for (int i = 0; i < _size; i++) {
				if (tokens[i] != null) {
					tokens[i].push(s);
				}
			}
		}

		Map<String, Integer> check(Set<Long> crcs, Set<String> stopTags) {
			assert (crcs != null);
			Map<String, Integer> toRet = new HashMap<String, Integer>();
			for (int i = 0; i < _size; i++) {
				if (tokens[i] != null && (!stopTags.contains(tokens[i].getString())) && crcs.contains( Long.parseLong(tokens[i]._crc) )) {
					toRet.put(tokens[i].getString(), (tokens[i]._size) * (tokens[i]._size));
				}
			}

			return toRet;
		}
	}

	private static class MapValueComparer implements Comparator<String> {
		private Map<String, Integer> _data = null;
		public MapValueComparer(Map<String, Integer> data) {
			super();
			_data = data;
		}

		public int compare(String o1, String o2) {
			Integer i1 = _data.get(o1);
			Integer i2 = _data.get(o2);

			if (i1.intValue() != i2.intValue())
				return i2.compareTo(i1);
			else
				return o1.compareTo(o2);
		}
	}

	private static class MapFloatValueComparer implements Comparator<String> {
		private Map<String, Float> _data = null;
		public MapFloatValueComparer(Map<String, Float> data) {
			super();
			_data = data;
		}

		public int compare(String o1, String o2) {
			Float f1 = _data.get(o1);
			Float f2 = _data.get(o2);

			if (f1.floatValue() != f2.floatValue())
				return f2.compareTo(f1);
			else
				return o1.compareTo(o2);
		}
	}

}
