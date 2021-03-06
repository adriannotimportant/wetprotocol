package oldStuff;

//see https://docs.oracle.com/javase/tutorial/displayCode.html?code=https://docs.oracle.com/javase/tutorial/uiswing/examples/components/GenealogyExampleProject/src/components/GenealogyModel.java
//package ui;
//
//import otology.Operation;
//
//import javax.swing.event.TreeModelEvent;
//import javax.swing.event.TreeModelListener;
//import javax.swing.tree.MutableTreeNode;
//import javax.swing.tree.TreeModel;
//import javax.swing.tree.TreeNode;
//import javax.swing.tree.TreePath;
//import java.util.ArrayList;
//import java.util.List;
//
//public class ProtocolTreeModel implements TreeModel {
//    private List<TreeModelListener> treeModelListeners = new ArrayList<>();
//    private Operation rootOperation;
//
//    public ProtocolTreeModel(Operation root) {
//        rootOperation = root;
//    }
//    //////////////// Fire events //////////////////////////////////////////////
//    //todo I'm not sure this is needed
//
//    /**
//     * The only event raised by this model is TreeStructureChanged with the
//     * root as path, i.e. the whole tree has changed.
//     */
//    protected void fireTreeStructureChanged(Operation oldRoot) {
//        int len = treeModelListeners.size();
//        TreeModelEvent e = new TreeModelEvent(this,
//                new Object[]{oldRoot});
//        for (TreeModelListener tml : treeModelListeners) {
//            tml.treeStructureChanged(e);
//        }
//    }
//
//
//    public void insertNodeInto(MutableTreeNode newChild,
//                               MutableTreeNode parent, int index){
//        parent.insert(newChild, index);
//
//        int[]           newIndexs = new int[1];
//
//        newIndexs[0] = index;
//        nodesWereInserted(parent, newIndexs);
//    }
//
//    /**
//     * Invoke this method after you've inserted some TreeNodes into
//     * node.  childIndices should be the index of the new elements and
//     * must be sorted in ascending order.
//     */
//    public void nodesWereInserted(TreeNode node, int[] childIndices) {
//        if(treeModelListeners != null && node != null && childIndices != null
//                && childIndices.length > 0) {
//            int               cCount = childIndices.length;
//            Object[]          newChildren = new Object[cCount];
//
//            for(int counter = 0; counter < cCount; counter++)
//                newChildren[counter] = node.getChildAt(childIndices[counter]);
//            fireTreeNodesInserted(this, getPathToRoot(node), childIndices,
//                    newChildren);
//        }
//    }
//
//    /**
//     * Notifies all listeners that have registered interest for
//     * notification on this event type.  The event instance
//     * is lazily created using the parameters passed into
//     * the fire method.
//     *
//     * @param source the source of the {@code TreeModelEvent};
//     *               typically {@code this}
//     * @param path the path to the parent the nodes were added to
//     * @param childIndices the indices of the new elements
//     * @param children the new elements
//     */
//    protected void fireTreeNodesInserted(Object source, Object[] path,
//                                         int[] childIndices,
//                                         Object[] children) {
//        // Guaranteed to return a non-null array
//        Object[] listeners = treeModelListeners.getListenerList();
//        TreeModelEvent e = null;
//        // Process the listeners last to first, notifying
//        // those that are interested in this event
//        for (int i = listeners.length-2; i>=0; i-=2) {
//            if (listeners[i]==TreeModelListener.class) {
//                // Lazily create the event:
//                if (e == null)
//                    e = new TreeModelEvent(source, path,
//                            childIndices, children);
//                ((TreeModelListener)listeners[i+1]).treeNodesInserted(e);
//            }
//        }
//    }
////////////////// TreeModel interface implementation ///////////////////////
//
//    /**
//     * Adds a listener for the TreeModelEvent posted after the tree changes.
//     */
//    @Override
//    public void addTreeModelListener(TreeModelListener l) {
//        treeModelListeners.add(l);
//    }
//
//    /**
//     * Returns the child of parent at index index in the parent's child array.
//     */
//    @Override
//    public Object getChild(Object parent, int index) {
//        Operation p = (Operation) parent;
//        return p.getChildAt(index);
//    }
//
//    /**
//     * Returns the number of children of parent.
//     */
//    @Override
//    public int getChildCount(Object parent) {
//        Operation p = (Operation) parent;
//        return p.getChildCount();
//    }
//
//    /**
//     * Returns the index of child in parent.
//     */
//    @Override
//    public int getIndexOfChild(Object parent, Object child) {
//        Operation p = (Operation) parent;
//        return p.getIndex((Operation) child);
//    }
//
//    /**
//     * Returns the root of the tree.
//     */
//    @Override
//    public Object getRoot() {
//        return rootOperation;
//    }
//
//    /**
//     * Returns true if node is a leaf.
//     */
//    @Override
//    public boolean isLeaf(Object node) {
//        Operation p = (Operation) node;
//        return p.getChildCount() == 0;
//    }
//
//    /**
//     * Removes a listener previously added with addTreeModelListener().
//     */
//    @Override
//    public void removeTreeModelListener(TreeModelListener l) {
//        treeModelListeners.remove(l);
//    }
//
//    /**
//     * Messaged when the user has altered the value for the item
//     * identified by path to newValue.  Not used by this model.
//     */
//    @Override
//    public void valueForPathChanged(TreePath path, Object newValue) {
//        System.out.println("*** valueForPathChanged : " + path + " --> " + newValue);
//    }
//}