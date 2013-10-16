/*
Copyright (c) 2008-2010 Daniel Marbach & Thomas Schaffter

We release this software open source under an MIT license (see below). If this
software was useful for your scientific work, please cite our paper(s) listed
on http://gnw.sourceforge.net.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package ch.epfl.lis.gnwgui;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/** This class defines a simple formatter to display log.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * 
 */
public class GnwConsoleSimpleFormatter extends Formatter {
	
	/** Universal separator, sensible to the OS used. */
	static final String lineSep = System.getProperty("line.separator");

	/** Constructor */
	public GnwConsoleSimpleFormatter() {}
	
	
	// ============================================================================
	// PUBLIC METHODS
	
	/**
	 * Define the format used to display the logs.
	 * Here, only the log text is returned (no date, level information, etc.
	 */
	public String format(LogRecord record) {

		StringBuffer buf = new StringBuffer(180);
		buf.append(formatMessage(record));
		buf.append(lineSep);

		Throwable throwable = record.getThrown();
		if (throwable != null) {
			StringWriter sink = new StringWriter();
			throwable.printStackTrace(new PrintWriter(sink, true));
			buf.append(sink.toString());
		}
		return buf.toString();
	}
}