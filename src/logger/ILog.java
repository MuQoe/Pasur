package logger;

public interface ILog {

	public void log(String format, Object ... args);
	public boolean CheckLevel(ELogLevel level);
}
