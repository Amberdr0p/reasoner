package dsf;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class LauncherGet {
	
	public static void main(String args[]) {	
		
		String message = "@prefix mcht: <http://purl.org/NET/ssnext/machinetools#> . @prefix meter: <http://purl.org/NET/ssnext/meters/core#> . @prefix ssn: <http://purl.oclc.org/NET/ssnx/ssn#> . @prefix xsd: <http://www.w3.org/2001/XMLSchema#> . @prefix : <http://example.com/230.230.230.230.230.230/machinetool#> .  :11111111 a mcht:ButtonsObservation ;     ssn:observationResultTime \"2005-08-15T15:52:01+00:00\"^^xsd:dateTime ;     ssn:observedBy <http://example.com/230.230.230.230.230.230> ;     ssn:observationResult :11111111-result .  :11111111-result a ssn:SensorOutput ;     ssn:isProducedBy <http://example.com/230.230.230.230.230.230> ;     ssn:hasValue mcht:PauseStateValue .";
		for (int j=0; j<5; j++) {
			prRun(message);
			oldRun(message);
		}
	}
	
	private static void oldRun(String message) {
		int i = 0;
		String TIMESTAMP = "timestamp";
		String ENUM_VALUE = "enum_value";
		
		String query = "prefix hmtr: <http://purl.org/NET/ssnext/heatmeters#> "
				+ "prefix emtr: <http://purl.org/NET/ssnext/electricmeters#> "
				+ "prefix mcht: <http://purl.org/NET/ssnext/machinetools#> "
				+ "prefix meter: <http://purl.org/NET/ssnext/meters/core#> "
				+ "prefix ssn: <http://purl.oclc.org/NET/ssnx/ssn#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ "SELECT ?timestamp ?enum_value "
				+ "WHERE {?x ssn:observationResultTime ?timestamp; "
				+ "ssn:observationResult ?result."
				+ " ?result ssn:hasValue ?enum_value.}";
		Query METRICS_QUERY = QueryFactory
				.create(query);
		long start = System.currentTimeMillis();
		for(;i<50000;i++) {
			Model description = ModelFactory.createDefaultModel().read(
					IOUtils.toInputStream(message), null,
					"TURTLE");
			
			QueryExecution qe = QueryExecutionFactory.create(METRICS_QUERY,
					description);
			ResultSet metrics = qe.execSelect();

			while (metrics.hasNext()) {
				QuerySolution qs = metrics.next();
				qs.getLiteral(TIMESTAMP).getString();
				qs.getResource(ENUM_VALUE).getURI();
			}
		}
		long end = System.currentTimeMillis();
		//System.out.println(start);
		//System.out.println(end);
		System.out.println(end-start);
	}
	
	private static void prRun(String message) {
		int i = 0;
		long start = System.currentTimeMillis(); 
		for (;i<50000;i++) {
			Model description = ModelFactory.createDefaultModel().read(
					IOUtils.toInputStream(message), null,
					"TURTLE");
			
			//Property property = description.getProperty("http://purl.oclc.org/NET/ssnx/ssn#hasValue");
			NodeIterator ni = description.listObjectsOfProperty(
					description.getProperty("http://purl.oclc.org/NET/ssnx/ssn#hasValue"));
			NodeIterator rt = description.listObjectsOfProperty(
					description.getProperty("http://purl.oclc.org/NET/ssnx/ssn#observationResultTime"));
			
			while (ni.hasNext()) {
				RDFNode ns = ni.next();
				ns.toString();
				if(rt.hasNext()) {
					RDFNode rs = rt.next();
					rs.toString();
				}
			}
			/*while (rt.hasNext()) {
				RDFNode rs = rt.next();
				System.out.println(rs.toString());
			}*/
		}
		long end = System.currentTimeMillis();
		//System.out.println(start);
		//System.out.println(end);
		System.out.println(end-start);
		
		
		/*while (ri.hasNext()) {
			Resource rs = ri.next();
			System.out.println(rs.getURI());
		}*/
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
}
