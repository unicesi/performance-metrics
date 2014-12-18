package co.edu.icesi.driso.measurement.metrics;

import java.io.Serializable;
import java.util.HashMap;

/**
 * This class encapsulates a measurement, including the metric-children's 
 * measurements.
 * 
 * @author Miguel A. Jiménez
 * @date 26/11/2014
 */
public class Measurement implements Serializable {
	
	/**
	 * This class encapsulates an Entry, a combination of stage (key)
	 * and its corresponding measurement (value). It is intended to be used
	 * in Maps.
	 * 
	 * @author Miguel A. Jiménez
	 * @date 03/12/2014
	 */
	public static class Entry implements Serializable {
		
		/**
		 * Default serial version UID
		 */
		private static final long serialVersionUID = 1L;
		
		private final String stageName;
		private final Measurement measurement;
		
		public Entry(String stageName, Measurement measurement){
			this.stageName = stageName;
			this.measurement = measurement;
		}
		
		@Override
		public String toString(){
			return stageName + ": " + measurement;
		}
		
		public String getStageName(){
			return stageName;
		}
		
		public Measurement getMeasurement(){
			return measurement;
		}
	}
	
	/**
	 * Default serial version UID
	 */
	private static final long serialVersionUID = 1L;
	
	private final boolean hasValue;
	private final long value;
	private final HashMap<String, Long> values;
	
	/**
	 * This constructor is used when merging the child's values into the father's.
	 * At that moment, the father has no value set yet.
	 */
	public Measurement(){
		hasValue = false;
		value = 0;
		values = new HashMap<String, Long>();
	}
	
	/**
	 * This constructor is used when a measure is taken, and it is being stored.
	 * 
	 * @param value		System time in milliseconds
	 */
	public Measurement(long value){
		hasValue = true;
		this.value = value;
		values = new HashMap<String, Long>();
	}
	
	/**
	 * Adds a child measurement into the father.
	 * 
	 * @param key				The child's metric identifier
	 * @param value				The measurement
	 * @throws MetricException	It's thrown if there's already a value set for that child
	 */
	public void setValue(String key, long value) throws MetricException {
		if(values.containsKey(key)){
			throw new MetricException(1, 
					"Element \"" + key + "\" is already set in the composed measurement");
		}else{
			values.put(key, value);
		}
	}
	
	/**
	 * @return Wheater this measurement has a set value or not
	 */
	public boolean hasValue(){
		return hasValue;
	}
	
	/**
	 * @return	The own measurement value
	 */
	public long getOwnValue(){
		return value;
	}
	
	/**
	 * @return	A map containing pairs of children's identifier and measurement value
	 */
	public HashMap<String, Long> getChildValues(){
		return values;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (value ^ (value >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Measurement other = (Measurement) obj;
		if (value != other.value)
			return false;
		return true;
	}

	@Override
	public String toString(){
		String str = String.valueOf(value);
		
		if(!values.isEmpty()){
			str += ", " + 
					(values.toString().replace("{", "(").replace("}", ")"));
		}
		
		return str;
	}

}
