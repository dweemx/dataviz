package MLSOD.datavisualization.exam2014;
// http://processing.org/tutorials/eclipse/


import processing.core.*;
import processing.data.JSONArray;
import processing.data.JSONObject;

public class CountryPIBPerHabitant extends PApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7666688909781254054L;
	JSONArray pibs;
	JSONArray pops;
	JSONArray cc;

	JSONObject json = new JSONObject();



	public void setup() {
	  
	  cc = loadJSONArray("../country-code-simple.json");
	  
	  for (int i = 0; i < cc.size(); i++) {
	    
	    JSONObject country = cc.getJSONObject(i); 

	    String cca = country.getString("alpha-2");
	    String cn = country.getString("name");
	    
	    println(cn + ","+ cca);
	    
	    pibs = loadJSONArray("http://api.worldbank.org/countries/"+cca+"/indicators/NY.GDP.MKTP.CD?date=2009&format=json");
	    pops = loadJSONArray("http://api.worldbank.org/countries/"+cca+"/indicators/SP.POP.TOTL?date=2009&format=json");
	    
	    double pib = 0;
	    double pop = 1;
	    
	    // Get the first array of  of pibs
	    try {
		    if(pibs.size() > 1) {
		      JSONArray values = pibs.getJSONArray(1);
		      JSONObject item = values.getJSONObject(0); 
		      pib = Double.parseDouble(item.getString("value"));
		    }
		    
		    // Get the first array of  of pops
		    if(pops.size() > 1) {
		      JSONArray values2 = pops.getJSONArray(1);
		      JSONObject item = values2.getJSONObject(0); 
		      pop = Double.parseDouble(item.getString("value"));
		    }
	    } catch(RuntimeException e)
        {}
	    
	    double pibph = pib/pop;
	    
	    json.setDouble(cn, pibph);

	    println(cn + ", " + pibph);
	  }
	  saveJSONObject(json, "C:/Users/Max/Documents/Projects/Programming/JavaScript/D3/FlightsVisualization/data/pibph2009.json");

	}

	// Sketch prints:
	// 0, Panthera leo, Lion
}