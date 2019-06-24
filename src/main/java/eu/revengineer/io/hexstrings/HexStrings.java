/*
 * Copyright 2017-2019 RevEngineer.eu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.revengineer.io.hexstrings;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;

/**
 * Utility class for conversions between {@code byte[]} and {@code char[]}/{@code String}.
 *
 * @author savormix
 */
public class HexStrings {
    private static int MALFORMED_INPUT_CONTEXTUAL_RADIUS = 10;
    
    /**
     * Allows to configure how much of a malformed hex string should be retained in the exception message.<BR>
     * The default value of 10 means that the string excerpt will contain at most 10 preceding characters, the faulting character and at most 10 trailing characters.
     *
     * @param radius
     *         how many characters should precede and trail the detected faulting character
     * @throws IllegalArgumentException
     *         if {@code radius} is negative
     */
    public static void setMalformedInputContextualRadius(int radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("Contextual radius must be positive");
        }
        MALFORMED_INPUT_CONTEXTUAL_RADIUS = radius;
    }
    
    private HexStrings() {
        // utility class
    }
    
    private static char[] toHexChars(byte[] bytes, int offset, int length, char[] hexChars) {
        Objects.requireNonNull(bytes);
        
        final char[] result = new char[length << 1];
        for (int i = 0; i < length; i++) {
            final int b = bytes[offset + i] & 0xFF;
            result[i << 1] = hexChars[b >>> 4];
            result[(i << 1) | 1] = hexChars[b & 0x0F];
        }
        return result;
    }
    
    /**
     * Converts a byte array to a hex octet string. Performs exactly one memory allocation: the resulting character array.
     * No guarantees on allocation count are given if invalid input is supplied.
     *
     * @param bytes
     *         byte array (non-{@code null})
     * @param offset
     *         array offset
     * @param length
     *         amount of bytes to convert
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @return hexadecimal characters
     */
    public static char[] toHexChars(byte[] bytes, int offset, int length, LetterCase letterCase) {
        return toHexChars(bytes, offset, length, letterCase.getHexChars());
    }
    
    /**
     * Converts a byte array to a hex octet string. Performs exactly one memory allocation: the resulting character array.
     * No guarantees on allocation count are given if invalid input is supplied.
     *
     * @param bytes
     *         byte array (non-{@code null})
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @return hexadecimal characters
     */
    public static char[] toHexChars(byte[] bytes, LetterCase letterCase) {
        return toHexChars(bytes, 0, bytes.length, letterCase.getHexChars());
    }
    
    /**
     * Converts a byte array to a hex octet string. Performs exactly three memory allocations:
     * <OL>
     * <LI>Character array (result)</LI>
     * <LI>Character array copy (for wrapper {@link String})</LI>
     * <LI>Wrapper {@link String}</LI>
     * </OL>
     * No guarantees on allocation count are given if invalid input is supplied.
     *
     * @param bytes
     *         byte array (non-{@code null})
     * @param offset
     *         array offset
     * @param length
     *         amount of bytes to convert
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @return hex octet string
     */
    public static String toHexString(byte[] bytes, int offset, int length, LetterCase letterCase) {
        return new String(toHexChars(bytes, offset, length, letterCase.getHexChars()));
    }
    
    /**
     * Converts a byte array to a hex octet string. Performs exactly three memory allocations:
     * <OL>
     * <LI>Character array (result)</LI>
     * <LI>Character array copy (for wrapper {@link String})</LI>
     * <LI>Wrapper {@link String}</LI>
     * </OL>
     * No guarantees on allocation count are given if invalid input is supplied.
     *
     * @param bytes
     *         byte array (non-{@code null})
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @return hex octet string
     */
    public static String toHexString(byte[] bytes, LetterCase letterCase) {
        return new String(toHexChars(bytes, 0, bytes.length, letterCase.getHexChars()));
    }
    
    private static char[] toHexChars(byte[] bytes, int offset, int length, char[] hexChars, char delimiter) {
        Objects.requireNonNull(bytes);
        
        if (length < 1) {
            return new char[0];
        }
        final char[] result = new char[(length << 1) + length - 1];
        {
            final int b = bytes[offset] & 0xFF;
            result[0] = hexChars[b >>> 4];
            result[1] = hexChars[b & 0x0F];
        }
        for (int i = 1; i < length; i++) {
            final int b = bytes[offset + i] & 0xFF, o = i * 3 - 1;
            result[o + 0] = delimiter;
            result[o + 1] = hexChars[b >>> 4];
            result[o + 2] = hexChars[b & 0x0F];
        }
        return result;
    }
    
    /**
     * Converts a byte array to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs exactly one memory
     * allocation: the resulting character array.
     * No guarantees on allocation count are given if invalid input is supplied.
     *
     * @param bytes
     *         byte array (non-{@code null})
     * @param offset
     *         array offset
     * @param length
     *         amount of bytes to convert
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static char[] toHexChars(byte[] bytes, int offset, int length, LetterCase letterCase, char delimiter) {
        return toHexChars(bytes, offset, length, letterCase.getHexChars(), delimiter);
    }
    
    /**
     * Converts a byte array to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs exactly one memory
     * allocation: the resulting character array.
     * No guarantees on allocation count are given if invalid input is supplied.
     *
     * @param bytes
     *         byte array (non-{@code null})
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static char[] toHexChars(byte[] bytes, LetterCase letterCase, char delimiter) {
        return toHexChars(bytes, 0, bytes.length, letterCase.getHexChars(), delimiter);
    }
    
    /**
     * Converts a byte array to a hex octet string. Performs exactly three memory allocations:
     * <OL>
     * <LI>Character array (result)</LI>
     * <LI>Character array copy (for wrapper {@link String})</LI>
     * <LI>Wrapper {@link String}</LI>
     * </OL>
     * No guarantees on allocation count are given if invalid input is supplied.
     *
     * @param bytes
     *         byte array (non-{@code null})
     * @param offset
     *         array offset
     * @param length
     *         amount of bytes to convert
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param delimiter
     *         hex octet delimiter
     * @return hex octet string
     */
    public static String toHexString(byte[] bytes, int offset, int length, LetterCase letterCase, char delimiter) {
        return new String(toHexChars(bytes, offset, length, letterCase.getHexChars(), delimiter));
    }
    
    /**
     * Converts a byte array to a hex octet string. Performs exactly three memory allocations:
     * <OL>
     * <LI>Character array (result)</LI>
     * <LI>Character array copy (for wrapper {@link String})</LI>
     * <LI>Wrapper {@link String}</LI>
     * </OL>
     * No guarantees on allocation count are given if invalid input is supplied.
     *
     * @param bytes
     *         byte array (non-{@code null})
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param delimiter
     *         hex octet delimiter
     * @return hex octet string
     */
    public static String toHexString(byte[] bytes, LetterCase letterCase, char delimiter) {
        return new String(toHexChars(bytes, 0, bytes.length, letterCase.getHexChars(), delimiter));
    }
    
    private static char[] toHexChars(byte[] bytes, int offset, int length, char[] hexChars, CharSequence delimiter) {
        Objects.requireNonNull(bytes);
        
        if (bytes.length < 1) {
            return new char[0];
        }
        if (delimiter == null || delimiter.length() < 1) {
            return toHexChars(bytes, offset, length, hexChars);
        }
        final int dl = delimiter.length();
        if (dl == 1) {
            return toHexChars(bytes, offset, length, hexChars, delimiter.charAt(0));
        }
        
        final char[] result = new char[(length << 1) + (length - 1) * dl];
        {
            final int b = bytes[offset] & 0xFF;
            result[0] = hexChars[b >>> 4];
            result[1] = hexChars[b & 0x0F];
        }
        for (int i = 1; i < length; i++) {
            final int b = bytes[offset + i] & 0xFF, o = (i << 1) + (i - 1) * dl;
            // this is definitely a better option than getting a char array and copying
            // the delimiter is heavily biased towards being very small
            for (int j = 0; j < dl; ++j) {
                result[o + j] = delimiter.charAt(j);
            }
            result[o + dl] = hexChars[b >>> 4];
            result[o + dl + 1] = hexChars[b & 0x0F];
        }
        return result;
    }
    
    /**
     * Converts a byte array to a hex octet string, with {@code delimiter} inserted between each pair of octets.
     * Performs exactly one memory allocation: the resulting character array.
     * No guarantees on allocation count are given if invalid input is supplied.
     *
     * @param bytes
     *         byte array (non-{@code null})
     * @param offset
     *         array offset
     * @param length
     *         amount of bytes to convert
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param delimiter
     *         hex octet delimiter or {@code null}
     * @return hexadecimal characters
     */
    public static char[] toHexChars(byte[] bytes, int offset, int length, LetterCase letterCase, CharSequence delimiter) {
        return toHexChars(bytes, offset, length, letterCase.getHexChars(), delimiter);
    }
    
    /**
     * Converts a byte array to a hex octet string, with {@code delimiter} inserted between each pair of octets.
     * Performs exactly one memory allocation: the resulting character array.
     * No guarantees on allocation count are given if invalid input is supplied.
     *
     * @param bytes
     *         byte array (non-{@code null})
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param delimiter
     *         hex octet delimiter or {@code null}
     * @return hexadecimal characters
     */
    public static char[] toHexChars(byte[] bytes, LetterCase letterCase, CharSequence delimiter) {
        return toHexChars(bytes, 0, bytes.length, letterCase.getHexChars(), delimiter);
    }
    
    /**
     * Converts a byte array to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs exactly three memory allocations:
     * <OL>
     * <LI>Character array (result)</LI>
     * <LI>Character array copy (for wrapper {@link String})</LI>
     * <LI>Wrapper {@link String}</LI>
     * </OL>
     * No guarantees on allocation count are given if invalid input is supplied.
     *
     * @param bytes
     *         byte array (non-{@code null})
     * @param offset
     *         array offset
     * @param length
     *         amount of bytes to convert
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param delimiter
     *         hex octet delimiter or {@code null}
     * @return hex octet string
     */
    public static String toHexString(byte[] bytes, int offset, int length, LetterCase letterCase, CharSequence delimiter) {
        return new String(toHexChars(bytes, offset, length, letterCase.getHexChars(), delimiter));
    }
    
    /**
     * Converts a byte array to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs exactly three memory allocations:
     * <OL>
     * <LI>Character array (result)</LI>
     * <LI>Character array copy (for wrapper {@link String})</LI>
     * <LI>Wrapper {@link String}</LI>
     * </OL>
     * No guarantees on allocation count are given if invalid input is supplied.
     *
     * @param bytes
     *         byte array (non-{@code null})
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param delimiter
     *         hex octet delimiter or {@code null}
     * @return hex octet string
     */
    public static String toHexString(byte[] bytes, LetterCase letterCase, CharSequence delimiter) {
        return new String(toHexChars(bytes, 0, bytes.length, letterCase.getHexChars(), delimiter));
    }
    
    /*
     * Not implemented
     * @param bytes
     *            byte array (non-{@code null})
     * @param letterCase
     *            {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @return not implemented
     * @throws AbstractMethodError always
     *//*
    public static String toDebugString(byte[] bytes, LetterCase letterCase) {
        // Objects.requireNonNull(bytes);
        // FIXME: not implemented
        throw new AbstractMethodError();
    }*/
    
    private static byte[] toByteArray(CharSequence hexString, char[] radixMap, Void unused) {
        Objects.requireNonNull(hexString);
        
        if ((hexString.length() & 1) != 0) {
            throw new IllegalArgumentException(getContextFrom(hexString, hexString.length()));
        }
        
        final byte[] result = new byte[hexString.length() >> 1];
        for (int i = 0; i < result.length; ++i) {
            final int idx = i << 1;
            final int mso = Arrays.binarySearch(radixMap, hexString.charAt(idx)), lso = Arrays.binarySearch(radixMap, hexString.charAt(idx | 1));
            if (mso < 0 || lso < 0) {
                throw new IllegalArgumentException(getContextFrom(hexString, idx));
            }
            result[i] = (byte) ((mso << 4) | lso);
        }
        return result;
    }
    
    /**
     * Converts a (continuous/non-delimited) hex octet string to a byte array. Performs exactly one memory allocation: the resulting byte array.
     * No guarantees on allocation count are given if invalid input is supplied.
     *
     * @param hexString
     *         hex octet string (non-{@code null})
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @return byte array
     * @throws IllegalArgumentException
     *         if an invalid character is encountered
     * @throws IndexOutOfBoundsException
     *         if only half of a hex octet is given
     */
    public static byte[] toByteArray(CharSequence hexString, LetterCase letterCase) throws IllegalArgumentException {
        return toByteArray(hexString, letterCase.getHexChars(), null);
    }
    
    private static byte[] toByteArray(CharSequence hexString, Void unused) {
        Objects.requireNonNull(hexString);
        
        if ((hexString.length() & 1) != 0) {
            throw new IllegalArgumentException(getContextFrom(hexString, hexString.length()));
        }
        
        final byte[] result = new byte[hexString.length() >> 1];
        for (int i = 0; i < result.length; ++i) {
            final int idx = i << 1;
            int mso = Arrays.binarySearch(LetterCase.LOWERCASE.getHexChars(), hexString.charAt(idx));
            if (mso < 0) {
                mso = Arrays.binarySearch(LetterCase.UPPERCASE.getHexChars(), hexString.charAt(idx));
            }
            int lso = Arrays.binarySearch(LetterCase.LOWERCASE.getHexChars(), hexString.charAt(idx | 1));
            if (lso < 0) {
                lso = Arrays.binarySearch(LetterCase.UPPERCASE.getHexChars(), hexString.charAt(idx | 1));
            }
            if (mso < 0 || lso < 0) {
                throw new IllegalArgumentException(getContextFrom(hexString, idx));
            }
            result[i] = (byte) ((mso << 4) | lso);
        }
        return result;
    }
    
    /**
     * Converts a (continuous/non-delimited) hex octet string to a byte array. Performs exactly one memory allocation: the resulting byte array.
     * No guarantees on allocation count are given if invalid input is supplied.
     *
     * @param hexString
     *         hex octet string (non-{@code null})
     * @return byte array
     * @throws IllegalArgumentException
     *         if an invalid character is encountered
     * @throws IndexOutOfBoundsException
     *         if only half of a hex octet is given
     */
    public static byte[] toByteArray(CharSequence hexString) throws IllegalArgumentException {
        return toByteArray(hexString, (Void) null);
    }
    
    private static byte[] toByteArrayDelimited(CharSequence hexString, char[] radixMap, int delimiterLength) {
        Objects.requireNonNull(hexString);
        
        if (delimiterLength < 1 || hexString.length() <= 2) {
            return toByteArray(hexString, radixMap, null);
        }
        
        final int divisor = 2 + delimiterLength;
        final int lenWithTrailingDelimiter = hexString.length() + delimiterLength;
        final int mod = lenWithTrailingDelimiter % divisor;
        if (mod != 0) {
            throw new IllegalArgumentException(getContextFrom(hexString, hexString.length()));
        }
        final byte[] result = new byte[lenWithTrailingDelimiter / divisor];
        for (int i = 0; i < result.length; ++i) {
            final int idx = i * divisor;
            final int mso = Arrays.binarySearch(radixMap, hexString.charAt(idx)),
                    lso = Arrays.binarySearch(radixMap, hexString.charAt(idx + 1));
            if (mso < 0 || lso < 0) {
                throw new IllegalArgumentException(getContextFrom(hexString, idx));
            }
            result[i] = (byte) ((mso << 4) | lso);
        }
        return result;
    }
    
    /**
     * Converts a delimited hex octet string to a byte array. Performs exactly one memory allocation: the resulting byte array.
     * No guarantees on allocation count are given if invalid input is supplied.
     *
     * @param hexString
     *         hex octet string (non-{@code null})
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param delimiterLength
     *         amount of characters that delimit hex octets in the given string
     * @return byte array
     * @throws IllegalArgumentException
     *         if an invalid character is encountered
     * @throws IndexOutOfBoundsException
     *         if only half of a hex octet is given or {@code delimiterLength} is incorrect
     */
    public static byte[] toByteArrayDelimited(CharSequence hexString, LetterCase letterCase, int delimiterLength) {
        return toByteArrayDelimited(hexString, letterCase.getHexChars(), delimiterLength);
    }
    
    private static byte[] toByteArrayDelimited(CharSequence hexString, int delimiterLength, Void unused) {
        Objects.requireNonNull(hexString);
        
        if (delimiterLength < 1 || hexString.length() <= 2) {
            return toByteArray(hexString, (Void) null);
        }
        
        final int divisor = 2 + delimiterLength;
        final int lenWithTrailingDelimiter = hexString.length() + delimiterLength;
        final int mod = lenWithTrailingDelimiter % divisor;
        if (mod != 0) {
            throw new IllegalArgumentException(getContextFrom(hexString, hexString.length()));
        }
        final byte[] result = new byte[lenWithTrailingDelimiter / divisor];
        for (int i = 0; i < result.length; ++i) {
            final int idx = i * divisor;
            int mso = Arrays.binarySearch(LetterCase.LOWERCASE.getHexChars(), hexString.charAt(idx));
            if (mso < 0) {
                mso = Arrays.binarySearch(LetterCase.UPPERCASE.getHexChars(), hexString.charAt(idx));
            }
            int lso = Arrays.binarySearch(LetterCase.LOWERCASE.getHexChars(), hexString.charAt(idx + 1));
            if (lso < 0) {
                lso = Arrays.binarySearch(LetterCase.UPPERCASE.getHexChars(), hexString.charAt(idx + 1));
            }
            if (mso < 0 || lso < 0) {
                throw new IllegalArgumentException(getContextFrom(hexString, idx));
            }
            result[i] = (byte) ((mso << 4) | lso);
        }
        return result;
    }
    
    /**
     * Converts a delimited hex octet string to a byte array. Performs exactly one memory allocation: the resulting byte array.
     * No guarantees on allocation count are given if invalid input is supplied.
     *
     * @param hexString
     *         hex octet string (non-{@code null})
     * @param delimiterLength
     *         amount of characters that delimit hex octets in the given string
     * @return byte array
     * @throws IllegalArgumentException
     *         if an invalid character is encountered
     * @throws IndexOutOfBoundsException
     *         if only half of a hex octet is given or {@code delimiterLength} is incorrect
     */
    public static byte[] toByteArrayDelimited(CharSequence hexString, int delimiterLength) {
        return toByteArrayDelimited(hexString, delimiterLength, null);
    }
    
    /**
     * Converts the given byte value to a hex octet string. Performs 1 memory allocation.
     *
     * @param b
     *         an 8-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @return hexadecimal characters
     */
    public static char[] toHexChars(byte b, LetterCase letterCase) {
        final char[] hexChars = letterCase.getHexChars();
        final char[] result = new char[2];
        result[0] = hexChars[(b >>> 4) & 0x0F];
        result[1] = hexChars[b & 0x0F];
        return result;
    }
    
    /**
     * Converts the given byte value to a hex octet string. Performs 3 memory allocations.
     *
     * @param b
     *         an 8-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @return hex string
     */
    public static String toHexString(byte b, LetterCase letterCase) {
        return new String(toHexChars(b, letterCase));
    }
    
    /**
     * Converts the given Java character value to a hex octet string. Performs 1 memory allocation.
     *
     * @param c
     *         a 16-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @return hexadecimal characters
     */
    public static char[] toHexChars(char c, LetterCase letterCase, ByteOrder endian) {
        return toHexChars((short) c, letterCase, endian);
    }
    
    /**
     * Converts the given Java character value to a hex octet string. Performs 3 memory allocations.
     *
     * @param c
     *         a 16-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @return hexadecimal characters
     */
    public static String toHexString(char c, LetterCase letterCase, ByteOrder endian) {
        return new String(toHexChars((short) c, letterCase, endian));
    }
    
    /**
     * Converts the given short value to a hex octet string. Performs 1 memory allocation.
     *
     * @param s
     *         a 16-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @return hexadecimal characters
     */
    public static char[] toHexChars(short s, LetterCase letterCase, ByteOrder endian) {
        final char[] result = new char[4];
        final int b1, b2;
        if (ByteOrder.LITTLE_ENDIAN.equals(endian)) {
            b1 = s & 0xFF;
            b2 = s >>> 8;
        } else {
            b2 = s & 0xFF;
            b1 = s >>> 8;
        }
        final char[] hexChars = letterCase.getHexChars();
        result[0] = hexChars[(b1 >>> 4) & 0x0F];
        result[1] = hexChars[b1 & 0x0F];
        result[2] = hexChars[(b2 >>> 4) & 0x0F];
        result[3] = hexChars[b2 & 0x0F];
        return result;
    }
    
    /**
     * Converts the given short value to String. Performs 3 memory allocations.
     *
     * @param s
     *         a 16-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @return hexadecimal characters
     */
    public static String toHexString(short s, LetterCase letterCase, ByteOrder endian) {
        return new String(toHexChars(s, letterCase, endian));
    }
    
    /**
     * Converts the given integer value to a hex octet string. Performs 1 memory allocation.
     *
     * @param i
     *         a 32-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @return hexadecimal characters
     */
    public static char[] toHexChars(int i, LetterCase letterCase, ByteOrder endian) {
        final char[] result = new char[8];
        final int b1, b2, b3, b4;
        if (ByteOrder.LITTLE_ENDIAN.equals(endian)) {
            b1 = i & 0xFF;
            b2 = (i >>> 8) & 0xFF;
            b3 = (i >>> 16) & 0xFF;
            b4 = i >>> 24;
        } else {
            b4 = i & 0xFF;
            b3 = (i >>> 8) & 0xFF;
            b2 = (i >>> 16) & 0xFF;
            b1 = i >>> 24;
        }
        final char[] hexChars = letterCase.getHexChars();
        result[0] = hexChars[b1 >>> 4];
        result[1] = hexChars[b1 & 0x0F];
        result[2] = hexChars[b2 >>> 4];
        result[3] = hexChars[b2 & 0x0F];
        result[4] = hexChars[b3 >>> 4];
        result[5] = hexChars[b3 & 0x0F];
        result[6] = hexChars[b4 >>> 4];
        result[7] = hexChars[b4 & 0x0F];
        return result;
    }
    
    /**
     * Converts the given integer value to a hex octet string. Performs 3 memory allocations.
     *
     * @param i
     *         a 32-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @return hexadecimal characters
     */
    public static String toHexString(int i, LetterCase letterCase, ByteOrder endian) {
        return new String(toHexChars(i, letterCase, endian));
    }
    
    /**
     * Converts the given float value to a hex octet string. Performs 1 memory allocation.
     *
     * @param f
     *         a 32-bit floating point value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @return hexadecimal characters
     */
    public static char[] toHexChars(float f, LetterCase letterCase, ByteOrder endian) {
        return toHexChars(Float.floatToRawIntBits(f), letterCase, endian);
    }
    
    /**
     * Converts the given float value to a hex octet string. Performs 3 memory allocations.
     *
     * @param f
     *         a 32-bit floating point value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @return hexadecimal characters
     */
    public static String toHexString(float f, LetterCase letterCase, ByteOrder endian) {
        return new String(toHexChars(Float.floatToRawIntBits(f), letterCase, endian));
    }
    
    /**
     * Converts the given long value to a hex octet string. Performs 1 memory allocation.
     *
     * @param l
     *         a 64-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @return hexadecimal characters
     */
    public static char[] toHexChars(long l, LetterCase letterCase, ByteOrder endian) {
        final char[] result = new char[16];
        final int b1, b2, b3, b4, b5, b6, b7, b8;
        if (ByteOrder.LITTLE_ENDIAN.equals(endian)) {
            b1 = (int) l & 0xFF;
            b2 = (int) (l >>> 8) & 0xFF;
            b3 = (int) (l >>> 16) & 0xFF;
            b4 = (int) (l >>> 24) & 0xFF;
            b5 = (int) (l >>> 32) & 0xFF;
            b6 = (int) (l >>> 40) & 0xFF;
            b7 = (int) (l >>> 48) & 0xFF;
            b8 = (int) (l >>> 56);
        } else {
            b8 = (int) l & 0xFF;
            b7 = (int) (l >>> 8) & 0xFF;
            b6 = (int) (l >>> 16) & 0xFF;
            b5 = (int) (l >>> 24) & 0xFF;
            b4 = (int) (l >>> 32) & 0xFF;
            b3 = (int) (l >>> 40) & 0xFF;
            b2 = (int) (l >>> 48) & 0xFF;
            b1 = (int) (l >>> 56);
        }
        final char[] hexChars = letterCase.getHexChars();
        result[0] = hexChars[b1 >>> 4];
        result[1] = hexChars[b1 & 0x0F];
        result[2] = hexChars[b2 >>> 4];
        result[3] = hexChars[b2 & 0x0F];
        result[4] = hexChars[b3 >>> 4];
        result[5] = hexChars[b3 & 0x0F];
        result[6] = hexChars[b4 >>> 4];
        result[7] = hexChars[b4 & 0x0F];
        result[8] = hexChars[b5 >>> 4];
        result[9] = hexChars[b5 & 0x0F];
        result[10] = hexChars[b6 >>> 4];
        result[11] = hexChars[b6 & 0x0F];
        result[12] = hexChars[b7 >>> 4];
        result[13] = hexChars[b7 & 0x0F];
        result[14] = hexChars[b8 >>> 4];
        result[15] = hexChars[b8 & 0x0F];
        return result;
    }
    
    /**
     * Converts the given long value to a hex octet string. Performs 3 memory allocations.
     *
     * @param l
     *         a 64-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @return hexadecimal characters
     */
    public static String toHexString(long l, LetterCase letterCase, ByteOrder endian) {
        return new String(toHexChars(l, letterCase, endian));
    }
    
    /**
     * Converts the given double value to a hex octet string. Performs 1 memory allocation.
     *
     * @param d
     *         a 64-bit floating point value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @return hexadecimal characters
     */
    public static char[] toHexChars(double d, LetterCase letterCase, ByteOrder endian) {
        return toHexChars(Double.doubleToRawLongBits(d), letterCase, endian);
    }
    
    /**
     * Converts the given double value to a hex octet string. Performs 3 memory allocations.
     *
     * @param d
     *         a 64-bit floating point value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @return hexadecimal characters
     */
    public static String toHexString(double d, LetterCase letterCase, ByteOrder endian) {
        return new String(toHexChars(Double.doubleToRawLongBits(d), letterCase, endian));
    }
    
    /**
     * Converts the given Java character value to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs 1 memory allocation.
     *
     * @param c
     *         a 16-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static char[] toHexChars(char c, LetterCase letterCase, ByteOrder endian, char delimiter) {
        return toHexChars((short) c, letterCase, endian, delimiter);
    }
    
    /**
     * Converts the given Java character value to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs 3 memory allocations.
     *
     * @param c
     *         a 16-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static String toHexString(char c, LetterCase letterCase, ByteOrder endian, char delimiter) {
        return new String(toHexChars((short) c, letterCase, endian, delimiter));
    }
    
    /**
     * Converts the given short value to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs 1 memory allocation.
     *
     * @param s
     *         a 16-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static char[] toHexChars(short s, LetterCase letterCase, ByteOrder endian, char delimiter) {
        final char[] result = new char[5];
        final int b1, b2;
        if (ByteOrder.LITTLE_ENDIAN.equals(endian)) {
            b1 = s & 0xFF;
            b2 = s >>> 8;
        } else {
            b2 = s & 0xFF;
            b1 = s >>> 8;
        }
        final char[] hexChars = letterCase.getHexChars();
        result[0] = hexChars[(b1 >>> 4) & 0x0F];
        result[1] = hexChars[b1 & 0x0F];
        result[2] = delimiter;
        result[3] = hexChars[(b2 >>> 4) & 0x0F];
        result[4] = hexChars[b2 & 0x0F];
        return result;
    }
    
    /**
     * Converts the given short value to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs 3 memory allocations.
     *
     * @param s
     *         a 16-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static String toHexString(short s, LetterCase letterCase, ByteOrder endian, char delimiter) {
        return new String(toHexChars(s, letterCase, endian, delimiter));
    }
    
    /**
     * Converts the given integer value to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs 1 memory allocation.
     *
     * @param i
     *         a 32-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static char[] toHexChars(int i, LetterCase letterCase, ByteOrder endian, char delimiter) {
        final char[] result = new char[11];
        final int b1, b2, b3, b4;
        if (ByteOrder.LITTLE_ENDIAN.equals(endian)) {
            b1 = i & 0xFF;
            b2 = (i >>> 8) & 0xFF;
            b3 = (i >>> 16) & 0xFF;
            b4 = i >>> 24;
        } else {
            b4 = i & 0xFF;
            b3 = (i >>> 8) & 0xFF;
            b2 = (i >>> 16) & 0xFF;
            b1 = i >>> 24;
        }
        final char[] hexChars = letterCase.getHexChars();
        result[0] = hexChars[b1 >>> 4];
        result[1] = hexChars[b1 & 0x0F];
        result[2] = delimiter;
        result[3] = hexChars[b2 >>> 4];
        result[4] = hexChars[b2 & 0x0F];
        result[5] = delimiter;
        result[6] = hexChars[b3 >>> 4];
        result[7] = hexChars[b3 & 0x0F];
        result[8] = delimiter;
        result[9] = hexChars[b4 >>> 4];
        result[10] = hexChars[b4 & 0x0F];
        return result;
    }
    
    /**
     * Converts the given integer value to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs 3 memory allocations.
     *
     * @param i
     *         a 32-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static String toHexString(int i, LetterCase letterCase, ByteOrder endian, char delimiter) {
        return new String(toHexChars(i, letterCase, endian, delimiter));
    }
    
    /**
     * Converts the given float value to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs 1 memory allocation.
     *
     * @param f
     *         a 32-bit floating point value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static char[] toHexChars(float f, LetterCase letterCase, ByteOrder endian, char delimiter) {
        return toHexChars(Float.floatToRawIntBits(f), letterCase, endian, delimiter);
    }
    
    /**
     * Converts the given float value to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs 3 memory allocations.
     *
     * @param f
     *         a 32-bit floating point value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static String toHexString(float f, LetterCase letterCase, ByteOrder endian, char delimiter) {
        return new String(toHexChars(Float.floatToRawIntBits(f), letterCase, endian, delimiter));
    }
    
    /**
     * Converts the given long value to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs 1 memory allocation.
     *
     * @param l
     *         a 64-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static char[] toHexChars(long l, LetterCase letterCase, ByteOrder endian, char delimiter) {
        final char[] result = new char[23];
        final int b1, b2, b3, b4, b5, b6, b7, b8;
        if (ByteOrder.LITTLE_ENDIAN.equals(endian)) {
            b1 = (int) l & 0xFF;
            b2 = (int) (l >>> 8) & 0xFF;
            b3 = (int) (l >>> 16) & 0xFF;
            b4 = (int) (l >>> 24) & 0xFF;
            b5 = (int) (l >>> 32) & 0xFF;
            b6 = (int) (l >>> 40) & 0xFF;
            b7 = (int) (l >>> 48) & 0xFF;
            b8 = (int) (l >>> 56);
        } else {
            b8 = (int) l & 0xFF;
            b7 = (int) (l >>> 8) & 0xFF;
            b6 = (int) (l >>> 16) & 0xFF;
            b5 = (int) (l >>> 24) & 0xFF;
            b4 = (int) (l >>> 32) & 0xFF;
            b3 = (int) (l >>> 40) & 0xFF;
            b2 = (int) (l >>> 48) & 0xFF;
            b1 = (int) (l >>> 56);
        }
        final char[] hexChars = letterCase.getHexChars();
        result[0] = hexChars[b1 >>> 4];
        result[1] = hexChars[b1 & 0x0F];
        result[2] = delimiter;
        result[3] = hexChars[b2 >>> 4];
        result[4] = hexChars[b2 & 0x0F];
        result[5] = delimiter;
        result[6] = hexChars[b3 >>> 4];
        result[7] = hexChars[b3 & 0x0F];
        result[8] = delimiter;
        result[9] = hexChars[b4 >>> 4];
        result[10] = hexChars[b4 & 0x0F];
        result[11] = delimiter;
        result[12] = hexChars[b5 >>> 4];
        result[13] = hexChars[b5 & 0x0F];
        result[14] = delimiter;
        result[15] = hexChars[b6 >>> 4];
        result[16] = hexChars[b6 & 0x0F];
        result[17] = delimiter;
        result[18] = hexChars[b7 >>> 4];
        result[19] = hexChars[b7 & 0x0F];
        result[20] = delimiter;
        result[21] = hexChars[b8 >>> 4];
        result[22] = hexChars[b8 & 0x0F];
        return result;
    }
    
    /**
     * Converts the given long value to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs 3 memory allocations.
     *
     * @param l
     *         a 64-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static String toHexString(long l, LetterCase letterCase, ByteOrder endian, char delimiter) {
        return new String(toHexChars(l, letterCase, endian, delimiter));
    }
    
    /**
     * Converts the given double value to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs 1 memory allocation.
     *
     * @param d
     *         a 64-bit floating point value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static char[] toHexChars(double d, LetterCase letterCase, ByteOrder endian, char delimiter) {
        return toHexChars(Double.doubleToRawLongBits(d), letterCase, endian, delimiter);
    }
    
    /**
     * Converts the given double value to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs 3 memory allocations.
     *
     * @param d
     *         a 64-bit floating point value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static String toHexString(double d, LetterCase letterCase, ByteOrder endian, char delimiter) {
        return new String(toHexChars(Double.doubleToRawLongBits(d), letterCase, endian, delimiter));
    }
    
    /**
     * Converts the given Java character value to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs 1 memory allocation.
     *
     * @param c
     *         a 16-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static char[] toHexChars(char c, LetterCase letterCase, ByteOrder endian, CharSequence delimiter) {
        return toHexChars((short) c, letterCase, endian, delimiter);
    }
    
    /**
     * Converts the given Java character value to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs 3 memory allocations.
     *
     * @param c
     *         a 16-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static String toHexString(char c, LetterCase letterCase, ByteOrder endian, CharSequence delimiter) {
        return new String(toHexChars((short) c, letterCase, endian, delimiter));
    }
    
    /**
     * Converts the given short value to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs 1 memory allocation.
     *
     * @param s
     *         a 16-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static char[] toHexChars(short s, LetterCase letterCase, ByteOrder endian, CharSequence delimiter) {
        if (delimiter == null || delimiter.length() < 1) {
            return toHexChars(s, letterCase, endian);
        }
        final int dl = delimiter.length();
        if (dl == 1) {
            return toHexChars(s, letterCase, endian, delimiter.charAt(0));
        }
        
        final char[] result = new char[4 + dl];
        final int b1, b2;
        if (ByteOrder.LITTLE_ENDIAN.equals(endian)) {
            b1 = s & 0xFF;
            b2 = s >>> 8;
        } else {
            b2 = s & 0xFF;
            b1 = s >>> 8;
        }
        final char[] hexChars = letterCase.getHexChars();
        int index = 0;
        result[index++] = hexChars[(b1 >>> 4) & 0x0F];
        result[index++] = hexChars[b1 & 0x0F];
        index = insertDelimiter(result, delimiter, index);
        result[index++] = hexChars[(b2 >>> 4) & 0x0F];
        result[index++] = hexChars[b2 & 0x0F];
        return result;
    }
    
    /**
     * Converts the given short value to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs 3 memory allocations.
     *
     * @param s
     *         a 16-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static String toHexString(short s, LetterCase letterCase, ByteOrder endian, CharSequence delimiter) {
        return new String(toHexChars(s, letterCase, endian, delimiter));
    }
    
    /**
     * Converts the given integer value to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs 1 memory allocation.
     *
     * @param i
     *         a 32-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static char[] toHexChars(int i, LetterCase letterCase, ByteOrder endian, CharSequence delimiter) {
        if (delimiter == null || delimiter.length() < 1) {
            return toHexChars(i, letterCase, endian);
        }
        final int dl = delimiter.length();
        if (dl == 1) {
            return toHexChars(i, letterCase, endian, delimiter.charAt(0));
        }
        
        final char[] result = new char[8 + dl * 3];
        final int b1, b2, b3, b4;
        if (ByteOrder.LITTLE_ENDIAN.equals(endian)) {
            b1 = i & 0xFF;
            b2 = (i >>> 8) & 0xFF;
            b3 = (i >>> 16) & 0xFF;
            b4 = i >>> 24;
        } else {
            b4 = i & 0xFF;
            b3 = (i >>> 8) & 0xFF;
            b2 = (i >>> 16) & 0xFF;
            b1 = i >>> 24;
        }
        final char[] hexChars = letterCase.getHexChars();
        int index = 0;
        result[index++] = hexChars[(b1 >>> 4) & 0x0F];
        result[index++] = hexChars[b1 & 0x0F];
        index = insertDelimiter(result, delimiter, index);
        result[index++] = hexChars[(b2 >>> 4) & 0x0F];
        result[index++] = hexChars[b2 & 0x0F];
        index = insertDelimiter(result, delimiter, index);
        result[index++] = hexChars[b3 >>> 4];
        result[index++] = hexChars[b3 & 0x0F];
        index = insertDelimiter(result, delimiter, index);
        result[index++] = hexChars[b4 >>> 4];
        result[index++] = hexChars[b4 & 0x0F];
        return result;
    }
    
    /**
     * Converts the given integer value to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs 3 memory allocations.
     *
     * @param i
     *         a 32-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static String toHexString(int i, LetterCase letterCase, ByteOrder endian, CharSequence delimiter) {
        return new String(toHexChars(i, letterCase, endian, delimiter));
    }
    
    /**
     * Converts the given float value to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs 1 memory allocation.
     *
     * @param f
     *         a 32-bit floating point value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static char[] toHexChars(float f, LetterCase letterCase, ByteOrder endian, CharSequence delimiter) {
        return toHexChars(Float.floatToRawIntBits(f), letterCase, endian, delimiter);
    }
    
    /**
     * Converts the given float value to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs 3 memory allocations.
     *
     * @param f
     *         a 32-bit floating point value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static String toHexString(float f, LetterCase letterCase, ByteOrder endian, CharSequence delimiter) {
        return new String(toHexChars(Float.floatToRawIntBits(f), letterCase, endian, delimiter));
    }
    
    /**
     * Converts the given long value to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs 1 memory allocation.
     *
     * @param l
     *         a 64-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static char[] toHexChars(long l, LetterCase letterCase, ByteOrder endian, CharSequence delimiter) {
        if (delimiter == null || delimiter.length() < 1) {
            return toHexChars(l, letterCase, endian);
        }
        final int dl = delimiter.length();
        if (dl == 1) {
            return toHexChars(l, letterCase, endian, delimiter.charAt(0));
        }
        
        final char[] result = new char[16 + dl * 7];
        final int b1, b2, b3, b4, b5, b6, b7, b8;
        if (ByteOrder.LITTLE_ENDIAN.equals(endian)) {
            b1 = (int) l & 0xFF;
            b2 = (int) (l >>> 8) & 0xFF;
            b3 = (int) (l >>> 16) & 0xFF;
            b4 = (int) (l >>> 24) & 0xFF;
            b5 = (int) (l >>> 32) & 0xFF;
            b6 = (int) (l >>> 40) & 0xFF;
            b7 = (int) (l >>> 48) & 0xFF;
            b8 = (int) (l >>> 56);
        } else {
            b8 = (int) l & 0xFF;
            b7 = (int) (l >>> 8) & 0xFF;
            b6 = (int) (l >>> 16) & 0xFF;
            b5 = (int) (l >>> 24) & 0xFF;
            b4 = (int) (l >>> 32) & 0xFF;
            b3 = (int) (l >>> 40) & 0xFF;
            b2 = (int) (l >>> 48) & 0xFF;
            b1 = (int) (l >>> 56);
        }
        final char[] hexChars = letterCase.getHexChars();
        int index = 0;
        result[index++] = hexChars[(b1 >>> 4) & 0x0F];
        result[index++] = hexChars[b1 & 0x0F];
        index = insertDelimiter(result, delimiter, index);
        result[index++] = hexChars[(b2 >>> 4) & 0x0F];
        result[index++] = hexChars[b2 & 0x0F];
        index = insertDelimiter(result, delimiter, index);
        result[index++] = hexChars[b3 >>> 4];
        result[index++] = hexChars[b3 & 0x0F];
        index = insertDelimiter(result, delimiter, index);
        result[index++] = hexChars[b4 >>> 4];
        result[index++] = hexChars[b4 & 0x0F];
        index = insertDelimiter(result, delimiter, index);
        result[index++] = hexChars[b5 >>> 4];
        result[index++] = hexChars[b5 & 0x0F];
        index = insertDelimiter(result, delimiter, index);
        result[index++] = hexChars[b6 >>> 4];
        result[index++] = hexChars[b6 & 0x0F];
        index = insertDelimiter(result, delimiter, index);
        result[index++] = hexChars[b7 >>> 4];
        result[index++] = hexChars[b7 & 0x0F];
        index = insertDelimiter(result, delimiter, index);
        result[index++] = hexChars[b8 >>> 4];
        result[index++] = hexChars[b8 & 0x0F];
        return result;
    }
    
    /**
     * Converts the given long value to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs 3 memory allocations.
     *
     * @param l
     *         a 64-bit integer value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static String toHexString(long l, LetterCase letterCase, ByteOrder endian, CharSequence delimiter) {
        return new String(toHexChars(l, letterCase, endian, delimiter));
    }
    
    /**
     * Converts the given double value to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs 1 memory allocation.
     *
     * @param d
     *         a 64-bit floating point value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static char[] toHexChars(double d, LetterCase letterCase, ByteOrder endian, CharSequence delimiter) {
        return toHexChars(Double.doubleToRawLongBits(d), letterCase, endian, delimiter);
    }
    
    /**
     * Converts the given double value to a hex octet string, with {@code delimiter} inserted between each pair of octets. Performs 3 memory allocations.
     *
     * @param d
     *         a 64-bit floating point value
     * @param letterCase
     *         {@link LetterCase#UPPERCASE} for A-F, {@link LetterCase#LOWERCASE} for a-f (non-{@code null})
     * @param endian
     *         byte order
     * @param delimiter
     *         hex octet delimiter
     * @return hexadecimal characters
     */
    public static String toHexString(double d, LetterCase letterCase, ByteOrder endian, CharSequence delimiter) {
        return new String(toHexChars(Double.doubleToRawLongBits(d), letterCase, endian, delimiter));
    }
    
    private static String getContextFrom(CharSequence malformedInput, int index) {
        final int start = Math.max(0, index - MALFORMED_INPUT_CONTEXTUAL_RADIUS), end = Math.min(malformedInput.length(), index + MALFORMED_INPUT_CONTEXTUAL_RADIUS + 1);
        return "At index " + (index - start) + " in '" + malformedInput.subSequence(start, end) + '\'';
    }
    
    private static int insertDelimiter(char[] hexString, CharSequence delimiter, int index) {
        final int dl = delimiter.length();
        for (int i = 0; i < dl; ++i) {
            hexString[index + i] = delimiter.charAt(i);
        }
        return index + dl;
    }
}
