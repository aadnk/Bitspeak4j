# Bitspeak
A Java-library for encoding/decoding byte streams into pronounceable text, based on a 
Javascript implementation and specification in [MaiaVictor/Bitspeak](https://github.com/MaiaVictor/Bitspeak).

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

It is also possible to convert the bytes in an _InputStream_ to a _Reader_ with encoded Bitspeak, 
or the other way around:
```java
Path input = Paths.get("input.data");
Path output = Paths.get("output.txt");

try (Reader reader = Bitspeak.bs6().newEncodeStream(Files.newInputStream(input));
     BufferedWriter writer = Files.newBufferedWriter(output)) {
    CharStreams.copy(reader, writer); // Guava
}
```

## Maven

You can use this as maven dependency. Repository:
```XML
<reporitory>
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
    <version>0.1-SNAPSHOT</version>
    <scope>compile</scope>
</dependency>
```

# License
This project is under the the GPL v2 License, look at the LICENSE file for more informations.
