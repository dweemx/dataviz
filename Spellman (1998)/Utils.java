package MLSOD.datavisualization.exam2015;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

public class Utils {
	
	public static void save(String fileName, Object o) {
		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(o);
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Object read(String fileName) {
		ObjectInputStream iis = null;
		Object o = null;
		try {
			FileInputStream fis = new FileInputStream(fileName);
			iis = new ObjectInputStream(fis);
			o = (byte[][]) iis.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		} finally {
			try {
				iis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return o;
	}
	
	public static byte[][] round(double[][] r) {
		byte[][] ar = new byte[r.length][r.length];
		for (int i = 0; i < r.length; i ++ ) {
			for(int j = 0; j < r[i].length; j++) {
				ar[i][j] = ((byte) (r[i][j]*100));
			}
		}
		return ar;
	}
	
    public static RealMatrix computePearsonCorrelationMatrix(double[][] data) {
    	PearsonsCorrelation pcc = new PearsonsCorrelation();
    	return pcc.computeCorrelationMatrix(data);
    }
    
    public static RealMatrix computeSpearmansCorrelationMatrix(double[][] data) {
    	SpearmansCorrelation pcc = new SpearmansCorrelation();
    	return pcc.computeCorrelationMatrix(data);
    }
	
}
