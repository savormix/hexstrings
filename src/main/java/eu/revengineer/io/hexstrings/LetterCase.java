/*
 * Copyright 2019 RevEngineer.eu
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

/**
 * Explicitly names upper- and lowercase for immediate clarity in the API.
 *
 * @author savormix
 */
public enum LetterCase {
    /** Lowercase hexadecimal: {@code 0123456789abcdef} */
    LOWERCASE("0123456789abcdef".toCharArray()),
    /** Uppercase hexadecimal: {@code 0123456789ABCDEF} */
    UPPERCASE("0123456789ABCDEF".toCharArray());
    
    private char[] hexChars;
    
    LetterCase(char[] hexChars) {
        this.hexChars = hexChars;
    }
    
    char[] getHexChars() {
        return hexChars;
    }
    
    /**
     * Returns {@code true} if this indicates {@link #LOWERCASE}.
     *
     * @return {@code true} for lowercase
     */
    public boolean isLowerCase() {
        return this == LOWERCASE;
    }
    
    /**
     * Returns {@code true} if this indicates {@link #UPPERCASE}.
     *
     * @return {@code true} for uppercase
     */
    public boolean isUpperCase() {
        return this == UPPERCASE;
    }
}
