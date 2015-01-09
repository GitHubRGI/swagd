/*  Copyright (C) 2014 Reinventing Geospatial, Inc
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>,
 *  or write to the Free Software Foundation, Inc., 59 Temple Place -
 *  Suite 330, Boston, MA 02111-1307, USA.
 */

/**
 * From RFC 2045 (http://www.ietf.org/rfc/rfc2045.txt):
 * <pre>
 * 6.8.  Base64 Content-Transfer-Encoding
 * 
 *    The Base64 Content-Transfer-Encoding is designed to represent
 *    arbitrary sequences of octets in a form that need not be humanly
 *    readable.  The encoding and decoding algorithms are simple, but the
 *    encoded data are consistently only about 33 percent larger than the
 *    unencoded data.  This encoding is virtually identical to the one used
 *    in Privacy Enhanced Mail (PEM) applications, as defined in RFC 1421.
 * 
 *    A 65-character subset of US-ASCII is used, enabling 6 bits to be
 *    represented per printable character. (The extra 65th character, "=",
 *    is used to signify a special processing function.)
 * 
 *    NOTE:  This subset has the important property that it is represented
 *    identically in all versions of ISO 646, including US-ASCII, and all
 *    characters in the subset are also represented identically in all
 *    versions of EBCDIC. Other popular encodings, such as the encoding
 *    used by the uuencode utility, Macintosh binhex 4.0 [RFC-1741], and
 *    the base85 encoding specified as part of Level 2 PostScript, do not
 *    share these properties, and thus do not fulfill the portability
 *    requirements a binary transport encoding for mail must meet.
 * 
 *    The encoding process represents 24-bit groups of input bits as output
 *    strings of 4 encoded characters.  Proceeding from left to right, a
 *    24-bit input group is formed by concatenating 3 8bit input groups.
 *    These 24 bits are then treated as 4 concatenated 6-bit groups, each
 *    of which is translated into a single digit in the base64 alphabet.
 *    When encoding a bit stream via the base64 encoding, the bit stream
 *    must be presumed to be ordered with the most-significant-bit first.
 *    That is, the first bit in the stream will be the high-order bit in
 *    the first 8bit byte, and the eighth bit will be the low-order bit in
 *    the first 8bit byte, and so on.
 * 
 *    Each 6-bit group is used as an index into an array of 64 printable
 *    characters.  The character referenced by the index is placed in the
 *    output string.  These characters, identified in Table 1, below, are
 *    selected so as to be universally representable, and the set excludes
 *    characters with particular significance to SMTP (e.g., ".", CR, LF)
 *    and to the multipart boundary delimiters defined in RFC 2046 (e.g.,
 *    "-").
 *    
 *                 Table 1: The Base64 Alphabet
 * 
 *      Value Encoding  Value Encoding  Value Encoding  Value Encoding
 *          0 A            17 R            34 i            51 z
 *          1 B            18 S            35 j            52 0
 *          2 C            19 T            36 k            53 1
 *          3 D            20 U            37 l            54 2
 *          4 E            21 V            38 m            55 3
 *          5 F            22 W            39 n            56 4
 *          6 G            23 X            40 o            57 5
 *          7 H            24 Y            41 p            58 6
 *          8 I            25 Z            42 q            59 7
 *          9 J            26 a            43 r            60 8
 *         10 K            27 b            44 s            61 9
 *         11 L            28 c            45 t            62 +
 *         12 M            29 d            46 u            63 /
 *         13 N            30 e            47 v
 *         14 O            31 f            48 w         (pad) =
 *         15 P            32 g            49 x
 *         16 Q            33 h            50 y
 * 
 *    The encoded output stream must be represented in lines of no more
 *    than 76 characters each.  All line breaks or other characters not
 *    found in Table 1 must be ignored by decoding software.  In base64
 *    data, characters other than those in Table 1, line breaks, and other
 *    white space probably indicate a transmission error, about which a
 *    warning message or even a message rejection might be appropriate
 *    under some circumstances.
 * 
 *    Special processing is performed if fewer than 24 bits are available
 *    at the end of the data being encoded.  A full encoding quantum is
 *    always completed at the end of a body.  When fewer than 24 input bits
 *    are available in an input group, zero bits are added (on the right)
 *    to form an integral number of 6-bit groups.  Padding at the end of
 *    the data is performed using the "=" character.  Since all base64
 *    input is an integral number of octets, only the following cases can
 *    arise: (1) the final quantum of encoding input is an integral
 *    multiple of 24 bits; here, the final unit of encoded output will be
 *    an integral multiple of 4 characters with no "=" padding, (2) the
 *    final quantum of encoding input is exactly 8 bits; here, the final
 *    unit of encoded output will be two characters followed by two "="
 *    padding characters, or (3) the final quantum of encoding input is
 *    exactly 16 bits; here, the final unit of encoded output will be three
 *    characters followed by one "=" padding character.
 * 
 *    Because it is used only for padding at the end of the data, the
 *    occurrence of any "=" characters may be taken as evidence that the
 *    end of the data has been reached (without truncation in transit).  No
 *    such assurance is possible, however, when the number of octets
 *    transmitted was a multiple of three and no "=" characters are
 *    present.
 * 
 *    Any characters outside of the base64 alphabet are to be ignored in
 *    base64-encoded data.
 * 
 *    Care must be taken to use the proper octets for line breaks if base64
 *    encoding is applied directly to text material that has not been
 *    converted to canonical form.  In particular, text line breaks must be
 *    converted into CRLF sequences prior to base64 encoding.  The
 *    important thing to note is that this may be done directly by the
 *    encoder rather than in a prior canonicalization step in some
 *    implementations.
 * </pre> 
 *
 *	@author Duff Means
 *	@version 1.0, January 7, 2009
 */
package com.rgi.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class BinUtil {
	private final static String MAP = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

	/**
	 * Encodes the contents of the InputStream using Base64, and writes the
	 * result to the OutputStream.
	 * 
	 * @param in
	 *            The data to be encoded
	 * @param out
	 *            The destination for encoded data
	 * @return the number of bytes encoded
	 * @throws IOException
	 *             If there is a problem reading from or writing to the provided
	 *             streams
	 */
	public static int encode(InputStream in, OutputStream out) throws IOException {
		int len = 0;
		byte[] buf = new byte[3];
		int read = 0;
		while ((read = in.read(buf)) == 3) {
			out.write(MAP.charAt((buf[0] >> 2) & 0x3F));
			out.write(MAP.charAt(((buf[0] & 0x3) << 4) + ((buf[1] >> 4) & 0x0F)));
			out.write(MAP.charAt(((buf[1] & 0xF) << 2) + ((buf[2] >> 6) & 0x3)));
			out.write(MAP.charAt(((buf[2]) & 0x3F)));
			len += 3;
		}
		if (read > 0) {
			out.write(MAP.charAt((buf[0] >> 2) & 0x3F));
			if (read > 1) {
				out.write(MAP.charAt(((buf[0] & 0x3) << 4) + ((buf[1] >> 4) & 0xF)));
				out.write(MAP.charAt((buf[1] & 0xF) << 2));
			} else {
				out.write(MAP.charAt((buf[0] & 0x3) << 4));
				out.write(MAP.charAt(64));
			}
			out.write(MAP.charAt(64));
		}
		return len + read;
	}

	/**
	 * Encodes the provided data using Base64.
	 * 
	 * @param data
	 *            the data to be Base64 encoded
	 * @return the encoded data
	 */
	public static String encode(byte[] data) {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			encode(in, out);
		} catch (IOException ioe) {
			// ByteArrayInputStream and ByteArrayOutputStream don't actually
			// throw IOException
			// No need to close() ByteArrayInputStream and ByteArrayOutputStream
			ioe.printStackTrace();
		}
		return out.toString();
	}

	/**
	 * Decodes the contents of the provided input stream, and writes the decoded
	 * data to the provided output stream. Strips whitespace from the input.
	 * Returns the number of bytes written to the output stream. Does not close
	 * the streams.
	 * 
	 * @param in
	 *            the input stream containing the Base64-encoded data
	 * @param out
	 *            the output stream for decoded raw binary data
	 * @return the number of bytes written to the output stream
	 * @throws IllegalCharacterException
	 *             if an illegal character is encountered in the input
	 * @throws InvalidLengthException
	 *             if the input has the wrong length for Base64-encoded data
	 * @throws IOException
	 *             if there is a problem reading from or writing to the provided
	 *             streams
	 */
	public static int decode(InputStream in, OutputStream out) throws IOException, IllegalCharacterException,
			InvalidLengthException {
		int len = 0;
		int c;
		int v = 0;
		int n = 0;
		int k = 0;
		while ((c = in.read()) >= 0) {
			if (Character.isWhitespace((char) c))
				continue;
			byte b = (byte) MAP.indexOf(c);
			if (b < 0)
				throw new IllegalCharacterException(c);
			if (b < 64)
				++n;
			v = (v << 6) | (b & 0x3F);
			if (++k == 4) {
				out.write((byte) ((v >> 16) & 0xFF));
				++len;
				if (n > 2) {
					out.write((byte) ((v >> 8) & 0xFF));
					++len;
				}
				if (n > 3) {
					out.write((byte) (v & 0xFF));
					++len;
				}
				v = 0;
				n = 0;
				k = 0;
			}
		}
		// there shouldn't be any characters left in the buffer
		if (k > 0)
			throw new InvalidLengthException(len + k);

		return len;
	}

	/**
	 * Attempts to decode the provided string using Base64.
	 * 
	 * @param in
	 *            the string to be decoded
	 * @return the decoded data
	 */
	public static byte[] decode(String in) throws IllegalCharacterException, InvalidLengthException {
		ByteArrayOutputStream outStr = new ByteArrayOutputStream();
		ByteArrayInputStream inStr = new ByteArrayInputStream(in.getBytes());
		try {
			decode(inStr, outStr);
		} catch (IOException ioe) {
			// ByteArrayInputStream and ByteArrayOutputStream don't actually
			// throw IOException
			// No need to close() ByteArrayInputStream and ByteArrayOutputStream
			ioe.printStackTrace();
		}
		return outStr.toByteArray();
	}

	/**
	 * Prints the provided data in a hex dump style format.
	 * 
	 * @param data
	 * @param out
	 * @param base
	 */
	public static void dump(byte[] data, PrintStream out, long base) {
		int i = 0;
		for (i = 0; i < (data.length - 16); i += 16) {
			for (int j = 7; j >= 1; --j) {
				if ((base + i) < (0x1L << (j * 4))) {
					out.print("0");
				}
			}
			out.print(Integer.toHexString((int) (base + i)));
			out.print("  ");
			for (int k = 0; k < 16; ++k) {
				if ((data[i + k] & 0xFF) < 0x10) {
					out.print("0");
				}
				out.print(Integer.toHexString(data[i + k] & 0xFF));
				out.print(" ");
				if (k == 7)
					out.print(" ");
			}
			out.println();
		}
		if (i < data.length) {
			for (int j = 7; j >= 1; --j) {
				if ((base + i) < (0x1L << (j * 4))) {
					out.print("0");
				}
			}
			out.print(Integer.toHexString((int) (base + i)));
			out.print("  ");
			for (; i < data.length; ++i) {
				if ((data[i] & 0xFF) < 0x10)
					out.print("0");
				out.print(Integer.toHexString(data[i] & 0xFF));
				out.print(" ");
				if (i % 16 == 7)
					out.print(" ");
			}
		}
		out.println();
	}

	private static void usage() {
		System.out.println("Usage: java -jar base64.jar {-e|--encode|-d|--decode} [-s] input [output]");
		System.out.println("  -e, --encode\tSpecifies encode mode");
		System.out.println("  -d, --decode\tSpecifies decode mode");
		System.out.println("  -e, --encode\tSpecifies encode mode");
		System.out.println("  -s\t\tIf specified, input is a string to be encoded or decoded");
		System.out.println("  input\t\tPath to input file, or string to be encoded or decoded.");
		System.out.println("  output\t\tPath to output file (ignored if -s is specified)");
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			usage();
			return;
		}
		final boolean encode;
		if ("encode".equalsIgnoreCase(args[0]) || "-e".equalsIgnoreCase(args[0])
				|| "--encode".equalsIgnoreCase(args[0])) {
			encode = true;
		} else if ("decode".equalsIgnoreCase(args[0]) || "-d".equalsIgnoreCase(args[0])
				|| "--decode".equalsIgnoreCase(args[0])) {
			encode = false;
		} else {
			usage();
			return;
		}

		long start = System.currentTimeMillis();
		if ("-s".equalsIgnoreCase(args[1])) {
			if (encode) {
				System.out.println(encode(args[2].getBytes()));
			} else {
				try {
					System.out.println(new String(decode(args[2])));
				} catch (IllegalCharacterException ice) {
					System.err.println("Problem with input: Illegal character: " + ice.getMessage());
				} catch (InvalidLengthException ile) {
					System.err.println("Problem with input: Invalid length: " + ile.getMessage());
				}
			}
		} else {
			boolean overwrite = false;
			File inFile = new File(args[1]);
			File outFile;
			if (!inFile.exists()) {
				System.err.println("Input file \"" + args[1] + "\" does not exist.");
				return;
			}
			if (!inFile.canRead()) {
				System.err.println("Input file \"" + args[1] + "\" cannot be read.");
				return;
			}
			if (args.length < 3) {
				System.out.println("Are you sure you want to overwrite the input file?");
				try {
					int x = System.in.read();
					if ('y' == x || 'Y' == x) {
						overwrite = true;
					} else {
						System.out.println("Please specify an output file. Exiting.");
						return;
					}
				} catch (IOException ioe) {
					System.err.println("Error reading input: " + ioe.getMessage());
				}
				if (!inFile.canWrite()) {
					System.err.println("Output file \"" + args[1] + "\" cannot be written.");
					return;
				}
				try {
					outFile = File.createTempFile("b64", null);
				} catch (IOException ioe) {
					System.err.println("Unable to create temp file for writing.");
					ioe.printStackTrace();
					return;
				}
			} else {
				outFile = new File(args[2]);
				if (outFile.exists() && !outFile.canWrite()) {
					System.err.println("Output file \"" + args[2] + "\" cannot be written.");
					return;
				}
			}

            
            try(BufferedInputStream  in  = new BufferedInputStream (new FileInputStream (inFile));
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));)
            {
                try
                {
                    if(encode)
                    {
                        encode(in, out);
                    }
                    else
                    {
                        try
                        {
                            decode(in, out);
                        }
                        catch(IllegalCharacterException ice)
                        {
                            System.err.println("Problem with input: Illegal character: " + ice.getMessage());
                        }
                        catch(InvalidLengthException ile)
                        {
                            System.err.println("Problem with input: Invalid length: " + ile.getMessage());
                        }
                    }
                }
                catch(IOException ioe)
                {
                    System.err.println(ioe.getMessage());
                }
            }
            catch(IOException ioe)
            {
                System.err.println("Error reading file: " + ioe.getMessage());
                return;
            }

            if(overwrite)
            {
                try(BufferedInputStream  tmp = new BufferedInputStream (new FileInputStream (outFile));
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(inFile)))
                {
                    while(tmp.available() > 0)
                    {
                        try
                        {
                            out.write(tmp.read());
                        }
                        catch(IOException ioe)
                        {
                            System.err.println("Error reading temp file: " + ioe.getMessage());
                            return;
                        }
                    }
                }
                catch(IOException ioe)
                {
                    System.err.println("Error reading file: " + ioe.getMessage());
                    return;
                }
            }
		}
		long end = System.currentTimeMillis();
		System.out.println((encode ? "Encoding" : "Decoding") + " took " + (end - start) + " ms.");
	}
}
