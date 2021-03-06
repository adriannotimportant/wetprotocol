package ui.property;

import java.awt.Color;
import javax.swing.ComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.RDFNode;

import ont.OntManager;
import resources.ResourceFinding;
import uiutil.AbstractProtocolFormattedTextBox;
import uiutil.UITextValueInterface;
import uiutil.UiUtils;

@SuppressWarnings("serial")
public abstract class AbstractTreeCellPanel extends JPanel {
	protected PropertyAndIndividual propertyAndIndividual;
	public static final Icon ICON_LEAF_CLASS = UIManager.getIcon("FileChooser.detailsViewIcon");// http://en-human-begin.blogspot.ca/2007/11/javas-icons-by-default.html
	// maybe next is application_edit
	public static final Icon ICON_LITERAL = UIManager.getIcon("Tree.leafIcon");// http://en-human-begin.blogspot.ca/2007/11/javas-icons-by-default.html
	static final ImageIcon ICON_PREEXISTING_OBJECT = ResourceFinding.createImageIcon("icons/brick_add.png");
	static final ImageIcon ICON_CHOICE_SUBCLASS = ResourceFinding.createImageIcon("icons/page_white_ruby.png");
	JLabel icon = new JLabel("");
	JLabel localComponent = new JLabel("dummy local");
	JLabel rangeComponent = new JLabel("dummy range");
	JLabel domainComponent = new JLabel("dummy domain");
	JComboBox<WrappedOntResource<?>> individualOrClassChooser = new JComboBox<WrappedOntResource<?>>();
	JLabel debug = new JLabel("debug");
	public final String SELECT_CLASS_TEXT = "name";
	UITextValueInterface valueComponent;

	public AbstractTreeCellPanel(PropertyAndIndividual propertyAndIndividual) {
		this.propertyAndIndividual = propertyAndIndividual;
		debug.setText("individual:" + propertyAndIndividual.getIndividual().getLocalName());
		if ("text".equals(propertyAndIndividual.getOntProperty().getRange().getLocalName().toString())) {
			valueComponent = new PropertyValueTextArea();// for large comments
		} else {
			valueComponent = new PropertyValueFormattedTextBox();// this seems to generate dummy on escape for JformattedTextField and no change for JText
		}
		valueComponent.addPropertyChangeListener("value", new ValueChangeListener());// TODO not necessary?
		RDFNode propertyValue = propertyAndIndividual.getIndividual().getPropertyValue(propertyAndIndividual.getOntProperty());
		if (propertyValue != null) {
			if (propertyValue.isLiteral()) {
				if ("text".equals(propertyAndIndividual.getOntProperty().getRange().getLocalName().toString())) {
					System.out.println("fond a text property for prop:" + propertyValue);
				}
				valueComponent.setValue(propertyValue.asLiteral().getValue().toString());
				// System.out.println("in Abstract Cell Panel constructor setting the valueComponent to:"+valueComponent.getText());
			} else if (propertyValue.isResource()) {
				valueComponent.setValue(propertyValue.asResource().getLocalName());
				// System.out.println("in Abstract Cell Panel constructor setting the valueComponent to:"+valueComponent.getText());
			}
		} else {
			valueComponent.setValue("-");
			// System.out.println("in Abstract Cell Panel constructor setting the valueComponent to:"+valueComponent.getText());
		}
		individualOrClassChooser.setMaximumRowCount(200);
	}

	public void setIcon(Icon icon) {
		this.icon.setIcon(icon);
	}

	public void setTextAndAddComponents() {
		OntProperty ontProperty = propertyAndIndividual.getOntProperty();
		localComponent.setText(ontProperty.getLocalName() + ":");
		UiUtils.showConditionDialog(ontProperty.getRange() == null, this, "Property:" + ontProperty + " must have a non null range");
		rangeComponent.setText("Range:" + ontProperty.getRange().getLocalName().toString());
		domainComponent.setText("Coming from super class:" + ontProperty.getDomain().getLocalName());
		add(icon);
		add(localComponent);
		valueComponent.setEditable(false);
		valueComponent.addToContainer(this);
		add(individualOrClassChooser);
		add(rangeComponent);
		add(domainComponent);
		add(debug);
		debug.setText(propertyAndIndividual.getNodeType().name());
		individualOrClassChooser.setVisible(false);
		individualOrClassChooser.setEnabled(false);
		RDFNode propertyValue = propertyAndIndividual.getIndividual().getPropertyValue(ontProperty);
		switch (propertyAndIndividual.getNodeType()) {// todo check order to avoid weird side effects
		case LITERAL_NODE:
			valueComponent.setValue(propertyValue != null ? "" + propertyValue.asLiteral().getValue() : "-");// todo this should extract the type and the value properly and attach the formatter
			// use getDataType to set the type
			localComponent.setForeground(Color.GREEN);
			icon.setIcon(EditCellPanel.ICON_LITERAL);
			break;
		case DATA_TYPE_NODE_FOR_CHOICE_PREEXISTING_OBJECT:
			OntManager.loadPossibleIndividualValues(individualOrClassChooser, ontProperty.getRange().asClass());
			if (propertyValue != null) {// individual already created
				Individual individual = ((OntResource) propertyValue).asIndividual();
				valueComponent.setValue(individual.getLocalName());
				setSelectedItemByComparing(individualOrClassChooser, individual.getLocalName());
			} else {
				valueComponent.setValue("please select an existing object from the combo box");
			}
			localComponent.setForeground(Color.PINK);
			individualOrClassChooser.setVisible(true);
			individualOrClassChooser.setEnabled(true);
			// individualOrClassChooser.addActionListener(new ActionListener() {
			// @Override
			// public void actionPerformed(ActionEvent arg0) {
			// WrappedOntResource selectedProperty = (WrappedOntResource) (individualOrClassChooser.getSelectedItem());
			// System.out.println("selected:" + selectedProperty);
			// }
			// });
			icon.setIcon(EditCellPanel.ICON_PREEXISTING_OBJECT);
			break;
		case DATA_TYPE_NODE_FOR_LEAF_CLASS:
			valueComponent.setValue("" + (((OntResource) propertyValue).asIndividual().getLocalName()));
			valueComponent.setForeground(Color.GRAY);
			valueComponent.setColumns(10);
			localComponent.setForeground(Color.CYAN);
			icon.setIcon(EditCellPanel.ICON_LEAF_CLASS);
			break;
		case DATA_TYPE_NODE_FOR_CHOICE_SUBCLASS:// RED RUBY
			valueComponent.setEditable(true);
			loadPossibleLeafClassValues(individualOrClassChooser);
			if (propertyValue != null) {// individual already created
				String localName = "" + ((OntResource) propertyValue).asIndividual().getLocalName();
				valueComponent.setValue(localName);
				setSelectedItemByComparing(individualOrClassChooser, ((OntResource) propertyValue).asIndividual().getOntClass().getLocalName());
				// we need to keep going down in here as long as it's populated
			} else {
				valueComponent.setValue(SELECT_CLASS_TEXT);
			}
			localComponent.setForeground(Color.RED);
			individualOrClassChooser.setVisible(true);
			individualOrClassChooser.setEnabled(true);
			icon.setIcon(EditCellPanel.ICON_CHOICE_SUBCLASS);
			break;
		default:
			localComponent.setForeground(Color.MAGENTA);
			// too error dialog
			break;
		}
	}

	private void loadPossibleLeafClassValues(JComboBox<WrappedOntResource<?>> individualOrClassChooser2) {
		OntProperty ontProperty = propertyAndIndividual.getOntProperty();
		for (OntClass subclassFromRange : ontProperty.getRange().asClass().listSubClasses(false).toSet()) {
			// System.out.println("\t\t dealing with range subclass:" + subclassFromRange.getLocalName());
			if (OntManager.isLeafClass(subclassFromRange)) { // only leaf classes
				individualOrClassChooser2.addItem(new WrappedOntResource<OntClass>(subclassFromRange));
			}
			// DefaultMutableTreeNode newClassChild = new DefaultMutableTreeNode(subclassFromRange);
			// DefaultMutableTreeNode rangeLevelNode = currentTopNode;
			// currentTopNode.add(newClassChild);// would be Read Only as it's an object property
			// // currentTopNode = newClassChild;
			// // Set<OntProperty> props = OntologyManager.getInstance().dumpCalculatedPropertiesForAClass(subclassFromRange);
			// // for (OntProperty p : props) {
			// // System.out.println("\t\t\t dealing with range property:" + p.getLocalName());
			// // createNode(p, currentTopNode, true);
			// // }
			// currentTopNode = rangeLevelNode;
			// System.out.println("\t\t finished dealing with range subclass:" + subclassFromRange.getLocalName());
		}
	}

	private void setSelectedItemByComparing(JComboBox<WrappedOntResource<?>> individualOrClassChooser2, String localName) {
		ComboBoxModel<WrappedOntResource<?>> model = individualOrClassChooser2.getModel();
		for (int i = 0; i < model.getSize(); i++) {
			WrappedOntResource<?> elementAt = model.getElementAt(i);
			if (elementAt.toString().equalsIgnoreCase(localName)) {
				individualOrClassChooser2.setSelectedItem(elementAt);
				return;
			}
		}
		assert (true);// ,"I should have found the selection") ;
	}
}
// }
// for (OntClass subclassFromRange : range.asClass().listSubClasses(false).toSet()) {
// System.out.println("\t\t dealing with range subclass:" + subclassFromRange.getLocalName());
// DefaultMutableTreeNode newClassChild = new DefaultMutableTreeNode(subclassFromRange);
// DefaultMutableTreeNode rangeLevelNode = currentTopNode;
// currentTopNode.add(newClassChild);// would be Read Only as it's an object property
// // currentTopNode = newClassChild;
// // Set<OntProperty> props = OntologyManager.getInstance().dumpCalculatedPropertiesForAClass(subclassFromRange);
// // for (OntProperty p : props) {
// // System.out.println("\t\t\t dealing with range property:" + p.getLocalName());
// // createNode(p, currentTopNode, true);
// // }
// currentTopNode = rangeLevelNode;
// System.out.println("\t\t finished dealing with range subclass:" + subclassFromRange.getLocalName());
// }
// DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(new PropertyAndIndividual(ontProperty, individual, NodeType.DATA_TYPE_NODE_FOR_CHOICE_SUBCLASS));//, ontProperty.getRange().asClass().listSubClasses(false).toSet()));// Pop up
// System.out.println("\tfinished with range :" + range);
// // describe the property itself even if it do
// // if (!OntologyManager.isStandalone(ontProperty)) {
// // Set<OntProperty> rangeProps = OntologyManager.getInstance().dumpCalculatedPropertiesForAClass(range.asClass());
// // for (OntProperty rangeProperty : rangeProps) {
// // System.out.println("creating first level prop node for " + rangeProperty.getLocalName());
// // createNode(rangeProperty, currentTopNode, false);
// // }
// // }
// }
