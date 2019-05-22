# hexstrings

[![License](https://img.shields.io/github/license/savormix/hexstrings.svg)](LLICENSE)
[![Build Status](https://dev.azure.com/savormix/RevEngineer.eu%20-%20Public/_apis/build/status/savormix.hexstrings?branchName=develop)](https://dev.azure.com/savormix/RevEngineer.eu%20-%20Public/_build/latest?definitionId=1&branchName=develop)
![Dependency Status](https://img.shields.io/librariesio/github/savormix/hexstrings.svg)
![Tests](https://img.shields.io/azure-devops/tests/savormix/RevEngineer.eu%20-%20Public/1/develop.svg)
![Coverage](https://img.shields.io/azure-devops/coverage/savormix/RevEngineer.eu%20-%20Public/1/develop.svg)

The `hexstrings` library allows you to convert between byte arrays, Java primitive types and strings composed of hexadecimal octets.
This library can generate and parse uniformly delimited hex strings of any case (upper, lower, mixed).

The minimalistic design (no dependencies) and focus on avoiding needless memory allocations makes this library perfect for anything that needs to handle binary data.

## Usage

For array operations, use the `HexStrings` utility class. Key methods are: `toHexChars` (1 memory allocation), `toHexString` (3 memory allocations) and `toByteArray`/`toByteArrayDelimited` (1 memory allocation).

- `HexStrings.toHexString(new byte[] { 0x00, 0x0F, 0x31 }, LetterCase.LOWERCASE)` = `"000f31"`
- `HexStrings.toHexString(new byte[] { 0x00, 0x0F, 0x31 }, LetterCase.UPPERCASE, ' ')` = `"00 0F 31"`
- `HexStrings.toHexString(new byte[] { 0x00, 0x0F, 0x31 }, LetterCase.LOWERCASE, "__")` = `"00__0f__31"`


- `HexStrings.toByteArray("000F31", LetterCase.UPPERCASE)` = `byte[3] { 0, 15, 49 }`
- `HexStrings.toByteArray("AabBCCdd")` = `byte[4] { -86, -69, -52, -35 }`
- `HexStrings.toByteArrayDelimited("00 0F 3A 9D", LetterCase.UPPERCASE, 1)` = `byte[4] { 0, 15, 58, -99 }`
- `HexStrings.toByteArrayDelimited("00, 0F, 3A, 9D", LetterCase.UPPERCASE, 2)` = `byte[4] { 0, 15, 58, -99 }`

## License

This library is provided under the [Apache License, Version 2.0](LICENSE).
