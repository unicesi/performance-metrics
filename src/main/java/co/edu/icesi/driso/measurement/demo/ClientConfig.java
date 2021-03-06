package co.edu.icesi.driso.measurement.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import co.edu.icesi.driso.measurement.metrics.Measurement;
import co.edu.icesi.driso.measurement.metrics.MeasurementPhase;
import co.edu.icesi.driso.measurement.metrics.MetricConfig;
import co.edu.icesi.driso.measurement.metrics.MetricException;

/**
 * This class provides an example on how to extend the metric-configuration
 * class MetricConfig.
 * 
 * @see co.edu.icesi.driso.measurement.metrics.MetricConfig
 * @author Miguel A. Jiménez
 * @date 26/11/2014
 */
public class ClientConfig extends MetricConfig {

	public ClientConfig(String configFile) {
		super(configFile);
	}

	/**
	 * Default serial version UID
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public List<MeasurementPhase> configurePhases() throws MetricException {
		
		// configured phases
		List<MeasurementPhase> phases = new ArrayList<MeasurementPhase>();
		
		MeasurementPhase sorting = new MeasurementPhase("Sorting"){
		
			private static final long serialVersionUID = 1L;
			
			@Override
			public long calculateLevelValue(List<Measurement.Entry> entries){
				long value = 0;
				/*
				 *  "measurements" contains two elements that correspond to
				 *  the two stages in the level, in the same order they were
				 *  registered: 0:start, 1:end
				 */
				Measurement.Entry startEntry = entries.get(0);
				Measurement.Entry endEntry = entries.get(1);
				
				Measurement start = startEntry.getMeasurement();
				Measurement end = endEntry.getMeasurement();
				
				if(start != null && end != null){
					// children's identifiers (e.g., grid0, grid1, grid2, etc.)
					Set<String> keys = start.getChildValues().keySet();
					
					/*
					 * In case "keys" is empty, the levels's value should be the one
					 * measured by the child itself
					 */
					if(!keys.isEmpty()){
						for (String key: keys) {
							long temp = end.getChildValues().get(key) - start.getChildValues().get(key);
							if(temp > value){
								value = temp;
							}
						}
					}else{
						value = end.getOwnValue() - start.getOwnValue();
					}
				}
				
				return value;
			}
			
			@Override
			public long calculatePhaseValue(HashMap<String, Long> levelValues){
				long value = 0;
				/*
				 * "levelValues" contains the registered levels (key) along with
				 * its corresponding calculated value (value)
				 */
				
				// level identifiers
				Set<String> levelIds = levelValues.keySet();
				
				for (String levelId: levelIds) {
					Long levelValue = levelValues.get(levelId);
					value += levelValue;
				}
				
				return value;
			}
		};
		
		MeasurementPhase invented = new MeasurementPhase("Distribution"){
			
			private static final long serialVersionUID = 1L;
			
			@Override
			public long calculateLevelValue(List<Measurement.Entry> entries){
				long value = 0;
				
				/*
				 *  "measurements" contains two elements that correspond to
				 *  the two stages in the level, in the same order they were
				 *  registered: 0:start, 1:end
				 */
				Measurement.Entry startEntry = entries.get(0);
				Measurement.Entry endEntry = entries.get(1);
				
				Measurement start = startEntry.getMeasurement();
				Measurement end = endEntry.getMeasurement();
				
				if(start != null && end != null){
					// children's identifiers (e.g., grid0, grid1, grid2, etc.)
					Set<String> keys = start.getChildValues().keySet();
					
					/*
					 * In case "keys" is empty, the levels's value should be the one
					 * measured by the child itself
					 */
					if(!keys.isEmpty()){
						for (String key: keys) {
							long temp = end.getChildValues().get(key) - start.getChildValues().get(key);
							if(temp > value){
								value = temp;
							}
						}
					}else{
						value = end.getOwnValue() - start.getOwnValue();
					}
				}
				
				return value;
			}
			
			@Override
			public long calculatePhaseValue(HashMap<String, Long> levelValues){
				long value = 0;
				/*
				 * "levelValues" contains the registered levels (key) along with
				 * its corresponding calculated value (value)
				 */
				
				// level identifiers
				Set<String> levelIds = levelValues.keySet();
				
				for (String levelId: levelIds) {
					Long levelValue = levelValues.get(levelId);
					value += levelValue;
				}
				
				return value;
			}
		};
		
		MeasurementPhase merge = new MeasurementPhase("Merge"){
			
				private static final long serialVersionUID = 1L;
				
				@Override
				public long calculateLevelValue(List<Measurement.Entry> entries){
					long value = 0;
					
					Measurement.Entry startEntry = entries.get(0);
					Measurement.Entry endEntry = entries.get(1);
					
					Measurement start = startEntry.getMeasurement();
					Measurement end = endEntry.getMeasurement();
					
					if(start != null && end != null){
						// children's identifiers (e.g., grid0, grid1, grid2, etc.)
						Set<String> keys = start.getChildValues().keySet();
						
						/*
						 * In case "keys" is empty, the level's value should be the one
						 * measured by the child itself
						 */
						if(!keys.isEmpty()){
							for (String key: keys) {
								long temp = end.getChildValues().get(key) - start.getChildValues().get(key);
								if(temp > value){
									value = temp;
								}
							}
						}else{
							value = end.getOwnValue() - start.getOwnValue();
						}
					}
					
					return value;
				}
				
				@Override
				public long calculatePhaseValue(HashMap<String, Long> levelValues){
					long value = 0;
					/*
					 * "levelValues" contains the registered levels (key) along with
					 * its corresponding calculated value (value)
					 */
					
					// level identifiers
					Set<String> levelIds = levelValues.keySet();
					
					for (String levelId: levelIds) {
						Long levelValue = levelValues.get(levelId);
						value += levelValue;
					}
					
					return value;
				}
		};
		
		phases.add(sorting);
		phases.add(merge);
		phases.add(invented);
		
		return phases;
	}

	@Override
	public double scaleValue(long measurementValue) {
		return measurementValue / 10.0;
	}
	
}
