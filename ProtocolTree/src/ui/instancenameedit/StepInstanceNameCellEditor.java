package ui.instancenameedit;

import static ui.UiUtils.expandTree;

import java.awt.Component;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Literal;

import ont.OntManager;
import ui.UiUtils;
import ui.property.PropertyAndIndividual;
import ui.property.ClassPropertyEditorPanel.NodeType;

public class StepInstanceNameCellEditor extends AbstractCellEditor implements TreeCellEditor {
	private InstanceNameEditCellPanel instanceNameEditCellPanel;
	Individual individual;


	@Override // I think it's called when somebody from outside want the edited value. Maybe when clicking outside
	public Object getCellEditorValue() { // builds and returns propertyAndIndividual from field EditRenderPanel, Returns the value contained in the editor.
		Individual newIndividual= instanceNameEditCellPanel.getNewIndividualValueScrapedFromEditPanel();		
		return individual;
	}

	// clearly this is called first of the 2 methods to show the display panel
	@Override // when we finish editing?
	public Component getTreeCellEditorComponent(JTree tree, Object individualNode, boolean isSelected, boolean expanded, boolean leaf, int row) {
		if (individualNode != null && individualNode instanceof DefaultMutableTreeNode) {
			Object userObject = ((DefaultMutableTreeNode) individualNode).getUserObject();
			if (userObject instanceof Individual) {
				individual =(Individual) userObject ;
				instanceNameEditCellPanel = new InstanceNameEditCellPanel(individual, (DefaultMutableTreeNode)individualNode );
			}
		} else {
			UiUtils.showDialog(tree, "Uknown object type:" + individualNode);
		}
		return instanceNameEditCellPanel;
	}

	@Override
	public boolean stopCellEditing() {
		return super.stopCellEditing();
	}

	@Override
	protected void fireEditingStopped() {
		// controller.setMode(Mode.RENDER);
		super.fireEditingStopped();
	}
}