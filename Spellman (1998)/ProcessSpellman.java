// http://stackoverflow.com/questions/11967258/using-jsoup-to-find-a-node-with-some-particular-text

package MLSOD.datavisualization.exam2015;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.jsoup.Jsoup;

import processing.core.PApplet;
import processing.data.Table;
import processing.data.TableRow;

public class ProcessSpellman extends PApplet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7643720973330196265L;

//	public JSONObject formatChr = (JSONObject) JSONValue.parse(
//			"{\"I\":1,\"II\":2,\"III\":3,\"IV\":4,"
//			+ "\"V\":5,\"VI\":6,\"VII\":7,\"VIII\":8,"
//			+ "\"IX\":9,\"X\":10,\"XI\":11,\"XII\":12,"
//			+ "\"XIII\":13,\"XIV\":14,\"XV\":15,\"XVI\"}"); 
	public JSONObject formatChr = (JSONObject) JSONValue.parse(""
			+ "{\"I\":1,\"II\":2,\"III\":3,\"IV\":4,"
			+ "\"V\":5,\"VI\":6,\"VII\":7,\"VIII\":8,"
			+ "\"IX\":9,\"X\":10,\"XI\":11,\"XII\":12,"
			+ "\"XIII\":13,\"XIV\":14,\"XV\":15,\"XVI\":16}");
	
	Table spellmanNew = new Table();
	
	public ProcessSpellman() {
		createTable();
		
		Table data = loadTable("C:/Users/Max/Documents/Projects/Programming/KULeuven/src/MLSOD/datavisualization/exam2015/Spellman.csv", "header");
		for(int i=0; i<data.getRowCount(); i++) {
			TableRow newRow = spellmanNew.addRow();
			String gene = data.getString(i, 0);			
			// Retrieve Chr, startPos, endPos
			long chrId = 0;
			int startPos = 0,
					endPos = 0;
			String d = null;
			try {
				d = Jsoup.connect("http://wiki.yeastgenome.org/index.php/"+gene).get()
						.select("td:matchesOwn(Chr)").first().text();
				String[] coordinates = d.split(":");
				chrId = (long) formatChr.get(coordinates[0].substring(4));
				String[] position = coordinates[1].split("\\.\\.");
				startPos = Integer.parseInt(position[0]);
				endPos = Integer.parseInt(position[1]);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			System.out.println(chrId+":"+startPos+">"+endPos);

			newRow.setString("gene", gene);
			newRow.setLong("chr", chrId);
			newRow.setInt("startPos", startPos);
			newRow.setInt("endPos", endPos);
			for(int j=1; j<=23; j+=1) {
				int columnName = 40+(j-1)*10;
				newRow.setDouble(Integer.toString(columnName), data.getDouble(i, j));
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
				
		saveTable(spellmanNew, "Spellman_new.csv");
	}
	
	
	/**
	 * Create new pre processed Spellman Data set
	 */
	public void createTable() {
		spellmanNew.addColumn("gene");
		spellmanNew.addColumn("chr");
		spellmanNew.addColumn("startPos");
		spellmanNew.addColumn("endPos");
		for(int i=40; i<=260; i+=10) {
			spellmanNew.addColumn(Integer.toString(i));
		}
	}

	public static void main(String[] args) {
		new ProcessSpellman();
	}
	
}
