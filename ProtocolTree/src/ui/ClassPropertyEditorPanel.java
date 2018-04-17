package ui;

//root nodes) after tree.setRootVisible(false) call tree.setShowsRootHandles(true).
import ont.OntologyManager;
import ont.PropertyAndIndividual;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import resources.ResourceFindingDummyClass;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import static ui.UiUtils.expandTree;
import static ui.WetProtocolMainPanel.WITH_OF_PROTOCOL_TREE;

public class ClassPropertyEditorPanel extends JPanel implements TreeSelectionListener {
	private JEditorPane htmlPane;
	private URL helpURL;
	// private JButton addNewSiblingNodeButton = new JButton("New Sibling");
	private JButton okButton = new JButton("OK");
	private JButton expandTreeButton = new JButton("Expand Tree");
	// Create the nodes.
	private DefaultMutableTreeNode protocolTreeNode;// the individual to change properties for

	// most below could be cached
	private JTree jProtocolTree;

	private DefaultTreeModel treeModel = new DefaultTreeModel(new DefaultMutableTreeNode(OntologyManager.getInstance().getTopPropertyAndIndividual()));
	DefaultMutableTreeNode topNode = (DefaultMutableTreeNode) treeModel.getRoot();
	private JTree jPropertyAndIndividualTree = new JTree(treeModel);// my property tree

	public ClassPropertyEditorPanel(DefaultMutableTreeNode protocolTreeNode, JTree jProtocolTree) {// todo we don't need to pass the tree in here ?
		super(new GridLayout(1, 1));
		this.protocolTreeNode = protocolTreeNode;
		if (protocolTreeNode == null) {
			System.out.println("Passed node is null");
		}
		this.jProtocolTree = jProtocolTree;
		initiateTree();
		JPanel treeViewPanel = new JPanel(new BorderLayout());
		treeViewPanel.add(jPropertyAndIndividualTree, BorderLayout.PAGE_START);
		JPanel treeViewButtonPanel = new JPanel();
		// treeViewButtonPanel.add(addNewSiblingNodeButton);
		treeViewButtonPanel.add(okButton);
		treeViewButtonPanel.add(expandTreeButton);
		treeViewPanel.add(treeViewButtonPanel, BorderLayout.PAGE_END);
		// Create the scroll pane and add the tree view panel to it.
		JScrollPane treeView = new JScrollPane(treeViewPanel);
		// Create the HTML viewing pane.
		htmlPane = new JEditorPane();
		htmlPane.setEditable(true);
		OntResource userObject = (OntResource) protocolTreeNode.getUserObject();// can be individual or class
		htmlPane.setText(userObject.getLocalName());// todo show the getComment with .setPage
		// initHelp();
		JScrollPane htmlView = new JScrollPane(htmlPane);
		treeView.setPreferredSize(new Dimension(400, 300));
		// Add the split pane to this panel.
		//////////////
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setTopComponent(treeView);
		splitPane.setBottomComponent(htmlView);
		Dimension minimumSize = new Dimension(100, 50);
		htmlView.setMinimumSize(minimumSize);
		treeView.setMinimumSize(minimumSize);
		splitPane.setDividerLocation(400); // XXX: ignored in some releases
		// of Swing. bug 4101306
		// workaround for bug 4101306:
		splitPane.setPreferredSize(new Dimension(400, 600));
		// Add the split pane to this panel.
		add(splitPane);
		AddTreeButtonListeners(treeView);
	}

	private void initiateTree() {
		// Operation topOperation = (Operation) root.getUserObject();
		// jClassTree.setEditable(true);
		jPropertyAndIndividualTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		jPropertyAndIndividualTree.setShowsRootHandles(true);
		createNodes();
		expandTree(jPropertyAndIndividualTree);
		jPropertyAndIndividualTree.setCellRenderer(new PropertiesCellRenderer());
		jPropertyAndIndividualTree.setEditable(true);
		jPropertyAndIndividualTree.setCellEditor(new PropertyCellEditor());
		// Enable tool tips.
		ToolTipManager.sharedInstance().registerComponent(jPropertyAndIndividualTree);
		// Listen for when the selection changes.
		jPropertyAndIndividualTree.addTreeSelectionListener(this);
		addTreeNodeMouseListeners();
	}

	private void AddTreeButtonListeners(Component splitPane) {
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				acceptPropertiesResponse(splitPane);
			}
		});
		// todo add cancel button
		expandTreeButton.addActionListener(e -> {
			expandTree(jPropertyAndIndividualTree);
		});
	}

	/**
	 * Todo need to update the protocol tree node with the value of the properties and maybe change it's icon and refresh to show the changes
	 */
	private void acceptPropertiesResponse(Component splitPane) {
//		DefaultMutableTreeNode selectedClassNode = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();
//		if (selectedClassNode == null) {
//			UiUtils.showDialog(splitPane, "Selected node is null");
//		} else {
//			// UiUtils.showDialog(splitPane, "okButton called");
//			DefaultMutableTreeNode selectedProtocolNode = (DefaultMutableTreeNode) jProtocolTree.getLastSelectedPathComponent();
//			if (selectedProtocolNode == protocolTreeNode) {// insert child as last child of parent
//				protocolTreeNode.insert(selectedClassNode, protocolTreeNode.getChildCount());
//				DefaultMutableTreeNode insertedChild = (DefaultMutableTreeNode) protocolTreeNode.getChildAt(protocolTreeNode.getChildCount() - 1);
//				jProtocolTree.expandPath(new TreePath(protocolTreeNode.getPath()));
//				jProtocolTree.setSelectionPath(new TreePath(insertedChild.getPath()));// select it
//			} else {// insert sibling after the selected one
//				protocolTreeNode.insert(selectedClassNode, selectedProtocolNode.getParent().getIndex(selectedProtocolNode) + 1);
//				DefaultMutableTreeNode addedChild = (DefaultMutableTreeNode) protocolTreeNode.getChildAt(selectedProtocolNode.getParent().getIndex(selectedProtocolNode) + 1);
//				jProtocolTree.setSelectionPath(new TreePath(addedChild.getPath()));// select it
//			}
//			jProtocolTree.updateUI();
//			((JFrame) ClassPropertyEditorPanel.this.getTopLevelAncestor()).dispose();
//		}
	}

	/**
	 * Required by TreeSelectionListener interface.
	 */
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) jPropertyAndIndividualTree.getLastSelectedPathComponent();
		if (node == null)
			return;
		Object nodeInfo = node.getUserObject();
		// displayURL(null);
		System.out.println(" PropertyTreeSelectionChanged");
	}

	private void addTreeNodeMouseListeners() {
		jPropertyAndIndividualTree.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) jPropertyAndIndividualTree.getLastSelectedPathComponent();
					if (node == null)
						return;
					acceptPropertiesResponse(jPropertyAndIndividualTree);
				}
			}
		});
	}

	// private void initHelp() {
	// String s = "TreeDemoHelp.html";
	// helpURL = ResourceFindingDummyClass.getResource(s);
	// if (helpURL == null) {
	// System.err.println("Couldn't open help file: " + s);
	// }
	// displayURL(helpURL);
	// }

	public void createNodes() {
		DefaultMutableTreeNode topNode = (DefaultMutableTreeNode) treeModel.getRoot();
		Object userObject = protocolTreeNode.getUserObject();// in protocol we have individuals
		assert userObject instanceof Individual;
		Individual individual= ((Individual) userObject);
		OntClass ontClass =individual.getOntClass();
		Set<OntProperty> props = OntologyManager.getInstance().dumpCalculatedPropertiesForAClass(ontClass);
		props.forEach(p -> {
			topNode.add(new DefaultMutableTreeNode(new PropertyAndIndividual(p, individual)));
		});
		// Set properties = OntologyManager.getInstance().getPropertiesInSignature(protocolTreeParentNode.getUserObject().toString());
		// if(protocolTreeNode==null) return;
		// @SuppressWarnings("rawtypes")
		// Set properties = OntologyManager.getInstance().getPropertiesInIndividual((OWLIndividual) protocolTreeNode.getUserObject());
		// System.out.println(properties);
		// DefaultMutableTreeNode book;
		// // Tutorial Continued
		// book = new DefaultMutableTreeNode(new Operation("The Java Tutorial Continued: The Rest of the JDK", "tutorialcont.html"));
		// topNode.add(book);
		// // JFC Swing Tutorial
		// book = new DefaultMutableTreeNode(new Operation("The JFC Swing Tutorial: A Guide to Constructing GUIs", "swingtutorial.html"));
		// topNode.add(book);
		// // Bloch
		// book = new DefaultMutableTreeNode(new Operation("Effective Java Programming Language Guide", "bloch.html"));
		// topNode.add(book);
	}

	@Override
	public void setBackground(Color bg) {
		// TODO Auto-generated method stub
		super.setBackground(Color.RED);
	}

}
