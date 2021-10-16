package logger;

import java.util.ArrayList;

public class LogSubject {
	private static LogSubject _instance = null;
	private final ArrayList<ILog> LogObservers = new ArrayList<>();
	private ELogLevel level;

	private LogSubject(ELogLevel level) {
		this.level = level;
	}

	public static LogSubject getInstance() {
		if (_instance == null) {
			synchronized(LogSubject.class) {
				if (_instance == null) {
					// Set the Default Level to DEBUG level
					_instance = new LogSubject(ELogLevel.DEBUG);
				}
			}
		}
		return _instance;
	}

	public void setLevel(ELogLevel level) {
		this.level = level;
	}

	public void Attach(ILog observer){
		this.LogObservers.add(observer);
	}

	public void Detach(ILog observer){
		this.LogObservers.remove(observer);
	}

	public void notify(String message, Object... args){
		for(ILog l : LogObservers){
			if(l.CheckLevel(this.level)){
				l.log(message, args);
			}
		}
	}

}
