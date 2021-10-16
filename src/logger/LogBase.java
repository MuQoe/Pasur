package logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class LogBase implements ILog{
	private static final boolean IS_OUT_PUT_TO_CONSOLE = false;
	private PrintWriter writer = null;
	private ELogLevel LogLevel;

	public LogBase(String OutPutFileName, ELogLevel level) {
		this.LogLevel = level;
		try{
			writer = new PrintWriter(new FileWriter(OutPutFileName),true);
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	public void log(String format, Object ... args){
		String OutPut = String.format(format, args);
		if(IS_OUT_PUT_TO_CONSOLE){
			System.out.println(OutPut);
		} else {
			writer.println(OutPut);
		}
	}

	public void setLogLevel(ELogLevel logLevel) {
		LogLevel = logLevel;
	}

	@Override
	public boolean CheckLevel(ELogLevel level) {
		return this.LogLevel.ordinal() <= level.ordinal();
	}
}
