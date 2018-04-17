package ont;

import resources.ResourceFindingDummyClass;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectImpl;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.tree.DefaultMutableTreeNode;

public class OntologyManager {
	private static OntologyManager instance;
	private static OntModel ontologyModel;
	private static String ONTOLOGY_LOCATION = ResourceFindingDummyClass.getResource("AdrianProtocol.owl").getFile();
	public static String NS = "http://www.wet.protocol#";// namespace and #
	private static Individual topProtocolInstance;
	private static PropertyAndIndividual topPropertyANdIndividual;
	//

	public static final OntologyManager getInstance() {
		if (instance == null) {
			instance = new OntologyManager();
			ontologyModel = ModelFactory.createOntologyModel();// full? hierarchy reasoner
			ontologyModel.read(ONTOLOGY_LOCATION);
		}
		return instance;
	}

	public Set<OntClass> getClassesInSignature() {
		// used to populate the add step pop-up
		return ontologyModel.listClasses().toSet();
	}

	public Literal getTypedLiteral(String text) {
		return ontologyModel.createTypedLiteral(text);
	}

	public Individual getTopProtocoInstancel() {
		if (topProtocolInstance == null) {
			OntClass protocolClass = ontologyModel.getOntClass(NS + "Protocol");
			topProtocolInstance = ontologyModel.createIndividual(NS + "TadaMySampleProtocol", protocolClass);
			topProtocolInstance.setPropertyValue(ontologyModel.getOntProperty(NS + "version"), ontologyModel.createTypedLiteral("Version 0.0"));
		}
		return topProtocolInstance;
	}

	public PropertyAndIndividual getTopPropertyAndIndividual() {
		if (topPropertyANdIndividual == null) {
			Individual dummyIndividual = ontologyModel.getIndividual(NS + "dummyIndividual");
			topPropertyANdIndividual = new PropertyAndIndividual(ontologyModel.getOntProperty(NS + "dummyDataProperty"), dummyIndividual);
			// the value will be null
			// no values for properties as this ode will be invisible
		}
		//System.out.println("topPropertyInstance:" + topPropertyInstance);
		return topPropertyANdIndividual;
	}

	public void printStringClassNames() {
		getClassesInSignature().forEach(System.out::println);
		// for (OWLClass owlClass : classesInSignature) {
		// System.out.println("Class Name:" + owlClass.getIRI().getFragment());
		// //showSetSuperclasses(owlClass.getSuperClasses(myOntology));
		// }
	}

	public OntClass getStringClass(String clazz) {
		return ontologyModel.getOntClass(clazz);
	}

	public Individual createIndividual(String instanceName, OntClass ontClass) {
		return ontologyModel.createIndividual(NS + instanceName, ontClass);
	}

	public Individual createIndividual(String instanceName, String className) {
		// TODO Auto-generated method stub
		OntClass ontClass = OntologyManager.getInstance().getStringClass(className);
		return ontologyModel.createIndividual(instanceName, ontClass);
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

	public void testStuff() {
		// testDumpExistingIndividualPropertiesAndValues();
		dumpCalculatedPropertiesForAClass(getStringClass(NS + "CentrifugeTube"));
		// dumpPropertiesForAllClasses();
		// Individual tinyValueIndividual = ontologyModel.getIndividual(NS +
		// "tinyVolume");
		// System.out.println(tinyValueIndividual);
		// testDumpExistingIndividualPropertiesAndValues(tinyValueIndividual);
		// Individual myProtejeCreatedMicroCentrifugeTube =
		// ontologyModel.getIndividual(NS +
		// "myProtejeCreatedMicroCentrifugeTube");//existing
		// testDumpExistingIndividualPropertiesAndValues(myProtejeCreatedMicroCentrifugeTube);
		// testCreateIndividualAndAssignLiteralPropertyValues();
		// testCreateIndividualAndAssignLiteralAndClassPropertyValues();
		// //class listed props
		// System.out.println("class listed props");
		// dumpPropertiesAndValuesForClass(getStringClass(NS + "CentrifugeTube"));//
		// dumpPropertiesForClass(getStringClass(NS + "Pipette"));

		// test top protocol
		// dumpPropertiesAndValues(getTopProtocol());
	}

	private void dumpPropertiesForAllClasses() {
		Set<OntClass> classesSet = ontologyModel.listClasses().toSet();
		final int indent = 1;
		classesSet.forEach(ontClass -> {
			dumpAllPropertiesForAClass(ontClass);
		});
	}

	public void dumpAllPropertiesForAClass(OntClass ontClass) {
		final int indent = 1;
		Set<OntProperty> declaredOntProperties = ontClass.listDeclaredProperties().toSet();
		ontClass.listSuperClasses(true).toSet().forEach(superClass -> {
			dumpAllDirectPropertiesForAClass(superClass, declaredOntProperties);
		});
		declaredOntProperties.forEach(ontProperty -> {
			System.out.println(String.join("", Collections.nCopies(indent, "\t")) + (ontProperty.isObjectProperty() ? "object property:" : "data property:") + ontProperty.getLocalName() + "<" + ontProperty.getRange() + "> of Type:" + ontProperty.getRDFType());
		});
	}

	public void dumpAllDirectPropertiesForAClass(OntClass ontClass, Set<OntProperty> declaredOntProperties) {
		final int indent = 1;
		System.out.println("class:" + ontClass.getLocalName());
		declaredOntProperties.addAll(ontClass.listDeclaredProperties().toSet());
		ontClass.listSuperClasses(true).toSet().forEach(superClass -> {
			dumpAllDirectPropertiesForAClass(superClass, declaredOntProperties);
		});
		System.out.println("class:" + ontClass.getLocalName());
	}

	public Set<OntProperty> dumpCalculatedPropertiesForAClass(OntClass ontClass) {
		Set<OntProperty> collected = ontologyModel.listAllOntProperties().toSet().stream().filter(dataTypeProperty -> {
			return dataTypeProperty.hasDomain(ontClass);
		}).collect(Collectors.toSet());
		ontClass.listSuperClasses(true).toSet().forEach(superClass -> {
			dumpCalculatedPropertiesForAClass(superClass, collected);
		});
		collected.forEach(System.out::println);
		return collected;
	}

	public void dumpCalculatedPropertiesForAClass(OntClass ontClass, final Set<OntProperty> collected) {
		//System.out.println("calculated for class:" + ontClass.getLocalName());
		collected.addAll(ontologyModel.listAllOntProperties().toSet().stream().filter(dataTypeProperty -> {
			return dataTypeProperty.hasDomain(ontClass);
		}).collect(Collectors.toSet()));
		ontClass.listSuperClasses(true).toSet().forEach(superClass -> {
			dumpCalculatedPropertiesForAClass(superClass, collected);
		});

	}

	private void testCreateIndividualAndAssignLiteralAndClassPropertyValues() {
		Individual newlyCreatedIndividual = createIndividual("myCodeCreatedMicroCentrifugeTube", NS + "MicroCentrifugeTube");
		System.out.println(newlyCreatedIndividual);
		// https://jena.apache.org/documentation/notes/typed-literals.html

		// will create a typed literal with the lexical value "2", of type xsd:int.
		// Could use model.createTypedLiteral(value, datatype).
		// model.createLiteral(25); still works but is deprecated because it does string
		// conversions
		Literal literalPropertyValue = ontologyModel.createTypedLiteral("QIGEN");
		OntProperty stringValueProperty = ontologyModel.getOntProperty(NS + "manufacturer");
		newlyCreatedIndividual.setPropertyValue(stringValueProperty, literalPropertyValue);
		dumpPropertiesAndValuesInIndividual(newlyCreatedIndividual);
		System.out.println("-------------");

	}

	private void testCreateIndividualAndAssignLiteralPropertyValues() {
		Individual createdIndividual = createIndividual("nanoCodeCreatedVolume", NS + "Volume");
		System.out.println(createdIndividual);
		// https://jena.apache.org/documentation/notes/typed-literals.html

		// will create a typed literal with the lexical value "2", of type xsd:int.
		// Could use model.createTypedLiteral(value, datatype).
		// model.createLiteral(25); still works but is deprecated because it does string
		// conversions
		Literal literalPropertyValue = ontologyModel.createTypedLiteral(new Integer(2));
		OntProperty numericValueProperty = ontologyModel.getOntProperty(NS + "numericValue");
		createdIndividual.setPropertyValue(numericValueProperty, literalPropertyValue);
		dumpPropertiesAndValuesInIndividual(createdIndividual);
		System.out.println("-------------");
	}

	private void testDumpExistingIndividualPropertiesAndValues() {
		Individual individual = getProtejeCreatedMicrocetrifugeTube();
		System.out.println("individual using properties");
		dumpPropertiesAndValuesInIndividual(individual);
		System.out.println("-------------");
		System.out.println("individual using listDeclaredProperties from class only");
		dumpAllPropertiesForAClass(individual.getOntClass());
		// dumpAllPropertiesForAClass(getStringClass(NS+"Container"));
		System.out.println("-------dumpCalculatedPropertiesForAClass-----");
		dumpCalculatedPropertiesForAClass(individual.getOntClass());
	}

	public static void main(String[] args) {
		getInstance().testStuff();

	}

	public static void printSet(Set<?> set, int indent) {
		set.forEach(elem -> System.out.println(String.join("", Collections.nCopies(indent, "\t")) + elem));
	}

	public static void printSet(String message, Set set) {
		System.out.println("Printing " + message);
		printSet(set, 0);
	}

	public static void printSet(String message, Set set, int indent) {
		System.out.println(message);
		printSet(set, indent);
	}

	public Individual getProtejeCreatedMicrocetrifugeTube() {
		return ontologyModel.getIndividual(NS + "myProtejeCreatedMicroCentrifugeTube");
	}
}