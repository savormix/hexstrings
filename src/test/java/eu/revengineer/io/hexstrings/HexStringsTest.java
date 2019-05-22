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
    private static final char[] DELIM_CHARS = {' ', '-', ':'};
    private static final String[] DELIM_STRINGS = {null, "", " ", ", ", " | "};
    
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
                builder.add(Arguments.of(withOffset, i, bytes.length, bool, bool.isUpperCase() ? result.toUpperCase(Locale.ENGLISH) : result));
            }
        }
        for (final LetterCase bool : BOOLS) {
            builder.add(Arguments.of(null, -1, -1, bool, null));
        }
        return builder.build();
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
                builder.add(Arguments.of(bytes, bool, (bool.isUpperCase() ? result.toUpperCase(Locale.ENGLISH) : result)));
            }
        }
        for (final LetterCase bool : BOOLS) {
            builder.add(Arguments.of(null, bool, null));
        }
        return builder.build();
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
        return toHexStringWithCharDelimiterArgs(HexStringsTest::toHexStringArgs);
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
        return toHexStringWithCharDelimiterArgs(HexStringsTest::toHexStringEntireArrayArgs);
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
        return toHexStringWithStringDelimiterArgs(HexStringsTest::toHexStringArgs);
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
        return toHexStringWithStringDelimiterArgs(HexStringsTest::toHexStringEntireArrayArgs);
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
                assertEquals(sampler.allocations.length, sampler.allocationIndex, "Memory allocation count");
            }
        } catch (NullPointerException e) {
            if (expectedResult != null) {
                throw e;
            }
        }
    }
    
    static Stream<Arguments> toHexStringWithCharDelimiterArgs(Supplier<Stream<Arguments>> argsWithoutDelimSupplier) {
        Stream<Arguments> result = Stream.empty();
        for (final char delim : DELIM_CHARS) {
            result = Stream.concat(result, argsWithoutDelimSupplier.get().map(a -> {
                final Object[] args = Arrays.copyOf(a.get(), a.get().length + 1);
                final String expected = (String) args[args.length - 2];
                args[args.length - 2] = delim;
                args[args.length - 1] = expected;
                if (expected != null && expected.length() > 2) {
                    final StringBuilder sb = new StringBuilder(expected.length() + expected.length() / 2 - 1);
                    sb.append(expected.charAt(0)).append(expected.charAt(1));
                    for (int i = 3; i < expected.length(); i += 2) {
                        sb.append(delim).append(expected.charAt(i - 1)).append(expected.charAt(i));
                    }
                    args[args.length - 1] = sb.toString();
                }
                return Arguments.of(args);
            }));
        }
        return result;
    }
    
    static Stream<Arguments> toHexStringWithStringDelimiterArgs(Supplier<Stream<Arguments>> argsWithoutDelimSupplier) {
        Stream<Arguments> result = Stream.empty();
        for (final String delim : DELIM_STRINGS) {
            result = Stream.concat(result, argsWithoutDelimSupplier.get().map(a -> {
                final Object[] args = Arrays.copyOf(a.get(), a.get().length + 1);
                final String expected = (String) args[args.length - 2];
                args[args.length - 2] = delim;
                args[args.length - 1] = expected;
                if (delim != null && expected != null && expected.length() > 2) {
                    final StringBuilder sb = new StringBuilder(expected.length() + (expected.length() / 2 - 1) * delim.length());
                    sb.append(expected.charAt(0)).append(expected.charAt(1));
                    for (int i = 3; i < expected.length(); i += 2) {
                        sb.append(delim).append(expected.charAt(i - 1)).append(expected.charAt(i));
                    }
                    args[args.length - 1] = sb.toString();
                }
                return Arguments.of(args);
            }));
        }
        return result;
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
        Stream<Arguments> result = Stream.empty();
        for (final char delim : DELIM_CHARS) {
            result = Stream.concat(result, argsWithoutDelimSupplier.get().map(a -> {
                final Object[] args = Arrays.copyOf(a.get(), a.get().length + 1);
                final char[] expected = (char[]) args[args.length - 2];
                args[args.length - 2] = delim;
                args[args.length - 1] = expected;
                if (expected != null && expected.length > 2) {
                    final StringBuilder sb = new StringBuilder(expected.length + expected.length / 2 - 1);
                    sb.append(expected[0]).append(expected[1]);
                    for (int i = 3; i < expected.length; i += 2) {
                        sb.append(delim).append(expected[i - 1]).append(expected[i]);
                    }
                    args[args.length - 1] = sb.toString().toCharArray();
                }
                return Arguments.of(args);
            }));
        }
        return result;
    }
    
    static Stream<Arguments> toHexCharsWithStringDelimiterArgs(Supplier<Stream<Arguments>> argsWithoutDelimSupplier) {
        Stream<Arguments> result = Stream.empty();
        for (final String delim : DELIM_STRINGS) {
            result = Stream.concat(result, argsWithoutDelimSupplier.get().map(a -> {
                final Object[] args = Arrays.copyOf(a.get(), a.get().length + 1);
                final char[] expected = (char[]) args[args.length - 2];
                args[args.length - 2] = delim;
                args[args.length - 1] = expected;
                if (delim != null && expected != null && expected.length > 2) {
                    final StringBuilder sb = new StringBuilder(expected.length + (expected.length / 2 - 1) * delim.length());
                    sb.append(expected[0]).append(expected[1]);
                    for (int i = 3; i < expected.length; i += 2) {
                        sb.append(delim).append(expected[i - 1]).append(expected[i]);
                    }
                    args[args.length - 1] = sb.toString().toCharArray();
                }
                return Arguments.of(args);
            }));
        }
        return result;
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
        final AllocationDescriptor[] allocations;
        int allocationIndex = 0;
        // null - awaiting further allocations
        // TRUE - success (as long as no extra allocations are received)
        // FALSE - failure (unexpected allocation, too many allocations, etc.)
        Boolean result = null;
        
        AllocationTester(AllocationDescriptor... allocations) {
            this.allocations = allocations;
        }
        
        @Override
        public void sampleAllocation(int count, String desc, Object newObj, long size) {
            if (result == Boolean.FALSE) {
                return; // doesn't matter, already failed
            }
            
            if ("com/google/monitoring/runtime/instrumentation/Sampler".equals(desc)) {
                return; // during removal step
            }
            
            if (result == Boolean.TRUE) {
                result = Boolean.FALSE;
                fail("Unexpected allocation: " + describe(count, desc));
            }
            
            AllocationDescriptor expectedAllocation = allocations[allocationIndex];
            if (!expectedAllocation.type.equals(desc) || expectedAllocation.arraySize != count) {
                result = Boolean.FALSE;
                fail("Allocated " + describe(count, desc) + ", expected " + describe(expectedAllocation.arraySize, expectedAllocation.type));
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
