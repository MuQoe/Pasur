package logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * The logger instance that logs the info to a specific file
 */
public class PasurTrainerLogger implements ILog{
	// file name that needs to log to
	private static final String LOG_FILE_NAME = "pasur.log";
	// options to log to console or the file
	private static final boolean IS_OUT_PUT_TO_CONSOLE = false;
	// the file writer
	private PrintWriter writer = null;

	public PasurTrainerLogger() {
		try{
			writer = new PrintWriter(new FileWriter(LOG_FILE_NAME),true);
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	/**
	 * logging the log information to the log file
	 * @param format the string that needs to format
	 * @param args the argument that needs to format
	 */
	public void log(String format, Object ... args){
		String OutPut = String.format(format, args);
		if(IS_OUT_PUT_TO_CONSOLE){
			System.out.println(OutPut);
		} else {
			writer.println(OutPut);
		}
	}
}
