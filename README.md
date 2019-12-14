# Bitspeak4j
A Java-library for encoding/decoding byte streams into pronounceable text, based on a 
Javascript implementation and specification in [MaiaVictor/Bitspeak](https://github.com/MaiaVictor/Bitspeak).

* [JavaDoc](https://comphenix.net/bitspeak/javadoc/index.html)

## Usage
To convert a byte array, select the Bitspeak format you would like to use (either BS-6 or BS-8) 
using `Bitspeak.bs6()` or `Bitspeak.bs8()`, then call the _decode_ method:
```java
// Returns "pakatape"
Bitspeak.bs6().encode(new byte[] { 1, 2, 3 }); 
```
The resulting string can be decoded using the _decode_ method:
```java
// Returns an array with [1, 2, 3]
Bitspeak.bs6().decode("pakatape")); 
```

It is also possible to write the bytes in an _InputStream_ to a _Writer_ with encoded Bitspeak:
```java
Path input = Paths.get("input.data");
Path output = Paths.get("output.txt");

try (InputStream reader = Files.newInputStream(input);
     BufferedWriter writer = Files.newBufferedWriter(output)) {
    Bitspeak.bs6().encodeStream(reader, writer);
}
```

The reverse is also possible:
```java
Path input = Paths.get("output.txt");
Path output = Paths.get("roundtrip.data");

try (BufferedReader reader = Files.newBufferedReader(input);
     OutputStream writer = Files.newOutputStream(output)) {
    Bitspeak.bs6().decodeStream(reader, writer);
}
```

## Maven

You can use this as a Maven dependency, using the following repository:
```XML
<repository>
    <id>comphenix</id>
    <name>Comphenix Maven Public</name>
    <url>https://repo.comphenix.net/content/repositories/public/</url>
</repository>
```

Dependency:
```XML
<dependency>
    <groupId>com.comphenix</groupId>
    <artifactId>Bitspeak</artifactId>
    <version>0.3-SNAPSHOT</version>
    <scope>compile</scope>
</dependency>
```

# License
This project is under the the LGPL v3 License, consult the LICENSE file for more information.
