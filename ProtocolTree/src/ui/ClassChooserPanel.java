package ui;

import ont.OntManager;
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
import static ui.UiUtils.createEmpyIndividualNodes;
import static ui.UiUtils.expandTree;

public class ClassChooserPanel extends JPanel implements TreeSelectionListener {
    private JEditorPane htmlPane;
    private URL helpURL;
    // private JButton addNewSiblingNodeButton = new JButton("New Sibling");
    private JButton okButton = new JButton("OK");
    private JButton expandTreeButton = new JButton("Expand Tree");
    //Create the nodes.
    private DefaultMutableTreeNode protocolTreeParentNode;
    //most below could be cached
    private JTree jProtocolTree;
    private DefaultTreeModel classTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode(OntManager.getInstance().getTopProtocoInstancel()));
    private JTree jClassTree = new JTree(classTreeModel);
    private static boolean DEBUG = true; //adrian

    public ClassChooserPanel(DefaultMutableTreeNode protocolTreeParentNode, JTree jProtocolTree) {
        super(new GridLayout(1, 1));
        this.protocolTreeParentNode = protocolTreeParentNode;
        this.jProtocolTree = jProtocolTree;
        initiateTree();
        JPanel treeViewPanel = new JPanel(new BorderLayout());
        treeViewPanel.add(jClassTree, BorderLayout.PAGE_START);
        JPanel treeViewButtonPanel = new JPanel();
        //treeViewButtonPanel.add(addNewSiblingNodeButton);
        treeViewButtonPanel.add(okButton);
        treeViewButtonPanel.add(expandTreeButton);
        treeViewPanel.add(treeViewButtonPanel, BorderLayout.PAGE_END);
        //Create the scroll pane and add the tree view panel to it.
        JScrollPane treeView = new JScrollPane(treeViewPanel);
        //Create the HTML viewing pane.
        htmlPane = new JEditorPane();
        htmlPane.setEditable(true);
        initHelp();
        JScrollPane htmlView = new JScrollPane(htmlPane);
        treeView.setPreferredSize(new Dimension(400, 300));
        //Add the split pane to this panel.
        //////////////
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(treeView);
        splitPane.setBottomComponent(htmlView);
        Dimension minimumSize = new Dimension(100, 50);
        htmlView.setMinimumSize(minimumSize);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(400); //XXX: ignored in some releases
        //of Swing. bug 4101306
        //workaround for bug 4101306:
        splitPane.setPreferredSize(new Dimension(400, 600));
        //Add the split pane to this panel.
        add(splitPane);
        AddTreeButtonListeners(treeView);
    }




    private void initiateTree() {
        // Operation topOperation = (Operation) root.getUserObject();
        //jClassTree.setEditable(true);
        jClassTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jClassTree.setShowsRootHandles(true);
        createEmpyIndividualNodes(jClassTree,  OntManager.getInstance().getClassesInSignature());// this should be cached or we could cache the whole chooser
        expandTree(jClassTree);
        //Enable tool tips.
        ToolTipManager.sharedInstance().registerComponent(jClassTree);
        //jClassTree.setCellRenderer(new MyRenderer());
        jClassTree.setCellRenderer(new ProtocolInstanceCellRenderer());
        //Listen for when the selection changes.
        jClassTree.addTreeSelectionListener(this);
        addTreeNodeMouseListeners();
    }

    private void AddTreeButtonListeners(Component splitPane) {
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ClassChosenResponse(splitPane);
            }
        });
        expandTreeButton.addActionListener(e -> {
            expandTree(jClassTree);
        });
    }

    private void ClassChosenResponse(Component splitPane) {
        DefaultMutableTreeNode selectedClassNode = (DefaultMutableTreeNode) jClassTree.getLastSelectedPathComponent();
        if (selectedClassNode == null) {
            UiUtils.showDialog(splitPane, "Selected node is null");//should never get here
        } else {
            //UiUtils.showDialog(splitPane, "okButton called");
            DefaultMutableTreeNode selectedProtocolNode = (DefaultMutableTreeNode) jProtocolTree.getLastSelectedPathComponent();
            if (selectedProtocolNode == protocolTreeParentNode) {//insert child as last child of parent
                protocolTreeParentNode.insert(selectedClassNode, protocolTreeParentNode.getChildCount());
                DefaultMutableTreeNode insertedChild = (DefaultMutableTreeNode)protocolTreeParentNode.getChildAt(protocolTreeParentNode.getChildCount()-1);
                jProtocolTree.expandPath(new TreePath(protocolTreeParentNode.getPath()));
                jProtocolTree.setSelectionPath(new TreePath(insertedChild.getPath()));//select it
            } else {//insert sibling after the selected one
                protocolTreeParentNode.insert(selectedClassNode, selectedProtocolNode.getParent().getIndex(selectedProtocolNode) + 1);
                DefaultMutableTreeNode addedChild = (DefaultMutableTreeNode)protocolTreeParentNode.getChildAt(selectedProtocolNode.getParent().getIndex(selectedProtocolNode) + 1);
                jProtocolTree.setSelectionPath(new TreePath(addedChild.getPath()));//select it
            }
            jProtocolTree.updateUI();
            ((JFrame) ClassChooserPanel.this.getTopLevelAncestor()).dispose();
        }
    }

    /**
     * Required by TreeSelectionListener interface.
     */
    @Override
	public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) jClassTree.getLastSelectedPathComponent();
//        if (node == null) return;
//        Object nodeInfo = node.getUserObject();
//        if (node.isLeaf()) {
//            Operation book = (Operation) nodeInfo;
//            displayURL(book.url);
//            if (DEBUG) {
//                System.out.print(book.url + ":  \n    ");
//            }
//        } else {
//            displayURL(helpURL);
//        }
//        if (DEBUG) {
//            System.out.println(nodeInfo.toString());
//        }
    }

    private void addTreeNodeMouseListeners() {
        jClassTree.addMouseListener(new MouseAdapter() {
            @Override
			public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) jClassTree.getLastSelectedPathComponent();
                    if (node == null) return;
                    ClassChosenResponse(jClassTree);
                }
            }
        });
    }

    private void initHelp() {
        String s = "TreeDemoHelp.html";
        helpURL = ResourceFindingDummyClass.getResource(s);
        if (helpURL == null) {
            System.err.println("Couldn't open help file: " + s);
        } else if (DEBUG) {
            System.out.println("Help URL is " + helpURL);
        }
        displayURL(helpURL);
    }

    private void displayURL(URL url) {
        try {
            if (url != null) {
                htmlPane.setPage(url);
            } else { //null url
                htmlPane.setText("File Not Found");
                if (DEBUG) {
                    System.out.println("Attempted to display a null URL.");
                }
            }
        } catch (IOException e) {
            System.err.println("Attempted to read a bad URL: " + url);
        }
    }
}
