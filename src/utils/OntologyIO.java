package utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.spin.inference.DefaultSPINRuleComparator;
import org.topbraid.spin.inference.SPINInferences;
import org.topbraid.spin.inference.SPINRuleComparator;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.util.CommandWrapper;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.util.SPINQueryFinder;
import org.topbraid.spin.vocabulary.SPIN;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mainPackage.Main;
import models.AbstractBoilerPlate;
import models.Architecture;
import models.GenericBoilerPlate;
import models.OntologyWordModel;
import models.PropertyModel;
import models.PropertyPattern;
import models.PropertyStatusModel;
import models.RequirementModel;
import models.RequirementStatusModel;
import models.ValidationResultModel;

public class OntologyIO {

	// Models
	static Model csspModel, model, rblpModel, dsoModel, prpModel, infModel;
	private static HashMap<String, RequirementModel> reqIDtoObject;
	public static final String INSTANCE_FILE_NAME = "instances-tool.rdf";
	public static final String PRP_FILE_NAME = "PRP-Instances.rdf";
	private static final String ONT_BASE_PATH = "ontologies/";
	public static final String DSO_FILE_NAME = ONT_BASE_PATH + "dsoinstancesfull.rdf";
	public static final String CSSP_FILE_NAME = ONT_BASE_PATH + "cssp.ttl";
	public static final String RBLP_FILE_NAME = ONT_BASE_PATH + "RBLP.ttl";
	private static final String BASE_ARCHIVE_PATH = ONT_BASE_PATH + "archives/";
	private static final String ARCHIVE_FORMAT = "yyyy.MM.dd.HH.mm";
	private static final Logger logger = Logger.getLogger(OntologyIO.class.getName());

	public OntologyIO() {
	}

	public static void init() {
		// model = = FileManager.get().loadModel("instances-tool.rdf");
		csspModel = FileManager.get().loadModel(CSSP_FILE_NAME);
		rblpModel = FileManager.get().loadModel(RBLP_FILE_NAME);

		// This needs a different reader so it can be read (and written)
		// according to RDF Specifications
		model = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
		InputStream in = FileManager.get().open(INSTANCE_FILE_NAME);
		if (in == null) {
			throw new IllegalArgumentException("File: " + INSTANCE_FILE_NAME + " not found");
		}
		// read the RDF/XML file
		model.read(in, null);

		dsoModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
		InputStream in2 = FileManager.get().open(DSO_FILE_NAME);
		if (in2 == null) {
			throw new IllegalArgumentException("File: " + DSO_FILE_NAME + " not found");
		}
		// read the RDF/XML file
		dsoModel.read(in2, null);

		prpModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
		InputStream in3 = FileManager.get().open(PRP_FILE_NAME);
		if (in3 == null) {
			throw new IllegalArgumentException("File: " + PRP_FILE_NAME + " not found");
		}
		// read the RDF/XML file
		prpModel.read(in3, null);
	}

	/**
	 * Saves a timestamped copy of existing ontology files before we write
	 */
	public static void archive() {
		String archivePath = BASE_ARCHIVE_PATH + new SimpleDateFormat(ARCHIVE_FORMAT).format(new Date());
		Path dir = Paths.get(archivePath);
		logger.fine("Beginning archiving process");
		try {
			Files.createDirectory(dir);

			try {
				Files.copy(Paths.get(INSTANCE_FILE_NAME), Paths.get(archivePath + "/" + INSTANCE_FILE_NAME));
			} catch (IOException e) {
				logger.log(Level.WARNING, "Unable to archive " + INSTANCE_FILE_NAME, e);
			}
			try {
				// DSO path needs to be put in ontologies folder so we create it
				Files.createDirectory(Paths.get(archivePath + "/" + ONT_BASE_PATH));
				Files.copy(Paths.get(DSO_FILE_NAME), Paths.get(archivePath + "/" + DSO_FILE_NAME));
			} catch (IOException e) {
				logger.log(Level.WARNING, "Unable to archive " + DSO_FILE_NAME, e);
			}
			try {
				Files.copy(Paths.get(PRP_FILE_NAME), Paths.get(archivePath + "/" + PRP_FILE_NAME));
			} catch (IOException e) {
				logger.log(Level.WARNING, "Unable to archive " + PRP_FILE_NAME, e);
			}

		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to create archive directory. No backup of the ontology files can be kept",
					e);
		}
		logger.fine("Archiving complete");
	}

	public static void write() {

		archive();

		try {
			File file = new File(INSTANCE_FILE_NAME);
			DataOutputStream stream = new DataOutputStream(new FileOutputStream(file));
			model.write(stream);
			stream.close();
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, "File: " + INSTANCE_FILE_NAME + " not found", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try (DataOutputStream stream = new DataOutputStream(new FileOutputStream(new File(DSO_FILE_NAME)))) {
			dsoModel.write(stream);
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, "File: " + DSO_FILE_NAME + " not found", e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "File: " + DSO_FILE_NAME + " not found", e);
		}

		try (DataOutputStream stream = new DataOutputStream(new FileOutputStream(new File(PRP_FILE_NAME)))) {
			prpModel.write(stream);
		} catch (NullPointerException e) {
			logger.log(Level.SEVERE, "Filename for properties ontology file not found", e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "File: " + PRP_FILE_NAME + " not found", e);
		}
	}

	public static void close() {
		rblpModel.close();
		csspModel.close();
		model.close();
		dsoModel.close();
	}

	//
	/**
	 * Returns the sub-classes of a given class.
	 * 
	 * @param reqClass
	 *            The name of any ontology class.
	 * @return
	 * 
	 */
	public static ArrayList<OntologyWordModel> getSubclassesOfCategory(String reqClass) {

		ArrayList<OntologyWordModel> results = new ArrayList<>();
		if (reqClass.isEmpty())
			return results;

		reqClass += ". }";
		// Query string without the CSSP:Requirement.
		String queryStr = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { "
				+ "?x rdf:type owl:Class ." + "?x rdfs:subClassOf+ ";
		// The final query string after the addition of the abstraction level,
		// as this is given as input to the method.
		queryStr += reqClass;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.
		// Loaded from field

		// Creation of a query object.
		Query query = QueryFactory.create(queryStr);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, csspModel);

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();
				// Parsing of the string to get the class name only
				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				String namespace = "CSSP:";
				// req = namespace+req;
				// Addition of the strings (classes) to the array list.
				results.add(new OntologyWordModel(namespace, req));
			}
		} finally {
			qexec.close();
		}
		return results;
	}

	//
	/**
	 * Returns the top level sub-classes of a given class.
	 * 
	 * @param reqClass
	 *            The name of any ontology class.
	 * @return
	 * 
	 */
	public static ArrayList<OntologyWordModel> getSubclassesOfCategoryTopLevel(String reqClass) {

		ArrayList<OntologyWordModel> results = new ArrayList<>();
		if (reqClass.isEmpty())
			return results;

		reqClass += ". }";
		// Query string without the CSSP:Requirement.
		String queryStr = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { "
				+ "?x rdf:type owl:Class ." + "?x rdfs:subClassOf ";
		// The final query string after the addition of the requirement
		// category, as this is given as input to the method.
		queryStr += reqClass;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.
		// Loaded from field

		// Creation of a query object.
		Query query = QueryFactory.create(queryStr);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, csspModel);

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();
				// Parsing of the string to get the class name only
				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				String namespace = "CSSP:";
				// req = namespace+req;
				// Addition of the strings (classes) to the array list.
				results.add(new OntologyWordModel(namespace, req));
			}
		} finally {
			qexec.close();
		}
		return results;
	}

	/**
	 * Returns all category names for a given abstraction level
	 * 
	 * @param absLevel
	 *            The name of the abstraction level
	 * @return
	 * 
	 */
	public static ArrayList<OntologyWordModel> getCategoriesForAbstractionLevel(String absLevel) {

		ArrayList<OntologyWordModel> results = new ArrayList<>();
		if (absLevel.isEmpty())
			return results;

		absLevel += ". }";
		// Query string without the OoSSA:AbstractionLevel.
		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE {"
				+ "?x rdf:type owl:Class ." + "?x rdfs:subClassOf+ CSSP:Requirement ." + "?x rdfs:subClassOf ?R ."
				+ "?R a owl:Restriction ." + "?R owl:onProperty CSSP:hasAbstractionLevel ."
				+ "?R owl:allValuesFrom/(owl:unionOf/rdf:rest*/rdf:first)? ";

		// The final query string after the addition of the abstraction level,
		// as this is given as input to the method.
		queryString += absLevel;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.
		// Loaded from field

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, csspModel);

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();

				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				String namespace = "CSSP:";
				// req = namespace+req;

				// Addition of the strings (classes) to the array list.
				results.add(new OntologyWordModel(namespace, req));
			}

		} finally {
			qexec.close();
		}

		return results;
	}

	/**
	 * Returns the all the sub-classes of the "CSSP:AbstractionLevel" class.
	 * 
	 * @param absLevel
	 *            The name of an abstraction level.
	 * @return
	 * 
	 */
	public static ArrayList<OntologyWordModel> getCategoriesForAbstractionLevelTopLevel(String absLevel) {

		ArrayList<OntologyWordModel> results = new ArrayList<>();
		if (absLevel.isEmpty())
			return results;

		absLevel += ". }";
		// Query string without the OoSSA:AbstractionLevel.
		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE {"
				+ "?x rdf:type owl:Class ." + "?x rdfs:subClassOf CSSP:Requirement ." + "?x rdfs:subClassOf ?R ."
				+ "?R a owl:Restriction ." + "?R owl:onProperty CSSP:hasAbstractionLevel ."
				+ "?R owl:allValuesFrom/(owl:unionOf/rdf:rest*/rdf:first)? ";

		// The final query string after the addition of the abstraction level,
		// as this is given as input to the method.
		queryString += absLevel;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.
		// Loaded from field

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, csspModel);

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();

				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				String namespace = "CSSP:";
				// req = namespace+req;
				// Addition of the strings (classes) to the array list.
				results.add(new OntologyWordModel(namespace, req));
			}

		} finally {
			qexec.close();
		}

		return results;
	}

	//
	//
	/**
	 * Returns the all the top level sub-classes of the "CSSP:AbstractionLevel"
	 * class.
	 * 
	 * @param
	 * @return
	 * 
	 */
	public static ArrayList<OntologyWordModel> getAbstractionLevels() {

		ArrayList<OntologyWordModel> results = new ArrayList<>();

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { "
				+ "?x rdf:type owl:Class . " + "?x rdfs:subClassOf+ OoSSA:AbstractionLevel . }";

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.
		// Loaded from field

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, csspModel);

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();

				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				String namespace = "OoSSA:";
				// req = namespace+req;
				// System.out.println(req);
				// Addition of the strings (classes) to the array list.
				results.add(new OntologyWordModel(namespace, req));
			}

		} finally {
			qexec.close();
		}

		return results;
	}

	//
	/**
	 * Returns all requirement instances.
	 * 
	 * @param
	 * @return
	 * 
	 */
	public static ArrayList<OntologyWordModel> getRequirementInstances() {

		ArrayList<OntologyWordModel> results = new ArrayList<>();

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT DISTINCT  ?x " + "WHERE { "
				+ "?x rdf:type ?y ." + "?y rdfs:subClassOf* CSSP:Requirement . }" + "ORDER BY ?x ";
		// + "?y rdfs:subClassOf CSSP:Requirement }";

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.

		// The union of the 2 pre-defined models. In this way jena can see the
		// classes that are declared in RBLP.ttl and then imported to CSSP.ttl.
		Model model3 = model.union(csspModel);

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model3);

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();

				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				// System.out.println(req);
				String namespace = "CSSP:";
				// Addition of the strings (classes) to the array list.
				results.add(new OntologyWordModel(namespace, req));
			}

		} finally {
			qexec.close();
		}

		return results;
	}

	public static ArrayList<String> getBoilerplatesForRequirement(String requirement) {

		ArrayList<String> results = new ArrayList<>();
		if (requirement.isEmpty())
			return results;

		// Query string without the CSSP:Requirement.
		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { ";

		String queryString2 = " rdfs:subClassOf ?x . " + "?x a owl:Restriction . "
				+ "?x owl:onProperty CSSP:hasClause . " + "}";

		queryString += requirement;
		queryString += queryString2;

		System.out.println(queryString);

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.
		// Loaded from field

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, csspModel);

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {

				String req = result.nextSolution().toString();
				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				// req = req.substring(req.indexOf("#") + 1);

				// req = req.substring(0, req.indexOf("\""));

				// Addition of the strings (classes) to the array list.
				System.out.println(req);
				results.add(req);
			}

		} finally {
			qexec.close();
		}

		return results;
	}

	/**
	 * Returns the main boilerplates for a given requirement category.
	 * 
	 * @param categoryName
	 *            The name of a requirement category.
	 * @return
	 * 
	 */
	public static ArrayList<OntologyWordModel> getBodiesForCategory(String categoryName) {
		ArrayList<OntologyWordModel> results = new ArrayList<>();
		if (categoryName.isEmpty())
			return results;

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?y " + "WHERE { ";

		String queryString2 = " rdfs:subClassOf ?x . " + "?x a owl:Restriction . " + "?x owl:onProperty CSSP:hasMain . "
				+ "?y rdfs:subClassOf RBLP:Main . " + "?x owl:allValuesFrom/(owl:unionOf/rdf:rest*/rdf:first)? ?y"
				+ "}";

		queryString += categoryName;
		queryString += queryString2;

		// System.out.println(queryString);

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.
		// Loaded from field

		// The union of the 2 pre-defined models. In this way jena can see the
		// classes that are declared in RBLP.ttl and then imported to CSSP.ttl.
		Model model3 = csspModel.union(rblpModel);

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model3);

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {

				String req = result.nextSolution().toString();
				// Parsing of the string
				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				String namespace = "RBLP:";
				// Addition of the strings (classes) to the array list.
				results.add(new OntologyWordModel(namespace, req));
			}

		} finally {
			qexec.close();
		}

		return results;

	}

	/**
	 * Returns the prefix boilerplates for a given requirement category.
	 * 
	 * @param categoryName
	 *            The name of a requirement category.
	 * @return
	 * 
	 */
	public static ArrayList<OntologyWordModel> getPrefixesForCategory(String categoryName) {
		ArrayList<OntologyWordModel> results = new ArrayList<>();
		if (categoryName.isEmpty())
			return results;

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?y " + "WHERE { ";

		String queryString2 = " rdfs:subClassOf ?x . " + "?x a owl:Restriction . "
				+ "?x owl:onProperty CSSP:hasPrefix . " + "?y rdfs:subClassOf RBLP:SimplePrefix . "
				+ "?x owl:allValuesFrom/(owl:unionOf/rdf:rest*/rdf:first)?  ?y " + "}";

		queryString += categoryName;
		queryString += queryString2;

		// System.out.println(queryString);

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.
		// Loaded from field

		// The union of the 2 pre-defined models. In this way jena can see the
		// classes that are declared in RBLP.ttl and then imported to CSSP.ttl.
		Model model3 = csspModel.union(rblpModel);

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model3);

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {

				String req = result.nextSolution().toString();
				// Parsing of the string
				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				String namespace = "RBLP:";
				// Addition of the strings (classes) to the array list.
				results.add(new OntologyWordModel(namespace, req));
			}

		} finally {
			qexec.close();
		}

		return results;

	}

	/**
	 * Returns the suffix boilerplates for a given requirement category.
	 * 
	 * @param categoryName
	 *            The name of a requirement category.
	 * @return
	 * 
	 */
	public static ArrayList<OntologyWordModel> getSuffixesForCategory(String categoryName) {
		ArrayList<OntologyWordModel> results = new ArrayList<>();
		if (categoryName.isEmpty())
			return results;

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?y " + "WHERE { ";

		String queryString2 = " rdfs:subClassOf ?x . " + "?x a owl:Restriction . "
				+ "?x owl:onProperty CSSP:hasSuffix . " + "?y rdfs:subClassOf RBLP:SimpleSuffix . "
				+ "?x owl:allValuesFrom/(owl:unionOf/rdf:rest*/rdf:first)?  ?y " + "}";

		queryString += categoryName;
		queryString += queryString2;

		// System.out.println(queryString);

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.
		// Loaded from field

		// The union of the 2 pre-defined models. In this way jena can see the
		// classes that are declared in RBLP.ttl and then imported to CSSP.ttl.
		Model model3 = csspModel.union(rblpModel);

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model3);

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {

				String req = result.nextSolution().toString();
				// Parsing of the string
				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				String namespace = "RBLP:";
				// System.out.println(req);
				// Addition of the strings (classes) to the array list.
				results.add(new OntologyWordModel(namespace, req));
			}

		} finally {
			qexec.close();
		}

		return results;

	}

	/**
	 * Returns the prefixes for a Main boilerplate category.
	 * 
	 * @param blpID
	 *            The name of a main boilerplate category.
	 * @return
	 * 
	 */
	public static ArrayList<OntologyWordModel> getPrefixesForBoilerPlate(String blpID) {
		ArrayList<OntologyWordModel> results = new ArrayList<>();
		if (blpID.isEmpty())
			return results;

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?y " + "WHERE { ";

		String queryString2 = " rdfs:subClassOf ?x . " + "?x a owl:Restriction . "
				+ "?x owl:onProperty RBLP:isRelatedToPrefixClause . " + "?y rdfs:subClassOf RBLP:SimplePrefix . "
				+ "?x  owl:allValuesFrom/(owl:unionOf/rdf:rest*/rdf:first)? ?y  " + "}";

		queryString += blpID;
		queryString += queryString2;

		// System.out.println(queryString);

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, rblpModel);

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {

				String req = result.nextSolution().toString();
				// Parsing of the string
				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				String namespace = "RBLP:";
				// System.out.println(req);
				// Addition of the strings (classes) to the array list.
				results.add(new OntologyWordModel(namespace, req));
			}

		} finally {
			qexec.close();
		}

		return results;
	}

	/**
	 * Returns the suffixes for a Main boilerplate category.
	 * 
	 * @param blpID
	 *            The name of a main boilerplate category.
	 * @return
	 * 
	 */
	public static ArrayList<OntologyWordModel> getSuffixesForBoilerPlate(String blpID) {
		ArrayList<OntologyWordModel> results = new ArrayList<>();
		if (blpID.isEmpty())
			return results;

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?y " + "WHERE { ";

		String queryString2 = " rdfs:subClassOf ?x . " + "?x a owl:Restriction . "
				+ "?x owl:onProperty RBLP:isRelatedToSuffixClause . " + "?y rdfs:subClassOf RBLP:SimpleSuffix . "
				+ "?x  owl:allValuesFrom/(owl:unionOf/rdf:rest*/rdf:first)? ?y  " + "}";

		queryString += blpID;
		queryString += queryString2;

		// System.out.println(queryString);

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, rblpModel);

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {

				String req = result.nextSolution().toString();
				// Parsing of the string
				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				String namespace = "RBLP:";
				// System.out.println(req);
				// Addition of the strings (classes) to the array list.
				results.add(new OntologyWordModel(namespace, req));
			}

		} finally {
			qexec.close();
		}

		return results;
	}

	// Returns the description of a boilerplate category.
	/**
	 * Returns the description of a boilerplate category.
	 * 
	 * @param blpID
	 *            The name of a boilerplate category.
	 * @return
	 * 
	 */
	public static String getDescriptionForBoilerPlate(String blpID) {
		if (blpID.isEmpty())
			return "";

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?y " + "WHERE { ";

		String queryString2 = " rdfs:comment ?y }";

		queryString += blpID;
		queryString += queryString2;

		// System.out.println(queryString);

		// System.out.println(queryString);

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, rblpModel);
		String des = null;

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {

				des = result.nextSolution().toString();
				// Parsing of the string
				des = des.substring(des.indexOf("=") + 1);
				des = des.substring(0, des.indexOf(")"));
				// String namespace = "RBLP:";
				// Addition of the strings (classes) to the array list.
				// results.add(new OntologyWordModel(namespace, des)); // To
				// return ArrayList of Strings!!
			}

		} finally {
			qexec.close();
		}

		return des;
	}

	/**
	 * Returns the description of a requirement category.
	 * 
	 * @param ptrnID
	 *            The name of a boilerplate category.
	 * @return
	 * 
	 */
	public static String getDescriptionForRequirementCategory(String reqName) {
		if (reqName.isEmpty())
			return "";

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?y " + "WHERE { ";

		String queryString2 = " rdfs:comment ?y }";

		queryString += reqName;
		queryString += queryString2;

		// System.out.println(queryString);

		// System.out.println(queryString);

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.
		// Loaded from field

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, csspModel);
		String des = null;

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {

				des = result.nextSolution().toString();
				// Parsing of the string
				des = des.substring(des.indexOf("=") + 1);
				des = des.substring(0, des.indexOf(")"));
				// String namespace = "RBLP:";
				// Addition of the strings (classes) to the array list.
				// results.add(new OntologyWordModel(namespace, des));
			}

		} finally {
			qexec.close();
		}

		return des;
	}

	/**
	 * Returns the super-classes of a requirement category.
	 * 
	 * @param ptrnID
	 *            The name of a boilerplate category.
	 * @return
	 * 
	 */
	public static ArrayList<OntologyWordModel> getSuperClassOfRequirementCategory(String reqName) {
		ArrayList<OntologyWordModel> results = new ArrayList<>();
		if (reqName.isEmpty())
			return results;

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { "
				+ "?x rdf:type owl:Class . ";

		String queryString2 = " rdfs:subClassOf ?x . }";

		queryString += reqName;
		queryString += queryString2;

		// System.out.println(queryString);

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.
		// Loaded from field

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, csspModel);

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {

				String req = result.nextSolution().toString();
				// Parsing of the string
				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				String namespace = "CSSP:";
				// System.out.println(req);
				// Addition of the strings (classes) to the array list.
				results.add(new OntologyWordModel(namespace, req));
			}

		} finally {
			qexec.close();
		}

		return results;
	}

	/**
	 * Returns the instances of a class.
	 * 
	 * @param ptrnID
	 *            The name of a boilerplate category.
	 * @return
	 * 
	 */
	public static ArrayList<OntologyWordModel> getInstancesFromClassAndSubclasses(String className) {
		ArrayList<OntologyWordModel> results = new ArrayList<>();
		if (className.isEmpty())
			return results;

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix uomvocab: <http://purl.oclc.org/net/muo/muo#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { " + "?x rdf:type ?y ."
				+ "?y rdfs:subClassOf* ";

		String queryString2 = " . }";

		queryString += className;
		queryString += queryString2;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		try {

			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();
				req = req.replaceFirst(".*\\/", "").replaceFirst("> \\)", "").replaceFirst("#", ":")
						.replaceFirst(".ttl:", ":");
				// System.out.println(req);
				results.add(new OntologyWordModel(req));
			}

		} finally {
			qexec.close();
		}

		return results;
	}

	public static ArrayList<OntologyWordModel> getMainForRequirement(String reqID) {
		ArrayList<OntologyWordModel> results = new ArrayList<>();
		if (reqID.isEmpty())
			return results;

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP.ttl#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { ";
		queryString += reqID;
		String queryString2 = " CSSP:hasMain ?x . }";
		queryString += queryString2;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.

		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		try {

			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();
				// Parsing of the string
				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				String namespace = "RBLP:";
				// System.out.println("main" + req);
				// Addition of the strings (classes) to the array list.
				results.add(new OntologyWordModel(namespace, req));
				// System.out.println(req);
			}

		} finally {
			qexec.close();
		}
		return results;
	}

	public static ArrayList<OntologyWordModel> getPrefixForRequirement(String reqID) {
		ArrayList<OntologyWordModel> results = new ArrayList<>();
		if (reqID.isEmpty())
			return results;

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP.ttl#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { ";
		queryString += reqID;
		String queryString2 = " CSSP:hasPrefix ?x . }";
		queryString += queryString2;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.

		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		try {

			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();
				// Parsing of the string
				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				String namespace = "RBLP:";
				// System.out.println("Prefix:"+ req);
				// Addition of the strings (classes) to the array list.
				results.add(new OntologyWordModel(namespace, req));
			}

		} finally {
			qexec.close();
		}
		return results;
	}

	public static ArrayList<OntologyWordModel> getSuffixForRequirement(String reqID) {
		ArrayList<OntologyWordModel> results = new ArrayList<>();
		if (reqID.isEmpty())
			return results;

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP.ttl#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { ";
		queryString += reqID;
		String queryString2 = " CSSP:hasSuffix ?x . }";
		queryString += queryString2;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.

		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		try {

			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();
				// Parsing of the string
				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				String namespace = "RBLP:";
				// System.out.println("Suffix:" + req);
				// Addition of the strings (classes) to the array list.
				results.add(new OntologyWordModel(namespace, req));
			}

		} finally {
			qexec.close();
		}
		return results;
	}

	public static OntologyWordModel getRequirementType(String reqID) {

		OntologyWordModel type = new OntologyWordModel("CSSP", "Requirement");

		if (reqID.isEmpty())
			return type;

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP.ttl#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { ";
		queryString += reqID;
		String queryString2 = " rdf:type ?x . }";
		queryString += queryString2;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.

		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		try {

			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			if (result.hasNext()) {
				String req = result.nextSolution().toString();
				// Parsing of the string
				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				// System.out.println("Req id: "+ reqID+ "req type "+ req);
				String namespace = "CSSP:";
				// System.out.println(req);
				// Addition of the strings (classes) to the array list.
				type = new OntologyWordModel(namespace, req);
			}

		} finally {
			qexec.close();
		}
		return type;
	}

	public static OntologyWordModel getRequirementAbstractionLevel(String reqID) {
		OntologyWordModel abs = new OntologyWordModel("");
		if (reqID.isEmpty())
			return abs;

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP.ttl#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { ";
		queryString += reqID;
		String queryString2 = " CSSP:hasAbstractionLevel ?x . }";
		queryString += queryString2;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.

		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		try {

			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();
				// Parsing of the string
				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				// abs = "CSSP:"+req;
				String namespace = "OoSSA:";
				// System.out.println(req);
				// Addition of the strings (classes) to the array list.
				abs = new OntologyWordModel(namespace, req);
			}

		} finally {
			qexec.close();
		}
		return abs;
	}

	public static String getRequirementStatus(String reqID) {

		if (reqID.isEmpty())
			return null;
		String Status = null;

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP.ttl#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { ";
		queryString += reqID;
		String queryString2 = " CSSP:hasStatus ?x . }";
		queryString += queryString2;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.

		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		try {

			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();
				// Parsing of the string
				req = req.replace("( ?x = xsd:string", "");
				req = req.substring(0, req.lastIndexOf(")")).trim();
				String namespace = "";

				Status = req.replace("_", " ");
				// Addition of the strings (classes) to the array list.

			}

		} finally {
			qexec.close();
		}
		return Status;
	}

	public static ArrayList<String> getRefinesForRequirement(String reqID) {
		ArrayList<String> results = new ArrayList<String>();
		if (reqID == null || reqID.isEmpty())
			return results;

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP.ttl#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { ";
		queryString += reqID;
		String queryString2 = " CSSP:refines ?x . }";
		queryString += queryString2;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.

		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		try {

			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();
				// Parsing of the string
				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				String namespace = "CSSP:";
				// Addition of the strings (classes) to the array list.
				// results.add(new OntologyWordModel(namespace, req));
				results.add(namespace + req);
			}

		} finally {
			qexec.close();
		}
		return results;
	}

	public static ArrayList<String> getisRefinedByForRequirement(String reqID) {
		ArrayList<String> results = new ArrayList<String>();
		if (reqID == null || reqID.isEmpty())
			return results;

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP.ttl#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { ";
		queryString += reqID;
		String queryString2 = " CSSP:isRefinedBy ?x . }";
		queryString += queryString2;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.

		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();
				// Parsing of the string
				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				String namespace = "CSSP:";
				// System.out.println(req);
				// Addition of the strings (classes) to the array list.
				// results.add(new OntologyWordModel(namespace, req));
				results.add(namespace + req);
			}
		} finally {
			qexec.close();
		}
		return results;
	}

	public static ArrayList<String> getConcretizesForRequirement(String reqID) {
		ArrayList<String> results = new ArrayList<String>();
		if (reqID == null || reqID.isEmpty())
			return results;

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP.ttl#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { ";
		queryString += reqID;
		String queryString2 = " CSSP:concretizes ?x . }";
		queryString += queryString2;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.

		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();
				// Parsing of the string
				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				String namespace = "CSSP:";
				// System.out.println(req);
				// Addition of the strings (classes) to the array list.
				// results.add(new OntologyWordModel(namespace, req));
				results.add(namespace + req);
			}
		} finally {
			qexec.close();
		}
		return results;
	}

	public static ArrayList<String> getisConcretizedByForRequirement(String reqID) {
		ArrayList<String> results = new ArrayList<String>();
		if (reqID == null || reqID.isEmpty())
			return results;

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP.ttl#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { ";
		queryString += reqID;
		String queryString2 = " CSSP:isConcretizedBy ?x . }";
		queryString += queryString2;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.

		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();
				// Parsing of the string
				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				String namespace = "CSSP:";
				// System.out.println(req);
				// Addition of the strings (classes) to the array list.
				// results.add(new OntologyWordModel(namespace, req));
				results.add(namespace + req);
			}
		} finally {
			qexec.close();
		}
		return results;
	}

	public static String getSerializedFor(String objectID) {
		// ArrayList<OntologyWordModel> results = new ArrayList<>();
		if (objectID.isEmpty())
			return null;
		String serialized = null;
		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { ";
		queryString += objectID;
		String queryString2 = " rdfs:comment ?x . }";
		queryString += queryString2;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.

		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		try {

			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();
				// Parsing of the string
				req = req.substring(req.indexOf("\"") + 1);
				req = req.substring(0, req.indexOf("\""));
				String namespace = "";
				// Addition of the strings (classes) to the array list.
				// results.add(new OntologyWordModel(namespace, req));
				serialized = req;
			}

		} finally {
			qexec.close();
		}
		return serialized;
	}

	/**
	 * Returns a list of all classes that support user instance creation
	 * 
	 * @return ArrayList with all the classes the user can add items to
	 */
	public static ArrayList<OntologyWordModel> getUserInstanceEnabledClasses() {

		// TODO (eRigas) Placeholder
		ArrayList<OntologyWordModel> classList = new ArrayList<>();
		classList.add(new OntologyWordModel("OoSSA:SystemEntity"));
		classList.add(new OntologyWordModel("OoSSA:SystemAttribute"));
		classList.add(new OntologyWordModel("OoSSA:SystemAction"));
		return classList;
	}

	public static void addRequirementToOntology(ArrayList<String> prefixes, String mainName, ArrayList<String> suffixes,
			String reqName, String reqCategory, String absLevel, String Status,
			ObservableList<RequirementModel> refines, ObservableList<RequirementModel> isRefinedBy,
			ObservableList<RequirementModel> concretizes, ObservableList<RequirementModel> isConcretizedBy,
			ArrayList<String> abstracts) throws IOException {

		boolean isAbstract = false;
		if (abstracts != null && !abstracts.isEmpty()) {
			isAbstract = true;
		}

		// Creation of the model of the cssp ontology.

		String reqURI = "http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#";
		String ns = "/rdf";
		// System.out.println("REQ NAME = " + reqName+ "main Name = "+ mainName
		// + "suffix name = "+ suffixName);

		if (reqName != null) {
			// Requirement name.
			Resource Requirement = model.createResource(reqURI + reqName);
			// Addition of requirement prefix boilerplate.
			for (String prefixName : prefixes) {
				if (!prefixName.contains(" ") && !prefixName.isEmpty()) {
					Property p1 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#" + "hasPrefix");
					Resource X1 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + prefixName);
					model.add(Requirement, p1, X1);
				}
			}

			// Addition of requirement main boilerplate.
			if (!mainName.contains(" ") && !mainName.isEmpty()) {
				Property p2 = model.createProperty("http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#" + "hasMain");
				Resource X2 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + mainName);
				model.add(Requirement, p2, X2);
			}
			// Addition of requirement suffix boilerplate.
			for (String suffixName : suffixes) {
				if (!suffixName.contains(" ") && !suffixName.isEmpty()) {
					Property p = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#" + "hasSuffix");
					Resource X = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + suffixName);
					model.add(Requirement, p, X);
				}
			}

			// Addition of requirement type.
			if (reqCategory != null) {
				// Property p3 = model.createProperty(
				// "http://lpis.csd.auth.gr/ontologies/2015/rdf#" + "type");
				Resource X3 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#" + reqCategory);
				model.add(Requirement, RDF.type, X3);
			}
			if (isAbstract == true) {
				Resource X1 = model
						.createResource("http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#" + "AbstractRequirement");
				model.add(Requirement, RDF.type, X1);
			}
			if (absLevel != null) {
				Property p5 = model
						.createProperty("http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#" + "hasAbstractionLevel");
				Resource X5 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#" + absLevel);
				model.add(Requirement, p5, X5);
			} else {
				Resource X4 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#" + "Requirement");
				model.add(Requirement, RDF.type, X4);
			}

			Property P6 = model.createProperty("http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#" + "hasStatus");
			Resource X6 = model.createResource("http://www.w3.org/2001/XMLSchema#string" + Status.replace(" ", "_"));
			model.add(Requirement, P6, X6);

			if (!refines.isEmpty()) {
				refines.forEach((reqMdl) -> {
					Property P7 = model.createProperty("http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#" + "refines");
					Resource X7 = model.createResource(
							"http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#" + reqMdl.getReqIDNoNS());
					model.add(Requirement, P7, X7);
				});
			}
			if (!isRefinedBy.isEmpty()) {
				isRefinedBy.forEach((reqMdl) -> {
					Property P7 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#" + "isRefinedBy");
					Resource X7 = model.createResource(
							"http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#" + reqMdl.getReqIDNoNS());
					model.add(Requirement, P7, X7);
				});
			}
			if (!concretizes.isEmpty()) {
				Resource X3 = model
						.createResource("http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#" + "ConcreteRequirement");
				model.add(Requirement, RDF.type, X3);
				concretizes.forEach((reqMdl) -> {
					Property P7 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#" + "concretizes");
					Resource X7 = model.createResource(
							"http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#" + reqMdl.getReqIDNoNS());
					model.add(Requirement, P7, X7);
				});
			}
			if (!isConcretizedBy.isEmpty()) {
				Resource X3 = model
						.createResource("http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#" + "AbstractRequirement");
				model.add(Requirement, RDF.type, X3);
				isConcretizedBy.forEach((reqMdl) -> {
					Property P7 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#" + "isConcretizedBy");
					Resource X7 = model.createResource(
							"http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#" + reqMdl.getReqIDNoNS());
					model.add(Requirement, P7, X7);
				});
			}
		} else {
			System.out.println("Insufficient information to create the requirement");
		}

		// model.add(Requirement20, p, X).add(Requirement20, p2,
		// X2).add(Requirement20, RDF.type, X3);
		// write it to standard out
		// write it to standard out

	}

	public static void removeRequirementFromOntology(String reqID) throws IOException {
		String reqURI = "http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#";

		// model.setNsPrefix("rdf", "http://rdf#");
		// Property P = model.createProperty(
		// "http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl" + "Requirement" );

		ArrayList<OntologyWordModel> prefix = new ArrayList<OntologyWordModel>();
		prefix = getPrefixForRequirement("CSSP:" + reqID);
		for (int i = 0; i < prefix.size(); i++) {
			removeBoilerplateFromOntology(prefix.get(i).getShortName());
		}
		ArrayList<OntologyWordModel> main = new ArrayList<OntologyWordModel>();
		main = getMainForRequirement("CSSP:" + reqID);
		for (int i = 0; i < main.size(); i++) {
			removeBoilerplateFromOntology(main.get(i).getShortName());
		}
		ArrayList<OntologyWordModel> suffix = new ArrayList<OntologyWordModel>();
		suffix = getSuffixForRequirement("CSSP:" + reqID);
		for (int i = 0; i < suffix.size(); i++) {
			removeBoilerplateFromOntology(suffix.get(i).getShortName());
		}

		Resource Requirement = model.createResource(reqURI + reqID);// .addProperty(RDF.type,
																	// "http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#Requirement");
		Requirement.removeProperties();
	}

	public static void removeBoilerplateFromOntology(String blpName) throws IOException {

		String reqURI = "http://lpis.csd.auth.gr/ontologies/2015/RBLP#";
		Resource Boilerplate = model.createResource(reqURI + blpName);
		Boilerplate.removeProperties();

		// write it to standard out
		// model.write(System.out);

	}

	// public static void updateRequirement(String prefixName, String mainName,
	// String suffixName, String reqName,
	// String reqCategory, String absLevel, String Status,
	// ObservableList<RequirementModel> refines,
	// ObservableList<RequirementModel> isRefinedBy,
	// ObservableList<RequirementModel> concretizes,
	// ObservableList<RequirementModel> isConcretizedBy) throws IOException {
	//
	// reqName = "CSSP:" + reqName;
	// removeRequirementFromOntology(reqName);
	//
	// addRequirementToOntology(prefixName, mainName, suffixName, reqName,
	// reqCategory, absLevel, Status, refines,
	// isRefinedBy, concretizes, isConcretizedBy);
	//
	// }

	public static void addClassInstanceToOntology(HashMap<OntologyWordModel, ArrayList<String>> contentMap) throws FileNotFoundException {

		String reqURI = "http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#";
		
		for (OntologyWordModel key : contentMap.keySet()) {
			// System.out.println("full name main:"+key.getFullName());
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("Subject")) {
				for (int i = 0; i < list.size(); i++) {
					Resource instance1 = dsoModel.createResource(reqURI + list.get(i));
					Resource instance2 = model.createResource(reqURI + list.get(i));
					
					Resource P1 = dsoModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl" + "SystemEntity");
					dsoModel.add(instance1, RDF.type, P1);
					
					Resource P2 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl" + "SystemEntity");
					model.add(instance2, RDF.type, P2);
				}
			}
			else {
				for (int i = 0; i < list.size(); i++) {
					Resource instance1 = dsoModel.createResource(reqURI + list.get(i));
					Resource instance2 = model.createResource(reqURI + list.get(i));
					
					Resource P1 = dsoModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl" + "SystemAttribute");
					dsoModel.add(instance1, RDF.type, P1);
					
					Resource P2 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl" + "SystemAttribute");
					model.add(instance2, RDF.type, P2);
				}
			}
		}
		
		
	}

	public static void addMainToOntology(String blpName, String blpCategory,

			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm,
			ArrayList<String> abstracts) throws IOException {

		String reqURI = "http://lpis.csd.auth.gr/ontologies/2015/RBLP#";

		boolean isAbstract = false;
		if (abstracts != null && !abstracts.isEmpty()) {
			isAbstract = true;
		}

		if ((blpCategory.contains("M1") || blpCategory.contains("M6") || blpCategory.contains("M7"))
				&& (!blpCategory.contains("10") || !blpCategory.contains("11") || !blpCategory.contains("12"))) { // Subject
			// +
			// Action
			Resource Main = model.createResource(reqURI + blpName);
			if (isAbstract == true) {
				for (int i = 0; i < abstracts.size(); i++) {
					Property p2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToSubject");
					Resource X2 = model
							.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + abstracts.get(i));
					model.add(Main, p2, X2);
				}
			}
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println("full name main:"+key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("Subject") && isAbstract != true) {

					for (int i = 0; i < list.size(); i++) {
						Property p2 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToSubject");
						Resource X2 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
						model.add(Main, p2, X2);

					}

				} else if (key.getFullName().contains("Action")) {
					for (int i = 0; i < list.size(); i++) {
						// System.out.println("In action" + i);
						Property p = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToAction");
						Resource X = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
						model.add(Main, p, X);
					}
				}
			}
			if (blpCategory.contains("M1")) {
				blpCategory = "M1";
			} else if (blpCategory.contains("M6")) {
				blpCategory = "M6";
			} else if (blpCategory.contains("M7")) {
				blpCategory = "M7";
			}
			Resource X3 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + blpCategory);
			model.add(Main, RDF.type, X3).add(Main, RDFS.label, blpName).add(Main, RDFS.comment, serializedForm);
		} else if ((blpCategory.contains("M2")) && (!blpCategory.contains("M12"))) { // Subject
																						// +
																						// state

			Resource Main = model.createResource(reqURI + blpName);

			if (isAbstract == true) {
				for (int i = 0; i < abstracts.size(); i++) {
					Property p2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToSubject");
					Resource X2 = model
							.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + abstracts.get(i));
					model.add(Main, p2, X2);
				}
			}
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println("Full name:"+key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("Subject") && isAbstract != true) {
					for (int i = 0; i < list.size(); i++) {
						Property p2 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToSubject");
						Resource X2 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + list.get(i));
						model.add(Main, p2, X2);
					}
				} else if (key.getFullName().contains("State")) {
					for (int i = 0; i < list.size(); i++) {
						Property p = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToState");
						Resource X = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
						model.add(Main, p, X);
					}
				}
			}
			if (blpCategory.contains("M2")) {
				blpCategory = "M2";
			}
			Resource X3 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + blpCategory);
			model.add(Main, RDF.type, X3).add(Main, RDFS.label, blpName).add(Main, RDFS.comment, serializedForm);
		}

		else if (blpCategory.contains("M5") || blpCategory.contains("M9") || blpCategory.contains("M10")) { // subject
			// +
			// entity
			Resource Main = model.createResource(reqURI + blpName);

			if (isAbstract == true) {
				for (int i = 0; i < abstracts.size(); i++) {
					Property p2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToSubject");
					Resource X2 = model
							.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + abstracts.get(i));
					model.add(Main, p2, X2);
				}
			}
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("Subject") && isAbstract != true) {
					for (int i = 0; i < list.size(); i++) {
						Property p2 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToSubject");
						Resource X2 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + list.get(i));
						model.add(Main, p2, X2);
					}
				} else if (key.getFullName().contains("Entity")) {
					for (int i = 0; i < list.size(); i++) {
						Property p = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToEntity");
						Resource X = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
						model.add(Main, p, X);
					}
				}
			}
			if (blpCategory.contains("M5")) {
				blpCategory = "M5";
			} else if (blpCategory.contains("M9")) {
				blpCategory = "M9";
			} else if (blpCategory.contains("M10")) {
				blpCategory = "M10";
			}
			Resource X3 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + blpCategory);
			model.add(Main, RDF.type, X3).add(Main, RDFS.label, blpName).add(Main, RDFS.comment, serializedForm);
		} else if (blpCategory.contains("M4")) { // Subject + entity + state
			Resource Main = model.createResource(reqURI + blpName);
			if (isAbstract == true) {
				for (int i = 0; i < abstracts.size(); i++) {
					Property p2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToSubject");
					Resource X2 = model
							.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + abstracts.get(i));
					model.add(Main, p2, X2);
				}
			}
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("Subject") && isAbstract != true) {
					for (int i = 0; i < list.size(); i++) {
						Property p2 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToSubject");
						Resource X2 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + list.get(i));
						model.add(Main, p2, X2);
					}
				} else if (key.getFullName().contains("Entity")) {
					for (int i = 0; i < list.size(); i++) {
						Property p = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToEntity");
						Resource X = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
						model.add(Main, p, X);
					}
				} else if (key.getFullName().contains("State")) {
					for (int i = 0; i < list.size(); i++) {
						Property p4 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToState");
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
						model.add(Main, p4, X4);
					}
				}
			}
			if (blpCategory.contains("M4")) {
				blpCategory = "M4";
			}
			Resource X3 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + blpCategory);
			model.add(Main, RDF.type, X3).add(Main, RDFS.label, blpName).add(Main, RDFS.comment, serializedForm);
		} else if (blpCategory.contains("M3")) { // subject + entity + quantity
													// + state
			Resource Main = model.createResource(reqURI + blpName);
			if (isAbstract == true) {
				for (int i = 0; i < abstracts.size(); i++) {
					Property p2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToSubject");
					Resource X2 = model
							.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + abstracts.get(i));
					model.add(Main, p2, X2);
				}
			}
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("Subject") && isAbstract != true) {
					for (int i = 0; i < list.size(); i++) {
						Property p2 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToSubject");
						Resource X2 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + list.get(i));
						model.add(Main, p2, X2);
					}
				} else if (key.getFullName().contains("Entity")) {
					for (int i = 0; i < list.size(); i++) {
						Property p = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToEntity");
						Resource X = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
						model.add(Main, p, X);
					}
				} else if (key.getFullName().contains("quantity")) {
					for (int i = 0; i < list.size(); i++) {
						Property p5 = model.createProperty(
								"http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToQuantity");
						Resource X5 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
						model.add(Main, p5, X5);
					}
				} else if (key.getFullName().contains("State")) {
					for (int i = 0; i < list.size(); i++) {
						Property p4 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToState");
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
						model.add(Main, p4, X4);
					}
				}
			}
			if (blpCategory.contains("M3")) {
				blpCategory = "M3";
			}
			Resource X3 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + blpCategory);
			model.add(Main, RDF.type, X3).add(Main, RDFS.label, blpName).add(Main, RDFS.comment, serializedForm);
		} else if (blpCategory.contains("M11")) { // Entity + Entity
			Resource Main = model.createResource(reqURI + blpName);
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("Entity")) {
					for (int i = 0; i < list.size(); i++) {
						Property p = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToEntity");
						Resource X = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
						model.add(Main, p, X);
					}
				}
			}
			if (blpCategory.contains("M11")) {
				blpCategory = "M11";
			}
			Resource X3 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + blpCategory);
			model.add(Main, RDF.type, X3).add(Main, RDFS.label, blpName).add(Main, RDFS.comment, serializedForm);
		} else if (blpCategory.contains("M12")) { // Entity + Entity + Action
			Resource Main = model.createResource(reqURI + blpName);
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("Entity")) {
					for (int i = 0; i < list.size(); i++) {
						Property p = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToEntity");
						Resource X = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
						model.add(Main, p, X);
					}
				} else if (key.getFullName().contains("Action")) {
					for (int i = 0; i < list.size(); i++) {
						Property p5 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToAction");
						Resource X5 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
						model.add(Main, p5, X5);
					}
				}
			}
			if (blpCategory.contains("M12")) {
				blpCategory = "M12";
			}
			Resource X3 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + blpCategory);
			model.add(Main, RDF.type, X3).add(Main, RDFS.label, blpName).add(Main, RDFS.comment, serializedForm);
		} else if (blpCategory.contains("M8")) {
			Resource Main = model.createResource(reqURI + blpName);
			if (isAbstract == true) {
				for (int i = 0; i < abstracts.size(); i++) {
					Property p2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToSubject");
					Resource X2 = model
							.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + abstracts.get(i));
					model.add(Main, p2, X2);
				}
			}
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("Subject") && isAbstract != true) {
					for (int i = 0; i < list.size(); i++) {
						Property p2 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToSubject");
						Resource X2 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + list.get(i));
						model.add(Main, p2, X2);
					}
				} else if (key.getFullName().contains("Action-block")) {
					for (int i = 0; i < list.size(); i++) {
						Property p = model.createProperty(
								"http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToActionBlock");
						Resource X = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
						model.add(Main, p, X);
					}
				}
			}
			if (blpCategory.contains("M8")) {
				blpCategory = "M8";
			}

			Resource X3 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + blpCategory);
			model.add(Main, RDF.type, X3).add(Main, RDFS.label, blpName).add(Main, RDFS.comment, serializedForm);
		} else {
			System.out.println("No such main boilerplate exists");
		}

		// write it to standard out
		// model.write(System.out);

	}

	public static void addPrefixToOntology(String blpName, String blpCategory,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm) throws IOException {

		String reqURI = "http://lpis.csd.auth.gr/ontologies/2015/RBLP#";

		if (blpCategory.contains("P1")) { // Event
			Resource Prefix = model.createResource(reqURI + blpName);
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("Event")) {
					for (int i = 0; i < list.size(); i++) {
						Property p2 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToEvent");
						Resource X2 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
						model.add(Prefix, p2, X2);
					}
				}
			}
			blpCategory = "P1";
			Resource X3 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + blpCategory);
			model.add(Prefix, RDF.type, X3).add(Prefix, RDFS.label, blpName).add(Prefix, RDFS.comment, serializedForm);
		} else if (blpCategory.contains("P2")) { // State
			Resource Prefix = model.createResource(reqURI + blpName);
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("State")) {
					for (int i = 0; i < list.size(); i++) {
						Property p2 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedtoState");
						Resource X2 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
						model.add(Prefix, p2, X2);
					}
				}
			}
			blpCategory = "P2";
			Resource X3 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + blpCategory);
			model.add(Prefix, RDF.type, X3).add(Prefix, RDFS.label, blpName).add(Prefix, RDFS.comment, serializedForm);
		} else if (blpCategory.contains("P3")) { // Action
			Resource Prefix = model.createResource(reqURI + blpName);
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("Action")) {
					for (int i = 0; i < list.size(); i++) {
						Property p2 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedtoAction");
						Resource X2 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
						model.add(Prefix, p2, X2);
					}
				}
			}
			blpCategory = "P3";
			Resource X3 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + blpCategory);
			model.add(Prefix, RDF.type, X3).add(Prefix, RDFS.label, blpName).add(Prefix, RDFS.comment, serializedForm);
		} else {
			System.out.println("No such prefix boilerplate exists : " + blpCategory);
		}

		// write it to standard out
		// model.write(System.out);

	}

	public static void addSuffixToOntology(String blpName, String blpCategory,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm) throws IOException {

		String reqURI = "http://lpis.csd.auth.gr/ontologies/2015/RBLP#";

		if (blpCategory.contains("S1")) { // Quantity + UOM
			Resource Suffix = model.createResource(reqURI + blpName);
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("quantity")) {
					for (int i = 0; i < list.size(); i++) {
						Property p2 = model.createProperty(
								"http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToQuantity");
						Resource X2 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + list.get(i));
						model.add(Suffix, p2, X2);
					}
				} else {
					for (int i = 0; i < list.size(); i++) {
						Property p2 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToUOM");
						Resource X2 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
						model.add(Suffix, p2, X2);
					}
				}
			}
			Resource X3 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "S1");
			model.add(Suffix, RDF.type, X3).add(Suffix, RDFS.label, blpName).add(Suffix, RDFS.comment, serializedForm);
		} else if (blpCategory.contains("S2")) { // Event
			Resource Suffix = model.createResource(reqURI + blpName);
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("event")) {
					for (int i = 0; i < list.size(); i++) {
						Property p2 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToEvent");
						Resource X2 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + list.get(i));
						model.add(Suffix, p2, X2);
					}
				}
			}
			Resource X3 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "S2");
			model.add(Suffix, RDF.type, X3).add(Suffix, RDFS.label, blpName).add(Suffix, RDFS.comment, serializedForm);
		} else if (blpCategory.contains("S3")) { // Quantity + UOM + Event
			Resource Suffix = model.createResource(reqURI + blpName);
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("Quantity")) {
					for (int i = 0; i < list.size(); i++) {
						Property p2 = model.createProperty(
								"http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToQuantity");
						Resource X2 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + list.get(i));
						model.add(Suffix, p2, X2);
					}
				} else if (key.getFullName().contains("Unit")) {
					for (int i = 0; i < list.size(); i++) {
						Property p2 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToUOM");
						Resource X2 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
						model.add(Suffix, p2, X2);
					}
				} else {
					for (int i = 0; i < list.size(); i++) {
						Property p4 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToEvent");
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
						model.add(Suffix, p4, X4);
					}
				}
			}
			Resource X3 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "S3");
			model.add(Suffix, RDF.type, X3).add(Suffix, RDFS.label, blpName).add(Suffix, RDFS.comment, serializedForm);
		} else if (blpCategory.contains("S4") || blpCategory.contains("S5")) { // Action
																				// +
																				// Entity
			Resource Suffix = model.createResource(reqURI + blpName);
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("action")) {
					for (int i = 0; i < list.size(); i++) {
						Property p2 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToAction");
						Resource X2 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + list.get(i));
						model.add(Suffix, p2, X2);
					}
				} else {
					for (int i = 0; i < list.size(); i++) {
						Property p2 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToEntity");
						Resource X2 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
						model.add(Suffix, p2, X2);
					}
				}
			}
			if (blpCategory.contains("S4")) {
				blpCategory = "S4";
			} else if (blpCategory.contains("S5")) {
				blpCategory = "S5";
			}
			Resource X3 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + blpCategory);
			model.add(Suffix, RDF.type, X3).add(Suffix, RDFS.label, blpName).add(Suffix, RDFS.comment, serializedForm);
		} else if (blpCategory.contains("S6")) { // Entity + EntityList
			Resource Suffix = model.createResource(reqURI + blpName);
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("entitylist")) {
					for (int i = 0; i < list.size(); i++) {
						Property p2 = model.createProperty(
								"http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToEntityList");
						Resource X2 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + list.get(i));
						model.add(Suffix, p2, X2);
					}
				} else {
					for (int i = 0; i < list.size(); i++) {
						Property p2 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToEntity");
						Resource X2 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
						model.add(Suffix, p2, X2);
					}
				}
			}
			Resource X3 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "S6");
			model.add(Suffix, RDF.type, X3).add(Suffix, RDFS.label, blpName).add(Suffix, RDFS.comment, serializedForm);
		} else if (blpCategory.contains("S7")) {
			Resource Suffix = model.createResource(reqURI + blpName);
			Resource X3 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "S7");
			model.add(Suffix, RDF.type, X3).add(Suffix, RDFS.label, blpName).add(Suffix, RDFS.comment, serializedForm);
		} else if (blpCategory.contains("S8")) { // Entity
			Resource Suffix = model.createResource(reqURI + blpName);
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("entity")) {
					for (int i = 0; i < list.size(); i++) {
						Property p2 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToEntity");
						Resource X2 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + list.get(i));
						model.add(Suffix, p2, X2);
					}
				}
			}
			Resource X3 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "S8");
			model.add(Suffix, RDF.type, X3).add(Suffix, RDFS.label, blpName).add(Suffix, RDFS.comment, serializedForm);
		} else {
			System.out.println("No such suffix boilerplate exists : " + blpCategory);
		}
		// write it to standard out
		// model.write(System.out);

	}

	public static HashMap<String, String> getBlpInstances() {

		HashMap<String, String> blps = new HashMap<String, String>();

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?y " + "WHERE { " + "?y rdf:type ?z . "
				+ "	    ?z  rdfs:subClassOf* RBLP:ReqDescriptor  }";

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.

		// model.write(System.out);

		// The union of the 2 pre-defined models. In this way jena can see the
		// classes that are declared in RBLP.ttl and then imported to CSSP.ttl.
		Model model3 = model.union(csspModel);

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model3);
		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();
				// Parsing of the string
				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				String namespace = "RBLP:";
				String boilerplate = namespace + req;

				String serialized = getSerializedFor(boilerplate);
				blps.put(boilerplate, serialized);

			}

		} finally {
			qexec.close();
		}

		return blps;
	}

	public static ArrayList<String> loadBlpsComplex() {

		ArrayList<String> complex = new ArrayList<String>();

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?e " + "WHERE { "
				+ " ?x rdfs:subClassOf+ RBLP:Attribute . " + " ?e rdf:type ?x . }";
		/*
		 * + "	?y rdfs:subPropertyOf+ RBLP:isRelatedToAttribute . ";
		 * 
		 * String queryString2 = " ?y ?e . }"; String queryString3 = queryString
		 * + blp + queryString2;
		 */
		// System.out.println(queryString3);

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.

		// model.write(System.out);

		// The union of the 2 pre-defined models. In this way jena can see the
		// classes that are declared in RBLP.ttl and then imported to CSSP.ttl.
		Model model3 = model.union(csspModel);

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model3);
		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();
				// Parsing of the string
				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				// System.out.println(req);
				String namespace = "RBLP:";
				String boilerplate = namespace + req;
				String serialized = getSerializedFor(boilerplate);
				if (serialized != null && !serialized.isEmpty())
					complex.add(serialized);
				else
					logger.log(Level.INFO, "Serialized form of " + boilerplate + " was not found in ontology");

			}
		} finally {
			qexec.close();
		}

		return complex;

	}

	public static ObservableList<RequirementModel> loadRequirementsFromOntology() {

		ObservableList<RequirementModel> requirements = FXCollections.observableArrayList();
		ArrayList<OntologyWordModel> requirementsList = new ArrayList<OntologyWordModel>();
		reqIDtoObject = new HashMap<>();

		requirementsList = getRequirementInstances();

		for (OntologyWordModel ReqID : requirementsList) {
			try {
				String req = ReqID.getFullName();
				// System.out.println("req name =" +req);
				RequirementStatusModel statMod = new RequirementStatusModel();
				String status = getRequirementStatus(req);
				if (status != null && !status.isEmpty())
					statMod.setCurrentStatus(status);
				OntologyWordModel category = getRequirementType(req);
				OntologyWordModel absLevel = getRequirementAbstractionLevel(req);
				// getRefines (or null)
				// getIsRefinedFrom (or null)
				RequirementModel newReq = new RequirementModel(ReqID.getShortName(), category, absLevel, statMod, null,
						null, null, null);
				// prefID = getPrefixID()
				// mainID = getMainID()
				// suffixID = getSuffixID()
				ArrayList<OntologyWordModel> prefixIDs = getPrefixForRequirement(req);
				ArrayList<OntologyWordModel> mainIDs = getMainForRequirement(req);
				ArrayList<OntologyWordModel> suffixIDs = getSuffixForRequirement(req);

				if (!prefixIDs.isEmpty()) {
					for (OntologyWordModel prefID : prefixIDs) {
						newReq.setPrefix(
								new GenericBoilerPlate(Memory.getBoilerplatesInMemory().get(prefID.getFullName())));
					}
				}

				if (!mainIDs.isEmpty()) {
					newReq.setMainBody(
							new GenericBoilerPlate(Memory.getBoilerplatesInMemory().get(mainIDs.get(0).getFullName())));
				}
				if (!suffixIDs.isEmpty()) {
					for (OntologyWordModel prefID : suffixIDs) {
						newReq.setSuffix(
								new GenericBoilerPlate(Memory.getBoilerplatesInMemory().get(prefID.getFullName())));
					}
				}

				requirements.add(newReq);
				reqIDtoObject.put(req, newReq);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Refines
		for (OntologyWordModel ReqID : requirementsList) {
			String req = ReqID.getFullName();
			if (reqIDtoObject.containsKey(req)) {
				reqIDtoObject.get(req).setRefinesByIDs(getRefinesForRequirement(req), reqIDtoObject);
			}
			if (reqIDtoObject.containsKey(req)) {
				reqIDtoObject.get(req).setIsRefinedFromByIDs(getisRefinedByForRequirement(req), reqIDtoObject);
			}
		}

		// Concretizes
		for (OntologyWordModel ReqID : requirementsList) {
			String req = ReqID.getFullName();
			if (reqIDtoObject.containsKey(req)) {
				reqIDtoObject.get(req).setConcretizesByIDs(getConcretizesForRequirement(req), reqIDtoObject);
			}
			if (reqIDtoObject.containsKey(req)) {
				reqIDtoObject.get(req).setIsConcretizedFromByIDs(getisConcretizedByForRequirement(req), reqIDtoObject);
			}
		}

		// Get all requirement IDs to a list allReqIDs
		// for (ReqID : allReqIDs){
		// StatusModel statMod = new RequirementStatusModel
		// status = getStatus
		// if (!status.isEmpty()) statMod.set(status)
		// getCategory
		// getAbsLevel (withNamespace if available)
		// getRefines (or null)
		// getIsRefinedFrom (or null)
		// RequirementModel newReq = new RequirementModel(ReqID,category,new
		// OntologyWordModel(absLevel),statMod,refines,isRefinedFrom)
		// prefID = getPrefixID()
		// mainID = getMainID()
		// suffixID = getSuffixID()
		// if (prefixID!=null) newReq.setPrefix(new GenericBoilerplate(
		// getSerializedFormOf(prefixID)) )
		// same for main, suffix
		// requirements.add(newReq)
		// }

		return requirements;
	}

	public static ObservableList<PropertyModel> loadPropertiesFromOntology() {

		ObservableList<PropertyModel> properties = FXCollections.observableArrayList();
		ArrayList<OntologyWordModel> propertiestsList = new ArrayList<OntologyWordModel>();

		propertiestsList = getPropertyInstances();

		for (OntologyWordModel propID : propertiestsList) {
			try {
				String prp = propID.getFullName();

				PropertyStatusModel statMod = new PropertyStatusModel();
				String status = getPropertyStatus(prp);
				if (status != null && !status.isEmpty())
					statMod.setCurrentStatus(status);
				OntologyWordModel category = getPropertyType(prp);
				Architecture arch = getArchitectureForProperty(prp);

				ObservableList<RequirementModel> isDerivedBy = FXCollections.observableArrayList();
				isDerivedBy.addAll(getisRefinedByForProperty(prp));
				PropertyModel newProp = new PropertyModel(propID.getShortName(), category, statMod, isDerivedBy, arch);
				// PropertyModel newProp = new PropertyModel(prp, category,
				// statMod, null, null);

				ArrayList<OntologyWordModel> patternIDs = getPatternsForProperty(prp);

				// if (prefixID!=null) newReq.setPrefix(new GenericBoilerplate(
				// getSerializedFormOf(prefixID)) )
				// same for main, suffix
				if (!patternIDs.isEmpty()) {
					newProp.setPattern(
							new PropertyPattern(Memory.getPatternsInMemory().get(patternIDs.get(0).getFullName())));
				}

				properties.add(newProp);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return properties;
	}

	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% REQUIREMENTS
	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% //

	public static void addEventToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm)
			throws FileNotFoundException {

		String URI = "http://lpis.csd.auth.gr/ontologies/2015/RBLP#";
		Resource instance = model.createResource(URI + className);
		Resource R1 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + classType);
		model.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);
		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("Actor")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToActor");
					model.add(instance, P2, R2);
				}
			} else if (key.getFullName().contains("Action")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToAction");
					model.add(instance, P2, R2);
				}
			}
			if (key.getFullName().contains("Entity")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToEntity");
					model.add(instance, P2, R2);
				}
			} else if (key.getFullName().contains("Verb")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model.createResource("http://www.w3.org/2001/XMLSchema#string" + list.get(i));
					Property P2 = model.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "hasVerb");
					model.add(instance, P2, R2);
				}
			}
		}
	}

	public static void addQuantityToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm)
			throws FileNotFoundException {

		String URI = "http://lpis.csd.auth.gr/ontologies/2015/RBLP#";
		Resource instance = model.createResource(URI + className);
		Resource R1 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + classType);
		model.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);
		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("Quantifier")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model.createResource("http://www.w3.org/2001/XMLSchema#string" + list.get(i));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "hasQuantifier");
					model.add(instance, P2, R2);
				}
			} else if (key.getFullName().contains("Number")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model.createResource("http://www.w3.org/2001/XMLSchema#integer" + list.get(i));
					Property P2 = model.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "hasNumber");
					model.add(instance, P2, R2);
				}
			} else if (key.getFullName().contains("Unit")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model.createResource("http://purl.oclc.org/net/muo/muo#" + list.get(i));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedtoUOM");
					model.add(instance, P2, R2);
				}
			}
		}
	}

	public static void addStateToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm)
			throws FileNotFoundException {

		String behURI = "http://lpis.csd.auth.gr/ontologies/2015/RBLP#";
		Resource instance = model.createResource(behURI + className);
		Resource R1 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + classType);
		model.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);
		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("Entity")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model
							.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + list.get(i));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToEntity");
					model.add(instance, P2, R2);
				}
			}
			if (key.getFullName().contains("Mode")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model
							.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + list.get(i));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToMode");
					model.add(instance, P2, R2);
				}
			}
		}
	}

	public static void addActionToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm)
			throws FileNotFoundException {
		String behURI = "http://lpis.csd.auth.gr/ontologies/2015/RBLP#";
		Resource instance = model.createResource(behURI + className);
		Resource R1 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + classType);
		model.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);
		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("Entity")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model
							.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + list.get(i));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToEntity");
					model.add(instance, P2, R2);
				}
			} else if (key.getFullName().contains("Verb")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model.createResource("http://www.w3.org/2001/XMLSchema#string" + list.get(i));
					Property P2 = model.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "hasVerb");
					model.add(instance, P2, R2);
				}
			}
		}
	}

	public static void addWhileBlockToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm)
			throws FileNotFoundException {

		String behURI = "http://lpis.csd.auth.gr/ontologies/2015/RBLP#";
		Resource instance = model.createResource(behURI + className);
		Resource R1 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + classType);
		model.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);
		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("Action")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToActionBlock");
					model.add(instance, P2, R2);
				}
			} else if (key.getFullName().contains("Cond")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToCondition");
					model.add(instance, P2, R2);
				}
			}
		}
	}

	public static void addIfBlockToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm)
			throws FileNotFoundException {

		String behURI = "http://lpis.csd.auth.gr/ontologies/2015/RBLP#";
		Resource instance = model.createResource(behURI + className);
		Resource R1 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + classType);
		model.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);
		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("Action")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
					Property P2 = model.createProperty(
							"http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "sRelatedToListOfActionBlocks");
					model.add(instance, P2, R2);
				}
			} else if (key.getFullName().contains("Cond")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToCondition");
					model.add(instance, P2, R2);
				}
			}
		}
	}

	public static void addActionBlockToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedform)
			throws FileNotFoundException {

		String behURI = "http://lpis.csd.auth.gr/ontologies/2015/RBLP#";
		Resource instance = model.createResource(behURI + className);
		Resource R1 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + classType);
		model.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedform);
		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("action")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model
							.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + list.get(i));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToAction");
					model.add(instance, P2, R2);
				}
			} else if (key.getFullName().contains("if-block")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(0));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToIfBlock");
					model.add(instance, P2, R2);
				}
			} else if (key.getFullName().contains("while-block")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(0));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToWhileBlock");
					model.add(instance, P2, R2);
				}
			}
		}
	}

	public static void addEntityListToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedform)
			throws FileNotFoundException {

		String behURI = "http://lpis.csd.auth.gr/ontologies/2015/RBLP#";
		Resource instance = model.createResource(behURI + className);
		Resource R1 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + classType);
		model.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedform);
		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("Entity")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model
							.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + list.get(i));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToEntity");
					model.add(instance, P2, R2);
				}
			} else if (key.getFullName().contains("Number")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model.createResource("http://www.w3.org/2001/XMLSchema#integer" + list.get(i));
					Property P2 = model.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "hasNumber");
					model.add(instance, P2, R2);
				}
			}
		}
	}

	public static void addEntityFilterToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedform)
			throws FileNotFoundException {

		String behURI = "http://lpis.csd.auth.gr/ontologies/2015/RBLP#";
		Resource instance = model.createResource(behURI + className);
		Resource R1 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + classType);
		model.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedform);
		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("Entity")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model
							.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + list.get(i));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToEntity");
					model.add(instance, P2, R2);
				}
			} else if (key.getFullName().contains("Action")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model
							.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + list.get(0));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToAction");
					model.add(instance, P2, R2);
				}
			} else if (key.getFullName().contains("State")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model
							.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + list.get(0));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToState");
					model.add(instance, P2, R2);
				}
			}
		}
	}

	public static void addEntityQuantToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm)
			throws FileNotFoundException {

		String behURI = "http://lpis.csd.auth.gr/ontologies/2015/RBLP#";
		Resource instance = model.createResource(behURI + className);
		Resource R1 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + classType);
		model.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);
		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("quantity")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToQuantity");
					model.add(instance, P2, R2);
				}
			}
		}
	}

	public static void addActEntSuffToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm)
			throws FileNotFoundException {

		String behURI = "http://lpis.csd.auth.gr/ontologies/2015/RBLP#";
		Resource instance = model.createResource(behURI + className);
		Resource R1 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + classType);
		model.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);
		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("entity")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model
							.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + list.get(i));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToEntity");
					model.add(instance, P2, R2);
				}
			} else if (key.getFullName().contains("action")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model
							.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + list.get(i));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToAction");
					model.add(instance, P2, R2);
				}
			}
		}
	}

	public static void addEntiListEntToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm)
			throws FileNotFoundException {

		String behURI = "http://lpis.csd.auth.gr/ontologies/2015/RBLP#";
		Resource instance = model.createResource(behURI + className);
		Resource R1 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + classType);
		model.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);
		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("entity-list")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToEntityList");
					model.add(instance, P2, R2);
				}
			} else if (key.getFullName().contains("entity")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(i));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToEntity");
					model.add(instance, P2, R2);
				}
			}
		}
	}

	public static void addLowLevelObjectToOntology(String className, String classType, String label) {
		String URI = "http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#";
		Resource r1 = model.createResource(URI + className);
		Resource r2 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#" + classType);
		model.add(r1, RDF.type, r2).add(r1, RDFS.label, label);
	}

	public static void addSimpleEntityToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm)
			throws FileNotFoundException {
		// System.out.println("EDW" + serializedForm);
		String behURI = "http://lpis.csd.auth.gr/ontologies/2015/RBLP#";
		Resource instance = model.createResource(behURI + className);
		Resource R1 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + classType);
		model.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);
		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("Entity")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model
							.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + list.get(i));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToEntity");
					model.add(instance, P2, R2);
				}
			} else if (key.getFullName().contains("Quantity")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + list.get(0));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToQuantity");
					model.add(instance, P2, R2);
				}
			} else if (key.getFullName().contains("Prep")) {
				// Disabled because it causes writing error
				// for (int i = 0; i < list.size(); i++) {
				// Resource R2 =
				// model.createResource("http://www.w3.org/2001/XMLSchema#integer#"
				// + list.get(0));
				// Property P2 = model
				// .createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#"
				// + "hasPreposition");
				// model.add(instance, P2, R2);
				// }
			} else if (key.getFullName().contains("Entity-quant")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model.createResource("http://www.w3.org/2001/XMLSchema#integer#" + list.get(0));
					Property P2 = model.createProperty(
							"http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToEntity-Quant");
					model.add(instance, P2, R2);
				}
			}
		}
	}

	public static void addSubjectToOntology(String className, String classType, String serializedForm)
			throws FileNotFoundException {
		// System.out.println("EDW" + serializedForm);
		String behURI = "http://lpis.csd.auth.gr/ontologies/2015/RBLP#";
		Resource instance = model.createResource(behURI + className);
		Resource R1 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "Actor");
		model.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);
	}

	public static void addBuildingBlockToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm)
			throws FileNotFoundException {

		String behURI = "http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#";
		Resource instance = model.createResource(behURI + className);
		Resource R1 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#" + "SystemEntity");
		model.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);
		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("event")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO#" + list.get(i));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#" + "isRelatedToEvent");
					model.add(instance, P2, R2);
				}
			}
			if (key.getFullName().contains("interface")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = model
							.createResource("http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#" + list.get(i));
					Property P2 = model
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "isRelatedToInterface");
					model.add(instance, P2, R2);
				}
			}

		}
	}

	public static void addInterfaceToOntology(String className, String classType, String serializedForm)
			throws FileNotFoundException {
		// System.out.println("EDW" + serializedForm);
		String behURI = "http://lpis.csd.auth.gr/ontologies/2015/DSO#";
		Resource instance = model.createResource(behURI + className);
		Resource R1 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + "Interface");
		model.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);
	}

	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// //
	// ----------------------------------------------- PROPERTIES
	// ----------------------------------------------------------------- //
	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// //

	public static void addBehaviourToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm) {
		String URI = "http://lpis.csd.auth.gr/ontologies/2015/PRP#";
		Resource instance = prpModel.createResource(URI + className);
		Resource R1 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "Behaviour");
		prpModel.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);
		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("Beh")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
					Property P2 = prpModel
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasBehaviour");
					prpModel.add(instance, P2, R2);
				}
			}
		}
	}

	// ---------------------------------------------- Simple behaviour
	// proposition
	// ---------------------------------------------------------------//
	public static void addSimpleBehaviourPropositionToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm) {

		String URI = "http://lpis.csd.auth.gr/ontologies/2015/PRP#";
		Resource instance = prpModel.createResource(URI + className);
		Resource R1 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "SimpleBehaviourProp");
		prpModel.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);

		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("Proposition")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
					Property P2 = prpModel
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasProposition");
					prpModel.add(instance, P2, R2);
				}
			} else if (key.getFullName().contains("Time-Interval")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
					Property P2 = prpModel
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "isRelatedToTimeInterval");
					prpModel.add(instance, P2, R2);
				}
			}
		}
	}

	public static void addModePropositionToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm) {

		String URI = "http://lpis.csd.auth.gr/ontologies/2015/PRP#";
		Resource instance = prpModel.createResource(URI + className);
		Resource R1 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "ModeProposition");
		prpModel.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);

		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("Component")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
					Property P2 = prpModel
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "isRelatedToComponent");
					prpModel.add(instance, P2, R2);
				}
			} else if (key.getFullName().contains("Mode")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
					Property P2 = prpModel.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasMode");
					prpModel.add(instance, P2, R2);
				}
			}
		}
	}

	public static void addLogicExpressionPropositionToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm) {

		String URI = "http://lpis.csd.auth.gr/ontologies/2015/PRP#";
		Resource instance = prpModel.createResource(URI + className);
		Resource R1 = prpModel
				.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "LogicExpressionProposition");
		prpModel.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);

		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("Logic-Expression")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
					Property P2 = prpModel
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "isRelatedToExpression");
					prpModel.add(instance, P2, R2);
				}
			}
		}
	}

	// ---------------------------------------------- Simple behaviour
	// proposition end
	// ---------------------------------------------------------------//

	// ---------------------------------------------- Simple behaviour event
	// ---------------------------------------------------------------//

	public static void addSimpleBehaviourEventToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm) {

		String URI = "http://lpis.csd.auth.gr/ontologies/2015/PRP#";
		Resource instance = prpModel.createResource(URI + className);
		Resource R1 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "SimpleBehaviourEvent");
		prpModel.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);

		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("Event")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
					Property P2 = prpModel
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "isRelatedToEvent");
					prpModel.add(instance, P2, R2);
				}
			} else if (key.getFullName().contains("Time-Interval")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
					Property P2 = prpModel
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "isRelatedToInterval");
					prpModel.add(instance, P2, R2);
				}

			} else if (key.getFullName().contains("Proposition")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
					Property P2 = prpModel
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasProposition");
					prpModel.add(instance, P2, R2);
				}

			}
		}
	}
	/*
	 * public static void addEventToOntology(String className, String classType,
	 * HashMap<OntologyWordModel, ArrayList<String>> contentMap) throws
	 * FileNotFoundException{
	 * 
	 * String behURI = "http://lpis.csd.auth.gr/ontologies/2015/PRP#"; Resource
	 * instance = prpModel.createResource(behURI + className); Resource R1 =
	 * prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP" +
	 * classType); prpModel.add(instance, RDF.type, R1);
	 * 
	 * for ( OntologyWordModel key : contentMap.keySet() ) { //
	 * System.out.println(key.getFullName()); ArrayList<String> list =
	 * contentMap.get(key); if(key.getFullName().contains("port")){ for(int i=0;
	 * i<list.size(); i++){ Resource R2 =
	 * prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP" +
	 * list.get(i)); Property P2 =
	 * prpModel.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP" +
	 * "hasPort"); prpModel.add(instance, P2, R2); } } else
	 * if(key.getFullName().contains("ModeChange")){ for(int i=0; i<list.size();
	 * i++){ Resource R2 =
	 * prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP" +
	 * list.get(i)); Property P2 =
	 * prpModel.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP" +
	 * "toMode"); prpModel.add(instance, P2, R2); } } else
	 * if(key.getFullName().contains("PropertyChange")){ for(int i=0;
	 * i<list.size(); i++){ Resource R2 =
	 * prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP" +
	 * list.get(i)); Property P2 =
	 * prpModel.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP" +
	 * "hasPropChange"); prpModel.add(instance, P2, R2); } } } }
	 */

	public static void addModeChangeToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm) {

		String URI = "http://lpis.csd.auth.gr/ontologies/2015/PRP#";
		Resource instance = prpModel.createResource(URI + className);
		Resource R1 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "ModeChange");
		prpModel.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);

		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("Component")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
					Property P2 = prpModel
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "isRelatedToComponent");
					prpModel.add(instance, P2, R2);
				}
			} else if (key.getFullName().contains("Mode")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
					Property P2 = prpModel.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "toMode");
					prpModel.add(instance, P2, R2);
				}
			}
		}
	}

	public static void addPropertyChangeToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm) {

		String URI = "http://lpis.csd.auth.gr/ontologies/2015/PRP#";
		Resource instance = prpModel.createResource(URI + className);
		Resource R1 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "PropChange");
		prpModel.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);

		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("Logic-Expression")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
					Property P2 = prpModel
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "isRelatedToExpression");
					prpModel.add(instance, P2, R2);
				}
			}
		}
	}

	public static void addInteractionToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm) {

		String URI = "http://lpis.csd.auth.gr/ontologies/2015/PRP#";
		Resource instance = prpModel.createResource(URI + className);
		Resource R1 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "Interaction");
		prpModel.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);

		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("Port")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
					Property P2 = prpModel.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasPort");
					prpModel.add(instance, P2, R2);
				}
			}
		}
	}

	public static void addIntervalToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm) {

		String URI = "http://lpis.csd.auth.gr/ontologies/2015/PRP#";
		Resource instance = prpModel.createResource(URI + className);
		Resource R1 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "Interval");
		prpModel.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);

		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("Evaluation")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
					Property P2 = prpModel
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasEvaluation");
					prpModel.add(instance, P2, R2);
				}
			}
		}
	}

	public static void addEvaluationToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm) {

		String URI = "http://lpis.csd.auth.gr/ontologies/2015/PRP#";
		Resource instance = prpModel.createResource(URI + className);
		Resource R1 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "Evaluation");
		prpModel.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);

		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("Function")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
					Property P2 = prpModel
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasFunction");
					prpModel.add(instance, P2, R2);
				}
			} else if (key.getFullName().contains("LiteralValue")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
					Property P2 = prpModel
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasLiteralValue");
					prpModel.add(instance, P2, R2);
				}
			} else if (key.getFullName().contains("Variable")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
					Property P2 = prpModel
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasVariable");
					prpModel.add(instance, P2, R2);
				}
			} else if (key.getFullName().contains("Expression")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
					Property P2 = prpModel
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "isRelatedToExpression");
					prpModel.add(instance, P2, R2);
				}
			}
		}
	}

	public static void addTimeIntervalToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm) {

		String URI = "http://lpis.csd.auth.gr/ontologies/2015/PRP#";
		Resource instance = prpModel.createResource(URI + className);
		Resource R1 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "TimeInterval");
		prpModel.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);

		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("Interval")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
					Property P2 = prpModel
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "isRelatedToInterval");
					prpModel.add(instance, P2, R2);
				}
			}
		}
	}

	public static void addPortListToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm) {

		String URI = "http://lpis.csd.auth.gr/ontologies/2015/PRP#";
		Resource instance = prpModel.createResource(URI + className);
		Resource R1 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "PortList");
		prpModel.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);

		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("port")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
					Property P2 = prpModel.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasPort");
					prpModel.add(instance, P2, R2);
				}
			}
		}
	}

	public static void addInteractionListToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm) {

		String URI = "http://lpis.csd.auth.gr/ontologies/2015/PRP#";
		Resource instance = prpModel.createResource(URI + className);
		Resource R1 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "InteractionList");
		prpModel.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);

		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("interaction")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
					Property P2 = prpModel
							.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasInteraction");
					prpModel.add(instance, P2, R2);
				}
			}
		}
	}

	public static void addModeListToOntology(String className, String classType,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm) {

		String URI = "http://lpis.csd.auth.gr/ontologies/2015/PRP#";
		Resource instance = prpModel.createResource(URI + className);
		Resource R1 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "ModeList");
		prpModel.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);

		for (OntologyWordModel key : contentMap.keySet()) {
			ArrayList<String> list = contentMap.get(key);
			if (key.getFullName().contains("mode")) {
				for (int i = 0; i < list.size(); i++) {
					Resource R2 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
					Property P2 = prpModel.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasMode");
					prpModel.add(instance, P2, R2);
				}
			}
		}
	}

	// ---------------------------------------------- Simple behaviour event end
	// ---------------------------------------------------------------//

	public static void addPortToOntology(String className, String classType, String serializedForm) {

		String URI = "http://lpis.csd.auth.gr/ontologies/2015/PRP#";
		Resource instance = prpModel.createResource(URI + className);
		Resource R1 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "PortActivation");
		prpModel.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);
	}

	public static void addComponentToOntology(String className, String classType, String serializedForm) {

		String URI = "http://lpis.csd.auth.gr/ontologies/2015/PRP#";
		Resource instance = prpModel.createResource(URI + className);
		Resource R1 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "Component");
		prpModel.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);
	}

	public static void addLogicExpressionToOntology(String className, String classType, String serializedForm) {

		String URI = "http://lpis.csd.auth.gr/ontologies/2015/PRP#";
		Resource instance = prpModel.createResource(URI + className);
		Resource R1 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "LogicExpression");
		prpModel.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);
	}

	public static void addModeToOntology(String className, String classType, String serializedForm) {

		String URI = "http://lpis.csd.auth.gr/ontologies/2015/PRP#";
		Resource instance = prpModel.createResource(URI + className);
		Resource R1 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "Mode");
		prpModel.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);
	}

	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% VALIDATION
	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

	public static void runSpinInferences() {
		// Initialize system functions and templates
		SPINModuleRegistry.get().init();

		// Load domain model with imports
		System.out.println("Loading domain ontology...");
		OntModel queryModel = loadModelWithImports("instances-tool.rdf");

		// Create and add Model for inferred triples
		Model newTriples = ModelFactory.createDefaultModel();
		newTriples.setNsPrefix("RBLP", "http://lpis.csd.auth.gr/ontologies/2015/RBLP#");
		newTriples.setNsPrefix("CSSP", "http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#");
		newTriples.setNsPrefix("OoSSA", "http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#");
		queryModel.addSubModel(newTriples);

		// Load OWL RL library from the web
		System.out.println("Loading OWL RL ontology...");
		// OntModel owlrlModel =
		// loadModelWithImports("http://topbraid.org/spin/owlrl-all");
		OntModel owlrlModel = loadModelWithImports("http://topbraid.org/spin/rdfsplus");

		// Register any new functions defined in OWL RL
		SPINModuleRegistry.get().registerAll(owlrlModel, null);

		// Build one big union Model of everything
		MultiUnion multiUnion = JenaUtil.createMultiUnion(new Graph[] { queryModel.getGraph(), owlrlModel.getGraph() });
		Model unionModel = ModelFactory.createModelForGraph(multiUnion);

		// Collect rules (and template calls) defined in OWL RL
		Map<Resource, List<CommandWrapper>> cls2Query = SPINQueryFinder.getClass2QueryMap(unionModel, queryModel,
				SPIN.rule, true, false);
		Map<Resource, List<CommandWrapper>> cls2Constructor = SPINQueryFinder.getClass2QueryMap(queryModel, queryModel,
				SPIN.constructor, true, false);
		SPINRuleComparator comparator = new DefaultSPINRuleComparator(queryModel);

		// Run all inferences
		System.out.println("Running SPIN inferences...");
		SPINInferences.run(queryModel, newTriples, cls2Query, cls2Constructor, null, null, false, SPIN.rule, comparator,
				null);
		System.out.println("Inferred triples: " + newTriples.size());
		// newTriples.write(System.out);

		infModel = unionModel.union(newTriples);
	}

	private static OntModel loadModelWithImports(String url) {
		Model baseModel = ModelFactory.createDefaultModel();
		baseModel.read(url);
		return JenaUtil.createOntologyModel(OntModelSpec.OWL_MEM, baseModel);
	}

	public static ValidationResultModel nonInstantiatedAbstrReqs() {

		ArrayList<OntologyWordModel> results1 = new ArrayList<>();
		ArrayList<OntologyWordModel> results2 = new ArrayList<>();
		ArrayList<OntologyWordModel> results3 = new ArrayList<>();

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#>"
				+ "prefix DSOInstances: <http://lpis.csd.auth.gr/ontologies/2015/DSOInstnaces#>"
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?ar ?mae ?inst " + "WHERE { "
				+ "?ar a CSSP:AbstractRequirement . " + "?ar CSSP:mainAbstractEntity ?mae . " + "?inst a ?mae . "
				+ "FILTER NOT EXISTS { " + "?inst CSSP:hasRequirement ?cr . " + "?cr CSSP:concretizes ?ar . } . }";

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.

		QueryExecution qexec = QueryExecutionFactory.create(query, infModel);

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();

				String req1 = req;
				String req2 = req;

				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));

				req1 = req1.substring(req1.indexOf("?mae") + 5);
				req1 = req1.substring(0, req1.indexOf(")"));

				req1 = req1.substring(req1.indexOf("#") + 1);
				req1 = req1.substring(0, req1.indexOf(">"));

				req2 = req2.substring(req2.indexOf("?inst") + 5);
				req2 = req2.substring(0, req2.indexOf(")"));

				req2 = req2.substring(req2.indexOf("#") + 1);
				req2 = req2.substring(0, req2.indexOf(">"));

				// System.out.println(req);
				String namespace = "CSSP:";
				String namespace1 = "RBLP:";

				// Addition of the strings (classes) to the array list.
				results1.add(new OntologyWordModel(namespace, req));
				results2.add(new OntologyWordModel(namespace1, req1));
				results3.add(new OntologyWordModel(namespace, req2));
			}

		} finally {
			qexec.close();
		}

		ValidationResultModel valResults = new ValidationResultModel();
		valResults.addResults(results1);
		valResults.addResults(results2);
		valResults.addResults(results3);

		return valResults;
	}

	public static ValidationResultModel NonConcretizedAbstrReqs() {

		ArrayList<OntologyWordModel> results1 = new ArrayList<>();
		ArrayList<OntologyWordModel> results2 = new ArrayList<>();

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#>"
				+ "prefix DSOInstances: <http://lpis.csd.auth.gr/ontologies/2015/DSOInstnaces#>"
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?ar ?mae " + "WHERE { "
				+ "?ar a CSSP:AbstractRequirement . " + "?ar CSSP:mainAbstractEntity ?mae . " + "FILTER NOT EXISTS { "
				+ "?inst a ?mae . } . }";

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.

		QueryExecution qexec = QueryExecutionFactory.create(query, infModel);

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();

				String req1 = req;

				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));

				req1 = req1.substring(req1.indexOf("?mae") + 5);
				req1 = req1.substring(0, req1.indexOf(")"));

				req1 = req1.substring(req1.indexOf("#") + 1);
				req1 = req1.substring(0, req1.indexOf(">"));

				// System.out.println(req);
				String namespace = "CSSP:";
				// Addition of the strings (classes) to the array list.
				results1.add(new OntologyWordModel(namespace, req));
				results2.add(new OntologyWordModel(namespace, req1));
			}

		} finally {
			qexec.close();
		}

		ValidationResultModel valResults = new ValidationResultModel();
		valResults.addResults(results1);
		valResults.addResults(results2);

		return valResults;
	}

	public static ValidationResultModel EntitiesWithourReqs() {

		ArrayList<OntologyWordModel> results = new ArrayList<>();

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#>"
				+ "prefix DSOInstances: <http://lpis.csd.auth.gr/ontologies/2015/DSOInstnaces#>"
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?e " + "WHERE { "
				+ "?e rdf:type RBLP:Entity . " + "FILTER NOT EXISTS { " + "?e CSSP:hasRequirement ?r . } . }";

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.

		QueryExecution qexec = QueryExecutionFactory.create(query, infModel);

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();

				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				// System.out.println(req);
				String namespace = "RBLP:";
				// Addition of the strings (classes) to the array list.
				results.add(new OntologyWordModel(namespace, req));
			}

		} finally {
			qexec.close();
		}

		ValidationResultModel valResults = new ValidationResultModel();
		valResults.addResults(results);

		return valResults;
	}

	public static ValidationResultModel SubjectEntitiesWithourReqs() {

		ArrayList<OntologyWordModel> results = new ArrayList<>();

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#>"
				+ "prefix DSOInstances: <http://lpis.csd.auth.gr/ontologies/2015/DSOInstnaces#>"
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?e " + "WHERE { "
				+ "?e rdf:type RBLP:SimpleEntity . " + "FILTER NOT EXISTS { "
				+ "?e CSSP:isSubjectofRequirement ?r . } . }";

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.

		QueryExecution qexec = QueryExecutionFactory.create(query, infModel);

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();

				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				// System.out.println(req);
				String namespace = "RBLP:";
				// Addition of the strings (classes) to the array list.
				results.add(new OntologyWordModel(namespace, req));
			}

		} finally {
			qexec.close();
		}

		ValidationResultModel valResults = new ValidationResultModel();
		valResults.addResults(results);

		return valResults;
	}

	public static ValidationResultModel ActortEntitiesWithourReqs() {

		ArrayList<OntologyWordModel> results = new ArrayList<>();

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#>"
				+ "prefix DSOInstances: <http://lpis.csd.auth.gr/ontologies/2015/DSOInstnaces#>"
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?e " + "WHERE { "
				+ "?e rdf:type RBLP:Actor . " + "FILTER NOT EXISTS { " + "?e CSSP:isSubjectofRequirement ?r . } . }";

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.

		QueryExecution qexec = QueryExecutionFactory.create(query, infModel);

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();

				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				// System.out.println(req);
				String namespace = "RBLP";
				// Addition of the strings (classes) to the array list.
				results.add(new OntologyWordModel(namespace, req));
			}

		} finally {
			qexec.close();
		}

		ValidationResultModel valResults = new ValidationResultModel();
		valResults.addResults(results);

		return valResults;
	}

	public static ValidationResultModel ContradictReqs() {

		ArrayList<OntologyWordModel> results1 = new ArrayList<>();
		ArrayList<OntologyWordModel> results2 = new ArrayList<>();

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#>"
				+ "prefix DSOInstances: <http://lpis.csd.auth.gr/ontologies/2015/DSOInstnaces#>"
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT DISTINCT ?r1 ?r2 " + "WHERE { "
				+ "?r1 a CSSP:Requirement . " + "?r1 CSSP:hasMain ?m1 ." + "?m1 RBLP:isRelatedToSubject ?s ."
				+ "?m1 RBLP:isRelatedToAction ?a1 ." + "?r2 a CSSP:Requirement . " + "?r2 CSSP:hasPrefix ?pr2 . "
				+ "?pr2 RBLP:isRelatedtoEvent ?ev . " + "?ev RBLP:isRelatedToActor ?s . "
				+ "?ev RBLP:isRelatedToAction ?a2 . " + "?a2 RBLP:contradictsWith ?a1 . } ";

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.

		QueryExecution qexec = QueryExecutionFactory.create(query, infModel);

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();
				String req1 = req;

				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));

				req1 = req1.substring(req1.indexOf("?r2") + 5);
				req1 = req1.substring(0, req1.indexOf(")"));

				req1 = req1.substring(req1.indexOf("#") + 1);
				req1 = req1.substring(0, req1.indexOf(">"));

				// System.out.println(req);
				String namespace = "CSSP:";
				// Addition of the strings (classes) to the array list.
				results1.add(new OntologyWordModel(namespace, req));
				results2.add(new OntologyWordModel(namespace, req1));
			}

		} finally {
			qexec.close();
		}

		ValidationResultModel valResults = new ValidationResultModel();
		valResults.addResults(results1);
		valResults.addResults(results2);

		return valResults;
	}

	/*
	 * public void testquery(){ String queryString =
	 * " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> " +
	 * "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#>" +
	 * "prefix DSOInstances: <http://lpis.csd.auth.gr/ontologies/2015/DSOInstnaces#>"
	 * + "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> " +
	 * "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  " +
	 * "prefix owl: <http://www.w3.org/2002/07/owl#> " +
	 * "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
	 * "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
	 * "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " +
	 * "SELECT DISTINCT ?r1 ?r2 " + "WHERE { " + "?r1 CSSP:hasMain ?r2 . } ";
	 * 
	 * OntModel queryModel = loadModelWithImports("instances-tool.rdf"); Query
	 * query = QueryFactory.create(queryString); // Execution of the query.
	 * 
	 * QueryExecution qexec = QueryExecutionFactory.create(query, queryModel);
	 * 
	 * try { ResultSet result = qexec.execSelect(); // As long as there are more
	 * results... while (result.hasNext()) { String req =
	 * result.nextSolution().toString(); System.out.println(req); String req1 =
	 * req;
	 * 
	 * //System.out.println("req1" + req1); req =
	 * req.substring(req.indexOf("ttl#") + 4); req = req.substring(0,
	 * req.indexOf(">"));
	 * 
	 * req1 = req1.substring(req1.indexOf("?r2") + 5); req1 = req1.substring(0,
	 * req1.indexOf(")"));
	 * 
	 * System.out.println(req1);
	 * 
	 * req1 = req1.substring(req1.indexOf("RBLP#") + 5); req1 =
	 * req1.substring(0, req1.indexOf(">")); System.out.println(req + " " +
	 * req1); String namespace = "CSSP:"; // Addition of the strings (classes)
	 * to the array list. //results.add(new OntologyWordModel(namespace, req));
	 * }
	 * 
	 * } finally { qexec.close(); } }
	 * 
	 */

	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% END VALIDATION
	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

	public static void addAllBoilerplatesToOntology(Collection<AbstractBoilerPlate> allBlps) {
		//// ------ Alternative 1 ------- /////
		if (!allBlps.isEmpty()) {
			allBlps.forEach(blp -> {
				blp.genID();
				if (blp.getCatNoNS().toLowerCase().equals("event")) {
					try {
						addEventToOntology(blp.getIDNoNS(), blp.getCatNoNS(), blp.getContentMap(), blp.serialize());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (blp.getCatNoNS().toLowerCase().equals("subject")) {
					try {
						addSubjectToOntology(blp.getIDNoNS(), blp.getCatNoNS(), blp.serialize());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (blp.getCatNoNS().toLowerCase().equals("quantity")) {
					try {
						addQuantityToOntology(blp.getIDNoNS(), blp.getCatNoNS(), blp.getContentMap(), blp.serialize());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (blp.getCatNoNS().toLowerCase().equals("state")) {
					try {
						addStateToOntology(blp.getIDNoNS(), blp.getCatNoNS(), blp.getContentMap(), blp.serialize());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if ((blp.getCatNoNS().toLowerCase().equals("action"))) {
					try {
						addActionToOntology(blp.getIDNoNS(), blp.getCatNoNS(), blp.getContentMap(), blp.serialize());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (blp.getCatNoNS().toLowerCase().equals("while-block")) {
					try {
						addWhileBlockToOntology(blp.getIDNoNS(), blp.getCatNoNS(), blp.getContentMap(),
								blp.serialize());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (blp.getCatNoNS().toLowerCase().equals("if-block")) {
					try {
						addIfBlockToOntology(blp.getIDNoNS(), blp.getCatNoNS(), blp.getContentMap(), blp.serialize());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if ((blp.getCatNoNS().toLowerCase().equals("entity-list"))) {
					try {
						addEntityListToOntology(blp.getIDNoNS(), blp.getCatNoNS(), blp.getContentMap(),
								blp.serialize());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (blp.getCatNoNS().toLowerCase().equals("entity-filter")) {
					try {
						addEntityFilterToOntology(blp.getIDNoNS(), blp.getCatNoNS(), blp.getContentMap(),
								blp.serialize());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (blp.getCatNoNS().toLowerCase().equals("entity-quant")) {
					try {
						addEntityQuantToOntology(blp.getIDNoNS(), blp.getCatNoNS(), blp.getContentMap(),
								blp.serialize());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				else if ((blp.getCatNoNS().toLowerCase().equals("entity"))) {
					try {
						addSimpleEntityToOntology(blp.getIDNoNS(), blp.getCatNoNS(), blp.getContentMap(),
								blp.serialize());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if ((blp.getCatNoNS().toLowerCase().equals("act-ent-suff"))) {
					try {
						addActEntSuffToOntology(blp.getIDNoNS(), blp.getCatNoNS(), blp.getContentMap(),
								blp.serialize());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if ((blp.getCatNoNS().toLowerCase().equals("entilist-ent"))) {
					try {
						addEntiListEntToOntology(blp.getIDNoNS(), blp.getCatNoNS(), blp.getContentMap(),
								blp.serialize());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if ((blp.getCatNoNS().toLowerCase().equals("building-block"))) {
					try {
						addBuildingBlockToOntology(blp.getIDNoNS(), blp.getCatNoNS(), blp.getContentMap(),
								blp.serialize());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if ((blp.getCatNoNS().toLowerCase().equals("prep"))) {
					try {
						// Add prep to ontology, content map will contain 1
						// entry
						// key: ComboBox
						// value: The chosen value by the user
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				else {
					System.out.println("BLP cat not found : " + blp.getCatNoNS());
				}
			});
		}
	}

	public static void addAllPatternsToOntology(Collection<AbstractBoilerPlate> allPtrns) {
		if (!allPtrns.isEmpty()) {
			allPtrns.forEach(ptrn -> {
				// We turn pattern to lowercase first and then compare to avoid
				// some errors
				// We cannot easily do the same for comparisons involving the
				// hashmap inside
				// so we need to use the proper capitalization there
				if (ptrn.getCatNoNS().toLowerCase().equals("beh"))
					addBehaviourToOntology(ptrn.getIDNoNS(), ptrn.getCatNoNS(), ptrn.getContentMap(), ptrn.serialize());

				else if (ptrn.getCatNoNS().toLowerCase().equals("simple-beh")) {
					ptrn.getContentMap().keySet().forEach(key -> {
						if (key.getFullName().contains("Proposition")) {
							addSimpleBehaviourPropositionToOntology(ptrn.getIDNoNS(), ptrn.getCatNoNS(),
									ptrn.getContentMap(), ptrn.serialize());
						} else if (key.getFullName().contains("Event")) {
							addSimpleBehaviourEventToOntology(ptrn.getIDNoNS(), ptrn.getCatNoNS(), ptrn.getContentMap(),
									ptrn.serialize());
						}
					});
				}

				else if (ptrn.getCatNoNS().toLowerCase().equals("proposition")) {
					ptrn.getContentMap().keySet().forEach(key -> {
						if (key.getFullName().contains("Component")) {
							addModePropositionToOntology(ptrn.getIDNoNS(), ptrn.getCatNoNS(), ptrn.getContentMap(),
									ptrn.serialize());
						} else if (key.getFullName().contains("Logic-Expression")) {
							addLogicExpressionPropositionToOntology(ptrn.getIDNoNS(), ptrn.getCatNoNS(),
									ptrn.getContentMap(), ptrn.serialize());
						}
					});
				}

				else if (ptrn.getCatNoNS().toLowerCase().equals("mode-change"))
					addModeChangeToOntology(ptrn.getIDNoNS(), ptrn.getCatNoNS(), ptrn.getContentMap(),
							ptrn.serialize());

				else if (ptrn.getCatNoNS().toLowerCase().equals("prop-change"))
					addPropertyChangeToOntology(ptrn.getIDNoNS(), ptrn.getCatNoNS(), ptrn.getContentMap(),
							ptrn.serialize());

				else if (ptrn.getCatNoNS().toLowerCase().equals("port"))
					addPortToOntology(ptrn.getIDNoNS(), ptrn.getCatNoNS(), ptrn.serialize());

				else if (ptrn.getCatNoNS().toLowerCase().equals("interaction"))
					addInteractionToOntology(ptrn.getIDNoNS(), ptrn.getCatNoNS(), ptrn.getContentMap(),
							ptrn.serialize());

				else if (ptrn.getCatNoNS().toLowerCase().equals("logic-expression"))
					addLogicExpressionToOntology(ptrn.getIDNoNS(), ptrn.getCatNoNS(), ptrn.serialize());

				else if (ptrn.getCatNoNS().toLowerCase().equals("mode"))
					addModeToOntology(ptrn.getIDNoNS(), ptrn.getCatNoNS(), ptrn.serialize());

				else if (ptrn.getCatNoNS().toLowerCase().equals("component"))
					addComponentToOntology(ptrn.getIDNoNS(), ptrn.getCatNoNS(), ptrn.serialize());

				else if (ptrn.getCatNoNS().toLowerCase().equals("evaluation"))
					addEvaluationToOntology(ptrn.getIDNoNS(), ptrn.getCatNoNS(), ptrn.getContentMap(),
							ptrn.serialize());

				else if (ptrn.getCatNoNS().toLowerCase().equals("interval"))
					addIntervalToOntology(ptrn.getIDNoNS(), ptrn.getCatNoNS(), ptrn.getContentMap(), ptrn.serialize());

				else if (ptrn.getCatNoNS().toLowerCase().equals("time-interval"))
					addTimeIntervalToOntology(ptrn.getIDNoNS(), ptrn.getCatNoNS(), ptrn.getContentMap(),
							ptrn.serialize());
				else if (ptrn.getCatNoNS().toLowerCase().equals("port-list"))
					addPortListToOntology(ptrn.getIDNoNS(), ptrn.getCatNoNS(), ptrn.getContentMap(), ptrn.serialize());
				else if (ptrn.getCatNoNS().toLowerCase().equals("mode-list"))
					addModeListToOntology(ptrn.getIDNoNS(), ptrn.getCatNoNS(), ptrn.getContentMap(), ptrn.serialize());
				else if (ptrn.getCatNoNS().toLowerCase().equals("interaction-list"))
					addInteractionListToOntology(ptrn.getIDNoNS(), ptrn.getCatNoNS(), ptrn.getContentMap(),
							ptrn.serialize());
			});
		}
	}

	public static void addPropertyToOntology(String patternName, String prpName, String prpCategory, String Status,
			ObservableList<RequirementModel> isDerivedBy, String architecture) throws IOException {

		/*
		 * String patternURI = "http://lpis.csd.auth.gr/ontologies/2015/PRP#";
		 * Resource Pattern = prpModel.createResource(patternURI + patternName);
		 * if (prpCategory.contains("Functional")) { Resource X3 = model
		 * .createResource("http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#" +
		 * "FunctionalPattern"); prpModel.add(Pattern, RDF.type, X3); } else if
		 * (prpCategory.contains("Mode")) { Resource X3 = model.createResource(
		 * "http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#" + "ModePattern");
		 * prpModel.add(Pattern, RDF.type, X3); }
		 */
		String PropertyURI = "http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#";
		Resource prp = prpModel.createResource(PropertyURI + prpName);

		Resource X3 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#" + prpCategory);
		prpModel.add(prp, RDF.type, X3);
		// System.out.println("PRP category:::::::::" + prpCategory);

		Property p3 = prpModel
				.createProperty("http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#" + "hasPropertyPattern");
		Resource X4 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + patternName);
		prpModel.add(prp, p3, X4);

		Property p1 = prpModel.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasStatus");
		Resource X1 = prpModel.createResource("http://www.w3.org/2001/XMLSchema#string" + Status.replace(" ", "_"));
		prpModel.add(prp, p1, X1);

		if (!isDerivedBy.isEmpty()) {
			isDerivedBy.forEach((reqMdl) -> {
				Property P2 = prpModel
						.createProperty("http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#" + "isDerivedBy");
				Resource X2 = prpModel
						.createResource("http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#" + reqMdl.getReqIDNoNS());
				prpModel.add(prp, P2, X2);
			});
		}

		if (architecture != null && !architecture.isEmpty()) {
			prpModel.add(prp, RDFS.comment, architecture);
		}

		// Relate the property to a pattern.

	}

	public static void addArchitectureToOntology(String archName, String archCategory,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm) {

		String PatternURI = "http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#";
		Resource prp = prpModel.createResource(PatternURI + archName);

		Resource X3 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + archCategory);
		prpModel.add(prp, RDF.type, X3).add(prp, RDFS.comment, serializedForm);

		if (archCategory.contains("md1") || archCategory.contains("md2")) {
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("component")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model.createProperty(
								"http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "isRelatedToComponent");
						prpModel.add(prp, P3, X4);
					}
				} else if (key.getFullName().contains("mode")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasMode");
						prpModel.add(prp, P3, X4);
					}

				} else if (key.getFullName().contains("mode-list")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasModeList");
						prpModel.add(prp, P3, X4);
					}

				}
			}
		} else if (archCategory.contains("md3")) {
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("interaction")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasInteraction");
						prpModel.add(prp, P3, X4);
					}
				} else if (key.getFullName().contains("mode-list")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasModeList");
						prpModel.add(prp, P3, X4);
					}

				}
			}
		} else if (archCategory.contains("md4")) {
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("interaction")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasInteraction");
						prpModel.add(prp, P3, X4);
					}
				} else if (key.getFullName().contains("mode")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasMode");
						prpModel.add(prp, P3, X4);
					}

				}
			}
		} else if (archCategory.contains("mx1")) {
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("component")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model.createProperty(
								"http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "isRelatedToComponent");
						prpModel.add(prp, P3, X4);
					}
				}
			}
		} else if (archCategory.contains("mx2")) {
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("port")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasPort");
						prpModel.add(prp, P3, X4);
					}
				}
			}
		} else if (archCategory.contains("bm1") || archCategory.contains("prm1") || archCategory.contains("af1")) {
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("interaction-list")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasInteractionList");
						prpModel.add(prp, P3, X4);
					}
				}
			}
		} else if (archCategory.contains("cs2") || archCategory.contains("afa2")) {
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("interaction")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasInteractiont");
						prpModel.add(prp, P3, X4);
					}
				} else if (key.getFullName().contains("interaction-list")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasInteractionList");
						prpModel.add(prp, P3, X4);
					}
				}
			}
		} else if (archCategory.contains("fm1") || archCategory.contains("af2")) {
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("interaction")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasInteractiont");
						prpModel.add(prp, P3, X4);
					}
				}
			}
		} else if (archCategory.contains("cs1")) {
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("component")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model.createProperty(
								"http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "isRelatedToComponent");
						prpModel.add(prp, P3, X4);
					}
				} else if (key.getFullName().contains("interaction-list")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasInteractionList");
						prpModel.add(prp, P3, X4);
					}
				}
			}
		} else if (archCategory.contains("prm2")) {
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().contains("behaviour")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasBehaviour");
						prpModel.add(prp, P3, X4);
					}
				}
			}
		}
	}

	/*
	 * public static ArrayList<OntologyWordModel> getArchitectureInstances() {
	 * ArrayList<OntologyWordModel> results = new ArrayList<>();
	 * 
	 * String queryString =
	 * " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> " +
	 * "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> " +
	 * "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  " +
	 * "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  " +
	 * "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> " +
	 * "prefix owl: <http://www.w3.org/2002/07/owl#> " +
	 * "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
	 * "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
	 * "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " +
	 * "SELECT DISTINCT  ?x " + "WHERE { " + "?x rdf:type ?y ." +
	 * "?y rdfs:subClassOf* CSSP:Architecture . }" + "ORDER BY ?x ";
	 * 
	 * FileManager.get().addLocatorClassLoader(Main.class.getClassLoader()); //
	 * Creation of the model of the cssp ontology.
	 * 
	 * // The union of the 2 pre-defined models. In this way jena can see the //
	 * classes that are declared in RBLP.ttl and then imported to CSSP.ttl.
	 * Model model3 = prpModel.union(prpModel);
	 * 
	 * // Creation of a query object. Query query =
	 * QueryFactory.create(queryString); // Execution of the query.
	 * QueryExecution qexec = QueryExecutionFactory.create(query, model3);
	 * 
	 * try { ResultSet result = qexec.execSelect(); // As long as there are more
	 * results... while (result.hasNext()) { String arch =
	 * result.nextSolution().toString(); arch = arch.substring(arch.indexOf("#")
	 * + 1); arch = arch.substring(0, arch.indexOf(">")); String namespace =
	 * "CSSP:"; // Addition of the strings (classes) to the array list.
	 * results.add(new OntologyWordModel(namespace, arch)); }
	 * 
	 * } finally { qexec.close(); }
	 * 
	 * return results; }
	 */

	public static void removePropertyFromOntology(String prpName) {
		System.out.println("Removing " + prpName + " from ontology");

		ArrayList<OntologyWordModel> pattern = new ArrayList<OntologyWordModel>();

		for (int i = 0; i < pattern.size(); i++) {
			removePatternFromOntology(pattern.get(i).getShortName());
		}

		String propURI = "http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#";
		Resource Property = prpModel.createResource(propURI + prpName);
		Property.removeProperties();

	}

	public static void removePatternFromOntology(String ptrnName) {
		// System.out.println("Removing " + ptrnName + " from ontology");

		String ptrnURI = "http://lpis.csd.auth.gr/ontologies/2015/PRP#";
		Resource Pattern = prpModel.createResource(ptrnURI + ptrnName);
		Pattern.removeProperties();

	}

	public static void addPatternToOntology(String patternName, String patternCategory,
			HashMap<OntologyWordModel, ArrayList<String>> contentMap, String serializedForm) {
		String PatternURI = "http://lpis.csd.auth.gr/ontologies/2015/PRP#";
		Resource prp = prpModel.createResource(PatternURI + patternName);
		Resource X3 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + patternCategory);
		prpModel.add(prp, RDF.type, X3);
		// System.out.println(patternCategory);

		// for (OntologyWordModel key : contentMap.keySet()) {
		// ArrayList<String> list = contentMap.get(key);
		// for (int i = 0; i < list.size(); i++) {
		// Property p3 =
		// prpModel.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#"
		// + "hasBehaviour");
		// Resource X4 =
		// prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#"
		// + list.get(i));
		// prpModel.add(prp, p3, X4);
		// }
		// }

		if (patternCategory.toLowerCase().contains("md1") || patternCategory.toLowerCase().contains("md2")) {
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().toLowerCase().contains("component")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model.createProperty(
								"http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "isRelatedToComponent");
						prpModel.add(prp, P3, X4);
					}
				} else if (key.getFullName().toLowerCase().contains("mode")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasMode");
						prpModel.add(prp, P3, X4);
					}

				} else if (key.getFullName().toLowerCase().contains("modelist")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasModeList");
						prpModel.add(prp, P3, X4);
					}

				}
			}
		} else if (patternCategory.toLowerCase().contains("prm2") || patternCategory.toLowerCase().contains("f1")
				|| patternCategory.toLowerCase().contains("f2")) {

			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().toLowerCase().contains("beh")) {

					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasBehaviour");
						prpModel.add(prp, P3, X4);
					}
				}
			}
		} else if (patternCategory.toLowerCase().contains("md3")) {
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().toLowerCase().contains("interaction")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasInteraction");
						prpModel.add(prp, P3, X4);
					}
				} else if (key.getFullName().toLowerCase().contains("modelist")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasModeList");
						prpModel.add(prp, P3, X4);
					}

				}
			}
		} else if (patternCategory.toLowerCase().contains("md4")) {
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().toLowerCase().contains("interaction")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasInteraction");
						prpModel.add(prp, P3, X4);
					}
				} else if (key.getFullName().toLowerCase().contains("mode")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasMode");
						prpModel.add(prp, P3, X4);
					}

				}
			}
		} else if (patternCategory.toLowerCase().contains("mx1")) {
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().toLowerCase().contains("component")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model.createProperty(
								"http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "isRelatedToComponent");
						prpModel.add(prp, P3, X4);
					}
				}
			}
		} else if (patternCategory.toLowerCase().contains("mx2")) {
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().toLowerCase().contains("port")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasPort");
						prpModel.add(prp, P3, X4);
					}
				}
			}
		} else if (patternCategory.toLowerCase().contains("bm1") || patternCategory.toLowerCase().contains("prm1")
				|| patternCategory.toLowerCase().contains("af1")) {
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().toLowerCase().contains("interactionlist")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasInteractionList");
						prpModel.add(prp, P3, X4);
					}
				}
			}
		} else if (patternCategory.toLowerCase().contains("cs2") || patternCategory.toLowerCase().contains("afa2")) {
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().toLowerCase().contains("interaction")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasInteractiont");
						prpModel.add(prp, P3, X4);
					}
				} else if (key.getFullName().toLowerCase().contains("interactionlist")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasInteractionList");
						prpModel.add(prp, P3, X4);
					}
				}
			}
		} else if (patternCategory.toLowerCase().contains("fm1") || patternCategory.toLowerCase().contains("af2")) {
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().toLowerCase().contains("interaction")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasInteractiont");
						prpModel.add(prp, P3, X4);
					}
				}
			}
		} else if (patternCategory.toLowerCase().contains("cs1")) {
			for (OntologyWordModel key : contentMap.keySet()) {
				// System.out.println(key.getFullName());
				ArrayList<String> list = contentMap.get(key);
				if (key.getFullName().toLowerCase().contains("component")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model.createProperty(
								"http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "isRelatedToComponent");
						prpModel.add(prp, P3, X4);
					}
				} else if (key.getFullName().toLowerCase().contains("interactionlist")) {
					for (int i = 0; i < list.size(); i++) {
						Resource X4 = model
								.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + list.get(i));
						Property P3 = model
								.createProperty("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + "hasInteractionList");
						prpModel.add(prp, P3, X4);
					}
				}
			}
		}

		Resource X4 = prpModel.createResource("http://lpis.csd.auth.gr/ontologies/2015/PRP#" + serializedForm);
		prpModel.add(prp, RDFS.comment, serializedForm);

	}

	public static HashMap<String, String> getPatternInstances() {
		HashMap<String, String> blps = new HashMap<String, String>();

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { " + "?x rdf:type ?y . "
				+ "	    ?y  rdfs:subClassOf* PRP:PropertyPattern . }";

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.

		// model.write(System.out);

		// The union of the 2 pre-defined models. In this way jena can see the
		// classes that are declared in RBLP.ttl and then imported to CSSP.ttl.
		Model model3 = prpModel.union(csspModel);

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model3);
		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {

				String req = result.nextSolution().toString();
				// Parsing of the string
				// System.out.println(req);
				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				String namespace = "PRP:";
				String pattern = namespace + req;

				String serialized = getPropertySerialized(pattern);
				blps.put(pattern, serialized);
			}

		} finally {
			qexec.close();
		}

		return blps;
	}

	private static String getPropertySerialized(String prpID) {
		if (prpID.isEmpty())
			return null;
		String serialized = null;
		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { ";
		queryString += prpID;
		String queryString2 = " rdfs:comment ?x . }";
		queryString += queryString2;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.

		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, prpModel);

		try {

			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();
				// Parsing of the string
				req = req.substring(req.indexOf("\"") + 1);
				req = req.substring(0, req.indexOf("\""));
				String namespace = "";
				// Addition of the strings (classes) to the array list.
				// results.add(new OntologyWordModel(namespace, req));
				serialized = req;
			}

		} finally {
			qexec.close();
		}
		return serialized;
	}

	private static ArrayList<OntologyWordModel> getPatternsForProperty(String property) {
		ArrayList<OntologyWordModel> results = new ArrayList<>();
		if (property.isEmpty())
			return results;

		// Query string without the CSSP:Requirement.
		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { ";

		String queryString2 = "  CSSP:hasPropertyPattern  ?x . " + "}";

		queryString += property;
		queryString += queryString2;

		// System.out.println(queryString);

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.
		// Loaded from field

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, prpModel);

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {

				String req = result.nextSolution().toString();
				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				String namespace = "PRP:";
				results.add(new OntologyWordModel(namespace, req));
			}

		} finally {
			qexec.close();
		}

		return results;
	}

	private static OntologyWordModel getPropertyType(String prpID) {
		OntologyWordModel type = new OntologyWordModel("CSSP", "Property");

		if (prpID.isEmpty())
			return type;

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP.ttl#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { ";
		queryString += prpID;
		String queryString2 = " rdf:type ?x . }";
		queryString += queryString2;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.

		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, prpModel);

		try {

			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();
				// Parsing of the string
				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				String namespace = "CSSP:";
				// System.out.println(req);
				// Addition of the strings (classes) to the array list.
				type = new OntologyWordModel(namespace, req);
			}

		} finally {
			qexec.close();
		}
		return type;
	}

	private static String getPropertyStatus(String prp) {
		if (prp.isEmpty())
			return null;
		String Status = null;

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP.ttl#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { ";
		queryString += prp;
		String queryString2 = " PRP:hasStatus ?x . }";
		queryString += queryString2;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.

		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, prpModel);

		try {

			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();
				// Parsing of the string
				req = req.replace("( ?x = xsd:string", "");
				req = req.substring(0, req.lastIndexOf(")")).trim();
				String namespace = "";

				Status = req.replace("_", " ");
				// Addition of the strings (classes) to the array list.

			}

		} finally {
			qexec.close();
		}
		return Status;
	}

	public static ArrayList<OntologyWordModel> getPropertyInstances() {
		ArrayList<OntologyWordModel> results = new ArrayList<>();

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT DISTINCT  ?x " + "WHERE { "
				+ "?x rdf:type ?y ." + "?y rdfs:subClassOf* CSSP:Property . }" + "ORDER BY ?x ";

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.

		// The union of the 2 pre-defined models. In this way jena can see the
		// classes that are declared in RBLP.ttl and then imported to CSSP.ttl.
		Model model3 = prpModel.union(csspModel);

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model3);

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String prp = result.nextSolution().toString();
				prp = prp.substring(prp.indexOf("#") + 1);
				prp = prp.substring(0, prp.indexOf(">"));
				String namespace = "CSSP:";
				// Addition of the strings (classes) to the array list.
				results.add(new OntologyWordModel(namespace, prp));
			}

		} finally {
			qexec.close();
		}

		return results;
	}

	public static ArrayList<String> loadPtrnsComplex() {
		ArrayList<String> complex = new ArrayList<String>();

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?e " + "WHERE { "
				+ " ?x rdfs:subClassOf+ PRP:Attribute . " + " ?e rdf:type ?x . }";

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.

		// model.write(System.out);

		// The union of the 2 pre-defined models. In this way jena can see the
		// classes that are declared in RBLP.ttl and then imported to CSSP.ttl.
		Model model3 = model.union(prpModel);

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model3);
		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();
				// Parsing of the string
				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				// System.out.println(req);
				String namespace = "PRP:";
				String pattern = namespace + req;
				String serialized = getPropertySerialized(pattern);
				if (serialized != null && !serialized.isEmpty())
					complex.add(serialized);
				else
					logger.log(Level.INFO, "Serialized form of " + pattern + " was not found in ontology");

			}
		} finally {
			qexec.close();
		}

		return complex;

	}

	private static ArrayList<RequirementModel> getisRefinedByForProperty(String prop) {

		ArrayList<RequirementModel> results = new ArrayList<>();

		ArrayList<String> resultStrings = new ArrayList<>();
		if (prop == null || prop.isEmpty())
			return results;

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP.ttl#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { ";
		queryString += prop;
		String queryString2 = " CSSP:isRefinedByProp ?x . }";
		queryString += queryString2;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.

		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		Model model3 = model.union(prpModel);
		QueryExecution qexec = QueryExecutionFactory.create(query, model3);

		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {
				String req = result.nextSolution().toString();
				// Parsing of the string
				req = req.substring(req.indexOf("#") + 1);
				req = req.substring(0, req.indexOf(">"));
				String namespace = "CSSP:";
				// System.out.println(req);
				// Addition of the strings (classes) to the array list.
				// results.add(new OntologyWordModel(namespace, req));
				resultStrings.add(namespace + req);
			}
		} finally {
			qexec.close();
		}

		// Do your query here, store the results in resultStrings

		// Like getisRefinedByForRequirement(), but after you have your
		// resultStrings
		// You need to do the following
		for (String reqString : resultStrings) {
			if (reqIDtoObject.containsKey(reqString))
				results.add(reqIDtoObject.get(reqString));
		}

		return results;
	}

	private static Architecture getArchitectureForProperty(String prop) {
		if (prop.isEmpty())
			return null;
		String serialized = null;
		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { ";
		queryString += prop;
		String queryString2 = " rdfs:comment ?x . }";
		queryString += queryString2;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());
		// Creation of the model of the cssp ontology.

		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		Model model3 = model.union(prpModel);
		QueryExecution qexec = QueryExecutionFactory.create(query, model3);

		Architecture newArch;
		try {

			ResultSet result = qexec.execSelect();
			// As long as there are more results...

			String req = result.nextSolution().toString();
			// Parsing of the string
			req = req.substring(req.indexOf("\"") + 1);
			req = req.substring(0, req.indexOf("\""));
			newArch = new Architecture(req);
		} catch (NoSuchElementException e) {
			newArch = null;

		} finally {
			qexec.close();
		}

		return newArch;

		// else just return null
	}

	/**
	 * Returns all top level classes in the ontology
	 * 
	 * @return
	 */
	public static ArrayList<OntologyWordModel> getClassesTopLevel() {

		ArrayList<OntologyWordModel> results = new ArrayList<OntologyWordModel>();

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x "
				+ "WHERE { ?x rdfs:subClassOf owl:Thing . }";

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());

		Model model = csspModel;
		Model model2 = rblpModel;
		Model model3 = prpModel;

		Model model4 = model.union(model2);
		Model model5 = model4.union(model3);

		// System.out.println(queryString);
		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.

		QueryExecution qexec = QueryExecutionFactory.create(query, model5);
		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {

				String req = result.nextSolution().toString();

				req = req.replaceFirst(".*\\/", "").replaceFirst("> \\)", "").replaceFirst("#", ":")
						.replaceFirst(".ttl:", ":");

				results.add(new OntologyWordModel(req));
			}

		} finally {
			qexec.close();
		}
		return results;
	}

	/**
	 * Returns all classes from all levels in the ontology
	 * 
	 * @return
	 */
	public static ArrayList<OntologyWordModel> getClassesFlat() {
		ArrayList<OntologyWordModel> results = new ArrayList<OntologyWordModel>();

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x "
				+ "WHERE { ?x rdfs:subClassOf+ owl:Thing . }";

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());

		Model model = csspModel;
		Model model2 = rblpModel;
		Model model3 = prpModel;

		Model model4 = model.union(model2);
		Model model5 = model4.union(model3);

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model5);
		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {

				String req = result.nextSolution().toString();
				// Parsing of the string
				req = req.replaceFirst(".*\\/", "").replaceFirst("> \\)", "").replaceFirst("#", ":")
						.replaceFirst(".ttl:", ":");

				results.add(new OntologyWordModel(req));
			}

		} finally {
			qexec.close();
		}
		return results;
	}

	/**
	 * Returns the comment field (or description field if we have one?) for the
	 * given class
	 * 
	 * @return
	 */
	public static ArrayList<String> getDescriptionForClass(String className) {

		ArrayList<String> results = new ArrayList<String>();

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { ";
		queryString += className;
		String queryString2 = " rdfs:comment ?x . }";
		queryString += queryString2;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());

		Model model = csspModel;
		Model model2 = rblpModel;
		Model model3 = prpModel;

		Model model4 = model.union(model2);
		Model model5 = model4.union(model3);

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model5);
		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {

				String req = result.nextSolution().toString();
				// Parsing of the string
				// System.out.println(req);
				req = req.substring(req.indexOf("\"") + 1);
				req = req.substring(0, req.indexOf("\""));
				results.add(req);
			}

		} finally {
			qexec.close();
		}
		return results;
	}

	/**
	 * Returns all classes that are parents of the given class
	 * 
	 * @return
	 */
	public static ArrayList<OntologyWordModel> getParentClassesForClass(String className) {
		ArrayList<OntologyWordModel> results = new ArrayList<OntologyWordModel>();

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x "
				+ "WHERE { ?x rdf:type owl:Class . ";

		String queryString2 = className + " rdfs:subClassOf+ ?x . }";

		queryString = queryString + queryString2;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());

		Model model = csspModel;
		Model model2 = rblpModel;
		Model model3 = prpModel;

		Model model4 = model.union(model2);
		Model model5 = model4.union(model3);

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model5);
		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {

				String req = result.nextSolution().toString();
				req = req.replaceFirst(".*\\/", "").replaceFirst("> \\)", "").replaceFirst("#", ":")
						.replaceFirst(".ttl:", ":");
				results.add(new OntologyWordModel(req));
			}

		} finally {
			qexec.close();
		}
		return results;
	}

	/**
	 * Returns all classes that are children of the given class
	 * 
	 * @return
	 */
	public static ArrayList<OntologyWordModel> getChildClassesForClass(String className) {
		ArrayList<OntologyWordModel> results = new ArrayList<OntologyWordModel>();

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { ?x rdfs:subClassOf+ ";

		String queryString2 = className + " . }";
		queryString = queryString + queryString2;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());

		Model model = csspModel;
		Model model2 = rblpModel;
		Model model3 = prpModel;

		Model model4 = model.union(model2);
		Model model5 = model4.union(model3);

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model5);
		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {

				String req = result.nextSolution().toString();
				req = req.replaceFirst(".*\\/", "").replaceFirst("> \\)", "").replaceFirst("#", ":")
						.replaceFirst(".ttl:", ":");
				results.add(new OntologyWordModel(req));
			}

		} finally {
			qexec.close();
		}
		return results;
	}

	/**
	 * Returns all instances of the given class
	 * 
	 * @return
	 */
	public static ArrayList<OntologyWordModel> getInstancesForClass(String className) {

		ArrayList<OntologyWordModel> results = new ArrayList<OntologyWordModel>();

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix uomvocab: <http://purl.oclc.org/net/muo/muo#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?x " + "WHERE { " + "?x rdf:type ?y ."
				+ "?y rdfs:subClassOf* ";

		String queryString2 = " . }";

		queryString += className;
		queryString += queryString2;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {

				String req = result.nextSolution().toString();
				req = req.replaceFirst(".*\\/", "").replaceFirst("> \\)", "").replaceFirst("#", ":")
						.replaceFirst(".ttl:", ":");
				results.add(new OntologyWordModel(req));
			}

		} finally {
			qexec.close();
		}
		return results;

	}

	public static ArrayList<OntologyWordModel> getDomainPropertiesForClass(String className) {

		ArrayList<OntologyWordModel> results = new ArrayList<OntologyWordModel>();

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix uomvocab: <http://purl.oclc.org/net/muo/muo#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?property "
				+ "WHERE { ?property rdfs:domain ";

		String queryString2 = " . }";

		queryString += className;
		queryString += queryString2;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {

				String req = result.nextSolution().toString();
				req = req.replaceFirst(".*\\/", "").replaceFirst("> \\)", "").replaceFirst("#", ":")
						.replaceFirst(".ttl:", ":");
				results.add(new OntologyWordModel(req));
			}

		} finally {
			qexec.close();
		}
		return results;

	}

	public static ArrayList<OntologyWordModel> getRangePropertiesForClass(String className) {

		ArrayList<OntologyWordModel> results = new ArrayList<OntologyWordModel>();

		String queryString = " prefix CSSP: <http://lpis.csd.auth.gr/ontologies/2015/CSSP.ttl#> "
				+ "prefix OoSSA: <http://lpis.csd.auth.gr/ontologies/2015/OoSSA.ttl#> "
				+ "prefix RBLP: <http://lpis.csd.auth.gr/ontologies/2015/RBLP#>  "
				+ "prefix DSO: <http://lpis.csd.auth.gr/ontologies/2015/DSO.ttl#>  "
				+ "prefix PRP: <http://lpis.csd.auth.gr/ontologies/2015/PRP#> "
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix uomvocab: <http://purl.oclc.org/net/muo/muo#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + "SELECT ?property "
				+ "WHERE { ?property rdfs:range ";

		String queryString2 = " . }";

		queryString += className;
		queryString += queryString2;

		FileManager.get().addLocatorClassLoader(Main.class.getClassLoader());

		// Creation of a query object.
		Query query = QueryFactory.create(queryString);
		// Execution of the query.
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		try {
			ResultSet result = qexec.execSelect();
			// As long as there are more results...
			while (result.hasNext()) {

				String req = result.nextSolution().toString();
				req = req.replaceFirst(".*\\/", "").replaceFirst("> \\)", "").replaceFirst("#", ":")
						.replaceFirst(".ttl:", ":");
				results.add(new OntologyWordModel(req));
			}

		} finally {
			qexec.close();
		}
		return results;

	}

	public static void addAnyInstance(String className, String classType, String serializedForm)
			throws FileNotFoundException {
		String URI = "http://lpis.csd.auth.gr/ontologies/2015/DSO#";
		Resource instance = model.createResource(URI + className);
		Resource R1 = model.createResource("http://lpis.csd.auth.gr/ontologies/2015/RBLP#" + classType);
		model.add(instance, RDF.type, R1).add(instance, RDFS.comment, serializedForm);
	}
}
