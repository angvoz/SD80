/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation 
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.DOMAST;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * @author dsteffle
 */
public class DOMASTNodeParent extends DOMASTNodeLeaf {
    private static final int NO_PREPROCESSOR_STATMENT = -1;
	private static final DOMASTNodeLeaf[] EMPTY_CHILDREN_ARRAY = new DOMASTNodeLeaf[0];
	private static final int DEFAULT_NODE_CHAIN_SIZE = 4;
	private static final int DEFAULT_CHILDREN_SIZE = 4;
	int index=0;
	private DOMASTNodeLeaf[] children;
	boolean cleanupedElements = false;
    private int indexFirstPreproStmnt=NO_PREPROCESSOR_STATMENT;
	
	public int getStartSearch() {
		return index;
	}

	public DOMASTNodeParent() {
		super(null);
		children = EMPTY_CHILDREN_ARRAY;
	}
	
	public DOMASTNodeParent(IASTNode node) {
		super(node);
		children = new DOMASTNodeLeaf[DEFAULT_CHILDREN_SIZE];
	}
	public void addChild(DOMASTNodeLeaf child) {
        if (child.getNode() instanceof IASTPreprocessorStatement && indexFirstPreproStmnt == NO_PREPROCESSOR_STATMENT) {
            indexFirstPreproStmnt=index;
        }
        
		if (index==children.length) {
			children = (DOMASTNodeLeaf[])ArrayUtil.append(DOMASTNodeLeaf.class, children, child);
			index++;
		} else {
			children[index++] = child;
		}
		
		child.setParent(this);
	}
	public void removeChild(DOMASTNodeLeaf child) {
		for(int i=0; i<children.length; i++) {
			if (children[i] == child) {
				children[i] = null; 
				break;
			}
		}
		child.setParent(null);
	}
	
    public DOMASTNodeLeaf[] getChildren(boolean cleanupElements) {
        if (cleanupElements) {
            return getChildren();
        }
        
        return children;
    }
    
	public DOMASTNodeLeaf [] getChildren() {
        // remove null children from the array (if not already done so)
        if (!cleanupedElements) {
            cleanChildren();
        }
        
        return children;
	}
    
    public void cleanChildren() {
        // remove null elements
        children = (DOMASTNodeLeaf[])ArrayUtil.removeNulls(DOMASTNodeLeaf.class, children);
        
        // sort the elements
        Arrays.sort(children, new Comparator() {
            public int compare(Object a, Object b) {
                if(a instanceof DOMASTNodeLeaf && b instanceof DOMASTNodeLeaf &&
                        ((DOMASTNodeLeaf)a).getNode() instanceof ASTNode &&
                        ((DOMASTNodeLeaf)b).getNode() instanceof ASTNode) {
                    return ((ASTNode)((DOMASTNodeLeaf)a).getNode()).getOffset() - ((ASTNode)((DOMASTNodeLeaf)b).getNode()).getOffset();
                }
                
                return 0;
            }
        });
        
        // need to also clean up the children's children, to make sure all nulls are removed (prevent expansion sign when there isn't one)
        for(int i=0; i<children.length; i++) {
            if (children[i] instanceof DOMASTNodeParent) {
                DOMASTNodeLeaf[] kids = ((DOMASTNodeParent)children[i]).children;
                // remove null elements
                kids = (DOMASTNodeLeaf[])ArrayUtil.removeNulls(DOMASTNodeLeaf.class, kids);
                ((DOMASTNodeParent)children[i]).setChildren(kids);
            }
        }
        
        cleanupedElements = true;
    }
    
	public boolean hasChildren() {
		return children.length>0;
	}

    /**
     * Returns the DOMASTNodeParent whose IASTNode is the parent of the IASTNode.
     *
     * This function is an optimization function used to only search merged preprocessor nodes.
     * 
     * @param node
     * @return
     */
    public DOMASTNodeParent findTreeParentForMergedNode(IASTNode node) {
        if (node == null || node.getParent() == null) return null;
        
        IASTNode parentToFind = node.getParent();
        
        // first check this node before checking children
        if (this.getNode() == parentToFind) {
            return this;
        }
        
        // build the chain of nodes... and use it to search the tree for the DOMASTNodeParent that owns the node's parent
        IASTNode[] nodeChain = new IASTNode[DEFAULT_NODE_CHAIN_SIZE];
        IASTNode topNode = node.getParent();
        ArrayUtil.append(IASTNode.class, nodeChain, node);
        nodeChain = (IASTNode[])ArrayUtil.append(IASTNode.class, nodeChain, topNode);
        while(topNode.getParent() != null && !(topNode.getParent() instanceof IASTTranslationUnit)) {
            topNode = topNode.getParent();
            nodeChain = (IASTNode[])ArrayUtil.append(IASTNode.class, nodeChain, topNode);
        }
        
        // loop through the chain of nodes and use it to only search the necessary children required to find the node
        DOMASTNodeLeaf[] childrenToSearch = children;
        int j=childrenToSearch.length-1;
        outerLoop: for(int i=nodeChain.length-1; i>=0; i--) {
            if (nodeChain[i] != null) {
                parentToFind = nodeChain[i];
                
                for(; j>=indexFirstPreproStmnt; j--) {
                    if (childrenToSearch[j] instanceof DOMASTNodeParent) {
                        if ( childrenToSearch[j].getNode() == node.getParent() ) {
                            return (DOMASTNodeParent)childrenToSearch[j];
                        }
                                                
                        if (childrenToSearch[j].getNode() == parentToFind) {
                            int pos = j;
                            j = ((DOMASTNodeParent)childrenToSearch[pos]).getStartSearch();
                            childrenToSearch = ((DOMASTNodeParent)childrenToSearch[pos]).getChildren(false);
                            continue outerLoop;
                        }
                    }
                }
            }
        }
        
        return null; // nothing found
    }
    
	/**
	 * Returns the DOMASTNodeParent whose IASTNode is the parent of the IASTNode.
	 * 
	 * @param node
	 * @return
	 */
	public DOMASTNodeParent findTreeParentForNode(IASTNode node) {
		if (node == null || node.getParent() == null) return null;
		
		IASTNode parentToFind = node.getParent();
		
		// first check this node before checking children
		if (this.getNode() == parentToFind) {
			return this;
		}
		
		// build the chain of nodes... and use it to search the tree for the DOMASTNodeParent that owns the node's parent
		IASTNode[] nodeChain = new IASTNode[DEFAULT_NODE_CHAIN_SIZE];
		IASTNode topNode = node.getParent();
		ArrayUtil.append(IASTNode.class, nodeChain, node);
		nodeChain = (IASTNode[])ArrayUtil.append(IASTNode.class, nodeChain, topNode);
		while(topNode.getParent() != null && !(topNode.getParent() instanceof IASTTranslationUnit)) {
			topNode = topNode.getParent();
			nodeChain = (IASTNode[])ArrayUtil.append(IASTNode.class, nodeChain, topNode);
		}
		
		// loop through the chain of nodes and use it to only search the necessary children required to find the node
		DOMASTNodeLeaf[] childrenToSearch = children;
		int j=getStartSearch();
		outerLoop: for(int i=nodeChain.length-1; i>=0; i--) {
			if (nodeChain[i] != null) {
				parentToFind = nodeChain[i];
				
				for(; j>=0; j--) { // use the DOMASTNodeParent's index to start searching at the end of it's children (performance optimization)
					if (j<childrenToSearch.length && childrenToSearch[j] instanceof DOMASTNodeParent) {
						if ( childrenToSearch[j].getNode() == node.getParent() ) {
							return (DOMASTNodeParent)childrenToSearch[j];
						}
												
						if (childrenToSearch[j].getNode() == parentToFind) {
							int pos = j;
							j = ((DOMASTNodeParent)childrenToSearch[pos]).getStartSearch();
							childrenToSearch = ((DOMASTNodeParent)childrenToSearch[pos]).getChildren(false);
							continue outerLoop;
						}
					}
				}
			}
		}
		
		// try finding the best parent possible
		IASTNode parent = node.getParent();
		DOMASTNodeParent tree = null;
		while (parent != null && tree == null) {
			tree = findTreeParentForNode(parent);
			if (tree != null) return tree;
			
			parent = parent.getParent();
		}
		
		return null; // nothing found
	}

	public int relativeNodePosition( IASTNode n ){
	    ASTNode astNode = (ASTNode) n;
	    if( !cleanupedElements ){
            cleanChildren();
        }
	    if( children.length > 0 ){
	        ASTNode first = (ASTNode) children[0].getNode();
	        if( first.getOffset() > astNode.getOffset() )
	            return -1;
	        ASTNode last = (ASTNode) children[ children.length - 1 ].getNode();
	        if( (last.getOffset() + last.getLength()) < (astNode.getOffset() + astNode.getLength()) )
	            return 1;
	        return 0;
	    }
	    return super.relativeNodePosition( n );
	}
	/**
	 * Returns the DOMASTNodeParent that corresponds to the IASTNode.  This is the DOMASTNodeParent
	 * that represents the IASTNode in the DOM AST View.
	 * 
	 * @param node
	 * @return
	 */
	public DOMASTNodeParent findTreeObject(IASTNode node, boolean useOffset) {
		if (node == null) return null;
		
		// first check this node before checking children
		if (equalNodes(node, this.getNode(), useOffset)) {
			return this;
		}
		if( !useOffset || node instanceof IASTPreprocessorStatement) {
		    IASTNode nodeToFind = node;
		    // build the chain of nodes... and use it to search the tree for the DOMASTNodeParent that contains the node
			IASTNode[] nodeChain = new IASTNode[DEFAULT_NODE_CHAIN_SIZE];
			IASTNode topNode = node;
			nodeChain = (IASTNode[])ArrayUtil.append(IASTNode.class, nodeChain, topNode);
			while(topNode.getParent() != null && !(topNode.getParent() instanceof IASTTranslationUnit)) {
				topNode = topNode.getParent();
				nodeChain = (IASTNode[])ArrayUtil.append(IASTNode.class, nodeChain, topNode);
			}
			
			// loop through the chain of nodes and use it to only search the necessary children required to find the node
			DOMASTNodeLeaf[] childrenToSearch = children;
			outerLoop: for(int i=nodeChain.length-1; i>=0; i--) {
				if (nodeChain[i] != null) {
					nodeToFind = nodeChain[i];
					
					for(int j=0; j<childrenToSearch.length; j++) {
						if (childrenToSearch[j] instanceof DOMASTNodeParent) {
							
							if ( equalNodes(childrenToSearch[j].getNode(), node, useOffset) ) { 
								return (DOMASTNodeParent)childrenToSearch[j];
							}						
							
							if ( equalNodes(childrenToSearch[j].getNode(), nodeToFind, useOffset) ) {
								childrenToSearch = ((DOMASTNodeParent)childrenToSearch[j]).getChildren(false);
								continue outerLoop;
							}
							
							// since the nodeChain doesn't include #includes, if an #include is encountered then search it's children
							if (childrenToSearch[j].getNode() instanceof IASTPreprocessorIncludeStatement) {
								DOMASTNodeParent foundParentInInclude = ((DOMASTNodeParent)childrenToSearch[j]).findTreeObject(node, useOffset);
								if(foundParentInInclude != null) {
									return foundParentInInclude;
								}
							}
						}
					}
				}
			}
		} else {
		    if( children.length == 0 )
			    return null;
		    if( !cleanupedElements ){
		        cleanChildren();
		    }
			int a = 0, z = children.length - 1;
			int idx = (z - a) / 2 ;
			while( true ){
			    int compare = children[ idx ].relativeNodePosition( node );
			    if( compare == 0 ){
			        if( children[idx] instanceof DOMASTNodeParent ){
			            return ((DOMASTNodeParent)children[idx]).findTreeObject( node, useOffset );
			        }
			        return null; //??
			    } else if( compare == -1 )
			        z = idx;
			    else
			        a = idx;
			    int diff = z - a;
			    if( diff == 0 )
			        return null;
			    else if( diff == 1 )
			        idx = ( idx == z ) ? a : z;
			    else 
			        idx = a + ( z - a ) / 2;
			    if( z == a )
			        return null;
			    if( z - a == 1 && children[ a ].relativeNodePosition( node ) == 1 && children[ z ].relativeNodePosition( node ) == -1 )
			        return null;
			}   
		}
		
		return null; // nothing found
	}
	
	private boolean equalNodes(IASTNode node1, IASTNode node2, boolean useOffset) {
		if (useOffset) {
			if (node1 instanceof ASTNode && node2 instanceof ASTNode) {
				if (((ASTNode)node1).getOffset() == ((ASTNode)node2).getOffset() &&
						((ASTNode)node1).getLength() == ((ASTNode)node2).getLength()) {
					if (node1.getClass().equals(node2.getClass()))
						return true;
					else
						return false;
				}
			} else {
				IASTNodeLocation[] locs1 = node1.getNodeLocations();
				IASTNodeLocation[] locs2 = node2.getNodeLocations();
				for(int i=0; i<locs1.length && i<locs2.length; i++) {
					if (locs1[i].getNodeOffset() != locs2[i].getNodeOffset() ||
							locs1[i].getNodeLength() != locs2[i].getNodeLength())
						return false;
				}
				
				if (node1.getClass().equals(node2.getClass()))
					return true;
				else
					return false;
			}
		} else {
			if ( node1 == node2 ) 
				return true;
		}
		
		return false;
	}
	
	public void setChildren(DOMASTNodeLeaf[] children) {
		this.children = children;
	}
	
}
