import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class SearchEngine implements ISearchEngine {
	
	private BTree<String, HashMap<String, Integer>> database;
	private DocumentBuilder builder;
	
	public SearchEngine(int minDegree) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.out.println("Error creating XML parser!");
		}
		database = new BTree<> (minDegree);
	}
	
	/*
	 * k => total number of words in all documents
	 * Time Complexity: O(klogk)
	 */
	@Override
	public void indexWebPage(String filePath) {
		File xml = new File(filePath);
		if(xml == null || xml.isDirectory()) {
			System.out.println("The entered path is of a directory not a file or doesn't exist!");
			return;
		}
		try {
			Document doc = builder.parse(xml);
			doc.getDocumentElement().normalize();
			NodeList tags = doc.getElementsByTagName("doc");
			for(int i = 0; i < tags.getLength(); i++) {
				Element document = (Element) tags.item(i);
				String id = document.getAttribute("id");
				String[] words = document.getTextContent().replace("\n", " ").split(" ");
				HashMap<String, Integer> wordMap = new HashMap<> ();
				for(String word : words) {
					word = word.toLowerCase();
					int freq = 0;
					if(wordMap.containsKey(word)) freq = wordMap.get(word);
					wordMap.put(word, freq+1);
				}
				wordMap.remove("");
				for(String word : wordMap.keySet()) {
					HashMap<String, Integer> wordVal = database.search(word);
					if(wordVal == null) {
						wordVal = new HashMap<> ();
						wordVal.put(id, wordMap.get(word));
						database.insert(word, wordVal);
					}
					else {
						wordVal.put(id, wordMap.get(word));
						database.updateInserted(word, wordVal);
					}
				}
			}
			
		} catch (SAXException | IOException e) {
			System.out.println("Error accessing or parsing xml file");
		}
	}
	/*
	 * n => number of files
	 * k => total number of words in all documents in each file
	 * Time Complexity: O(nklogk)
	 */
	@Override
	public void indexDirectory(String filePath) {
		File file = new File(filePath);
		if(file == null || !file.isDirectory()) {
			System.out.println("File doesn't exist or not a directory");
			return;
		}
		for(File subFile : file.listFiles()) {
			if(subFile.isDirectory()) indexDirectory(subFile.getPath());
			else indexWebPage(subFile.getPath());
		}
	}
	
	/*
	 * k => total number of words in all documents
	 * Time Complexity: O(klogk)
	 */
	@Override
	public void deleteWebPage(String filePath) {
		File xml = new File(filePath);
		if(xml == null || xml.isDirectory()) {
			System.out.println("The entered path is of a directory not a file or doesn't exist!");
			return;
		}
		try {
			Document doc = builder.parse(xml);
			NodeList tags = doc.getElementsByTagName("doc");
			for(int i = 0; i < tags.getLength(); i++) {
				Element document = (Element) tags.item(i);
				String id = document.getAttribute("id");
				String[] words = document.getTextContent().replace("\n", " ").split(" ");
				for(String word : words) {
					word = word.toLowerCase();
					HashMap<String, Integer> searchRes = database.search(word);
					if(searchRes != null && searchRes.containsKey(id)) {
						searchRes.remove(id);
						if(searchRes.isEmpty()) database.delete(word);
						else database.updateInserted(word, searchRes);
					}
				}
			}
		} catch (SAXException | IOException e) {
			System.out.println("Error accessing or parsing xml file");
		}
	}
	/*
	 * n => number of results
	 * k => total number of words in the BTree
	 * O{logk) for searching
	 * O(n) for adding to the list
	 * O(nlogn) for sorting results
	 * Time Complexity: O(nlogn)
	 */
	@Override
	public List<ISearchResult> searchByWordWithRanking(String word) {
		word = word.toLowerCase();
		HashMap<String, Integer> resMap = database.search(word);
		if(resMap == null) {
			return null;
		}
		List<ISearchResult> searchResList = new ArrayList<> ();
		for(String id : resMap.keySet()) {
			ISearchResult searchResult = new SearchResult();
			searchResult.setId(id);
			searchResult.setRank(resMap.get(id));
			searchResList.add(searchResult);
		}
		Collections.sort(searchResList, new Comparator<ISearchResult>() {
			@Override
			public int compare(ISearchResult res1, ISearchResult res2) {
				return ((Integer)res2.getRank()).compareTo(res1.getRank());
			}
		});
		return searchResList;
	}
	/*
	 * n => number of results
	 * s => number of words in the sentence
	 * k => total number of words in the BTree
	 * O{slogk) for searching
	 * O(n) for adding to the list
	 * O(nlogn) for sorting results
	 * Time Complexity: O(nlogn)
	 */
	@Override
	public List<ISearchResult> searchByMultipleWordWithRanking(String sentence) {
		String[] queryWords = sentence.split(" ");
		HashMap<String, Integer> totalResMap = database.search(queryWords[0].toLowerCase());
		for(int i = 1; i < queryWords.length; i++) {
			String queryWord = queryWords[i].toLowerCase();
			HashMap<String, Integer> resMap = database.search(queryWord);
			if(resMap == null) {
				return null;
			}
			HashMap<String, Integer> tempMap = new HashMap<> ();
			for(String id : totalResMap.keySet()) {
				if(resMap.containsKey(id)) tempMap.put(id, Math.min(totalResMap.get(id), resMap.get(id)));
			}
			totalResMap = tempMap;
		}
		List<ISearchResult> searchResList = new ArrayList<> ();
		for(String id : totalResMap.keySet()) {
			ISearchResult searchRes = new SearchResult();
			searchRes.setId(id);
			searchRes.setRank(totalResMap.get(id));
			searchResList.add(searchRes);
		}
		Collections.sort(searchResList, new Comparator<ISearchResult>() {
			@Override
			public int compare(ISearchResult res1, ISearchResult res2) {
				return ((Integer)res2.getRank()).compareTo(res1.getRank());
			}
		});
		return searchResList;
	}
	
	public static void main(String[] args) {
		int chosen;
		Scanner scanner = new Scanner(System.in);
		SearchEngine engine = new SearchEngine(9);
		do {
			System.out.println("Choose an option:");
			System.out.println("1. Index a Webpage\t2. Index a Directory\t3. delete a Webpage\n4. Search by a Single Word\t5. Search by a sentence\t6. Exit");
			chosen = scanner.nextInt();
			scanner.nextLine();
			if(chosen == 1) {
				System.out.println("File Path:");
				String filePath = scanner.nextLine();
				long start = System.currentTimeMillis();
				engine.indexWebPage(filePath);
				long end = System.currentTimeMillis();
				System.out.println("Indexing done successfully !");
				System.out.println("Time: " + (end - start) + "ms");
			}
			else if(chosen == 2) {
				System.out.println("Directory Path:");
				String directoryPath = scanner.nextLine();
				long start = System.currentTimeMillis();
				engine.indexDirectory(directoryPath);
				long end = System.currentTimeMillis();
				System.out.println("Indexing done successfully !");
				System.out.println("Time: " + (end - start) + "ms");
			}
			else if(chosen == 3) {
				System.out.println("File Path:");
				String filePath = scanner.nextLine();
				long start = System.currentTimeMillis();
				engine.deleteWebPage(filePath);
				long end = System.currentTimeMillis();
				System.out.println("Deletion done successfully !");
				System.out.println("Time: " + (end - start) + "ms");
			}
			else if(chosen == 4) {
				System.out.println("The Word required to be searched:");
				String word = scanner.next();
				scanner.nextLine();
				long start = System.currentTimeMillis();
				List<ISearchResult> list = engine.searchByWordWithRanking(word);
				long end = System.currentTimeMillis();
				if(list == null) System.out.println("No Results Found");
				else {
					System.out.println("Search Results:");
					for(ISearchResult res : list) {
						System.out.println("Document: " + res.getId() + " => Rank: " + res.getRank());
					}
				}
				System.out.println("Time: " + (end - start) + "ms");
			}
			else if(chosen == 5) {
				System.out.println("The sentence required to searched:");
				String sentence = scanner.nextLine();
				long start = System.currentTimeMillis();
				List<ISearchResult> list = engine.searchByMultipleWordWithRanking(sentence);
				long end = System.currentTimeMillis();
				if(list == null) System.out.println("No Results Found");
				else {
					System.out.println("Search Results:");
					for(ISearchResult res : list) {
						System.out.println("Document: " + res.getId() + " => Rank: " + res.getRank());
					}
				}
				System.out.println("Time: " + (end - start) + "ms");
			}
			else if(chosen != 6) {
				System.out.println("Invalid Choice !");
			}
			
		} while(chosen != 6);
		scanner.close();
	}
	
}
