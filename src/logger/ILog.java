package logger;

public interface ILog {
	/**
	 * logging the log information to the log file
	 * @param format the string that needs to format
	 * @param args the argument that needs to format
	 */
	void log(String format, Object ... args);
}
