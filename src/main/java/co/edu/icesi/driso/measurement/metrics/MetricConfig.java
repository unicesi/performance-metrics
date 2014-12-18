package co.edu.icesi.driso.measurement.metrics;

import java.io.Serializable;
import java.util.List;

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
	 * The list containing the configured measurement phases.
	 */
	private List<MeasurementPhase> phases;
	
	/**
	 * The array containing the configured metric-attached attributes.
	 */
	private String[] attributes;
	
	protected MetricConfig() {		
		
		try {
			attributes = configureAttributes();
			phases = configurePhases();
			
			validate();
			
		} catch (MetricException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method must be overwritten by the developer in order to establish
	 * the list that contains the configured measurement phases.
	 * 
	 * @return		A list containing the configured measurement phases.
	 */
	public abstract List<MeasurementPhase> configurePhases() throws MetricException;

	/**
	 * This method must be overwritten by the developer in order to establish
	 * the list that contains the configured metric-attached attributes.
	 * 
	 * @return		An array containing the configured metric-attached attributes.
	 */
	public abstract String[] configureAttributes();
	
	/**
	 * This method must be overwritten by the developer, in order to establish
	 * a way to differentiate a configuration class from another. This identifier
	 * should be unique among all metric-configuration classes.
	 * 
	 * @return		The metric-configuration class's identifier
	 */
	public abstract String getIdentifier();
	
	/**
	 * This method must be overwritten by the developer in order to give meaning
	 * to the measurement values; that is, give a more comprehensive scale to the
	 * measured values (e.g., scale 1417633315836ms to 1.41s).
	 * 
	 * @param measurementValue
	 * @return
	 */
	public abstract double scaleValue(long measurementValue);
	
	/**
	 * This method must be overwritten by the developer in order to establish
	 * the unit (after scaling the measurement values) in which all the measures 
	 * should be presented in the report and chart.
	 * 
	 * @return		An string containing the measure unit, after scaling the values
	 */
	public abstract String getMeasureUnit();
	
	/**
	 * Validates if all the identifiers for measurement phases and attributes are
	 * unique among them.
	 * 
	 * @throws MetricException		It's thrown in case two identifiers (among
	 * 								measurement phases, or among metric-attached
	 * 								attributes) are equal
	 */
	public void validate()  throws MetricException {
		// First: validate that all phase's identifier are unique
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
		
		// Second: validate that all attribute's name are unique
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

}
