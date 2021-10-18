package logger;

import java.util.ArrayList;

/**
 * The observer pattern Log Subject
 */
public class LogSubject {
	private static LogSubject _instance = null;
	private final ArrayList<ILog> LogObservers = new ArrayList<>();

	private LogSubject() { }

	/**
	 * Singleton pattern "GetInstance"
	 * @return the static instance of this class
	 */
	public static LogSubject getInstance() {
		if (_instance == null) {
			synchronized(LogSubject.class) {
				if (_instance == null) {
					// Set the Default Level to DEBUG level
					_instance = new LogSubject();
				}
			}
		}
		return _instance;
	}

	/**
	 * adding a new observer to the subject
	 * @param observer the observer that needs to add
	 */
	public void Attach(ILog observer){
		this.LogObservers.add(observer);
	}

	/**
	 * remove an observer to the subject
	 * @param observer the observer that needs to remove
	 */
	public void Detach(ILog observer){
		this.LogObservers.remove(observer);
	}

	/**
	 * notify all observer to log the info
	 * @param message the log information that needs to log
	 * @param args the arguments that need to format
	 */
	public void notify(String message, Object... args){
		for(ILog l : LogObservers){
			l.log(message, args);
		}
	}

}
