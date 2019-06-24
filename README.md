# hexstrings

[![License](https://img.shields.io/github/license/savormix/hexstrings.svg)](LICENSE)
[![Build Status](https://dev.azure.com/savormix/RevEngineer.eu%20-%20Public/_apis/build/status/savormix.hexstrings?branchName=develop)](https://dev.azure.com/savormix/RevEngineer.eu%20-%20Public/_build/latest?definitionId=1&branchName=develop)
![Tests](https://img.shields.io/azure-devops/tests/savormix/RevEngineer.eu%20-%20Public/1/develop.svg)
![Coverage](https://img.shields.io/azure-devops/coverage/savormix/RevEngineer.eu%20-%20Public/1/develop.svg)
![Dependency Status](https://img.shields.io/librariesio/github/savormix/hexstrings.svg)

The `hexstrings` library allows you to convert between byte arrays, Java primitive types and strings composed of hexadecimal octets.
This library can generate and parse uniformly delimited hex strings of any case (upper, lower, mixed).

The minimalistic design (no dependencies) and focus on avoiding needless memory allocations makes this library perfect for anything that needs to handle binary data.

## Usage

```xml
    <dependency>
        <groupId>eu.revengineer.io</groupId>
        <artifactId>hexstrings</artifactId>
        <version>0.9.0-SNAPSHOT</version>
    </dependency>
```

### From byte[] to String

- `HexStrings.toHexString(new byte[] { 0x00, 0x0F, 0x31 }, LetterCase.LOWERCASE)` = `"000f31"`
- `HexStrings.toHexString(new byte[] { 0x00, 0x0F, 0x31 }, LetterCase.UPPERCASE, ' ')` = `"00 0F 31"`
- `HexStrings.toHexString(new byte[] { 0x00, 0x0F, 0x31 }, LetterCase.LOWERCASE, "__")` = `"00__0f__31"`

### From primitive types to String

- `HexStrings.toHexString((byte) 100, LetterCase.LOWERCASE)` = `"64"`
- `HexStrings.toHexString('d', LetterCase.LOWERCASE, ByteOrder.LITTLE_ENDIAN)` = `"64 00"`
- `HexStrings.toHexString(100, LetterCase.LOWERCASE, ByteOrder.LITTLE_ENDIAN)` = `"64 00 00 00"`
- `HexStrings.toHexString(0F, LetterCase.LOWERCASE, ByteOrder.LITTLE_ENDIAN)` = `"00 00 00 00"`
- `HexStrings.toHexString(0D, LetterCase.LOWERCASE, ByteOrder.LITTLE_ENDIAN)` = `"00 00 00 00 00 00 00 00"`

### From String to byte[]

- `HexStrings.toByteArray("000F31", LetterCase.UPPERCASE)` = `byte[3] { 0, 15, 49 }`
- `HexStrings.toByteArray("AabBCCdd")` = `byte[4] { -86, -69, -52, -35 }`
- `HexStrings.toByteArrayDelimited("00 0F 3A 9D", LetterCase.UPPERCASE, 1)` = `byte[4] { 0, 15, 58, -99 }`
- `HexStrings.toByteArrayDelimited("00, 0F, 3A, 9D", LetterCase.UPPERCASE, 2)` = `byte[4] { 0, 15, 58, -99 }`

## License

This library is provided under the [Apache License, Version 2.0](LICENSE).
