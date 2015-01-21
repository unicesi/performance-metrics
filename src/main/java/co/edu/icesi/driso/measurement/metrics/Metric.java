package co.edu.icesi.driso.measurement.metrics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author Miguel A. Jiménez
 * @date 26/11/2014
 */
public class Metric implements Serializable {
	
	/**
	 * Default serial version UID
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The metric's name and also identifier (must be unique)
	 */
	private final String identifier;
	
	/**
	 * General attributes attached to the metric (e.g., computation-node name,
	 * component name, etc.)
	 */
	private final HashMap<String, Object> attributes;
	
	/**
	 * The sub-metrics attached to this metric. This list is only used to store
	 * the objects; their values are merge with the values of this metric, through
	 * the merge method.
	 * 
	 * @see +Metric.merge(Metric[]):void
	 */
	private final List<Metric> children;
	
	/**
	 * The measurements that have been taken for each phase 
	 * (key: phase_identifier-level_name-stage_name).
	 */
	private final HashMap<String, Measurement> measurements;
	
	/**
	 * The configuration object, containing the configured attributes and
	 * measurement phases.
	 */
	private final MetricConfig config;
	
	public Metric(String identifier, MetricConfig config){
		this.identifier = identifier;
		this.config = config;
		
		attributes = new HashMap<String, Object>();
		children = new ArrayList<Metric>();
		measurements = new HashMap<String, Measurement>();
	}
	
	/**
	 * Merges shared measurements from children to father. "Merge" means that
	 * the child's measured value is added to the "values" map of the father's
	 * measure object (corresponding to each phase-level-stage).
	 * 
	 * @see Measurement
	 * @param children			The children to be merged
	 * @throws MetricException	It's thrown when there is a mismatch between the
	 * 							child's configuration class and the father's
	 */
	public void merge(Metric ... children) throws MetricException {
		
		List<MeasurementPhase> configuredPhases = config.getPhases();
		
		for (int i = 0; i < children.length; i++) {
			// Validate that father and child have the same configuration
			if(!config.equals(children[i].config)){
				throw new MetricException(8, 
						"Mismatch in the configuration class. Cannot merge "
						+ "metrics configured as " + config.getIdentifier() + 
						" and " + children[i].config.getIdentifier());
			}
			
			// Add the child to the list
			this.children.add(children[i]);
			
			// Update the measures based on the child's values (only shared measurements)
			for (int j = 0; j < configuredPhases.size(); j++) {
				MeasurementPhase tempPhase = configuredPhases.get(j);
				MeasurementPhase.Level[] levels = tempPhase.getLevels();
				
				// Iterate the different levels in tempPhase
				for (int k = 0; k < levels.length; k++) {
					MeasurementPhase.Level tempLevel = levels[k];
					MeasurementPhase.Stage[] stages = tempLevel.getStages();
					
					// Iterate the different stages in tempLevel
					for (int l = 0; l < stages.length; l++) {
						MeasurementPhase.Stage tempStage = stages[l];
						Measurement childMeasurement = 
								children[i].getMeasurement(tempPhase.getName(), tempLevel.getName(), tempStage.getName());
					
						if(childMeasurement != null && tempStage.isShared()){
							Measurement ownMeasurement = 
									getMeasurement(tempPhase.getName(), tempLevel.getName(), tempStage.getName());
							/*
							 * The father did not set any value before 
							 * children[i] on phase tempPhase, stage tempStage
							 */
							if(ownMeasurement == null){
								ownMeasurement = new Measurement();
								setMeasure(tempPhase.getName(), tempLevel.getName(), tempStage.getName(), ownMeasurement);
							}
							
							ownMeasurement.setValue(children[i].getIdentifier(), childMeasurement.getOwnValue());
						}
					}
				}
				
			}
		}
	}
	
	/**
	 * Writes a report on the specified file, containing the configured attributes,
	 * measurement phases (including levels and stages) and their corresponding calculated values.
	 * 
	 * @param file				The file in which the report must be written
	 * @param append			Indicates if the report must be appended or overwritten in the file
	 * @throws MetricException	It's thrown when there are stages in the Metric object without a value
	 * 							(i.e., there isn't a measurement object in the phases map)
	 * @throws IOException		It's thrown when something went bad at writing the report
	 */
	public void report(File file, boolean append) throws MetricException, IOException {
		
		validate();
		
		String heading = "IDENTIFIER";
		String content = new String();
		
		String[] configuredAttrs = config.getAttributes();
		List<MeasurementPhase> configuredPhases = config.getPhases();

		// Add attributes' name to the head
		for (int i = 0; i < configuredAttrs.length; i++) {
			heading += "\t" + configuredAttrs[i];
		}
		
		// Add levels and phases to the head
		for (int i = 0; i < configuredPhases.size(); i++) {
			MeasurementPhase tempPhase = configuredPhases.get(i);
			MeasurementPhase.Level[] tempLevels = tempPhase.getLevels();
			
			for (int j = 0; j < tempLevels.length; j++) {
				if(!tempLevels[j].getName().equals(MeasurementPhase.Level.DEFAULT_NAME)){
					heading += "\t" + tempPhase.getName() + 
							"[" + tempLevels[j].getName() + "]";
				}
			}
			
			heading += "\t" + tempPhase.getName();
			heading += "\t" + tempPhase.getName() + "-DETAIL";
		}
		
		content += recursiveReport(this, "");
		
		// Write the report
		FileWriter writer = new FileWriter(file, append);
		PrintWriter printWriter = new PrintWriter(writer);
		
		printWriter.println("# " + new Date().toString());
		printWriter.println(heading);
		printWriter.println(content);
		
		printWriter.close();
		writer.close();
	}
	
	private String recursiveReport(Metric metric, String tab) throws MetricException {
		String content = metric.identifier;
		String[] configuredAttributes = metric.config.getAttributes();
		
		// Add attributes' values to the content
		for (int i = 0; i < configuredAttributes.length; i++) {
			content += "\t" + metric.getAttribute(configuredAttributes[i]);
		}

		// Add phases' values to the content
		for (int i = 0; i < metric.config.getPhases().size(); i++) {
			MeasurementPhase tempPhase = metric.config.getPhases().get(i);
			HashMap<String, List<Measurement.Entry>> tempMeasurements = 
					metric.getPhaseMeasurements(tempPhase);

			Set<String> tempLevels = tempMeasurements.keySet();
			HashMap<String, Long> levelValues = new HashMap<String, Long>();
			
			for (String tempLevel : tempLevels) {
				List<Measurement.Entry> levelMeasurements = tempMeasurements.get(tempLevel);
				long levelValue = tempPhase.calculateLevelValue(levelMeasurements);
				levelValues.put(tempLevel, levelValue);
				
				if(!tempLevel.equals(MeasurementPhase.Level.DEFAULT_NAME)){
					content += "\t" + metric.config.scaleValue(levelValue);
				}
			}
		
			content += "\t" + metric.config.scaleValue(tempPhase.calculatePhaseValue(levelValues));
			content += "\t" + tempPhase.toString(tempMeasurements);
		}
		
		for (int i = 0; i < metric.children.size(); i++) {
			content += System.getProperty("line.separator") + 
					recursiveReport(metric.children.get(i), tab + "\t");
		}
		
		return content;
	}
	
	/**
	 * Gets the measurement objects corresponding to a phase, in all its level-stages.
	 * 
	 * @param phase		The measurement phase to get the measurement objects
	 * @return			A hash map containing measurement objects per level
	 */
	public HashMap<String, List<Measurement.Entry>> getPhaseMeasurements(
			MeasurementPhase phase){

		HashMap<String, List<Measurement.Entry>> measurementsPerLevel =
				new HashMap<String, List<Measurement.Entry>>();
		MeasurementPhase.Level[] levels = phase.getLevels();
		
		for (int i = 0; i < levels.length; i++) {
			MeasurementPhase.Level tempLevel = levels[i];
			MeasurementPhase.Stage[] stages = tempLevel.getStages();
			
			List<Measurement.Entry> tempMeasurements = new ArrayList<Measurement.Entry>();
			
			for (int j = 0; j < stages.length; j++) {
				MeasurementPhase.Stage tempStage = stages[j];
				String tempKey = phase.getName() + "-" + tempLevel.getName() + "-" + tempStage.getName();
				
				Measurement tempMeasurement = measurements.get(tempKey);
				tempMeasurements.add(new Measurement.Entry(tempStage.getName(), tempMeasurement));
			}
			
			measurementsPerLevel.put(tempLevel.getName(), tempMeasurements);
		}
		
		return measurementsPerLevel;
	}
	
	/**
	 * Validates that this metric object has set a value in (i) all of its
	 * configured attributes, and (ii) all of its configured phases, in all 
	 * of their level-stages.
	 * 
	 * @throws MetricException		It's thrown if there is an attribute or a 
	 * 								measurement without its corresponding value
	 */
	private void validate() throws MetricException {

		String[] attrs = config.getAttributes();
		List<MeasurementPhase> phases = config.getPhases();
		
		for (int i = 0; i < attrs.length; i++) {
			if(attributes.get(attrs[i]) == null){
				throw new MetricException(2, "Attribute \"" + attrs[i] 
						+ "\" must be set before generating the report. "
						+ "Note this validation is case sensitive");
			}
		}
		
		for (int j = 0; j < phases.size(); j++) {
			
			MeasurementPhase tempPhase = phases.get(j);
			MeasurementPhase.Level[] tempLevels = tempPhase.getLevels();
			
			for (int i = 0; i < tempLevels.length; i++) {
				MeasurementPhase.Level tempLevel = tempLevels[i];
				MeasurementPhase.Stage[] tempStages = tempLevel.getStages();
				
				for (int k = 0; k < tempStages.length; k++) {
					MeasurementPhase.Stage tempStage = tempStages[k];
					String key = tempPhase.getName() + "-" + tempLevel.getName() + 
								"-" + tempStage.getName();
					
					if(this.measurements.get(key) == null){

						String message = "Phase \"" + tempPhase.getName() + 
								"\", level \"" + tempLevel.getName() + "\", in stage \"" 
								+ tempStage.getName() + "\", must be set before generating "
								+ "the report. Note this validation is case sensitive";
						
						if(tempLevel.getName().equals(MeasurementPhase.Level.DEFAULT_NAME)){
							message = "Phase \"" + tempPhase.getName() + 
									"\", in stage \"" 
									+ tempStage.getName() + "\", must be set before generating "
									+ "the report. Note this validation is case sensitive";
						}
						
						throw new MetricException(3, message);
					}
				}
			}
		}
		
	}
	
	/**
	 * Sets an attached attribute to this metric
	 * 
	 * @param key					The attribute's name
	 * @param value					The value to be set for the attribute
	 * @throws MetricException		It's thrown if the attribute is already set
	 */
	public void setAttribute(String key, Object value) throws MetricException {
		if(attributes.containsKey(key)){
			throw new MetricException(4, "The attribute \"" + key 
					+ "\" is already set to value: " + attributes.get(key));
		}else{
			attributes.put(key, value);
		}
	}
	
	/**
	 * Sets a value for an specific level-stage in a measurement phase.
	 * 
	 * @param phase					The measurement phase
	 * @param stage					The specific stage to be set
	 * @param value					The measured value
	 * @throws MetricException		It's thrown if the stage (in that hase) is
	 * 								already set
	 */
	public void setMeasure(String phase, String level, String stage, long value) 
			throws MetricException {
		
		String key = phase + "-" + level + "-" + stage;
		
		if(measurements.containsKey(key)){
			throw new MetricException(5, "Phase \"" + phase + "\", level \"" + 
					level + "\", in stage \"" + stage + "\" is already set");
		}
		
		measurements.put(key, new Measurement(value));
	}
	
	/**
	 * Sets a value for an specific stage in a measurement phase. As no level is
	 * specified, the default one is used.
	 * 
	 * @param phase					The measurement phase
	 * @param stage					The specific stage to be set
	 * @param value					The measured value
	 * @throws MetricException		It's thrown if the stage (in that hase) is
	 * 								already set
	 */
	public void setMeasure(String phase, String stage, long value) throws MetricException {
		setMeasure(phase, MeasurementPhase.Level.DEFAULT_NAME, stage, value);
	}
	
	/**
	 * For internal use only!
	 * 
	 * @param phase
	 * @param level
	 * @param stage
	 * @param measurement
	 */
	private void setMeasure(String phase, String level, String stage, 
			Measurement measurement) {
		
		String key = phase + "-" + level + "-" + stage;
		measurements.put(key, measurement);
	}
	
	/**
	 * Returns the measurement object for an specific level-stage in a 
	 * measurement phase.
	 * 
	 * @param phase					The measurement phase
	 * @param level					The measurement level
	 * @param stage					The specific stage
	 * @return						The measurement object set in the stage
	 */
	public Measurement getMeasurement(String phase, String level, String stage){
		return measurements.get(phase + "-" + level + "-" + stage);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((identifier == null) ? 0 : identifier.hashCode());
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
		Metric other = (Metric) obj;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		return true;
	}

	public String getIdentifier() {
		return identifier;
	}

	public List<Metric> getChildren() {
		return children;
	}

	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	public MetricConfig getConfig() {
		return config;
	}

}
