# pdfmerge
Merge multiple PDFs into one.

## Installation
Requirements: Java 8+
1. Download the prebuilt distribution from the project's [Releases](https://github.com/nemethi/pdfmerge/releases) page,
   or build it yourself (see instructions below).
2. Extract the `pdfmerge-<version>.zip` file where you want.
3. Add the `bin` directory inside the extracted `pdfmerge` directory to your `PATH`.
4. Confirm that you are able to run the program with `pdfmerge --version`.

## Usage
To confirm your installation you can display the help message by issuing:
```
pdfmerge --help
```
The output should be similar to this:
```
Usage: pdfmerge [-fhV] -o=OUTFILE FILE FILE...
Merge multiple PDF FILEs into OUTFILE.

      FILE FILE...       Path to the files to be merged.
  -f, --force            Overwrite OUTFILE.
  -h, --help             Show this help message and exit.
  -o, --output=OUTFILE   Path to the output file.
  -V, --version          Print version information and exit.
```

Usage is very simple: first, you have to specify the output file with the `-o` / `--output` option.
Then you have to specify at least two input files:
```
pdfmerge -o path/to/output.pdf path/to/input1.pdf path/to/input2.pdf
```

If the output file already exists the program will not overwrite it and it will exit with an error.
To overwrite the output file use the `-f` / `--force` option:
```
pdfmerge -f --output existing.pdf input1.pdf input2.pdf
```

The program only accepts file paths; but you can use globs to reference PDFs in a directory:
```
pdfmerge -o output.pdf mypdfs/*.pdf
```

## Building
Requirements: JDK 8+

The build defaults to building the ZIP file.

### Building the default ZIP distribution
To run the unit tests and build the ZIP file execute:
```
./mvnw package
```
To do all of the above and also run the integration tests execute:
```
./mvnw verify
```

The built ZIP file will be in the `target` directory.

### Building the executable fat JAR
To build an executable JAR that contains all dependencies use the `fatjar` profile:
```
./mvnw package -Pfatjar
```

The built `pdfmerge.jar` will be in the `target` directory.

You can use it like this:
```
java -jar pdfmerge.jar <options>
```

## Acknowledgements
This project is heavily building on these libraries:
* [picocli](https://picocli.info/)
* [Apache PDFBox](https://pdfbox.apache.org/)

Both libraries are licensed under the [Apache License version 2.0](https://www.apache.org/licenses/LICENSE-2.0).

## Contact
Gábor Némethi - [nemethi](https://github.com/nemethi)

Project - https://github.com/nemethi/pdfmerge

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more information.
