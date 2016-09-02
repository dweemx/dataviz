// http://stackoverflow.com/questions/1467193/java-serialization-of-multidimensional-array

package MLSOD.datavisualization.exam2015;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import processing.core.PApplet;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.data.Table;

public class Test extends PApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5795268707798491518L;
	Table data;
	String[] mRNANames;
	double[][] cellCycleExpressionData;
	byte[][] pcm;
	HashMap<String,ArrayList<String>> mRNAClusters = new HashMap<String,ArrayList<String>>();
	String fileName = "cellcycle-cdc15.dat";
	
	public void setup2() {
//		  size(800, 800);	  
		  data = loadTable("C:/Users/Max/Documents/Projects/Programming/KULeuven/src/MLSOD/datavisualization/exam2015/Spellman.csv", "header");
		  mRNANames = new String[data.getRowCount()];
		  
//		  HashMap<Double,Integer> count = new HashMap<Double,Integer>();
////		  System.out.println(data.getRowCount());
//		  for(int i=0; i<data.getRowCount(); i++) {
//			  double roundOff = Math.round(data.getDouble(i, 6) * 100.0) / (double) 100.0;
//			  if(!count.containsKey(roundOff)) {
//				  count.put(roundOff, 1);
//			  } else {
//				  count.put(roundOff, count.get(roundOff)+1);
//			  }
//		  }
//		  
//	      Map<Double, Integer> map = new TreeMap<Double, Integer>(count); 
//	      for(Double d: map.keySet()) {
//	    	  System.out.println(d+">"+map.get(d));
//	      }
		  		  
		  
		  cellCycleExpressionData = new double[data.getColumnCount()-1][data.getRowCount()];
		  for(int i=0; i<data.getRowCount(); i++) {
			  mRNANames[i] = data.getString(i, 0);
			  for(int j=1; j<data.getColumnCount()-1; j++) {
//				  if(j==1)
//					  if(data.getDouble(i, j+1)>1) {
//						  System.out.println(data.getString(i, 0));
//					  }
				  // Reverse the matrix to be able to compute 
				  // The Pearson Correlation on the mRNAs
				  cellCycleExpressionData[j][i] = data.getDouble(i, j+1);
			  }
		  }
		  // Compute the pearson correlation with the column
		  if(!new File(fileName).exists()) {
			  double[][] pcm = Utils.computeSpearmansCorrelationMatrix(cellCycleExpressionData).getData();
			  Utils.save(fileName,Utils.round(pcm));
		  } else {
			  pcm = (byte[][]) Utils.read(fileName);
			  System.out.println("done");
		  }
		  
		  for(int i=0; i<pcm.length; i++) {
			  for(int j=0; j<i; j++) {
//				  System.out.println(pcm[i][j]);
				  if(pcm[i][j] > 90) {
					  if(!mRNAClusters.containsKey(mRNANames[j])) {
						  mRNAClusters.put(mRNANames[j], new ArrayList<String>());
					  }
					  if(!mRNAClusters.containsKey(mRNANames[i])) {
						  mRNAClusters.put(mRNANames[i], new ArrayList<String>());
					  } else {
						  mRNAClusters.get(mRNANames[i]).add(mRNANames[j]);
					  }
				  }
			  }
		  }
		  int nb = 0, total = 0;
		  int nbNotExistAsKey = 0;
		  for(String e: mRNAClusters.keySet()) {
			  if(mRNAClusters.get(e).size() > 0) {
				  nb++;
				  System.out.println(e+">"+mRNAClusters.get(e));
				  total += mRNAClusters.get(e).size();
				  for(String m: mRNAClusters.get(e)) {
					  if(!mRNAClusters.containsKey(m)) {
						  nbNotExistAsKey++;
					  }
				  }
			  }
		  }
		  System.out.println("Total: "+total);
		  System.out.println("Unexisting as key: "+nbNotExistAsKey);
		  System.out.println("Nb not empty clusters: "+nb+"/"+mRNAClusters.size());
		  this.saveGraphToJSON();
//		  this.saveGraphToGEXF();
//		  this.saveClustersToJSON();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Test().setup2();
	}
	
	HashMap<String,Integer> mRNAIds = new HashMap<String,Integer>();
	
	public void saveClustersToJSON() {
		JSONArray coex = new JSONArray();
		for(String from: mRNAClusters.keySet()) {
			JSONObject l = new JSONObject();
			l.setString("from", from);
			JSONArray tos = new JSONArray();
			for(String to: mRNAClusters.get(from)) {
				tos.append(to);
			}
			if(tos.size() == 0)
				continue;
			l.setJSONArray("to", tos);
			coex.append(l);
		}
		saveJSONArray(coex, "cellcycle-coexgenes.json");
	}
	
	public void saveGraphToJSON() {
		JSONObject graph = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray links = new JSONArray();

//		// Create the nodes
		int i=0;
		for(String mRNAName: mRNAClusters.keySet()) {
			JSONObject mRNA = new JSONObject();
			mRNA.setString("name", mRNAName);
			mRNA.setInt("group", 1);
		    nodes.setJSONObject(i, mRNA);
		    mRNAIds.put(mRNAName, i);
		    i++;
		}
//		
//		// Create the links
		int c = 0;
		for(String e: mRNAClusters.keySet()) {
			if(mRNAClusters.get(e).size() > 0) {
				System.out.println(e+">"+mRNAClusters.get(e));
				for(String m: mRNAClusters.get(e)) {
					JSONObject o = new JSONObject();
					o.setInt("source", mRNAIds.get(e));
					o.setInt("target", mRNAIds.get(m));
					if(o != null)
						links.setJSONObject(c, o);
					c++;
				}
			}
		}
//		  
		graph.setJSONArray("nodes", nodes);
		graph.setJSONArray("links", links);
		saveJSONObject(graph, "cellcycle-mrnas-graph.json");
	}
	
	public void saveGraphToGEXF() {

		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			/***** BEGIN GEXF ELEMENT *******/
			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("gexf");
			doc.appendChild(rootElement);
			// Add xmnls attribute
			Attr attr = doc.createAttribute("xmnls");
			attr.setValue("http://www.gexf.net/1.2draft");
			rootElement.setAttributeNode(attr);

			// Add xmnls:viz attribute
			rootElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:viz", "http:///www.gexf.net/1.2draft/viz");
			rootElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			
			// Add version attribute
			attr = doc.createAttribute("version");
			attr.setValue("1.2");
			rootElement.setAttributeNode(attr);

			/***** > BEGIN GRAPH ELEMENT *******/

			Element graph = doc.createElement("graph");
			rootElement.appendChild(graph);

			// Add defaultedgetype attribute
			attr = doc.createAttribute("defaultedgetype");
			attr.setValue("undirected");
			graph.setAttributeNode(attr);

			/***** >> BEGIN NODES ELEMENT *******/			
			// Create the nodes
			Element nodes = doc.createElement("nodes");
			graph.appendChild(nodes);
			
			int i=0;
			for(String mRNAName: mRNAClusters.keySet()) {
				Element node = doc.createElement("node");
				nodes.appendChild(node);

				// Add id attribute
				attr = doc.createAttribute("id");
				attr.setValue(Integer.toString(i));
				node.setAttributeNode(attr);

				// Add label attribute
				attr = doc.createAttribute("label");
				attr.setValue(mRNAName);
				node.setAttributeNode(attr);
				
			    mRNAIds.put(mRNAName, i);
				i++;
			}

			/***** >> BEGIN EDGES ELEMENT *******/
			// Create the nodes
			Element edges = doc.createElement("edges");
			graph.appendChild(edges);
			
			for(String e: mRNAClusters.keySet()) {
				if(mRNAClusters.get(e).size() > 0) {
					System.out.println(e+">"+mRNAClusters.get(e));
					for(String m: mRNAClusters.get(e)) {
					
						Element edge = doc.createElement("edge");
						edges.appendChild(edge);
	
						// Add source attribute
						attr = doc.createAttribute("source");
						attr.setValue(Integer.toString(mRNAIds.get(e)));
						edge.setAttributeNode(attr);
	
						// Add target attribute
						attr = doc.createAttribute("target");
						attr.setValue(Integer.toString(mRNAIds.get(m)));
						edge.setAttributeNode(attr);
	
						// Add weight attribute
	//					attr = doc.createAttribute("weight");
	//					attr.setValue(Double.toString(pdbExtractor.getDistances()[i][j]));
	//					edge.setAttributeNode(attr);
					}
				}
			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("cellcycle.gexf"));

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);

			transformer.transform(source, result);
			System.out.println("File saved!");

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}

}
