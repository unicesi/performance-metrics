package co.edu.icesi.driso.measurement.metrics;

import java.io.Serializable;

/**
 * 
 * @author Miguel A. Jiménez
 * @date 26/11/2014
 */
public class MetricException extends Exception implements Serializable {
	
	/**
	 * Default serial version UID
	 */
	private static final long serialVersionUID = 1L;
	
	private final int errorCode;

	public MetricException(int errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
	
	public MetricException(int errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public int getErrorCode() {
		return errorCode;
	}
	
	@Override
	public String toString(){
		return "[" + errorCode + "] " + getMessage();
	}

}
