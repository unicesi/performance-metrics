package co.edu.icesi.driso.measurement.metrics;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * 
 * @author Miguel A. Jiménez
 * @date 26/11/2014
 */
public abstract class MetricConfig implements Serializable {
	
	/**
	 * Default serial version UID
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * A unique name for this configuration class
	 */
	private String identifier;

	/**
	 * The list containing the configured measurement phases.
	 */
	private List<MeasurementPhase> phases;
	
	/**
	 * The array containing the configured metric-attached attributes.
	 */
	private String[] attributes;
	
	/**
	 * Establishes the unit (after scaling measurement values) in which all 
	 * measures should be presented in reports and charts.
	 */
	private String measureUnit;
	
	/**
	 * The path name for the configuration (properties) file
	 */
	private final String configFile;
	
	protected MetricConfig(String configFile) {	
		
		this.configFile = configFile;
		
		try {
			HashMap<String, MeasurementPhase> autoConfiguredPhases = 
					configureClassBasics();
			phases = configurePhases();
			
			setLevelsInPhases(autoConfiguredPhases);
			validatePhases();
			
		} catch (MetricException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method must be overwritten in order to establish the list that 
	 * contains the configured measurement phases.
	 * 
	 * @return A list containing the configured measurement phases.
	 */
	public abstract List<MeasurementPhase> configurePhases() throws MetricException;
	
	/**
	 * This method must be overwritten in order to give meaning to the 
	 * measurement values; that is, give a more comprehensive scale to the
	 * measured values (e.g., scale 1417633315836ms to 1.41s).
	 * 
	 * @param measurementValue
	 * @return
	 */
	public abstract double scaleValue(long measurementValue);
	
	/**
	 * Reads the configuration file (a properties file) and creates the general 
	 * structure for this class, containing identifier, attributes, measurement
	 * unit, stages, levels, and phases.
	 * @throws 					IOException It's thrown if the configuration file 
	 * 							does not exist
	 * @throws MetricException 
	 */
	private HashMap<String, MeasurementPhase> configureClassBasics() throws IOException, MetricException{
		HashMap<String, MeasurementPhase> configuredPhases = 
				new HashMap<String, MeasurementPhase>();
		
		// Config file
		Properties configFileProps = new Properties();
		InputStream inputStream = 
				getClass().getClassLoader().getResourceAsStream(configFile);
		
		if(inputStream != null){
			
			configFileProps.load(inputStream);
			
			String identifierProp = configFileProps.getProperty("identifier");
			String attributesProp = configFileProps.getProperty("attributes");
			String measureUnitProp = configFileProps.getProperty("measureunit");
			String stagesProp = configFileProps.getProperty("stages");
			String levelsProp = configFileProps.getProperty("levels");
			String phasesProp = configFileProps.getProperty("phases");
			
			String commaRegex = "\\s*,\\s*";
			
			// Config class ID
			if(isValidIdentifier(identifierProp))
				identifier = identifierProp;
			else{
				throw new MetricException(14, "A Java valid identifier was "
						+ "expected. \"" + identifier + "\" was found");
			}
			
			// Configured attributes
			attributes = 
					attributesProp.replaceAll(commaRegex, ",").split(",");
			
			for (int i = 0; i < attributes.length; i++) {
				for (int j = i + 1; j < attributes.length; j++) {
					if(attributes[i].equals(attributes[j])){
						throw new MetricException(7, 
								"The metric-attached attributes must be unique in " +
								"the attributes array. Remove the duplicate element \"" + 
								attributes[j] + "\"");
					}
				}
			}
			
			// Measurement unit
			measureUnit = measureUnitProp;
			
			// Stages (name[:shared])
			HashMap<String, MeasurementPhase.Stage> configuredStages = 
					new HashMap<String, MeasurementPhase.Stage>();
			String[] stageExps =
					stagesProp.replaceAll(commaRegex, ",").split(",");
			
			for (int i = 0; i < stageExps.length; i++) {
				String tempStageExp = stageExps[i];
				MeasurementPhase.Stage tempStage;
				boolean shared = false;
				
				if(tempStageExp.endsWith(":shared")){
					shared = true;
					tempStageExp = 
							tempStageExp.substring(0, tempStageExp.length() - 7);
				}
				
				if(isValidIdentifier(tempStageExp)){
					tempStage = new MeasurementPhase.Stage(tempStageExp, shared);
					configuredStages.put(tempStageExp, tempStage);
				}else
					throw new MetricException(14, "A Java valid identifier was "
							+ "expected. \"" + tempStageExp + "\" was found");
			}

			// Levels (name:(stage1, stage2, stage3))
			HashMap<String, MeasurementPhase.Level> configuredLevels = 
					new HashMap<String, MeasurementPhase.Level>();
			String[] levelExps = 
					levelsProp.replaceAll(commaRegex, ",").split("\\),");
			
			for (int i = 0; i < levelExps.length; i++) {
				String tempLevelExp = levelExps[i];
				String[] tempLevelExpParts = tempLevelExp.split(":");
				
				tempLevelExpParts[1] = 
						tempLevelExpParts[1]
								.substring(1, 
										tempLevelExpParts[1].length() 
										- (tempLevelExpParts[1].endsWith(")") ? 1 : 0));
				
				if(tempLevelExpParts.length != 2)
					throw new MetricException(15, "Expected colon in level "
							+ "expression: a level must be registered along"
							+ " with its stages");

				String[] tempStageNames = tempLevelExpParts[1].split(",");
				MeasurementPhase.Level tempLevel;
				MeasurementPhase.Stage[] tempLevelStages = 
						new MeasurementPhase.Stage[tempStageNames.length];
				
				for (int j = 0; j < tempStageNames.length; j++) {
					MeasurementPhase.Stage tempStage = 
							configuredStages.get(tempStageNames[j]);
					if(tempStage != null)
						tempLevelStages[j] = tempStage;
					else
						throw new MetricException(16, "The specified stage \"" 
								+ tempStageNames[j] + "\" in level \"" 
								+ tempLevelExpParts[0] + "\" was not registered as a stage");
				}
				
				if(isValidIdentifier(tempLevelExpParts[0])){
					tempLevel = 
							new MeasurementPhase.Level(
									tempLevelExpParts[0], tempLevelStages);
					configuredLevels.put(tempLevelExpParts[0], tempLevel);
				}else
					throw new MetricException(14, "A Java valid identifier was "
							+ "expected. \"" + tempLevelExpParts[0] + "\" was found");
				
			}
			
			// Phases
			String[] phaseExps = phasesProp
					.replaceAll(commaRegex, ",")
					.split("(\\)|\\]),");
			
			if(phaseExps.length == 0){
				throw new MetricException(20, "There must be at least one measurement phase");
			}
			
			for (int i = 0; i < phaseExps.length; i++) {
				String tempPhaseExp = phaseExps[i]; // sorting:(start,end
				MeasurementPhase tempPhase = null;
				
				if(tempPhaseExp.endsWith(")") || tempPhaseExp.endsWith("]"))
					tempPhaseExp = tempPhaseExp.substring(0, tempPhaseExp.length() - 1);
				
				String[] tempPhaseExpParts = tempPhaseExp.split(":");
				if(tempPhaseExpParts.length != 2)
					throw new MetricException(15, "Expected colon in level "
							+ "expression: a level must be registered along"
							+ " with its stages");
				
				if(tempPhaseExpParts[1].startsWith("(")){ // phase without levels
					
					tempPhaseExpParts[1] = tempPhaseExpParts[1].substring(1);
					String[] tempStageNames = tempPhaseExpParts[1].split(",");
					MeasurementPhase.Stage[] phaseStages = 
							new MeasurementPhase.Stage[tempStageNames.length];
					
					for (int j = 0; j < tempStageNames.length; j++) {
						MeasurementPhase.Stage tempStage = configuredStages.get(tempStageNames[j]);
						if(tempStage != null)
							phaseStages[j] = tempStage;
						else
							throw new MetricException(16, "The specified stage \"" 
									+ tempStageNames[j] + "\" in phase \"" 
									+ tempPhaseExpParts[0] + "\" was not registered as a stage");
					}
					
					tempPhase = new MeasurementPhase(tempPhaseExpParts[0], phaseStages);
										
				}else if(tempPhaseExpParts[1].startsWith("[")){ // phase with levels
					
					tempPhaseExpParts[1] = tempPhaseExpParts[1].substring(1);
					String[] tempLevelNames = tempPhaseExpParts[1].split(",");
					MeasurementPhase.Level[] phaseLevels = 
							new MeasurementPhase.Level[tempLevelNames.length];
					
					for (int j = 0; j < tempLevelNames.length; j++) {
						MeasurementPhase.Level tempLevel = configuredLevels.get(tempLevelNames[j]);
						if(tempLevel != null)
							phaseLevels[j] = tempLevel;
						else
							throw new MetricException(16, "The specified level \"" 
									+ tempLevelNames[j] + "\" in phase \"" 
									+ tempPhaseExpParts[0] + "\" was not registered as a level");
					}
					
					tempPhase = new MeasurementPhase(tempPhaseExpParts[0], phaseLevels);
				}
				
				if(isValidIdentifier(tempPhaseExpParts[0])){
					configuredPhases.put(tempPhaseExpParts[0], tempPhase);
				}else
					throw new MetricException(14, "A Java valid identifier was "
							+ "expected. \"" + tempPhaseExpParts[0] + "\" was found");
					
			}
			
		}else{
			throw new FileNotFoundException("The configuration file " 
					+ configFile + " was not found in classpath");
		}
		
		return configuredPhases;
	}
	
	private void setLevelsInPhases(HashMap<String, MeasurementPhase> autoConfiguredPhases) throws MetricException{
		for (MeasurementPhase tempPhase : phases) {
			MeasurementPhase tempAutoConfPhase = autoConfiguredPhases.get(tempPhase.getName());
			if(tempAutoConfPhase != null){
				
				tempPhase.setLevels(tempAutoConfPhase.getLevels());
				
			}else{
				throw new MetricException(19, "Phase \"" + tempPhase.getName() 
						+ "\" must be configured both in properties file as "
						+ "well in the configuration class. "
						+ "Note this validation is case sensitive");
			}
		}
	}
	
	/**
	 * Validates that provided identifier is a Java valid identifier
	 * 
	 * @param str	The provided identifier
	 * @return		true if the identifier is a Java valid identifier, 
	 * 				false if opposite
	 */
	private boolean isValidIdentifier(String str){
		boolean b = true;
		
		if(str != null && str.length() >= 1){
			if(Character.isJavaIdentifierStart(str.charAt(0))){
				if(str.length() > 1)
					for(int i = 1; i < str.length(); i++)
						if(!Character.isJavaIdentifierPart(str.charAt(i)))
							b = false;
			}else{
				b = false;
			}
		}else{
			b = false;
		}
		
		return b;
	}
	
	/**
	 * Validates if all the identifiers for measurement phases are unique among 
	 * them.
	 * 
	 * @throws MetricException		It's thrown in case two identifiers (among
	 * 								measurement phases) are equal
	 */
	private void validatePhases()  throws MetricException {
		if(phases.isEmpty()){
			throw new MetricException(20, "There must be at least one measurement phase");
		}
		
		for (int i = 0; i < phases.size(); i++) {
			for (int j = i + 1; j < phases.size(); j++) {
				if(phases.get(i).getName().equals(phases.get(j).getName())){
					throw new MetricException(7, 
							"The measurement phases must be unique in the phases " +
							"list. Remove the duplicate element \"" 
							+ phases.get(j).getName() + "\"");
				}
			}
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((getIdentifier() == null) ? 0 : getIdentifier().hashCode());
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
		MetricConfig other = (MetricConfig) obj;
		if (getIdentifier() == null) {
			if (other.getIdentifier() != null)
				return false;
		} else if (!getIdentifier().equals(other.getIdentifier()))
			return false;
		return true;
	}

	public List<MeasurementPhase> getPhases() {
		return phases;
	}

	public String[] getAttributes() {
		return attributes;
	}
	
	public String getIdentifier(){
		return identifier;
	}
	
	public String getMeasureUnit(){
		return measureUnit;
	}

}
