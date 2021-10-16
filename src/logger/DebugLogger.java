package logger;

public class DebugLogger implements ILog{
	private final LogBase logger;
	private static final String LOG_FILE_NAME = "pasur.log";
	public DebugLogger() {
		this.logger = new LogBase(LOG_FILE_NAME, ELogLevel.DEBUG);
	}

	@Override
	public void log(String format, Object... args) {
		this.logger.log(format, args);
	}

	@Override
	public boolean CheckLevel(ELogLevel level) {
		return this.logger.CheckLevel(level);
	}
}
