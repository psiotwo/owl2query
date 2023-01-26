# OWL2Query
A SPARQL-DL<sup>NOT</sup> engine

The engine executes SPARQL-DL^NOT queries as presented in [1].

[1] Petr Kremen & Bogdan Kostov, 2012. "[Expressive OWL Queries: Design, Evaluation, Visualization](https://www.igi-global.com/article/expressive-owl-queries/75774)," International Journal on Semantic Web and Information Systems (IJSWIS), IGI Global, vol. 8(4), pages 57-79, October.

## Usage

OWL2Query is not hosted in Maven Central. Use the following repository instead:

```xml
<repository>
    <id>kbss</id>
    <name>KBSS Maven 2 Repository</name>
    <url>https://kbss.felk.cvut.cz/m2repo</url>
</repository>
```

Then,

```xml
<dependency>
    <groupId>cz.cvut.kbss</groupId>
    <artifactId>owl2query-owlapi</artifactId>
    <version>${cz.cvut.kbss.owl2query.version}</version>
</dependency>
```

imports the engine. However, for OWL2Query to work, an [OWL API](https://github.com/owlcs/owlapi)-compatible reasoner needs to be present as well. 
For example, using [Openllet](https://github.com/Galigator/openllet) would require an additional dependency:

```xml
<dependencies>
    <dependency>
        <groupId>com.github.galigator.openllet</groupId>
        <artifactId>openllet-owlapi</artifactId>
        <version>${com.github.galigator.openllet.version}</version>
    </dependency>
    <dependency>
        <groupId>com.github.galigator.openllet</groupId>
        <artifactId>openllet-explanation</artifactId>
        <version>${com.github.galigator.openllet.version}</version>
    </dependency>
</dependencies>
```

## License

LGPLv3
