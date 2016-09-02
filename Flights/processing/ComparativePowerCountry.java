package MLSOD.datavisualization.exam2014;
// http://processing.org/tutorials/eclipse/
// https://github.com/wikimedia/limn-data/blob/master/geo/country-codes.json

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import processing.core.*;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.data.Table;
import processing.data.TableRow;

public class ComparativePowerCountry extends PApplet {
	
	private Double mtpv; // Max topological profile value of the data set	

	
	final int VARPIB_DL = 0;
	int VARPIB_UL = 10;

	/**
	 * 
	 */
	private final int MTPV = 360; // Max topological profile value possible
	private static final long serialVersionUID = -6864043868248852916L;
	private final int nbc = 254; // Number of contries
	
	private Table data;
	private ArrayList<Double> tpv = new ArrayList<Double>(); // Topological Profiles Values
	double[][] topologicalPowerSimilarity;
	
	JSONObject pibph;

	public void setup() {
	  size(2*nbc, 2*nbc);	  
	  pibph = loadJSONObject("../pibph2009.json");
	  data = loadTable("../flights.csv", "header");
	  computeTotalDistanceFlightsAirlines();
	  computeRelativePowerAirlines();
	  computeRelativePowerCountries();
	  computeTopologicalPowerProfiles();
	  computePredictedRelativePIBPHCountries();
	  mtpv = getMaxTopologicalPowerProfileValue();
	  System.out.println(mtpv);
	  
//	  VARPIB_UL = (int) Math.round(mtpv);
//	  System.out.println(VARPIB_UL);
	  
//	  topologicalPowerSimilarity = computePearsonCorrelationMatrix(topologicalPowerProfiles).getData();
//	  saveGraphToJSON(topologicalPowerSimilarity); 
	  saveGraphToJSON(topologicalPowerProfiles);  
	}
	
	public void draw() {
		int ssl = 2; // Pixel size
		colorMode(HSB);
		for (int i = 0; i < nbc; i++) {
			for (int j = 0; j < nbc; j++) {
				int value = (int) topologicalPowerProfiles[i][j];
//				if(value > 0)
//					System.out.println(value);
//				if(value > 300 && value < 360) {
					stroke(value,360,360);
					fill(value,360,360);
				  	rect(5+ssl*j, 5+ssl*i, ssl, ssl);
//				}
			}
		}
	}
	
	// Total distance covered by flights for each airline
	HashMap<String,Integer> totalDistanceFlightsAirlines = new HashMap<String,Integer>();
	// List of all airlines for each country
	HashMap<String,ArrayList<String>> countryAirlines = new HashMap<String,ArrayList<String>>();
	HashMap<String,ArrayList<String>> countryAirlinesCopy;
	// Total distance covered by all flights
	int totalDistance = 0;
	public void computeTotalDistanceFlightsAirlines() {
		for (TableRow flight : data.rows()) {
	  	  	String airline = flight.getString("airline");
		    String airlineCountry = flight.getString("airline_country"); 
		    int distance = flight.getInt("distance");
		    if(countryAirlines.containsKey(airlineCountry)) {
		    	if(!countryAirlines.get(airlineCountry).contains(airline)) // Had forgotten
		    		countryAirlines.get(airlineCountry).add(airline);
		    } else {
		    	ArrayList<String> airlines = new ArrayList<String>();
		    	airlines.add(airline);
		    	countryAirlines.put(airlineCountry, airlines);
		    }
		    // Add distance of flight to the total distance of each airline
		    if(totalDistanceFlightsAirlines.containsKey(airline))
		    	totalDistanceFlightsAirlines.put(airline, totalDistanceFlightsAirlines.get(airline) + distance);
		    else
		    	totalDistanceFlightsAirlines.put(airline, distance);
		    // Add distance of flight to the total distance of all flights
		    totalDistance += distance;
    	}
		countryAirlinesCopy = new HashMap<String,ArrayList<String>>(countryAirlines);
	}
	
	// Calculate the relative power for each airline
	/*
	 * RPA = sum(total distance flights of airline)/sum(total distance all flights
	 */
	HashMap<String,Double> relativePowerAirlines = new HashMap<String,Double>();
	public void computeRelativePowerAirlines() {
		Iterator<Entry<String, Integer>> it = totalDistanceFlightsAirlines.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String,Integer> pairs = (Map.Entry<String,Integer>)it.next();
//	        System.out.println(pairs.getValue() + " <> "+totalDistance);
	        double relativePowerAirline = (double) pairs.getValue() / totalDistance /* 10000*/;
//	        System.out.println(pairs.getKey() + " > "+ relativePowerAirline);
	        relativePowerAirlines.put(pairs.getKey(), relativePowerAirline);
//	        System.out.println(pairs.getKey() + " = " + pairs.getValue());
	        it.remove(); // avoids a ConcurrentModificationException
	    }
//	    System.out.println(relativePowerAirlines.size());
	}
	
	// Calculate the relative power for each country
	/*
	 * RPC = sum(RPA)
	 */
	ArrayList<Double> relativePowerCountries = new ArrayList<Double>();
	HashMap<String,Integer> countryIndexes = new HashMap<String,Integer>();
	HashMap<Integer,String> indexCountries;
	public void computeRelativePowerCountries() {
		Iterator<Entry<String, ArrayList<String>>> it = countryAirlines.entrySet().iterator();
		int countryIndex = 0;
		while (it.hasNext()) {
	        Map.Entry<String, ArrayList<String>> pairs = (Map.Entry<String, ArrayList<String>>)it.next();
	        double relativePowerCountry = 0;
	        
//	        System.out.println(pairs.getValue().size());
	        
	        for(String airline: pairs.getValue()) {
	        	relativePowerCountry += relativePowerAirlines.get(airline);
	        }
	        relativePowerCountries.add(relativePowerCountry);      
	        if(!countryIndexes.containsKey(pairs.getKey())) {
	        	countryIndexes.put(pairs.getKey(),countryIndex);
	        	countryIndex++;
	        }
//	        System.out.println(pairs.getKey() + " > " + relativePowerCountry);
	        it.remove(); // avoids a ConcurrentModificationException
	    }
		indexCountries = (HashMap<Integer, String>) reverse(countryIndexes);
		
//	    System.out.println(relativePowerAirlines.size());
	}
	
	public int getNbAirlines(String country) {
		if(countryAirlinesCopy.get(country) != null) {
			return countryAirlinesCopy.get(country).size();
		} else
			return 1;
	}
	
	double[][] topologicalPowerProfiles = new double[nbc][nbc];
	ArrayList<Double> topologicalPowerProfilesAL = new ArrayList<Double>();
	public void computeTopologicalPowerProfiles() {
    	for (int i = 0; i < nbc; i++) {
			for (int j = 0; j < nbc; j++) {
				double topologicalPowerProfile = 0;
				try {
					/*
					 * Operator: - => Ellipse shape with the high powers in the center
					 * Operator: / => Train shape with high powers at a top
					 */
					topologicalPowerProfile = Math.abs(relativePowerCountries.get(i)/relativePowerCountries.get(j));
//					System.out.println(indexCountries.get(i) + "<- "+topologicalPowerProfile+" ->" + indexCountries.get(j));
				} catch(IndexOutOfBoundsException e) {}
				topologicalPowerProfiles[i][j] = topologicalPowerProfile;
				if(!Double.isNaN(topologicalPowerProfile) && !Double.isInfinite(topologicalPowerProfile))
					topologicalPowerProfilesAL.add(topologicalPowerProfile);
			}
		}
    }
	
	HashMap<String,Double>predictedRelativePIBPHCountries = new HashMap<String,Double>();
	public void computePredictedRelativePIBPHCountries() {
    	for (int i = 0; i < nbc; i++) {
			String centerCountry = indexCountries.get(i);
			double predictedPIBPHCenterCountry = 0;
			for (int j = 0; j < nbc; j++) {
				double value = topologicalPowerProfiles[i][j];
				if(value >= VARPIB_DL && value < VARPIB_UL) {
					String connectedCountry = indexCountries.get(j);
					double partialDistanceConnectedCountries = value/getTotalDistanceConnectedCountries(i);
	//				System.out.println(partialDistanceConnectedCountries);
					predictedPIBPHCenterCountry += partialDistanceConnectedCountries*getPIBPerHabitant(connectedCountry);
				}
			}
			predictedRelativePIBPHCountries.put(centerCountry, predictedPIBPHCenterCountry);
//			System.out.println(centerCountry+"> predicted: "+(int) predictedPIBPHCenterCountry+ " & true: "+getPIBPerHabitant(centerCountry));
		}
    }
	
	public double getTotalDistanceConnectedCountries(int centerCountry) {
    	double totalDistanceConnectedCountries = 0;
		for (int j = 0; j < nbc; j++) {
			double value = topologicalPowerProfiles[centerCountry][j];
			if(value >= VARPIB_DL && value < VARPIB_UL)
				totalDistanceConnectedCountries += topologicalPowerProfiles[centerCountry][j];
		}
		return totalDistanceConnectedCountries;
	}
	
	public double getTotalPIBPHConnectedCountries(int centerCountry) {
    	double totalPIBPHConnectedCountries = 0;
		for (int j = 0; j < nbc; j++) {
			totalPIBPHConnectedCountries += topologicalPowerProfiles[centerCountry][j];
		}
		return totalPIBPHConnectedCountries;
	}

	public int getPIBPerHabitant(String country) {
		int pib = 1;
		if(pibph.hasKey(country))
			pib = (int) Math.sqrt(pibph.getDouble(country))/8;
		return pib;
	}
	
    public RealMatrix computePearsonCorrelationMatrix(double[][] data) {
    	PearsonsCorrelation pcc = new PearsonsCorrelation();
    	return pcc.computeCorrelationMatrix(data);
    }
    
    public double getMaxTopologicalProfileValue() {
    	return Collections.max(tpv);
    }
    
    public double getMaxTopologicalPowerProfileValue() {
    	return Collections.max(topologicalPowerProfilesAL);
    }
    
    public double getNormalizedTopologicalProfileValue(double value) {
    	return Math.abs(value)/mtpv*MTPV;
    }
	
	public static void main(String args[]) {
	    PApplet.main(new String[] { "--present", "ComparativePowerCountry" });
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
	
	public void saveGraphToJSON(double[][] data) {
		JSONObject graph = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray links = new JSONArray();

		// Create the links
		for(Entry<Integer, String> entry : indexCountries.entrySet()){
			JSONObject country = new JSONObject();
		    country.setString("name", entry.getValue());
		    country.setInt("group", getContinentID(entry.getValue()));
		    String cn = entry.getValue();
		    int pib = 1;
		    if(pibph.hasKey(cn))
		    	pib = (int) Math.sqrt(pibph.getDouble(entry.getValue()))/8;
		    country.setInt("health", pib);
		    
		    int incoherence = 0;
		    if(predictedRelativePIBPHCountries.containsKey(cn)) {
		    	double predictedRelativePIBPHCountry = predictedRelativePIBPHCountries.get(cn);
		    	if(predictedRelativePIBPHCountry > this.getPIBPerHabitant(cn)) {
		    		incoherence = (int) predictedRelativePIBPHCountry-this.getPIBPerHabitant(cn);
		    		//System.out.println(entry.getValue()+" > "+ (predictedRelativePIBPHCountry-this.getPIBPerHabitant(cn)));
		    	}
//	    		System.out.println(entry.getValue()+" > "+ (predictedRelativePIBPHCountry-this.getPIBPerHabitant(cn)));
		    }
		    country.setInt("incoherence", incoherence);
		    nodes.setJSONObject(entry.getKey(), country);
		}
		
		// Create the links
		int counter = 0;
		for (int i = 0; i < nbc; i++) {
			for (int j = 0; j < nbc; j++) {
				int value = (int) data[i][j];
				/*
				 * With de "/" operator
				 * - If only one restriction => Train shape
				 * - If two restrictions => Hollow Cylinder shape
				 */
				if(value != 0) {
					if(value >= VARPIB_DL && value < VARPIB_UL) {
						JSONObject o = new JSONObject();
					    o.setInt("source", i);
					    o.setInt("target", j);
					    o.setInt("value", value);
					    if(o != null)
					    	links.setJSONObject(counter, o);
					    counter++;
					}
				}
			}
		}
		  
		graph.setJSONArray("nodes", nodes);
		graph.setJSONArray("links", links);
		saveJSONObject(graph, "C:/Users/Max/Documents/Projects/Programming/JavaScript/D3/FlightsVisualization/data/gpc.json");
	}	
	
	public int getContinentID(String country) {
	    int continentID = 0;
		JSONArray values = loadJSONArray("../country-codes.json");
		for (int i = 0; i < values.size(); i++) {
			JSONObject c = values.getJSONObject(i); 
		    String cname = c.getString("name");
		    if(cname.equals(country)) {
		    	String continentCode = c.getString("continent");
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
				return continentID;
		    }
		}
		return 0;
	}
}
