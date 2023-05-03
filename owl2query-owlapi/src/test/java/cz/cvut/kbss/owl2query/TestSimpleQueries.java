package cz.cvut.kbss.owl2query;

import cz.cvut.kbss.owl2query.engine.OWL2QueryEngine;
import cz.cvut.kbss.owl2query.model.OWL2Ontology;
import cz.cvut.kbss.owl2query.model.QueryResult;
import cz.cvut.kbss.owl2query.model.owlapi.OWLAPIv3OWL2Ontology;
import org.apache.jena.util.FileUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSimpleQueries {

    private List<Map<String,String>> loadResults(final InputStream file) throws IOException {
        final List<Map<String,String>> list = new ArrayList<>();
        try(final BufferedReader bis = new BufferedReader(new InputStreamReader(file))) {
            String line;
            line = bis.readLine();
            List<String> variables = Arrays.stream(line.split(",")).collect(Collectors.toList());

            while((line = bis.readLine()) != null) {
                List<String> values = Arrays.stream(line.split(",")).collect(Collectors.toList());
                final Map<String,String> row = new HashMap<>();
                for(int i = 0; i < variables.size(); i++) {
                    row.put(variables.get(i), values.get(i));
                }
                list.add(row);
            }
        }
        return list;
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/cases.csv", numLinesToSkip = 1)
    public void testValuesEvaluation(final String test) throws OWLOntologyCreationException, IOException {
        final OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        final OWLOntology oo = m.loadOntologyFromOntologyDocument(getClass().getResourceAsStream("/" + test + "/ontology.ttl"));
        final OWL2Ontology o = new OWLAPIv3OWL2Ontology(m, oo, TestConfiguration.get(TestConfiguration.PELLET).getFactory().createReasoner(oo));

        final String sparql = FileUtils.readWholeFileAsUTF8(getClass().getResourceAsStream("/" + test + "/query.rq" ));

        final QueryResult<OWLObject> e = OWL2QueryEngine.exec(sparql, o);
        final List<Map<String,String>> actual
                = StreamSupport.stream(e.spliterator(), false)
                .map( b -> {
                    final Map<String,String> map = new HashMap<>();
                    b.keySet().forEach( k -> map.put(k.toString(), b.get(k).toString()));
                    return map;
                } )
                .collect(Collectors.toList());
        final List<Map<String,String>> expected = loadResults(getClass().getResourceAsStream("/" + test + "/expected.csv"));
        assertEquals(expected, actual);
    }
}
