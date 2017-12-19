/*******************************************************************************
 * Copyright (C) 2011 Czech Technical University in Prague                                                                                                                                                        
 *                                                                                                                                                                                                                
 * This program is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any 
 * later version. 
 *                                                                                                                                                                                                                
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
 * details. You should have received a copy of the GNU General Public License 
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.hp.hpl.jena.sparql.util;

import java.util.HashMap ;
import java.util.Map ;
import java.util.Set ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarAlloc ;


/** Map from _:* form to bNodes */

public class LabelToNodeMap
{
    // Will be replaced by LabelToNode in RIOT
    Map<String, Node> bNodeLabels = new HashMap<String, Node>() ;
    
    // Variables or bNodes?
    // True means variables (query pattern)
    // False means blank node (construct template)
    boolean generateVars = false ;
    VarAlloc allocator = null ;
    
    /** Create blank nodes, with the same blank node returned for thre same label.  
     * 
     * @return LabelToNodeMap
     */

    public static LabelToNodeMap createBNodeMap()
    { return new LabelToNodeMap(false, null) ; }
    
    /** Create variables (Var), starting from zero each time
     * This means that parsing a query string will generate
     * the same variable names for bNode variables each time,
     * making Query.equals and Query.hashCode work.  
     * 
     * @return LabelToNodeMap
     */
    
    public static LabelToNodeMap createVarMap()
    { return new LabelToNodeMap(true, new VarAlloc(ARQConstants.allocParserAnonVars) ) ; }
    
    private LabelToNodeMap(boolean genVars, VarAlloc allocator) 
    {
        generateVars = genVars ;
        this.allocator = allocator ;
    }
    
    public Set<String> getLabels()  { return bNodeLabels.keySet() ; }
    

	public Node asNode(String label)
    {
        Node n = bNodeLabels.get(label) ;
        if ( n != null )
            return n ;
        String name = label;
        if(label.startsWith("_:"))
        	name = label.substring(2);
        	
        n = Var.alloc('?' + name); //allocNode() ; line changed
        bNodeLabels.put(label, n) ;
        return n ;
    }
    
    public Node allocNode()
    {
        if ( generateVars )
            return allocAnonVariable() ;
        return NodeFactory.createAnon() ;
    }
    
    private Node allocAnonVariable()
    {
        return allocator.allocVar() ;
    }
    
    public void clear()
    {
        bNodeLabels.clear() ;
    }
}
