/**
 * DBF reads a .fpt file and extracts various data from it.
 * An .fpt is also called a "memo" file and is sometimes referred to from
 * a .dbf file.
 * 
 * @author Andrea Chiavazza
 * Copyright 2010 licensed under GPL version 3 obtainable from http://www.gnu.org/licenses/gpl.html
 */
package cuxna.dbf;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class FPT {
	private int blockSize;
	private String fileName;
	private BufferedInputStream BIStream;
	private DataInputStream DIStream;
	private int blockPos;

	/**
	 * Creates an instance given a filename;
	 * 
	 * @param name
	 *            the filename
	 * @throws IOException
	 */
	FPT(String name) throws IOException {
		fileName = name;
		BIStream = new BufferedInputStream(new FileInputStream(fileName));
		DIStream = new DataInputStream(BIStream);
		if (DIStream.markSupported()) {
			DIStream.mark(32);
		}
		// read header
		DIStream.skip(4); // next free block
		DIStream.skip(2); // unused
		blockSize = DIStream.readShort();
		// stream.skip(504); // unused
		if (DIStream.markSupported()) {
			DIStream.reset();
			blockPos = 0;
		} else {
			DIStream.close();
		}
	}

	/**
	 * Reads the string found at a given block number.
	 * 
	 * @param blockNumber
	 * @return the string read
	 * @throws IOException
	 */
	String getString(int blockNumber) throws IOException {
		if (blockNumber <= blockPos || !DIStream.markSupported()) {
			DIStream.close();
			BIStream = new BufferedInputStream(new FileInputStream(fileName));
			DIStream = new DataInputStream(BIStream);
			blockPos = 0;
		}
		// find block
		DIStream.skip(blockNumber * (blockSize - blockPos));
		blockPos = blockSize;
		// read block
		int signature = DIStream.readInt(); // block signature 0-picture 1-text
		if (signature == 0) {
			throw new IOException("Picture memos not supported");
		}
		int memoLength = DIStream.readInt();
		String result = DBF.readText(DIStream, memoLength);
		return result;
	}

	public void close() throws IOException {
		BIStream.close();
	}
}