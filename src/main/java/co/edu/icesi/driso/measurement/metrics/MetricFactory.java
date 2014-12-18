package co.edu.icesi.driso.measurement.metrics;

import java.util.HashMap;

/**
 * This class provides a way to abstract the metric creation.
 * To add more classes, edit the method +getMetric(String, String):Metric
 * adding an else-if clause.
 * 
 * @author Miguel A. Jiménez
 * @date 26/11/2014
 */
public class MetricFactory {
	
	/**
	 * The collection containing the registered configuration classes
	 */
	private static HashMap<String, Class<? extends MetricConfig>> configs;
	
	static {
		configs = new HashMap<String, Class<? extends MetricConfig>>();
	}

	/**
	 * 
	 * 
	 * @param criterion
	 * @param identifier
	 * @return
	 * @throws MetricException 
	 */
	public static Metric getMetric(String criterion, String identifier) throws MetricException {
		
		Class<? extends MetricConfig> clazz = configs.get(criterion);
		
		if(clazz != null){
			try {
				
				return new Metric(identifier, clazz.newInstance());
				
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}else{
			throw new MetricException(13, "The configuration class corresponding "
					+ "to criterion \"" + criterion + "\" must be registered "
							+ "before getting Metric instances.");
		}
		
		return null;
	}
	
	/**
	 * 
	 * 
	 * @param criterion
	 * @param configClass
	 * @throws MetricException
	 */
	public static void registerConfigClass(String criterion, 
			Class<? extends MetricConfig> configClass) throws MetricException {

		if(configs.containsKey(criterion)){
			throw new MetricException(12, "There is a configuration class already"
					+ " set to criterion " + criterion);
		}else{
			configs.put(criterion, configClass);
		}
	}
	
}
