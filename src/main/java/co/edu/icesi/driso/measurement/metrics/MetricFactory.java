package co.edu.icesi.driso.measurement.metrics;

import java.lang.reflect.InvocationTargetException;
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
	 * Contains the registered configuration classes
	 */
	private static HashMap<String, Class<? extends MetricConfig>> configClasses;
	
	/**
	 * Contains the registered configuration files
	 */
	private static HashMap<String, String> configFiles;
	
	/**
	 * Contains already created instances for each configuration class
	 */
	private static HashMap<String, MetricConfig> configInstances;
	
	static {
		configClasses = new HashMap<String, Class<? extends MetricConfig>>();
		configFiles = new HashMap<String, String>();
		configInstances = new HashMap<String, MetricConfig>();
	}

	/**
	 * Creates a metric instance based on the specified configuration class
	 * 
	 * @param criterion				The config class identifier
	 * @param identifier			The new metric object's identifier
	 * @return						A new instance of Metric
	 * @throws MetricException 		It's thrown when the configuration class
	 * 								does not contains a public constructor
	 * 								overwriting MetricConfig's constructor
	 */
	public static Metric getMetric(String criterion, String identifier) throws MetricException {
		
		Class<? extends MetricConfig> clazz = configClasses.get(criterion);
		String configFile = configFiles.get(criterion);
		
		if(clazz != null){
			try {
				
				MetricConfig configInstance = configInstances.get(criterion);
				
				if(configInstance == null){
					configInstance = 
							clazz.getConstructor(String.class)
							.newInstance(configFile);
					configInstances.put(criterion, configInstance);
				}
				
				return new Metric(identifier, configInstance);
				
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				throw new MetricException(21, "Children classes of the MetricConfig "
						+ "class must overwrite a public constructor with "
						+ "the configuration file (path name as String) as "
						+ "an input parameter", e);
			} catch (SecurityException e) {
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
	 * Registers a new configuration class and its corresponding config file
	 * 
	 * @param criterion
	 * @param configClass
	 * @throws MetricException
	 */
	public static void registerConfigClass(String criterion, String configFile, 
			Class<? extends MetricConfig> configClass) throws MetricException {

		if(configClasses.containsKey(criterion)){
			throw new MetricException(12, "There is a configuration class already"
					+ " set to criterion " + criterion);
		}else{
			configClasses.put(criterion, configClass);
			configFiles.put(criterion, configFile);
		}
	}
	
}
