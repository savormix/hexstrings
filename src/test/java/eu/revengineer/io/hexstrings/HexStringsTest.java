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

import com.google.monitoring.runtime.instrumentation.AllocationRecorder;
import com.google.monitoring.runtime.instrumentation.Sampler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests whether the functional and nonfunctional requirements are met.
 *
 * @author savormix
 */
public class HexStringsTest {
    private static final String[] TO_BYTES_STRINGS = {"", "00", "fffe", "3132335a7a"};
    private static final String[] TO_BYTES_STRINGS_MULTICASE = {"", "00", "FffE", "3132335A7a"};
    private static final byte[][] TO_STRING_BYTES = {{}, {0x00}, {(byte) 0xFF, (byte) 0xFE}, {0x31, 0x32, 0x33, 0x5A, 0x7A}};
    private static final LetterCase[] BOOLS = {LetterCase.LOWERCASE, LetterCase.UPPERCASE};
    private static final ByteOrder[] ENDIANS = {ByteOrder.LITTLE_ENDIAN, ByteOrder.BIG_ENDIAN};
    private static final Character[] DELIM_CHARS = {' ', '-', ':'};
    private static final String[] DELIM_STRINGS = {null, "", " ", ", ", " | "};
    private static final double[] TO_STRING_FP = {-0D, +0D, -1D, +1D, Math.E, Math.PI, 1234.5678D, 1.2345678e-20};
    
    @BeforeAll
    static void forceLoadClass() {
        HexStrings.toByteArray("");
        LetterCase.LOWERCASE.isLowerCase();
        LetterCase.UPPERCASE.isLowerCase();
    }
    
    @Test
    final void testToByteArrayMalformedInput() {
        expectIllegalArgumentException(() -> HexStrings.toByteArray("x"), 1, "x");
        expectIllegalArgumentException(() -> HexStrings.toByteArray("xx"), 0, "xx");
        expectIllegalArgumentException(() -> HexStrings.toByteArray("a"), 1, "a");
        expectIllegalArgumentException(() -> HexStrings.toByteArray("a", LetterCase.LOWERCASE), 1, "a");
        expectIllegalArgumentException(() -> HexStrings.toByteArray("ai"), 0, "ai");
        expectIllegalArgumentException(() -> HexStrings.toByteArray("CC", LetterCase.LOWERCASE), 0, "CC");
        expectIllegalArgumentException(() -> HexStrings.toByteArray("aB", LetterCase.LOWERCASE), 0, "aB");
        expectIllegalArgumentException(() -> HexStrings.toByteArray("aB", LetterCase.UPPERCASE), 0, "aB");
        expectIllegalArgumentException(() -> HexStrings.toByteArray("ABBb", LetterCase.UPPERCASE), 2, "ABBb");
        expectIllegalArgumentException(() -> HexStrings.toByteArrayDelimited("x", 1), 1, "x");
        expectIllegalArgumentException(() -> HexStrings.toByteArrayDelimited("x", 1), 1, "x");
        expectIllegalArgumentException(() -> HexStrings.toByteArrayDelimited("a", 1), 1, "a");
        expectIllegalArgumentException(() -> HexStrings.toByteArrayDelimited("aa ", 1), 3, "aa ");
        expectIllegalArgumentException(() -> HexStrings.toByteArrayDelimited("aa a", 1), 4, "aa a");
        expectIllegalArgumentException(() -> HexStrings.toByteArrayDelimited("aaxa", 1), 4, "aaxa");
        expectIllegalArgumentException(() -> HexStrings.toByteArrayDelimited("aax", 3), 3, "aax");
        expectIllegalArgumentException(() -> HexStrings.toByteArrayDelimited("aaxxxa", 3), 6, "aaxxxa");
        expectIllegalArgumentException(() -> HexStrings.toByteArrayDelimited("aaxxxa", LetterCase.LOWERCASE, 3), 6, "aaxxxa");
        expectIllegalArgumentException(() -> HexStrings.toByteArrayDelimited("AAxxxaA", LetterCase.UPPERCASE, 3), 5, "AAxxxaA");
        expectIllegalArgumentException(() -> HexStrings.toByteArrayDelimited("AAxxxAa", LetterCase.UPPERCASE, 3), 5, "AAxxxAa");
        expectIllegalArgumentException(() -> HexStrings.toByteArrayDelimited("aaxxxAA", LetterCase.LOWERCASE, 3), 5, "aaxxxAA");
        HexStrings.toByteArray("aaBBcCDd");
        HexStrings.toByteArrayDelimited("aaxaa", 1);
        HexStrings.toByteArrayDelimited("aaxyz aa", 4);
        HexStrings.toByteArrayDelimited("AAxyz AA", LetterCase.UPPERCASE, 4);
        
        try {
            HexStrings.setMalformedInputContextualRadius(-1);
            fail("Accepted invalid contextual radius");
        } catch (IllegalArgumentException e) {
            // continue
        }
        
        HexStrings.setMalformedInputContextualRadius(1);
        expectIllegalArgumentException(() -> HexStrings.toByteArrayDelimited("0O 11 22 33 44 55 66 77 88 99 AA BB CC DD EE FF", 1), 0, "0O");
        expectIllegalArgumentException(() -> HexStrings.toByteArrayDelimited("OO 11 22 33 44 55 66 77 88 99 AA BB CC DD EE FF", 1), 0, "OO");
        expectIllegalArgumentException(() -> HexStrings.toByteArrayDelimited("00 i1 22 33 44 55 66 77 88 99 AA BB CC DD EE FF", 1), 1, " i1");
        expectIllegalArgumentException(() -> HexStrings.toByteArrayDelimited("00 1i 22 33 44 55 66 77 88 99 AA BB CC DD EE FF", 1), 1, " 1i");
        HexStrings.setMalformedInputContextualRadius(2);
        expectIllegalArgumentException(() -> HexStrings.toByteArrayDelimited("0O 11 22 33 44 55 66 77 88 99 AA BB CC DD EE FF", 1), 0, "0O ");
        expectIllegalArgumentException(() -> HexStrings.toByteArrayDelimited("00 i1 22 33 44 55 66 77 88 99 AA BB CC DD EE FF", 1), 2, "0 i1 ");
        HexStrings.setMalformedInputContextualRadius(10);
        expectIllegalArgumentException(() -> HexStrings.toByteArrayDelimited("00 11 22 33 44 55 66 77zZZz99 AA BB CC DD EE FF", 1), 10, " 55 66 77zZZz99 AA BB");
    }
    
    static Stream<Arguments> toHexCharsArgs() {
        final Builder<Arguments> builder = Stream.builder();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < TO_STRING_BYTES.length; ++i) {
            sb.setLength(0);
            final byte[] bytes = TO_STRING_BYTES[i];
            for (byte b : bytes) {
                final int ub = b & 0xFF;
                final String value = Integer.toHexString(ub);
                if (ub < 0x10) {
                    sb.append('0');
                }
                sb.append(value);
            }
            final String result = sb.toString();
            for (final LetterCase bool : BOOLS) {
                final byte[] withOffset = new byte[i + bytes.length];
                System.arraycopy(bytes, 0, withOffset, i, bytes.length);
                builder.add(Arguments.of(withOffset, i, bytes.length, bool, (bool.isUpperCase() ? result.toUpperCase(Locale.ENGLISH) : result).toCharArray()));
            }
        }
        for (final LetterCase bool : BOOLS) {
            builder.add(Arguments.of(null, -1, -1, bool, null));
        }
        return builder.build();
    }
    
    @ParameterizedTest(name = "toHexChars({0}, off: {1}, len: {2}, {3}) = {4}")
    @MethodSource("toHexCharsArgs")
    final void testToHexChars(byte[] bytes, int offset, int length, LetterCase letterCase, char[] result) {
        execute(() -> HexStrings.toHexChars(bytes, offset, length, letterCase), result, toHexCharsSampler(result));
    }
    
    static Stream<Arguments> toHexCharsEntireArrayArgs() {
        final Builder<Arguments> builder = Stream.builder();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < TO_STRING_BYTES.length; ++i) {
            sb.setLength(0);
            final byte[] bytes = TO_STRING_BYTES[i];
            for (byte b : bytes) {
                final int ub = b & 0xFF;
                final String value = Integer.toHexString(ub);
                if (ub < 0x10) {
                    sb.append('0');
                }
                sb.append(value);
            }
            final String result = sb.toString();
            for (final LetterCase bool : BOOLS) {
                builder.add(Arguments.of(bytes, bool, (bool.isUpperCase() ? result.toUpperCase(Locale.ENGLISH) : result).toCharArray()));
            }
        }
        for (final LetterCase bool : BOOLS) {
            builder.add(Arguments.of(null, bool, null));
        }
        return builder.build();
    }
    
    @ParameterizedTest(name = "toHexChars({0}, {1}) = {2}")
    @MethodSource("toHexCharsEntireArrayArgs")
    final void testToHexCharsEntireArray(byte[] bytes, LetterCase letterCase, char[] result) {
        execute(() -> HexStrings.toHexChars(bytes, letterCase), result, toHexCharsSampler(result));
    }
    
    static Stream<Arguments> toHexStringArgs() {
        return toHexStringArgs(toHexCharsArgs());
    }
    
    /**
     * Test method for {@link eu.revengineer.io.hexstrings.HexStrings#toHexString(byte[], int, int, LetterCase)}.
     *
     * @param bytes
     *         bytes to convert
     * @param offset
     *         array offset
     * @param length
     *         amount of bytes to convert
     * @param letterCase
     *         whether to return uppercase
     * @param result
     *         expected result
     */
    @ParameterizedTest(name = "toHexString({0}, off: {1}, len: {2}, {3}) = \"{4}\"")
    @MethodSource("toHexStringArgs")
    final void testToHexString(byte[] bytes, int offset, int length, LetterCase letterCase, String result) {
        execute(() -> HexStrings.toHexString(bytes, offset, length, letterCase), result, toHexStringSampler(result));
    }
    
    static Stream<Arguments> toHexStringEntireArrayArgs() {
        return toHexStringArgs(toHexCharsEntireArrayArgs());
    }
    
    @ParameterizedTest(name = "toHexString({0}, {1}) = \"{2}\"")
    @MethodSource("toHexStringEntireArrayArgs")
    final void testToHexStringEntireArray(byte[] bytes, LetterCase letterCase, String result) {
        execute(() -> HexStrings.toHexString(bytes, letterCase), result, toHexStringSampler(result));
    }
    
    static Stream<Arguments> toHexCharsWithCharDelimiterArgs() {
        return toHexCharsWithCharDelimiterArgs(HexStringsTest::toHexCharsArgs);
    }
    
    @ParameterizedTest(name = "toHexChars({0}, off: {1}, len: {2}, {3}, (char)\"{4}\") = {5}")
    @MethodSource("toHexCharsWithCharDelimiterArgs")
    final void testToHexCharsWithCharDelimiter(byte[] bytes, int offset, int length, LetterCase letterCase, char delimiter, char[] result) {
        execute(() -> HexStrings.toHexChars(bytes, offset, length, letterCase, delimiter), result, toHexCharsSampler(result));
    }
    
    static Stream<Arguments> toHexCharsEntireArrayWithCharDelimiterArgs() {
        return toHexCharsWithCharDelimiterArgs(HexStringsTest::toHexCharsEntireArrayArgs);
    }
    
    @ParameterizedTest(name = "toHexChars({0}, {1}, (char)\"{2}\") = {3}")
    @MethodSource("toHexCharsEntireArrayWithCharDelimiterArgs")
    final void testToHexCharsEntireArrayWithCharDelimiter(byte[] bytes, LetterCase letterCase, char delimiter, char[] result) {
        execute(() -> HexStrings.toHexChars(bytes, letterCase, delimiter), result, toHexCharsSampler(result));
    }
    
    static Stream<Arguments> toHexStringWithCharDelimiterArgs() {
        return toHexStringArgs(toHexCharsWithCharDelimiterArgs(HexStringsTest::toHexCharsArgs));
    }
    
    /**
     * Test method for {@link eu.revengineer.io.hexstrings.HexStrings#toHexString(byte[], int, int, LetterCase, char)}.
     *
     * @param bytes
     *         bytes to convert
     * @param offset
     *         array offset
     * @param length
     *         amount of bytes to convert
     * @param letterCase
     *         whether to return uppercase
     * @param delimiter
     *         hex octet delimiter
     * @param result
     *         expected result
     */
    @ParameterizedTest(name = "toHexString({0}, off: {1}, len: {2}, {3}, (char)\"{4}\") = \"{5}\"")
    @MethodSource("toHexStringWithCharDelimiterArgs")
    final void testToHexStringWithCharDelimiter(byte[] bytes, int offset, int length, LetterCase letterCase, char delimiter, String result) {
        execute(() -> HexStrings.toHexString(bytes, offset, length, letterCase, delimiter), result, toHexStringSampler(result));
    }
    
    static Stream<Arguments> toHexStringEntireArrayWithCharDelimiterArgs() {
        return toHexStringArgs(toHexCharsWithCharDelimiterArgs(HexStringsTest::toHexCharsEntireArrayArgs));
    }
    
    @ParameterizedTest(name = "toHexString({0}, {1}, (char)\"{2}\") = \"{3}\"")
    @MethodSource("toHexStringEntireArrayWithCharDelimiterArgs")
    final void testToHexStringEntireArrayWithCharDelimiter(byte[] bytes, LetterCase letterCase, char delimiter, String result) {
        execute(() -> HexStrings.toHexString(bytes, letterCase, delimiter), result, toHexStringSampler(result));
    }
    
    static Stream<Arguments> toHexCharsWithStringDelimiterArgs() {
        return toHexCharsWithStringDelimiterArgs(HexStringsTest::toHexCharsArgs);
    }
    
    @ParameterizedTest(name = "toHexChars({0}, off: {1}, len: {2}, {3}, \"{4}\") = {5}")
    @MethodSource("toHexCharsWithStringDelimiterArgs")
    final void testToHexCharsWithStringDelimiter(byte[] bytes, int offset, int length, LetterCase letterCase, CharSequence delimiter, char[] result) {
        execute(() -> HexStrings.toHexChars(bytes, offset, length, letterCase, delimiter), result, toHexCharsSampler(result));
    }
    
    static Stream<Arguments> toHexCharsEntireArrayWithStringDelimiterArgs() {
        return toHexCharsWithStringDelimiterArgs(HexStringsTest::toHexCharsEntireArrayArgs);
    }
    
    @ParameterizedTest(name = "toHexChars({0}, {1}, \"{2}\") = {3}")
    @MethodSource("toHexCharsEntireArrayWithStringDelimiterArgs")
    final void testToHexCharsEntireArrayWithStringDelimiter(byte[] bytes, LetterCase letterCase, CharSequence delimiter, char[] result) {
        execute(() -> HexStrings.toHexChars(bytes, letterCase, delimiter), result, toHexCharsSampler(result));
    }
    
    static Stream<Arguments> toHexStringWithStringDelimiterArgs() {
        return toHexStringArgs(toHexCharsWithStringDelimiterArgs(HexStringsTest::toHexCharsArgs));
    }
    
    /**
     * Test method for {@link eu.revengineer.io.hexstrings.HexStrings#toHexString(byte[], int, int, LetterCase, java.lang.CharSequence)}.
     *
     * @param bytes
     *         bytes to convert
     * @param offset
     *         array offset
     * @param length
     *         amount of bytes to convert
     * @param letterCase
     *         whether to return uppercase
     * @param delimiter
     *         hex octet delimiter
     * @param result
     *         expected result
     */
    @ParameterizedTest(name = "toHexString({0}, off: {1}, len: {2}, {3}, \"{4}\") = \"{5}\"")
    @MethodSource("toHexStringWithStringDelimiterArgs")
    final void testToHexStringWithStringDelimiter(byte[] bytes, int offset, int length, LetterCase letterCase, CharSequence delimiter, String result) {
        execute(() -> HexStrings.toHexString(bytes, offset, length, letterCase, delimiter), result, toHexStringSampler(result));
    }
    
    static Stream<Arguments> toHexStringEntireArrayWithStringDelimiterArgs() {
        return toHexStringArgs(toHexCharsWithStringDelimiterArgs(HexStringsTest::toHexCharsEntireArrayArgs));
    }
    
    @ParameterizedTest(name = "toHexString({0}, {1}, \"{2}\") = \"{3}\"")
    @MethodSource("toHexStringEntireArrayWithStringDelimiterArgs")
    final void testToHexStringEntireArrayWithStringDelimiter(byte[] bytes, LetterCase letterCase, CharSequence delimiter, String result) {
        execute(() -> HexStrings.toHexString(bytes, letterCase, delimiter), result, toHexStringSampler(result));
    }
    
    static Stream<Arguments> toByteArrayArgs() {
        final Builder<Arguments> builder = Stream.builder();
        for (final String hexString : TO_BYTES_STRINGS) {
            final byte[] result = new byte[hexString.length() >> 1];
            for (int i = 0; i < result.length; ++i) {
                result[i] = (byte) Integer.parseInt(hexString.substring(i << 1, (i + 1) << 1), 16);
            }
            for (final LetterCase bool : BOOLS) {
                builder.add(Arguments.of(bool.isUpperCase() ? hexString.toUpperCase(Locale.ENGLISH) : hexString, bool, result));
            }
        }
        for (final LetterCase bool : BOOLS) {
            builder.add(Arguments.of(null, bool, null));
        }
        return builder.build();
    }
    
    @ParameterizedTest(name = "toByteArray(\"{0}\", {1}) = {2}")
    @MethodSource("toByteArrayArgs")
    final void testToByteArray(CharSequence string, LetterCase letterCase, byte[] result) {
        execute(() -> HexStrings.toByteArray(string, letterCase), result, toByteArraySampler(result));
    }
    
    static Stream<Arguments> toByteArrayMulticaseArgs() {
        final Builder<Arguments> builder = Stream.builder();
        for (final String hexString : TO_BYTES_STRINGS_MULTICASE) {
            final byte[] result = new byte[hexString.length() >> 1];
            for (int i = 0; i < result.length; ++i) {
                result[i] = (byte) Integer.parseInt(hexString.substring(i << 1, (i + 1) << 1), 16);
            }
            builder.add(Arguments.of(hexString, result));
        }
        builder.add(Arguments.of(null, null));
        return builder.build();
    }
    
    @ParameterizedTest(name = "toByteArray(\"{0}\") = {1}")
    @MethodSource("toByteArrayMulticaseArgs")
    final void testToByteArrayMulticase(CharSequence string, byte[] result) {
        execute(() -> HexStrings.toByteArray(string), result, toByteArraySampler(result));
    }
    
    static Stream<Arguments> toByteArrayWithDelimArgs() {
        return toByteArrayWithDelimArgs(HexStringsTest::toByteArrayArgs);
    }
    
    @ParameterizedTest(name = "toByteArrayDelimited(\"{0}\", {1}, delimLength: {2}) = {3}")
    @MethodSource("toByteArrayWithDelimArgs")
    final void testToByteArrayDelimited(CharSequence string, LetterCase letterCase, int delimiterLength, byte[] result) {
        execute(() -> HexStrings.toByteArrayDelimited(string, letterCase, delimiterLength), result, toByteArraySampler(result));
    }
    
    static Stream<Arguments> toByteArrayWithDelimMulticaseArgs() {
        return toByteArrayWithDelimArgs(HexStringsTest::toByteArrayMulticaseArgs);
    }
    
    @ParameterizedTest(name = "toByteArrayDelimited(\"{0}\", delimLength: {1}) = {2}")
    @MethodSource("toByteArrayWithDelimMulticaseArgs")
    final void testToByteArrayDelimitedMulticase(CharSequence string, int delimiterLength, byte[] result) {
        execute(() -> HexStrings.toByteArrayDelimited(string, delimiterLength), result, toByteArraySampler(result));
    }
    
    static Stream<Arguments> toHexCharsByteArgs() {
        final Builder<Arguments> builder = Stream.builder();
        final StringBuilder sb = new StringBuilder(2);
        for (int b = Byte.MIN_VALUE; b <= Byte.MAX_VALUE; ++b) {
            sb.setLength(0);
            final int ub = b & 0xFF;
            final String value = Integer.toHexString(ub);
            if (ub < 0x10) {
                sb.append('0');
            }
            sb.append(value);
            final String result = sb.toString();
            for (final LetterCase bool : BOOLS) {
                builder.add(Arguments.of((byte) b, bool, (bool.isUpperCase() ? result.toUpperCase(Locale.ENGLISH) : result).toCharArray()));
            }
        }
        return builder.build();
    }
    
    @ParameterizedTest(name = "toHexChars({0}, {1}) = {2}")
    @MethodSource("toHexCharsByteArgs")
    final void testToHexCharsByte(byte b, LetterCase letterCase, char[] result) {
        execute(() -> HexStrings.toHexChars(b, letterCase), result, toHexCharsSampler(result));
    }
    
    static Stream<Arguments> toHexStringByteArgs() {
        return toHexStringArgs(toHexCharsByteArgs());
    }
    
    @ParameterizedTest(name = "toHexString({0}, {1}) = {2}")
    @MethodSource("toHexStringByteArgs")
    final void testToHexStringByte(byte b, LetterCase letterCase, String result) {
        execute(() -> HexStrings.toHexString(b, letterCase), result, toHexStringSampler(result));
    }
    
    static Stream<Arguments> toHexCharsCharArgs() {
        final Builder<Arguments> builder = Stream.builder();
        final StringBuilder sb = new StringBuilder(4);
        for (int c = Character.MIN_VALUE; c <= Character.MAX_VALUE; c += 100) {
            sb.setLength(0);
            final int uc = c & 0xFF_FF;
            final String value = Integer.toHexString(uc);
            sb.append(value);
            while (sb.length() < 4) {
                sb.insert(0, '0');
            }
            final String[] resultByEndian = {null, sb.toString()};
            resultByEndian[0] = changeEndian(sb).toString();
            for (final LetterCase bool : BOOLS) {
                for (int i = 0; i < ENDIANS.length; ++i) {
                    builder.add(Arguments.of((char) c, bool, ENDIANS[i], (bool.isUpperCase() ? resultByEndian[i].toUpperCase(Locale.ENGLISH) : resultByEndian[i]).toCharArray()));
                }
            }
        }
        return builder.build();
    }
    
    @ParameterizedTest(name = "toHexChars({0}, {1}, {2}) = {3}")
    @MethodSource("toHexCharsCharArgs")
    final void testToHexCharsChar(char c, LetterCase letterCase, ByteOrder endian, char[] result) {
        execute(() -> HexStrings.toHexChars(c, letterCase, endian), result, toHexCharsSampler(result));
    }
    
    static Stream<Arguments> toHexStringCharArgs() {
        return toHexStringArgs(toHexCharsCharArgs());
    }
    
    @ParameterizedTest(name = "toHexString({0}, {1}, {2}) = {3}")
    @MethodSource("toHexStringCharArgs")
    final void testToHexStringChar(char c, LetterCase letterCase, ByteOrder endian, String result) {
        execute(() -> HexStrings.toHexString(c, letterCase, endian), result, toHexStringSampler(result));
    }
    
    static Stream<Arguments> toHexCharsShortArgs() {
        return toHexCharsCharArgs().peek(a -> {
            final Object[] args = a.get();
            args[0] = (short) ((Character) args[0]).charValue();
        });
    }
    
    @ParameterizedTest(name = "toHexChars({0}, {1}, {2}) = {3}")
    @MethodSource("toHexCharsShortArgs")
    final void testToHexCharsShort(short s, LetterCase letterCase, ByteOrder endian, char[] result) {
        execute(() -> HexStrings.toHexChars(s, letterCase, endian), result, toHexCharsSampler(result));
    }
    
    static Stream<Arguments> toHexStringShortArgs() {
        return toHexStringArgs(toHexCharsShortArgs());
    }
    
    @ParameterizedTest(name = "toHexString({0}, {1}, {2}) = {3}")
    @MethodSource("toHexStringShortArgs")
    final void testToHexStringShort(short s, LetterCase letterCase, ByteOrder endian, String result) {
        execute(() -> HexStrings.toHexString(s, letterCase, endian), result, toHexStringSampler(result));
    }
    
    static Stream<Arguments> toHexCharsIntArgs() {
        final Builder<Arguments> builder = Stream.builder();
        final StringBuilder sb = new StringBuilder(8);
        for (long i = Integer.MIN_VALUE; i <= Integer.MAX_VALUE; i += 100_000_000) {
            sb.setLength(0);
            final String value = Integer.toHexString((int) i);
            sb.append(value);
            while (sb.length() < 8) {
                sb.insert(0, '0');
            }
            final String[] resultByEndian = {null, sb.toString()};
            resultByEndian[0] = changeEndian(sb).toString();
            for (final LetterCase bool : BOOLS) {
                for (int j = 0; j < ENDIANS.length; ++j) {
                    builder.add(Arguments.of((int) i, bool, ENDIANS[j], (bool.isUpperCase() ? resultByEndian[j].toUpperCase(Locale.ENGLISH) : resultByEndian[j]).toCharArray()));
                }
            }
        }
        return builder.build();
    }
    
    @ParameterizedTest(name = "toHexChars({0}, {1}, {2}) = {3}")
    @MethodSource("toHexCharsIntArgs")
    final void testToHexCharsInt(int i, LetterCase letterCase, ByteOrder endian, char[] result) {
        execute(() -> HexStrings.toHexChars(i, letterCase, endian), result, toHexCharsSampler(result));
    }
    
    static Stream<Arguments> toHexStringIntArgs() {
        return toHexStringArgs(toHexCharsIntArgs());
    }
    
    @ParameterizedTest(name = "toHexString({0}, {1}, {2}) = {3}")
    @MethodSource("toHexStringIntArgs")
    final void testToHexStringInt(int i, LetterCase letterCase, ByteOrder endian, String result) {
        execute(() -> HexStrings.toHexString(i, letterCase, endian), result, toHexStringSampler(result));
    }
    
    static Stream<Arguments> toHexCharsFloatArgs() {
        final float[] specialFloats = {Float.MIN_VALUE, Float.MAX_VALUE, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY,
                Float.NaN, // quiet NaN
                Float.intBitsToFloat(0x7F_80_00_01), // signalling NaN
                Float.intBitsToFloat(0x7F_FF_FF_FF),
                Float.intBitsToFloat(0x7F_BF_FF_FF),
                Float.intBitsToFloat(0x7F_C1_23_45),
        };
        final float[] allFloats = new float[TO_STRING_FP.length + specialFloats.length];
        for (int i = 0; i < TO_STRING_FP.length; ++i) {
            allFloats[i] = (float) TO_STRING_FP[i];
        }
        for (int i = 0; i < specialFloats.length; ++i) {
            allFloats[i + TO_STRING_FP.length] = specialFloats[i];
        }
        
        final Builder<Arguments> builder = Stream.builder();
        final StringBuilder sb = new StringBuilder(8);
        for (float f : allFloats) {
            sb.setLength(0);
            final int equivalent = Float.floatToRawIntBits(f);
            final String value = Integer.toHexString(equivalent);
            sb.append(value);
            while (sb.length() < 8) {
                sb.insert(0, '0');
            }
            final String[] resultByEndian = {null, sb.toString()};
            resultByEndian[0] = changeEndian(sb).toString();
            for (final LetterCase bool : BOOLS) {
                for (int j = 0; j < ENDIANS.length; ++j) {
                    builder.add(Arguments.of(f, bool, ENDIANS[j], (bool.isUpperCase() ? resultByEndian[j].toUpperCase(Locale.ENGLISH) : resultByEndian[j]).toCharArray()));
                }
            }
        }
        return builder.build();
    }
    
    @ParameterizedTest(name = "toHexChars({0}, {1}, {2}) = {3}")
    @MethodSource("toHexCharsFloatArgs")
    final void testToHexCharsFloat(float f, LetterCase letterCase, ByteOrder endian, char[] result) {
        execute(() -> HexStrings.toHexChars(f, letterCase, endian), result, toHexCharsSampler(result));
    }
    
    static Stream<Arguments> toHexStringFloatArgs() {
        return toHexStringArgs(toHexCharsFloatArgs());
    }
    
    @ParameterizedTest(name = "toHexString({0}, {1}, {2}) = {3}")
    @MethodSource("toHexStringFloatArgs")
    final void testToHexStringFloat(float f, LetterCase letterCase, ByteOrder endian, String result) {
        execute(() -> HexStrings.toHexString(f, letterCase, endian), result, toHexStringSampler(result));
    }
    
    static Stream<Arguments> toHexCharsLongArgs() {
        final Builder<Arguments> builder = Stream.builder();
        final StringBuilder sb = new StringBuilder(16);
        for (long l = Long.MIN_VALUE; l <= Long.MAX_VALUE - 10_000_000_000_000_000L; l += 10_000_000_000_000_000L) {
            sb.setLength(0);
            final String value = Long.toHexString(l);
            sb.append(value);
            while (sb.length() < 16) {
                sb.insert(0, '0');
            }
            final String[] resultByEndian = {null, sb.toString()};
            resultByEndian[0] = changeEndian(sb).toString();
            for (final LetterCase bool : BOOLS) {
                for (int j = 0; j < ENDIANS.length; ++j) {
                    builder.add(Arguments.of(l, bool, ENDIANS[j], (bool.isUpperCase() ? resultByEndian[j].toUpperCase(Locale.ENGLISH) : resultByEndian[j]).toCharArray()));
                }
            }
        }
        return builder.build();
    }
    
    @ParameterizedTest(name = "toHexChars({0}, {1}, {2}) = {3}")
    @MethodSource("toHexCharsLongArgs")
    final void testToHexCharsLong(long l, LetterCase letterCase, ByteOrder endian, char[] result) {
        execute(() -> HexStrings.toHexChars(l, letterCase, endian), result, toHexCharsSampler(result));
    }
    
    static Stream<Arguments> toHexStringLongArgs() {
        return toHexStringArgs(toHexCharsLongArgs());
    }
    
    @ParameterizedTest(name = "toHexString({0}, {1}, {2}) = {3}")
    @MethodSource("toHexStringLongArgs")
    final void testToHexStringLong(long l, LetterCase letterCase, ByteOrder endian, String result) {
        execute(() -> HexStrings.toHexString(l, letterCase, endian), result, toHexStringSampler(result));
    }
    
    static Stream<Arguments> toHexCharsDoubleArgs() {
        final double[] special = {Double.MIN_VALUE, Double.MAX_VALUE, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
                Double.NaN, // quiet NaN
                Double.longBitsToDouble(0x7F_F0_00_00_00_00_00_01L), // signalling NaN
                Double.longBitsToDouble(0x7F_FF_FF_FF_FF_FF_FF_FFL),
                Double.longBitsToDouble(0x7F_F7_FF_FF_FF_FF_FF_FFL),
                Double.longBitsToDouble(0x7F_F8_12_34_56_78_90_ABL),
        };
        final double[] all = new double[TO_STRING_FP.length + special.length];
        for (int i = 0; i < TO_STRING_FP.length; ++i) {
            all[i] = TO_STRING_FP[i];
        }
        for (int i = 0; i < special.length; ++i) {
            all[i + TO_STRING_FP.length] = special[i];
        }
        
        final Builder<Arguments> builder = Stream.builder();
        final StringBuilder sb = new StringBuilder(8);
        for (double d : all) {
            sb.setLength(0);
            final long equivalent = Double.doubleToRawLongBits(d);
            final String value = Long.toHexString(equivalent);
            sb.append(value);
            while (sb.length() < 16) {
                sb.insert(0, '0');
            }
            final String[] resultByEndian = {null, sb.toString()};
            resultByEndian[0] = changeEndian(sb).toString();
            for (final LetterCase bool : BOOLS) {
                for (int j = 0; j < ENDIANS.length; ++j) {
                    builder.add(Arguments.of(d, bool, ENDIANS[j], (bool.isUpperCase() ? resultByEndian[j].toUpperCase(Locale.ENGLISH) : resultByEndian[j]).toCharArray()));
                }
            }
        }
        return builder.build();
    }
    
    @ParameterizedTest(name = "toHexChars({0}, {1}, {2}) = {3}")
    @MethodSource("toHexCharsDoubleArgs")
    final void testToHexCharsDouble(double d, LetterCase letterCase, ByteOrder endian, char[] result) {
        execute(() -> HexStrings.toHexChars(d, letterCase, endian), result, toHexCharsSampler(result));
    }
    
    static Stream<Arguments> toHexStringDoubleArgs() {
        return toHexStringArgs(toHexCharsDoubleArgs());
    }
    
    @ParameterizedTest(name = "toHexString({0}, {1}, {2}) = {3}")
    @MethodSource("toHexStringDoubleArgs")
    final void testToHexStringDouble(double d, LetterCase letterCase, ByteOrder endian, String result) {
        execute(() -> HexStrings.toHexString(d, letterCase, endian), result, toHexStringSampler(result));
    }
    
    static Stream<Arguments> toHexCharsCharWithCharDelimiterArgs() {
        return toHexCharsWithCharDelimiterArgs(HexStringsTest::toHexCharsCharArgs);
    }
    
    @ParameterizedTest(name = "toHexChars({0}, {1}, {2}, {3}) = {4}")
    @MethodSource("toHexCharsCharWithCharDelimiterArgs")
    final void testToHexCharsCharWithCharDelimiter(char c, LetterCase letterCase, ByteOrder endian, char delimiter, char[] result) {
        execute(() -> HexStrings.toHexChars(c, letterCase, endian, delimiter), result, toHexCharsSampler(result));
    }
    
    static Stream<Arguments> toHexStringCharWithCharDelimiterArgs() {
        return toHexStringArgs(toHexCharsCharWithCharDelimiterArgs());
    }
    
    @ParameterizedTest(name = "toHexString({0}, {1}, {2}, {3}) = {4}")
    @MethodSource("toHexStringCharWithCharDelimiterArgs")
    final void testToHexStringCharWithCharDelimiter(char c, LetterCase letterCase, ByteOrder endian, char delimiter, String result) {
        execute(() -> HexStrings.toHexString(c, letterCase, endian, delimiter), result, toHexStringSampler(result));
    }
    
    static Stream<Arguments> toHexCharsShortWithCharDelimiterArgs() {
        return toHexCharsWithCharDelimiterArgs(HexStringsTest::toHexCharsShortArgs);
    }
    
    @ParameterizedTest(name = "toHexChars({0}, {1}, {2}, {3}) = {4}")
    @MethodSource("toHexCharsShortWithCharDelimiterArgs")
    final void testToHexCharsShortWithCharDelimiter(short s, LetterCase letterCase, ByteOrder endian, char delimiter, char[] result) {
        execute(() -> HexStrings.toHexChars(s, letterCase, endian, delimiter), result, toHexCharsSampler(result));
    }
    
    static Stream<Arguments> toHexStringShortWithCharDelimiterArgs() {
        return toHexStringArgs(toHexCharsShortWithCharDelimiterArgs());
    }
    
    @ParameterizedTest(name = "toHexString({0}, {1}, {2}, {3}) = {4}")
    @MethodSource("toHexStringShortWithCharDelimiterArgs")
    final void testToHexStringShortWithCharDelimiter(short s, LetterCase letterCase, ByteOrder endian, char delimiter, String result) {
        execute(() -> HexStrings.toHexString(s, letterCase, endian, delimiter), result, toHexStringSampler(result));
    }
    
    static Stream<Arguments> toHexCharsIntWithCharDelimiterArgs() {
        return toHexCharsWithCharDelimiterArgs(HexStringsTest::toHexCharsIntArgs);
    }
    
    @ParameterizedTest(name = "toHexChars({0}, {1}, {2}, {3}) = {4}")
    @MethodSource("toHexCharsIntWithCharDelimiterArgs")
    final void testToHexCharsIntWithCharDelimiter(int i, LetterCase letterCase, ByteOrder endian, char delimiter, char[] result) {
        execute(() -> HexStrings.toHexChars(i, letterCase, endian, delimiter), result, toHexCharsSampler(result));
    }
    
    static Stream<Arguments> toHexStringIntWithCharDelimiterArgs() {
        return toHexStringArgs(toHexCharsIntWithCharDelimiterArgs());
    }
    
    @ParameterizedTest(name = "toHexString({0}, {1}, {2}, {3}) = {4}")
    @MethodSource("toHexStringIntWithCharDelimiterArgs")
    final void testToHexStringIntWithCharDelimiter(int i, LetterCase letterCase, ByteOrder endian, char delimiter, String result) {
        execute(() -> HexStrings.toHexString(i, letterCase, endian, delimiter), result, toHexStringSampler(result));
    }
    
    static Stream<Arguments> toHexCharsFloatWithCharDelimiterArgs() {
        return toHexCharsWithCharDelimiterArgs(HexStringsTest::toHexCharsFloatArgs);
    }
    
    @ParameterizedTest(name = "toHexChars({0}, {1}, {2}, {3}) = {4}")
    @MethodSource("toHexCharsFloatWithCharDelimiterArgs")
    final void testToHexCharsFloatWithCharDelimiter(float f, LetterCase letterCase, ByteOrder endian, char delimiter, char[] result) {
        execute(() -> HexStrings.toHexChars(f, letterCase, endian, delimiter), result, toHexCharsSampler(result));
    }
    
    static Stream<Arguments> toHexStringFloatWithCharDelimiterArgs() {
        return toHexStringArgs(toHexCharsFloatWithCharDelimiterArgs());
    }
    
    @ParameterizedTest(name = "toHexString({0}, {1}, {2}, {3}) = {4}")
    @MethodSource("toHexStringFloatWithCharDelimiterArgs")
    final void testToHexStringFloatWithCharDelimiter(float f, LetterCase letterCase, ByteOrder endian, char delimiter, String result) {
        execute(() -> HexStrings.toHexString(f, letterCase, endian, delimiter), result, toHexStringSampler(result));
    }
    
    static Stream<Arguments> toHexCharsLongWithCharDelimiterArgs() {
        return toHexCharsWithCharDelimiterArgs(HexStringsTest::toHexCharsLongArgs);
    }
    
    @ParameterizedTest(name = "toHexChars({0}, {1}, {2}, {3}) = {4}")
    @MethodSource("toHexCharsLongWithCharDelimiterArgs")
    final void testToHexCharsLongWithCharDelimiter(long l, LetterCase letterCase, ByteOrder endian, char delimiter, char[] result) {
        execute(() -> HexStrings.toHexChars(l, letterCase, endian, delimiter), result, toHexCharsSampler(result));
    }
    
    static Stream<Arguments> toHexStringLongWithCharDelimiterArgs() {
        return toHexStringArgs(toHexCharsLongWithCharDelimiterArgs());
    }
    
    @ParameterizedTest(name = "toHexString({0}, {1}, {2}, {3}) = {4}")
    @MethodSource("toHexStringLongWithCharDelimiterArgs")
    final void testToHexStringLongWithCharDelimiter(long l, LetterCase letterCase, ByteOrder endian, char delimiter, String result) {
        execute(() -> HexStrings.toHexString(l, letterCase, endian, delimiter), result, toHexStringSampler(result));
    }
    
    static Stream<Arguments> toHexCharsDoubleWithCharDelimiterArgs() {
        return toHexCharsWithCharDelimiterArgs(HexStringsTest::toHexCharsDoubleArgs);
    }
    
    @ParameterizedTest(name = "toHexChars({0}, {1}, {2}, {3}) = {4}")
    @MethodSource("toHexCharsDoubleWithCharDelimiterArgs")
    final void testToHexCharsDoubleWithCharDelimiter(double d, LetterCase letterCase, ByteOrder endian, char delimiter, char[] result) {
        execute(() -> HexStrings.toHexChars(d, letterCase, endian, delimiter), result, toHexCharsSampler(result));
    }
    
    static Stream<Arguments> toHexStringDoubleWithCharDelimiterArgs() {
        return toHexStringArgs(toHexCharsDoubleWithCharDelimiterArgs());
    }
    
    @ParameterizedTest(name = "toHexString({0}, {1}, {2}, {3}) = {4}")
    @MethodSource("toHexStringDoubleWithCharDelimiterArgs")
    final void testToHexStringDoubleWithCharDelimiter(double d, LetterCase letterCase, ByteOrder endian, char delimiter, String result) {
        execute(() -> HexStrings.toHexString(d, letterCase, endian, delimiter), result, toHexStringSampler(result));
    }
    
    static Stream<Arguments> toHexCharsCharWithStringDelimiterArgs() {
        return toHexCharsWithStringDelimiterArgs(HexStringsTest::toHexCharsCharArgs);
    }
    
    @ParameterizedTest(name = "toHexChars({0}, {1}, {2}, {3}) = {4}")
    @MethodSource("toHexCharsCharWithStringDelimiterArgs")
    final void testToHexCharsCharWithStringDelimiter(char c, LetterCase letterCase, ByteOrder endian, CharSequence delimiter, char[] result) {
        execute(() -> HexStrings.toHexChars(c, letterCase, endian, delimiter), result, toHexCharsSampler(result));
    }
    
    static Stream<Arguments> toHexStringCharWithStringDelimiterArgs() {
        return toHexStringArgs(toHexCharsCharWithStringDelimiterArgs());
    }
    
    @ParameterizedTest(name = "toHexString({0}, {1}, {2}, {3}) = {4}")
    @MethodSource("toHexStringCharWithStringDelimiterArgs")
    final void testToHexStringCharWithStringDelimiter(char c, LetterCase letterCase, ByteOrder endian, CharSequence delimiter, String result) {
        execute(() -> HexStrings.toHexString(c, letterCase, endian, delimiter), result, toHexStringSampler(result));
    }
    
    static Stream<Arguments> toHexCharsShortWithStringDelimiterArgs() {
        return toHexCharsWithStringDelimiterArgs(HexStringsTest::toHexCharsShortArgs);
    }
    
    @ParameterizedTest(name = "toHexChars({0}, {1}, {2}, {3}) = {4}")
    @MethodSource("toHexCharsShortWithStringDelimiterArgs")
    final void testToHexCharsShortWithStringDelimiter(short s, LetterCase letterCase, ByteOrder endian, CharSequence delimiter, char[] result) {
        execute(() -> HexStrings.toHexChars(s, letterCase, endian, delimiter), result, toHexCharsSampler(result));
    }
    
    static Stream<Arguments> toHexStringShortWithStringDelimiterArgs() {
        return toHexStringArgs(toHexCharsShortWithStringDelimiterArgs());
    }
    
    @ParameterizedTest(name = "toHexString({0}, {1}, {2}, {3}) = {4}")
    @MethodSource("toHexStringShortWithStringDelimiterArgs")
    final void testToHexStringShortWithStringDelimiter(short s, LetterCase letterCase, ByteOrder endian, CharSequence delimiter, String result) {
        execute(() -> HexStrings.toHexString(s, letterCase, endian, delimiter), result, toHexStringSampler(result));
    }
    
    static Stream<Arguments> toHexCharsIntWithStringDelimiterArgs() {
        return toHexCharsWithStringDelimiterArgs(HexStringsTest::toHexCharsIntArgs);
    }
    
    @ParameterizedTest(name = "toHexChars({0}, {1}, {2}, {3}) = {4}")
    @MethodSource("toHexCharsIntWithStringDelimiterArgs")
    final void testToHexCharsIntWithStringDelimiter(int i, LetterCase letterCase, ByteOrder endian, CharSequence delimiter, char[] result) {
        execute(() -> HexStrings.toHexChars(i, letterCase, endian, delimiter), result, toHexCharsSampler(result));
    }
    
    static Stream<Arguments> toHexStringIntWithStringDelimiterArgs() {
        return toHexStringArgs(toHexCharsIntWithStringDelimiterArgs());
    }
    
    @ParameterizedTest(name = "toHexString({0}, {1}, {2}, {3}) = {4}")
    @MethodSource("toHexStringIntWithStringDelimiterArgs")
    final void testToHexStringIntWithStringDelimiter(int i, LetterCase letterCase, ByteOrder endian, CharSequence delimiter, String result) {
        execute(() -> HexStrings.toHexString(i, letterCase, endian, delimiter), result, toHexStringSampler(result));
    }
    
    static Stream<Arguments> toHexCharsFloatWithStringDelimiterArgs() {
        return toHexCharsWithStringDelimiterArgs(HexStringsTest::toHexCharsFloatArgs);
    }
    
    @ParameterizedTest(name = "toHexChars({0}, {1}, {2}, {3}) = {4}")
    @MethodSource("toHexCharsFloatWithStringDelimiterArgs")
    final void testToHexCharsFloatWithStringDelimiter(float f, LetterCase letterCase, ByteOrder endian, CharSequence delimiter, char[] result) {
        execute(() -> HexStrings.toHexChars(f, letterCase, endian, delimiter), result, toHexCharsSampler(result));
    }
    
    static Stream<Arguments> toHexStringFloatWithStringDelimiterArgs() {
        return toHexStringArgs(toHexCharsFloatWithStringDelimiterArgs());
    }
    
    @ParameterizedTest(name = "toHexString({0}, {1}, {2}, {3}) = {4}")
    @MethodSource("toHexStringFloatWithStringDelimiterArgs")
    final void testToHexStringFloatWithStringDelimiter(float f, LetterCase letterCase, ByteOrder endian, CharSequence delimiter, String result) {
        execute(() -> HexStrings.toHexString(f, letterCase, endian, delimiter), result, toHexStringSampler(result));
    }
    
    static Stream<Arguments> toHexCharsLongWithStringDelimiterArgs() {
        return toHexCharsWithStringDelimiterArgs(HexStringsTest::toHexCharsLongArgs);
    }
    
    @ParameterizedTest(name = "toHexChars({0}, {1}, {2}, {3}) = {4}")
    @MethodSource("toHexCharsLongWithStringDelimiterArgs")
    final void testToHexCharsLongWithStringDelimiter(long l, LetterCase letterCase, ByteOrder endian, CharSequence delimiter, char[] result) {
        execute(() -> HexStrings.toHexChars(l, letterCase, endian, delimiter), result, toHexCharsSampler(result));
    }
    
    static Stream<Arguments> toHexStringLongWithStringDelimiterArgs() {
        return toHexStringArgs(toHexCharsLongWithStringDelimiterArgs());
    }
    
    @ParameterizedTest(name = "toHexString({0}, {1}, {2}, {3}) = {4}")
    @MethodSource("toHexStringLongWithStringDelimiterArgs")
    final void testToHexStringLongWithStringDelimiter(long l, LetterCase letterCase, ByteOrder endian, CharSequence delimiter, String result) {
        execute(() -> HexStrings.toHexString(l, letterCase, endian, delimiter), result, toHexStringSampler(result));
    }
    
    static Stream<Arguments> toHexCharsDoubleWithStringDelimiterArgs() {
        return toHexCharsWithStringDelimiterArgs(HexStringsTest::toHexCharsDoubleArgs);
    }
    
    @ParameterizedTest(name = "toHexChars({0}, {1}, {2}, {3}) = {4}")
    @MethodSource("toHexCharsDoubleWithStringDelimiterArgs")
    final void testToHexCharsDoubleWithStringDelimiter(double d, LetterCase letterCase, ByteOrder endian, CharSequence delimiter, char[] result) {
        execute(() -> HexStrings.toHexChars(d, letterCase, endian, delimiter), result, toHexCharsSampler(result));
    }
    
    static Stream<Arguments> toHexStringDoubleWithStringDelimiterArgs() {
        return toHexStringArgs(toHexCharsDoubleWithStringDelimiterArgs());
    }
    
    @ParameterizedTest(name = "toHexString({0}, {1}, {2}, {3}) = {4}")
    @MethodSource("toHexStringDoubleWithStringDelimiterArgs")
    final void testToHexStringDoubleWithStringDelimiter(double d, LetterCase letterCase, ByteOrder endian, CharSequence delimiter, String result) {
        execute(() -> HexStrings.toHexString(d, letterCase, endian, delimiter), result, toHexStringSampler(result));
    }
    
    private static StringBuilder changeEndian(StringBuilder sb) {
        sb.reverse();
        for (int j = 1; j < sb.length(); j += 2) {
            char left = sb.charAt(j);
            sb.setCharAt(j, sb.charAt(j - 1));
            sb.setCharAt(j - 1, left);
        }
        return sb;
    }
    
    private static void expectIllegalArgumentException(Runnable r, int index, String excerpt) {
        try {
            r.run();
            fail("Accepted malformed input");
        } catch (IllegalArgumentException e) {
            final String msg = e.getMessage();
            final int excStart = msg.indexOf('\'') + 1;
            final int excEnd = msg.lastIndexOf('\'');
            final String actualExcerpt = msg.substring(excStart, excEnd);
            assertEquals(excerpt, actualExcerpt);
            final int idxEnd = msg.indexOf(" in '");
            final int idxStart = msg.lastIndexOf(' ', idxEnd - 1) + 1;
            final int actualIndex = Integer.parseInt(msg.substring(idxStart, idxEnd));
            assertEquals(index, actualIndex);
        }
    }
    
    private static <T> void execute(Supplier<T> test, T expectedResult, AllocationTester sampler) {
        try {
            if (sampler != null) {
                AllocationRecorder.addSampler(sampler);
            }
            final T actual;
            try {
                actual = test.get();
            } finally {
                if (sampler != null) {
                    AllocationRecorder.removeSampler(sampler);
                }
            }
            if (expectedResult == null) {
                fail("missing NPE");
            }
            assertEquals(expectedResult, actual);
            if (sampler != null) {
                if (sampler.failureReason != null) {
                    fail(sampler.failureReason);
                }
                assertEquals(sampler.allocations.length, sampler.allocationIndex, "Memory allocation count");
            }
        } catch (NullPointerException e) {
            if (expectedResult != null) {
                throw e;
            }
        }
    }
    
    private static void execute(Supplier<byte[]> test, byte[] expectedResult, AllocationTester sampler) {
        try {
            if (sampler != null) {
                AllocationRecorder.addSampler(sampler);
            }
            final byte[] actual;
            try {
                actual = test.get();
            } finally {
                if (sampler != null) {
                    AllocationRecorder.removeSampler(sampler);
                }
            }
            if (expectedResult == null) {
                fail("missing NPE");
            }
            assertArrayEquals(expectedResult, actual);
            if (sampler != null) {
                if (sampler.failureReason != null) {
                    fail(sampler.failureReason);
                }
                assertEquals(sampler.allocations.length, sampler.allocationIndex, "Memory allocation count");
            }
        } catch (NullPointerException e) {
            if (expectedResult != null) {
                throw e;
            }
        }
    }
    
    private static void execute(Supplier<char[]> test, char[] expectedResult, AllocationTester sampler) {
        try {
            if (sampler != null) {
                AllocationRecorder.addSampler(sampler);
            }
            final char[] actual;
            try {
                actual = test.get();
            } finally {
                if (sampler != null) {
                    AllocationRecorder.removeSampler(sampler);
                }
            }
            if (expectedResult == null) {
                fail("missing NPE");
            }
            assertArrayEquals(expectedResult, actual);
            if (sampler != null) {
                if (sampler.failureReason != null) {
                    fail(sampler.failureReason);
                }
                assertEquals(sampler.allocations.length, sampler.allocationIndex, "Memory allocation count");
            }
        } catch (NullPointerException e) {
            if (expectedResult != null) {
                throw e;
            }
        }
    }
    
    static Stream<Arguments> toHexStringArgs(Stream<Arguments> toHexCharsArgs) {
        return toHexCharsArgs.peek(a -> {
            final Object[] args = a.get();
            final char[] hexChars = (char[]) args[args.length - 1];
            if (hexChars != null) {
                args[args.length - 1] = new String(hexChars);
            }
        });
    }
    
    static Stream<Arguments> toByteArrayWithDelimArgs(Supplier<Stream<Arguments>> argsWithoutDelimSupplier) {
        Stream<Arguments> result = Stream.empty();
        for (final String delim : DELIM_STRINGS) {
            result = Stream.concat(result, argsWithoutDelimSupplier.get().map(a -> {
                final Object[] args = Arrays.copyOf(a.get(), a.get().length + 1);
                final byte[] expected = (byte[]) args[args.length - 2];
                args[args.length - 2] = delim != null ? delim.length() : -1;
                args[args.length - 1] = expected;
                final String hexString = (String) args[0];
                if (delim != null && hexString != null && hexString.length() > 2) {
                    final StringBuilder sb = new StringBuilder(hexString.length() + (hexString.length() / 2 - 1) * delim.length());
                    sb.append(hexString.charAt(0)).append(hexString.charAt(1));
                    for (int i = 3; i < hexString.length(); i += 2) {
                        sb.append(delim).append(hexString.charAt(i - 1)).append(hexString.charAt(i));
                    }
                    args[0] = sb.toString();
                }
                return Arguments.of(args);
            }));
        }
        return result;
    }
    
    static Stream<Arguments> toHexCharsWithCharDelimiterArgs(Supplier<Stream<Arguments>> argsWithoutDelimSupplier) {
        return Stream.of(DELIM_CHARS).flatMap(d -> argsWithoutDelimSupplier.get().map(a -> {
            final Object[] oldArgs = a.get();
            final Object[] newArgs = Arrays.copyOf(oldArgs, oldArgs.length + 1);
            final char[] oldResult = (char[]) oldArgs[oldArgs.length - 1];
            char[] newResult = oldResult;
            if (oldResult != null && oldResult.length > 0) {
                final StringBuilder sb = new StringBuilder(oldResult.length + (oldResult.length >> 1) - 1);
                sb.append(oldResult[0]).append(oldResult[1]);
                for (int i = 3; i < oldResult.length; i += 2) {
                    sb.append(d).append(oldResult[i - 1]).append(oldResult[i]);
                }
                newResult = sb.toString().toCharArray();
            }
            newArgs[newArgs.length - 2] = d;
            newArgs[newArgs.length - 1] = newResult;
            return Arguments.of(newArgs);
        }));
    }
    
    static Stream<Arguments> toHexCharsWithStringDelimiterArgs(Supplier<Stream<Arguments>> argsWithoutDelimSupplier) {
        return Stream.of(DELIM_STRINGS).flatMap(d -> argsWithoutDelimSupplier.get().map(a -> {
            final Object[] oldArgs = a.get();
            final Object[] newArgs = Arrays.copyOf(oldArgs, oldArgs.length + 1);
            final char[] oldResult = (char[]) oldArgs[oldArgs.length - 1];
            char[] newResult = oldResult;
            if (oldResult != null && oldResult.length > 0 && d != null && d.length() > 0) {
                final StringBuilder sb = new StringBuilder(oldResult.length + ((oldResult.length >> 1) - 1) * d.length());
                sb.append(oldResult[0]).append(oldResult[1]);
                for (int i = 3; i < oldResult.length; i += 2) {
                    sb.append(d).append(oldResult[i - 1]).append(oldResult[i]);
                }
                newResult = sb.toString().toCharArray();
            }
            newArgs[newArgs.length - 2] = d;
            newArgs[newArgs.length - 1] = newResult;
            return Arguments.of(newArgs);
        }));
    }
    
    private static AllocationTester toHexStringSampler(String result) {
        if (result == null) {
            return null;
        }
        
        return new AllocationTester(new AllocationDescriptor[]{
                new AllocationDescriptor("char", result.length()),
                new AllocationDescriptor("char", result.length()),
                new AllocationDescriptor("java/lang/String", -1),
        });
    }
    
    private static AllocationTester toByteArraySampler(byte[] result) {
        return result != null ? new AllocationTester(new AllocationDescriptor("byte", result.length)) : null;
    }
    
    private static AllocationTester toHexCharsSampler(char[] result) {
        return result != null ? new AllocationTester(new AllocationDescriptor("char", result.length)) : null;
    }
    
    private static class AllocationDescriptor {
        final String type;
        final int arraySize;
        
        AllocationDescriptor(String type, int arraySize) {
            this.type = Objects.requireNonNull(type);
            this.arraySize = arraySize;
        }
        
        @Override
        public String toString() {
            return "AllocationDescriptor{" +
                    "type=" + type +
                    ", arraySize=" + arraySize +
                    '}';
        }
    }
    
    private static class AllocationTester implements Sampler {
        private final Thread sampledThread = Thread.currentThread();
        final AllocationDescriptor[] allocations;
        int allocationIndex = 0;
        // null - awaiting further allocations
        // TRUE - success (as long as no extra allocations are received)
        // FALSE - failure (unexpected allocation, too many allocations, etc.)
        Boolean result = null;
        String failureReason = null;
        
        AllocationTester(AllocationDescriptor... allocations) {
            this.allocations = allocations;
        }
        
        @Override
        public void sampleAllocation(int count, String desc, Object newObj, long size) {
            if (Thread.currentThread() != sampledThread) {
                return;
            }
            
            if (result == Boolean.FALSE) {
                return; // doesn't matter, already failed
            }
    
            if (desc.startsWith("com/google/monitoring/runtime/instrumentation/")) {
                return; // allow all instrumentation activity to proceed normally
            }
            
            if (result == Boolean.TRUE) {
                result = Boolean.FALSE;
                failureReason = "Unexpected allocation: " + describe(count, desc);
                return;
            }
            
            AllocationDescriptor expectedAllocation = allocations[allocationIndex];
            if (!expectedAllocation.type.equals(desc) || expectedAllocation.arraySize != count) {
                result = Boolean.FALSE;
                failureReason = "Allocated " + describe(count, desc) + ", expected " + describe(expectedAllocation.arraySize, expectedAllocation.type);
                return;
            }
            
            if (++allocationIndex == allocations.length) {
                result = Boolean.TRUE;
            }
        }
        
        private static String describe(int count, String desc) {
            return desc + (count != -1 ? '[' + count + ']' : "");
        }
    }
}
