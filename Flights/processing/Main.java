package MLSOD.datavisualization.exam2014;
// http://processing.org/tutorials/eclipse/

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import processing.core.*;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.data.Table;
import processing.data.TableRow;

public class Main extends PApplet {

	/**
	 * 
	 */
	private final int MTPV = 360; // Max topological profile value possible
	private static final long serialVersionUID = -6864043868248852916L;
	private final int nbc = 254; // Number of contries
	
	private Table data;
	private double topologicalProfiles[][] = new double[nbc][nbc];
	private ArrayList<Double> tpv = new ArrayList<Double>(); // Topological Profiles Values
	private Double mtpv; // Max topological profile value of the data set
	private HashMap<String,Integer> cix;
	HashMap<Integer,String> cixr;
	
//	private ArrayList<String> countries = new ArrayList<String>();
//	private PImage bg;
//	private int y;

	private double[][] pearsonCorrelationMatrix;

	public void setup() {
	  size(2*nbc, 2*nbc);
	  // The background image must be the same size as the parameters
	  // into the size() method. In this program, the size of the image
	  // is 640 x 360 pixels.
//	  bg = loadImage("../world_blank_map_1200_596.jpg");
	  
	  data = loadTable("../flights.csv", "header");

	  
//	  data = loadJSONArray("../flights.json");
	  
	  topologicalProfiles = computeTopologicalProfiles();
	  mtpv = getMaxTopologicalProfileValue();
	  pearsonCorrelationMatrix = computePearsonCorrelationMatrix(topologicalProfiles).getData();
	  cixr = reverse(cix);
//	  printCountries();
	  saveGraphToJSON();
	  
	  ArrayList<String> lowTS = new ArrayList<String>();
	  println("/*************/");
	  
	  for(int i=0; i<nbc; i++)
		  for(int j=0; j<nbc; j++) {
			  int pcc = (int) Math.round(Math.abs(pearsonCorrelationMatrix[i][j])*MTPV);
			  if(pcc > 300 && pcc < 360) {
				  if(!lowTS.contains(cixr.get(j))) {
					  lowTS.add((String) cixr.get(j));
					  println(cixr.get(i) + " <- "+pcc+" -> " + cixr.get(j));
				  }
			  }
		  }
	}
	
//	public void draw() {
////		background(bg);
//		int ssl = 2; // Pixel size
//		colorMode(HSB);
//		for (int i = 0; i < nbc; i++) {
//			for (int j = 0; j < nbc; j++) {
//				int ntpv = (int) Math.round(Math.abs(pearsonCorrelationMatrix[i][j])*MTPV);
////				if(ntpv > 300 && ntpv < 360) {
//					stroke(ntpv,360,360);
//					fill(ntpv,360,360);
//				  	rect(5+ssl*j, 5+ssl*i, ssl, ssl);
////				}
//			}
//		}
//	}
	
    public RealMatrix computePearsonCorrelationMatrix(double[][] data) {
    	PearsonsCorrelation pcc = new PearsonsCorrelation();
    	return pcc.computeCorrelationMatrix(data);
    }
    
    public double[][] computeTopologicalProfiles() {
    	double cstp[][] = new double[nbc][nbc]; // Countries Topological Profiles
    	cix = new HashMap<String,Integer>();
    	tpv = new ArrayList<Double>(); // Topological Profiles Values
    	
    	int ci = 0; // Current Index
  	  	for (TableRow flight : data.rows()) {
	  	  	String fromCountry = flight.getString("from_country");
		    String toCountry = flight.getString("to_country");
		    if(!cix.containsKey(fromCountry)) {
		      cix.put(fromCountry, ci);
		      ci++;
		    }
		    if(!cix.containsKey(toCountry)) {
	  	      cix.put(toCountry, ci);
		      ci++;
		    }
		    if(!fromCountry.equals(toCountry)) // If commented huges are in
		    	tpv.add(cstp[cix.get(fromCountry)][cix.get(toCountry)]++);   
    	}
    	return cstp;
    }
    
    public double getMaxTopologicalProfileValue() {
    	return Collections.max(tpv);
    }
    
    public double getNormalizedTopologicalProfileValue(double value) {
    	return Math.abs(value)/mtpv*MTPV;
    }
    
    public void printCountries() {
    	HashMap<String,Integer> cixsbv = sortByValues(cix); // cix sort by values
    	for (Map.Entry<String,Integer> entry : cixsbv.entrySet()) {
    	    System.out.println(entry.getKey() + ", " + entry.getValue());
    	}
    }
	
	public static void main(String args[]) {
	    PApplet.main(new String[] { "--present", "Main" });
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static HashMap<String,Integer> sortByValues(HashMap<String,Integer> map) { 
		List list = new LinkedList(map.entrySet());
	    // Defined Custom Comparator here
	    Collections.sort(list, new Comparator() {
	    	public int compare(Object o1, Object o2) {
	             return ((Comparable) ((Map.Entry) (o1)).getValue())
	                 .compareTo(((Map.Entry) (o2)).getValue());
	        }
	    });

	    // Here I am copying the sorted list in HashMap
	    // using LinkedHashMap to preserve the insertion order
	    HashMap<String, Integer> sortedHashMap = new LinkedHashMap<String, Integer>();
	    for (Iterator it = list.iterator(); it.hasNext();) {
	    	Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) it.next();
	        sortedHashMap.put(entry.getKey(), entry.getValue());
	    } 
	    return sortedHashMap;
	}
	  
	public HashMap<Integer,String> reverse(HashMap<String,Integer> map) {
		Map<Integer, String> myNewHashMap = new HashMap<>();
		for(Entry<String, Integer> entry : map.entrySet()){
			myNewHashMap.put(entry.getValue(), entry.getKey());
		}
		return (HashMap<Integer, String>) myNewHashMap;
	}
	
	/**
	 * Save the Graph Values into JSON File
	 */
	
	public void saveGraphToJSON() {
		JSONObject graph = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray links = new JSONArray();

		// Create the links
		for(Entry<Integer, String> entry : cixr.entrySet()){
			JSONObject country = new JSONObject();
		    country.setString("name", entry.getValue());
		    country.setInt("group", getContinentID(entry.getValue()));
		    nodes.setJSONObject(entry.getKey(), country);
		}
		
		// Create the links
		int counter = 0;
		for (int i = 0; i < nbc; i++) {
			for (int j = 0; j < nbc; j++) {
				int ntpv = (int) Math.round(Math.abs(pearsonCorrelationMatrix[i][j])*MTPV);
				if(ntpv > 300 && ntpv < 360) {
					JSONObject o = new JSONObject();
				    o.setInt("source", i);
				    o.setInt("target", j);
				    o.setInt("value", ntpv);
				    if(o != null)
				    	links.setJSONObject(counter, o);
				    counter++;
				}
			}
		}
		  
		graph.setJSONArray("nodes", nodes);
		graph.setJSONArray("links", links);
		saveJSONObject(graph, "C:/Users/Max/Documents/Projects/Programming/JavaScript/D3/FlightsVisualization/data/gf.json");
	}
	
	public int getContinentID(String country) {
	    int continentID = 0;
		JSONArray values = loadJSONArray("../country-codes.json");
		for (int i = 0; i < values.size(); i++) {
			JSONObject c = values.getJSONObject(i); 
		    String cname = c.getString("name");
		    if(cname.equals(country)) {
		    	String continentCode = c.getString("continent_name");
		    	switch(continentCode) {
		    		case "AF":
		    			continentID = 1;
		    			break;
		    		case "EU":
		    			continentID = 2;
		    			break;
		    		case "AS":
		    			continentID = 3;
		    			break;
		    		case "NA":
		    			continentID = 4;
		    			break;
		    		case "OC":
		    			continentID = 5;
		    			break;
		    		case "SA":
		    			continentID = 6;
		    			break;
		    		case "AN":
		    			continentID = 7;
		    			break;
		    		default:
		    			continentID = 0;
		    			break;
		    	}
		    }
		}
		return continentID;
	}
}