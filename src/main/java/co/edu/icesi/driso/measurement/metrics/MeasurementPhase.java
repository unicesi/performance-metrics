package co.edu.icesi.driso.measurement.metrics;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * 
 * @author Miguel A. Jiménez
 * @date 26/11/2014
 */
public class MeasurementPhase implements Serializable {
	/**
	 * This class encapsulates an stage, a sequential part of a measurement
	 * phase.
	 * 
	 * @author Miguel A. Jiménez
	 * @date 26/11/2014
	 */
	public static class Stage implements Serializable {
		
		/**
		 * Default serial version UID
		 */
		private static final long serialVersionUID = 1L;
		
		/**
		 * The stage's name and also identifier (must be unique)
		 */
		private final String name;
		
		/**
		 * A shared stage is one that can be merged from child to father
		 */
		private final boolean shared;
		
		public Stage(String name, boolean shared){
			this.name = name;
			this.shared = shared;
		}
		
		public Stage(Stage other){
			this(other.name, other.shared);
		}
				
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + (shared ? 1231 : 1237);
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
			Stage other = (Stage) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (shared != other.shared)
				return false;
			return true;
		}

		public String getName(){
			return name;
		}
		
		public boolean isShared(){
			return shared;
		}
		
		@Override
		public String toString(){
			return name;
		}
	}
	
	/**
	 * This class encapsulates a level, a mechanism to measure stages in 
	 * different kinds of depth.
	 * 
	 * @author Miguel A. Jiménez
	 * @date 03/12/2014
	 */
	public static class Level implements Serializable {

		/**
		 * Default serial version UID
		 */
		private static final long serialVersionUID = 1L;
		
		/**
		 * Default name to be used when a measurement phase is created without
		 * levels; this name is assigned to an automatically created one.
		 */
		public static final String DEFAULT_NAME = "";
		
		/**
		 * The stage's name and also identifier (must be unique)
		 */
		private final String name;
		
		/**
		 * The stages composing the measurement phase
		 */
		private final Stage[] stages;
		
		public Level(String name, Stage[] stages){
			this.name = name;
			this.stages = stages;
		}
		
		public String getName(){
			return name;
		}
		
		public Stage[] getStages(){
			return stages;
		}
		
		@Override
		public String toString(){
			return name;
		}
	}
	
	/**
	 * Default serial version UID
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The measurement phase's name and also identifier (must be unique)
	 */
	private final String name;
	
	/**
	 * The levels composing the measurement phase
	 */
	private final Level[] levels;
	
	public MeasurementPhase(String name, Level[] levels) throws MetricException {
		this.name = name;
		this.levels = levels;
		
		if(levels.length == 0){
			throw new MetricException(9, 
					"The levels array must contain at least one element");
		}else{
			validateLevels();
		}
	}
	
	public MeasurementPhase(String name, Stage[] stages) {
		this.name = name;
		
		Level defaultLevel = new Level(Level.DEFAULT_NAME, stages);
		levels = new Level[]{defaultLevel};
	}
	
	private void validateLevels() throws MetricException {
		MeasurementPhase.Stage[] _stages = levels[0].getStages();
		for (int i = 0; i < levels.length; i++) {
			if(levels[i].getName().isEmpty()){
				throw new MetricException(11, "All levels must be given a name");
			}
			
			MeasurementPhase.Stage[] tempStages = levels[i].getStages();
			if(!Arrays.equals(_stages, tempStages)){
				throw new MetricException(10, "All levels in a measurement phase"
						+ " must contain the same array of stages");
			}
		}
	}
	
	/**
	 * This method must be overwritten in the measurement phase creation. In here,
	 * developers must specify how to calculate the final value for level in the
	 * measurement phase, having into account that one level has several stages, 
	 * and each stage has one value; also, if it being shared, several values regarding
	 * the metric's children.
	 * 
	 * @param measurements	The measurements corresponding to each stage of a level
	 * @return				The measurement value calculated for a level
	 */
	public long calculateLevelValue(List<Measurement.Entry> measurements){
		return 0;
	}
	
	/**
	 * This method must be overwritten in the measurement phase creation. In here,
	 * developers must specify how to calculate the final value for the entire
	 * measurement phase, having into account that one phase has several levels,
	 * and for each level there is a value calculation. 
	 * 
	 * @param levelValues	The calculated values regarding the phase's levels	
	 * @return				The measurement value calculated for a phase
	 */
	public long calculatePhaseValue(HashMap<String, Long> levelValues){
		return 0;
	}
	
	/**
	 * Shows the stages along with their corresponding measurements
	 * 
	 * @param measurements	The measurements corresponding to each level of the phase
	 * @return				A string representation of the phase and its stages (with values attached)
	 */
	public String toString(HashMap<String, List<Measurement.Entry>> measurements){
		Set<String> levelIds = measurements.keySet();
		StringBuilder sb = new StringBuilder();
		String stageSeparator = "; ", levelSeparator = "; ";
		int levelCounter = 0;
		sb.append("{");
		
		for (Iterator<String> iterator = levelIds.iterator(); iterator.hasNext();) {
			String levelName = iterator.next();
			sb.append(levelName + "=[");

			levelSeparator = levelCounter == levelIds.size() - 1 ? "" : levelSeparator;
			List<Measurement.Entry> levelMeasurements = measurements.get(levelName);
			
			for (int i = 0; i < levelMeasurements.size(); i++) {
				if(i == levelMeasurements.size() - 1){
					stageSeparator = "";
				}
				Measurement.Entry tempMeasurementEntry = levelMeasurements.get(i);
				sb.append(tempMeasurementEntry.getStageName() + "=");
				sb.append(
						tempMeasurementEntry.getMeasurement() != null ? 
								tempMeasurementEntry.getMeasurement().toString() : "NA");
				sb.append(stageSeparator);
			}
			
			stageSeparator = "; ";
			
			sb.append("]" + levelSeparator);
		}
		
		sb.append("}");
		return sb.toString();
	}
	
	@Override
	public String toString(){
		return name;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((name == null) ? 0 : name.hashCode());
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
		MeasurementPhase other = (MeasurementPhase) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public String getName(){
		return name;
	}
	
	public Level[] getLevels(){
		return levels;
	}

}
