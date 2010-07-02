/**
 * DBF reads a .dbf file and extracts various data from it.
 * 
 * @author Andrea Chiavazza
 * Copyright 2010 licensed under GPL version 3 obtainable from http://www.gnu.org/licenses/gpl.html
 */
package cuxna.dbf;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DBF {
	private DataInputStream stream;
	private FPT fpt;
	private int numFields;
	private int numRecords;
	private Map<String, String[]> stringColumns = new HashMap<String, String[]>();
	private Map<String, int[]> intColumns = new HashMap<String, int[]>();

	/**
	 * Creates an object representing data contained in a DBF file.
	 * 
	 * @param baseName
	 *            the name of the file without the extension
	 * @throws IOException
	 */
	public DBF(String baseName) throws IOException {
		String dbfFileName = findExt(baseName, "dbf");
		stream = new DataInputStream(new BufferedInputStream(
				new FileInputStream(dbfFileName)));
		// read header
		int dbType = stream.read() + 128;
		stream.skip(3); // last updated YYMMDD
		numRecords = Integer.reverseBytes(stream.readInt());
		int firstRecord = Short.reverseBytes(stream.readShort());
		stream.skip(2); // record size
		stream.skip(16); // reserved
		stream.skip(1); // table flags;
		stream.skip(1); // code page mark
		stream.skip(2); // reserved

		// read fields description
		numFields = (firstRecord - 32) / 32;
		if (dbType >= 0x83) {
			// Load the memo file
			String fptFileName = findExt(baseName, "fpt");
			fpt = new FPT(fptFileName);
		}

		int[] length = new int[numFields];
		char[] type = new char[numFields];
		String[] name = new String[numFields];

		for (int n = 0; n < numFields; n++) {
			String fieldName = readText(stream, 11);
			char fieldType = (char) stream.read();
			stream.skip(4); // field displacement;
			int fieldLength = stream.read();
			stream.skip(1); // number of decimal places
			stream.skip(1); // field flags
			stream.skip(13); // reserved

			length[n] = fieldLength;
			type[n] = fieldType;
			name[n] = fieldName;

			switch (fieldType) {
			case 'M':
			case 'C':
				stringColumns.put(fieldName, new String[numRecords]);
				break;
			case 'N':
				intColumns.put(fieldName, new int[numRecords]);
				break;
			}
		}
		stream.skip(1); // Header record terminator (should be 0x0D)
		// dbfStream.skip(263); // back-link information. doesn't seem to exist
		// read records
		for (int r = 0; r < numRecords; r++) {
			stream.skip(1); // delete-flag byte
			for (int c = 0; c < numFields; c++) {
				String d = readText(stream, length[c]);
				if (!d.isEmpty()) {
					switch (type[c]) {
					case 'C':
						stringColumns.get(name[c])[r] = d;
						break;
					case 'M':
						stringColumns.get(name[c])[r] = fpt.getString(Integer
								.parseInt(d));
						break;
					case 'N':
						intColumns.get(name[c])[r] = Integer.parseInt(d);
						break;
					}
				}
			}
		}
		fpt.close();
		stream.close();
	}

	/**
	 * @return the number of records(rows) in the database.
	 */
	public int getNumRecords() {
		return numRecords;
	}

	/**
	 * @return the number of fields(columns) in the database.
	 */
	public int getNumFields() {
		return numFields;
	}

	/**
	 * Reads a numeric value.
	 * 
	 * @param row
	 *            of the database
	 * @param column
	 *            of the database
	 * @return the numeric value
	 */
	public int readInt(int row, String column) {
		return intColumns.get(column)[row];
	}

	/**
	 * 
	 * @param row
	 *            of the database
	 * @param column
	 *            of the database
	 * @return the string
	 */
	public String readString(int row, String column) {
		return stringColumns.get(column)[row];
	}

	/**
	 * Utility function also used by the FPT class. It reads a string from an
	 * InputStream and returns it.
	 * 
	 * @param stream
	 *            the InputStream to read from
	 * @param size
	 *            how many bytes to read
	 * @return the string read
	 * @throws IOException
	 */
	static String readText(InputStream stream, int size) throws IOException {
		byte[] buf = new byte[size];
		stream.read(buf, 0, size);
		return new String(buf, "ISO-8859-1").trim();
	}

	/**
	 * 
	 * @param fileBase
	 *            the name of the file without the extension
	 * @param ext
	 *            the name of the extension
	 * @return the full name of the file, if one is found
	 * @throws FileNotFoundException
	 */
	private String findExt(String fileBase, String ext)
			throws FileNotFoundException {
		for (String e : new String[] { ext.toLowerCase(), ext.toUpperCase() }) {
			String name = fileBase + "." + e;
			if (new File(name).exists()) {
				return name;
			}
		}
		throw new FileNotFoundException();
	}
}