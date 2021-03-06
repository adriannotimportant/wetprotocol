package ont;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.swing.JComboBox;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;

import resources.ResourceFinding;
import ui.property.WrappedOntResource;
import ui.stepchooser.ClassAndIndividualName;
import uimain.WetProtocolMainPanel;
import uiutil.UiUtils;

public class OntManager {
	private static OntManager instance;
	private static OntModel ontologyModel;
	public static final String PROTOCOL_FILE = "WetProtocolEmptyFromWet.owl";
	// it needs to be saved and reloaded in Jena to show the proper class
	// public static final String PROTOCOL_FILE = "WetProtocolWithBasicProvisions.owl";
	public static String ONTOLOGY_LOCATION = new File(ResourceFinding.getOntFileDir(), PROTOCOL_FILE).getAbsolutePath();// this will be in the bin directory because that is where the class is
	public static final String NS = "http://www.wet.protocol#";// namespace and #
	public static OntProperty PREEXISTING;
	public static OntProperty COUNTER_PROPERTY;
	private static Individual TOP_STEPS_INSTANCE;
	private static OntProperty STEP_COORDINATES_PROPERTY;
	private static OntClass STEP_ONT_CLASS;
	// public static Resource NOTHING_SUBCLASS;
	public static AtomicInteger counter = new AtomicInteger(0); // todo make sure the counter value is saved to file and loaded when file is loaded
	private static String fileName;
	final static String NAMED_INDIVIDUAL_LINE = "<rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#NamedIndividual\"/>";
	// there still seem to be a problem with NamedIndividual
	//

	public static final OntManager getInstance() {
		if (instance == null) {
			instance = loadModelFromFileAndResetOntManager(ONTOLOGY_LOCATION);
		}
		return instance;
	}

	public static final OntManager loadModelFromFileAndResetOntManager(String pathOfOntFileToLoad) {
		instance = null;
		instance = new OntManager();
		ontologyModel = null;
		ontologyModel = ModelFactory.createOntologyModel(); // OntModelSpec.OWL_LITE_MEM);//OntModelSpec.OWL_MEM);// OntModelSpec.OWL_LITE_MEM);// "" isfull? hierarchy reasoner; OWL_MEM
		Reader ontFileReader = stripNamedIndividual(pathOfOntFileToLoad);
		ontologyModel.read(ontFileReader, "RDF/XML");
		//
		// ontologyModel.setStrictMode(true);
		System.out.println("after loding the ontology the individuals are:" + OntManager.getOntologyModel().listIndividuals().toList());
		PREEXISTING = ontologyModel.getOntProperty(NS + "preexisting");
		COUNTER_PROPERTY = ontologyModel.getOntProperty(NS + "protocolCounter");
		// NOTHING_SUBCLASS = ontologyModel.getOntClass("owl:Nothing");
		TOP_STEPS_INSTANCE = ontologyModel.getIndividual(NS + "topProtocolInstance");
		counter.set(TOP_STEPS_INSTANCE.getPropertyValue(COUNTER_PROPERTY).asLiteral().getInt());
		STEP_ONT_CLASS = OntManager.getOntClass("Step");
		//
		STEP_COORDINATES_PROPERTY = (ontologyModel.getOntProperty(NS + "stepCoordinatesProperty"));
		return instance;
	}

	/** This is needed as every time we use Proteje it seems to change the types to NamedIndividual instead of the correct type which is in the opening tag of the element.
	 * To correct that we remove all the rdf:NamedIndividual so the parser takes the type information from the openeing tag.
	 * kind of old style but maybe working */
	public static StringReader stripNamedIndividual(String pathOfOntFileToLoad) {
		try {
			File inputFile = new File(pathOfOntFileToLoad);
			StringWriter cleaned = new StringWriter();
			Files.lines(inputFile.toPath()).filter(line -> !line.contains(NAMED_INDIVIDUAL_LINE)).forEach(goodLine->cleaned.append(goodLine));
			//System.out.println("cleaned:"+cleaned.toString());
			cleaned.flush();
			cleaned.close();
			//System.out.println("cleaned:"+cleaned.toString());
			return new StringReader(cleaned.toString());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Set<OntProperty> calculateHierarchicalPropertiesForAClass(OntClass ontClass) {
		Set<OntProperty> collected = ontologyModel.listAllOntProperties().toSet().stream().filter(dataTypeProperty -> {
			return dataTypeProperty.hasDomain(ontClass);
		}).collect(Collectors.toSet());
		ontClass.listSuperClasses(true).toSet().forEach(superClass -> {
			calculatePropertiesForClass(superClass, collected);
		});
		collected.forEach(System.out::println);
		return collected;
	}

	public List<Individual> calculateStepIndividuals() {
		// the top protocol is already added so we can filter out
		List<Individual> allIndividuals = ontologyModel.listIndividuals().toList();
		List<Individual> allSteps = Lists.newArrayList();
		for (Individual ind : allIndividuals) {
			if (ind.getOntClass().hasSuperClass(STEP_ONT_CLASS)) {
				allSteps.add(ind);
				//System.out.println("found step individual:" + ind);
			}
		}
		return allSteps;
	}

	// will load in the combo box all existing individuals of the given class
	// TODO I think this one should be done like the calculateStepIndividuals
	public static void loadPossibleIndividualValues(JComboBox<WrappedOntResource<?>> individualOrClassChooser, OntClass ontClass) {
		Set<Individual> individualsSet = getOntologyModel().listIndividuals(ontClass).toSet();
		for (Individual individual : individualsSet) {
			individualOrClassChooser.addItem(new WrappedOntResource<>(individual));
		}
	}

	public Set<OntClass> getClassesInSignature() {
		// used to populate the add step pop-up
		return ontologyModel.listClasses().toSet();
	}

	public static Individual getTopStepsInstance() {
		return TOP_STEPS_INSTANCE;
	}

	public static Individual createStepIndividual(ClassAndIndividualName classAndIndividualName) {
		Individual createdIndividual = createIndividual(classAndIndividualName.getName() + counter.incrementAndGet(), classAndIndividualName.getOntClass());
		if (createdIndividual == null) {
			UiUtils.showDialog(null, "Error: createdIndividual == null");
		}
		createdIndividual.addLabel("Label:" + createdIndividual.getLocalName(), null);
		return createdIndividual;
	}

	public static Individual createNewIndividualOfSelectedClass(OntClass ontClass, String prefix) {
		Individual createdIndividual = createIndividual(prefix + counter.incrementAndGet(), ontClass);
		if (createdIndividual == null) {
			UiUtils.showDialog(null, "Error: createdIndividual == null");
		}
		createdIndividual.addLabel("Label:" + createdIndividual.getLocalName(), null);
		return createdIndividual;
	}

	public static Individual createLeafIndividual(OntClass ontClass, String prefix) {// for the leaf individuals
		Individual createdIndividual = createIndividual(prefix + counter.incrementAndGet() + "_ofClass_" + ontClass.getLocalName(), ontClass);
		if (createdIndividual == null) {
			UiUtils.showDialog(null, "Error: createdIndividual == null");
		}
		createdIndividual.addLabel("Label:" + createdIndividual.getLocalName(), null);
		return createdIndividual;
	}

	/** base one */
	private static Individual createIndividual(String instanceName, OntClass ontClass) {
		Individual createdIndividual = OntManager.getOntologyModel().createIndividual(NS + instanceName, ontClass);
		return createdIndividual;
	}
//** unnecessary
//	public static Individual createClonedStepIndividual(ClassAndIndividualName classAndIndividualName) {
//		Individual createdIndividual = createIndividual(classAndIndividualName.getName() + counter.incrementAndGet()+ "cloned", classAndIndividualName.getOntClass());
//		if (createdIndividual == null) {
//			UiUtils.showDialog(null, "Error: createdIndividual == null");
//		}
//		createdIndividual.addLabel("Label:" + createdIndividual.getLocalName(), null);
//		return createdIndividual;
//	}
//	
	
	public static Individual createClonedStepIndividual( String IndividualName, OntClass ontClass) {
		Individual createdIndividual = createIndividual(IndividualName +  "cloned", ontClass);
		if (createdIndividual == null) {
			UiUtils.showDialog(null, "Error: createdIndividual == null");
		}
		createdIndividual.addLabel("Label:" + createdIndividual.getLocalName(), null);
		return createdIndividual;
	}
	
	public static OntClass getOntClass(String clazz) {
		return OntManager.getOntologyModel().getOntClass(NS + clazz);
	}

	public void dumpPropertiesAndValuesInIndividual(Individual individual) {
		Set<Statement> objectProperties = individual.listProperties().toSet();
		// show the properties of this individual
		System.out.println("dumpProperties and values for individual:" + individual);
		objectProperties.forEach(prop -> {
			System.out.print("    " + prop.getPredicate().getLocalName() + " -> ");
			if (prop.getObject().isLiteral()) {
				System.out.println("Literal " + prop.getLiteral().getLexicalForm());
			} else if (prop.getObject().isAnon()) {
				System.out.println("Anon " + prop.getObject());
			} else if (prop.getObject().isResource()) {
				System.out.println("Resource " + prop.getObject());
			} else if (prop.getObject().isURIResource()) {
				System.out.println("URIResource " + prop.getObject());
			} else {
				System.out.println("No Literal Anon Resource etc" + prop.getObject());
			}
		});
		// printSet(instance.getPropertiesInIndividual(tinyValueIndividual));
	}
	// private void dumpPropertiesForAllClasses() {
	// Set<OntClass> classesSet = ontologyModel.listClasses().toSet();
	// final int indent = 1;
	// classesSet.forEach(ontClass -> {
	// dumpAllPropertiesForAClass(ontClass);
	// });
	// }
	// public void dumpAllPropertiesForAClass(OntClass ontClass) {
	// final int indent = 1;
	// Set<OntProperty> declaredOntProperties = ontClass.listDeclaredProperties().toSet();
	// ontClass.listSuperClasses(true).toSet().forEach(superClass -> {
	// dumpAllDirectPropertiesForAClass(superClass, declaredOntProperties);
	// });
	// declaredOntProperties.forEach(ontProperty -> {
	// System.out.println(String.join("", Collections.nCopies(indent, "\t")) + (ontProperty.isObjectProperty() ? "object property:" : "data property:") + ontProperty.getLocalName() + "<" + ontProperty.getRange() + "> of Type:" + ontProperty.getRDFType());
	// });
	// }

	// public void dumpAllDirectPropertiesForAClass(OntClass ontClass, Set<OntProperty> declaredOntProperties) {
	// final int indent = 1;
	// System.out.println("class:" + ontClass.getLocalName());
	// declaredOntProperties.addAll(ontClass.listDeclaredProperties().toSet());
	// ontClass.listSuperClasses(true).toSet().forEach(superClass -> {
	// dumpAllDirectPropertiesForAClass(superClass, declaredOntProperties);
	// });
	// System.out.println("class:" + ontClass.getLocalName());
	// }
	public void calculatePropertiesForClass(OntClass ontClass, final Set<OntProperty> collected) {
		// System.out.println("calculated for class:" + ontClass.getLocalName());
		collected.addAll(ontologyModel.listAllOntProperties().toSet().stream().filter(dataTypeProperty -> {
			return dataTypeProperty.hasDomain(ontClass);
		}).collect(Collectors.toSet()));
		ontClass.listSuperClasses(true).toSet().forEach(superClass -> {
			calculatePropertiesForClass(superClass, collected);
		});
	}

	public static Literal createValueAsStringLiteral(String newValue) {
		return OntManager.getOntologyModel().createTypedLiteral(newValue);
	}

	public static void printSet(Set<?> set, int indent) {
		set.forEach(elem -> System.out.println(String.join("", Collections.nCopies(indent, "\t")) + elem));
	}

	public static void printSet(String message, Set<?> set) {
		System.out.println("Printing " + message);
		printSet(set, 0);
	}

	public static void printSet(String message, Set<?> set, int indent) {
		System.out.println(message);
		printSet(set, indent);
	}

	public Individual getProtejeCreatedMicrocetrifugeTube() {
		return ontologyModel.getIndividual(NS + "myProtejeCreatedMicroCentrifugeTube");
	}

	public static boolean isPreexisting(OntProperty ontProperty) {
		return ontProperty.hasSuperProperty(OntManager.PREEXISTING, true);
	}

	public static OntModel getOntologyModel() {
		getInstance();// make sure it's loaded
		return ontologyModel;
	}

	public static boolean isLeafClass(OntClass ontClass) {
		// )!subclassFromRange.hasSubClass() todo should be a faster way but sometimes you get these ghost subclasses
		return ontClass.listSubClasses().toList().isEmpty();
	}

	public static Individual renameNode(Individual resource, String newValue, WetProtocolMainPanel wetProtocolMainPanel, boolean fromSteps) {
		Path tempFile;
		try {
			tempFile = Files.createTempFile("wettempfile", ".tmp");
		} catch (IOException e) {
			UiUtils.showDialog(null, "some issues creating temp ontology file" + e.getLocalizedMessage());
			e.printStackTrace();
			return resource;// did no rename
		}
		File file = tempFile.toFile();
		System.out.println("temporary file created at:" + file.getAbsolutePath());
		file.deleteOnExit();
		Path path = null;
		saveOntologyAndCoordinates(file, wetProtocolMainPanel.getjStepTree());
		try (FileOutputStream output = new FileOutputStream(file)) {
			writeOntModelToDisk(output);
			path = Paths.get(file.getAbsolutePath());
			Charset charset = StandardCharsets.US_ASCII;
			String content = new String(Files.readAllBytes(path), charset);
			content = content.replace(resource.getURI(), resource.getNameSpace() + newValue);
			Files.write(path, content.getBytes(charset));
		} catch (Exception e1) {
			UiUtils.showDialog(null, "some issues writing temp ontology file" + e1.getLocalizedMessage());
		}
		OntManager.loadModelFromFileAndResetOntManager(path.toString());// these 2 lines reset the whole model and UI
		// UiUtils.loadStepsTreeFromModel(topStepNode);
		Individual newIndividual = OntManager.getOntologyModel().getIndividual(NS + newValue);
		if (!fromSteps) {// avoids infinite loop TODO
			wetProtocolMainPanel.initiateOrRefreshTreeModelAndRest();
		}
		System.out.println("Rename returned node:" + newIndividual);
		return newIndividual;// NS + newValue);
	}

	public static OntProperty getStepCoordinatesProperty() {
		return STEP_COORDINATES_PROPERTY;
	}

	public static void saveOntologyAndCoordinates(File file, JTree jStepsTree) {
		Enumeration<?> preorderEnumeration = ((DefaultMutableTreeNode) jStepsTree.getModel().getRoot()).preorderEnumeration();
		int verticalDistance = 0;
		TOP_STEPS_INSTANCE.setPropertyValue(COUNTER_PROPERTY, OntManager.getOntologyModel().createTypedLiteral(counter.get()));// will create as int
		while (preorderEnumeration.hasMoreElements()) {// just add the order as hardcoded value of the ontology node
			DefaultMutableTreeNode defaultMutableTreeNode = (DefaultMutableTreeNode) (preorderEnumeration.nextElement());
			Individual individual = (Individual) (defaultMutableTreeNode.getUserObject());
			individual.addLiteral(OntManager.getStepCoordinatesProperty(), verticalDistance++ + "." + defaultMutableTreeNode.getLevel());
		}
		try (FileOutputStream output = new FileOutputStream(file)) {
			writeOntModelToDisk(output);
		} catch (Exception e1) {
			UiUtils.showDialog(jStepsTree, "Cannot open output file" + e1.getLocalizedMessage());
		}
	}

	private static void writeOntModelToDisk(FileOutputStream output) {
		OntManager.getOntologyModel().write(output, "RDF/XML", null);// OntManager.NS);
	}

	public static void setOwlFileName(String fileName) {
		getInstance().fileName = fileName;
	}

	public static String getOwlFileName() {
		return fileName;
	}
}
// TODO for some reason it seams that only the top protocol is saved with rdf type as </owl:NamedIndividual> but all the other newly created nodes are saved withe the right class and without rdf t