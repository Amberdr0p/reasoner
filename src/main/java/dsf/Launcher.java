package dsf;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;


public class Launcher {
	
	

	public static void main(String args[]) {	
		
		String message = "@prefix mcht: <http://purl.org/NET/ssnext/machinetools#> . @prefix meter: <http://purl.org/NET/ssnext/meters/core#> . @prefix ssn: <http://purl.oclc.org/NET/ssnx/ssn#> . @prefix xsd: <http://www.w3.org/2001/XMLSchema#> . @prefix : <http://example.com/230.230.230.230.230.230/machinetool#> .  :11111111 a mcht:ButtonsObservation ;     ssn:observationResultTime \"2005-08-15T15:52:01+00:00\"^^xsd:dateTime ;     ssn:observedBy <http://example.com/230.230.230.230.230.230> ;     ssn:observationResult :11111111-result .  :11111111-result a ssn:SensorOutput ;     ssn:isProducedBy <http://example.com/230.230.230.230.230.230> ;     ssn:hasValue mcht:PauseStateValue .";
		
		Model description = ModelFactory.createDefaultModel().read(
				IOUtils.toInputStream(message), null,
				"TURTLE");
		
		Model schema = ModelFactory.createDefaultModel(); 
		List<String> listOntology = new ArrayList<String>();
		listOntology.add("http://purl.oclc.org/NET/ssnx/ssn#");
		Map<String, String> prefixes = description.getNsPrefixMap();
		for (Map.Entry<String, String> entry : prefixes.entrySet()) {
			try {
				if(!listOntology.contains(entry.getValue())) {
					schema.add(FileManager.get().loadModel(entry.getValue()));
					listOntology.add(entry.getValue());
					System.out.println("Added ontology Key : " + entry.getKey() + " Value : " + entry.getValue());
				}
			} catch(Exception ex) {
				System.out.println("Cannot find ontology " + entry.getValue());
			}
		}
		
		InfModel infModel = ModelFactory.createRDFSModel(schema,
				description);
		
		QueryExecution qe = QueryExecutionFactory.create(METRICS_QUERY_WITH_REASONER,
				infModel);
		//System.out.println(toString(infModel));
		
		ResultSet metrics = qe.execSelect();
		
		while (metrics.hasNext()) {
			QuerySolution qs = metrics.next();
			String timestamp = qs.getLiteral(TIMESTAMP).getString();
			Literal valueLiteral = qs.getLiteral(VALUE);
			String value;
			if (valueLiteral != null) {
				value = valueLiteral.getString(); // value simulator
			} else {
				value = qs.getResource(ENUM_VALUE).getURI(); // instance enum uri 
			}
			String observation = qs.getResource(OBSERVATION).getURI();
			
			System.out.println(timestamp);
			System.out.println(observation);
			System.out.println(value);
		}
	}
	
	private static String toString(final Model model) {
        try (Writer writer = new StringWriter()) {
            model.write(writer, "TURTLE");
            return writer.toString();
        } catch (IOException ex) {
        	System.out.println(ex.getMessage());
        }
        return null;
    }
	
	
	private static final String TIMESTAMP = "timestamp";
	private static final String NAME_METRIC = "name";
	private static final String ENUM_VALUE = "enum_value";
	private static final String VALUE = "value";
	private static final String OBSERVATION = "observation";
	
	private static final Query METRICS_QUERY_WITH_REASONER = QueryFactory
			.create("prefix meter: <http://purl.org/NET/ssnext/meters/core#> "
					+ "prefix ssn: <http://purl.oclc.org/NET/ssnx/ssn#> "
					+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
					+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
					+ "SELECT ?timestamp ?name ?enum_value ?observation ?value "
					+ "WHERE { ?x a ?observation. "
					+ "?observation rdfs:subClassOf ssn:Observation. "
					+ "?x ssn:observationResultTime ?timestamp; "
					+ "ssn:observedBy ?name; "
					+ "ssn:observationResult ?result. "
					+ "?result ssn:hasValue ?enum_value. "
					+ "OPTIONAL { ?enum_value meter:hasQuantityValue  ?value}"
					+ "FILTER (?observation != ssn:Observation)}");
	
	private static final Query METRICS_QUERY = QueryFactory
			.create(new StringBuilder()
					.append("prefix hmtr: <http://purl.org/NET/ssnext/heatmeters#> ")
					.append("prefix emtr: <http://purl.org/NET/ssnext/electricmeters#> ")
					.append("prefix mcht: <http://purl.org/NET/ssnext/machinetools#> ")
					.append("prefix meter: <http://purl.org/NET/ssnext/meters/core#> ")
					.append("prefix ssn: <http://purl.oclc.org/NET/ssnx/ssn#> ")
					.append("prefix xsd: <http://www.w3.org/2001/XMLSchema#> ")
					.append("SELECT ?").append(TIMESTAMP).append(" ?")
					.append(NAME_METRIC).append(" ?").append(ENUM_VALUE)
					.append(" ?").append(OBSERVATION).append(" ?").append(VALUE)
					.append(" WHERE { {?x a hmtr:TemperatureObservation} ")
					.append("UNION{ ?x a hmtr:HeatObservation} ")
					.append("UNION{ ?x a emtr:AmperageObservation} ")
					.append("UNION{ ?x a emtr:VoltageObservation} ")
					.append("UNION{ ?x a emtr:PowerObservation} ")
					.append("UNION{ ?x a mcht:ButtonsObservation} ")
					.append("UNION{ ?x a mcht:WorkingStateObservation} ")
					.append("?x ssn:observationResultTime ?").append(TIMESTAMP)
					.append("; ").append("ssn:observedBy ?")
					.append(NAME_METRIC).append("; ")
					.append("ssn:observationResult ?result. ")
					.append("?result ssn:hasValue ?").append(ENUM_VALUE)
					.append(". ?x a ?").append(OBSERVATION)
					.append(". OPTIONAL { ?").append(ENUM_VALUE)
					.append(" meter:hasQuantityValue  ?")
					.append(VALUE).append("}}").toString());
}
