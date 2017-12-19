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
package com.hp.hpl.jena.sparql.serializer;

import java.io.OutputStream;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.logging.Log;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.util.NodeToLabelMapBNode;

public class MySerializer{
		
	static public void serializeARQ(Query query, IndentedWriter writer)
    {
		//Serialization Context that preserves b node labels
        // For the query pattern
        SerializationContext cxt1 = new SerializationContext(query, new NodeToLabelMapBNode("b", false){
			@Override
			protected String genStringForNode(Node n) {
				return "_:" + n.toString().substring(1);
			}
        }) ;
        //Serialization Context that preserves b node labels
        // For the construct pattern
        SerializationContext cxt2 = new SerializationContext(query, new NodeToLabelMapBNode("c", false)  ) ;
        
        serializeARQ(query, writer, 
                     new FormatterElement(writer, cxt1),
                     new FmtExprSPARQL(writer, cxt1),
                     new FmtTemplate(writer, cxt2)) ;
    }	
	static final int BLOCK_INDENT = 2 ;
    /** Output the query
     * 
     * @param query  The query
     * @param out    OutputStream
     */
    static public void serialize(Query query, OutputStream out)
    {
        serialize(query, out, null) ;
    }
    
    /** Output the query
     * 
     * @param query  The query
     * @param out     OutputStream
     * @param syntax  Syntax URI
     */
    
    static public void serialize(Query query, OutputStream out, Syntax syntax)
    {
        IndentedWriter writer = new IndentedWriter(out) ;
        serialize(query, writer, syntax) ;
        writer.flush() ;
        try { out.flush() ; } catch (Exception ex) { }
    }
    
    /** Format the query into the buffer
     * @param query  The query
     * @param buff    IndentedLineBuffer
     */
    
    static public void serialize(Query query, IndentedLineBuffer buff)
    {
        Syntax s = query.getSyntax() ;
        if ( s == null )
            s = Syntax.syntaxSPARQL ;
        serialize(query, buff, s) ;
    }
    
    /** Format the query
     * 
     * @param query      The query
     * @param buff       IndentedLineBuffer in which to place the unparsed query
     * @param outSyntax  Syntax URI
     */
    
    static public void serialize(Query query, IndentedLineBuffer buff, Syntax outSyntax)
    {
        serialize(query, buff, outSyntax) ;
    }
    
    /** Format the query
     * @param query   The query
     * @param writer  IndentedWriter
     */
    
    static public void serialize(Query query, IndentedWriter writer)
    {
        Syntax s = query.getSyntax() ;
        if ( s == null )
            s = Syntax.syntaxSPARQL ;
        serialize(query, writer, s) ;
    }
    
    /** Format the query
     * 
     * @param writer     IndentedWriter
     * @param outSyntax  Syntax URI
     */
    
    static public void serialize(Query query, IndentedWriter writer, Syntax outSyntax)
    {
        if ( outSyntax == null )
            outSyntax = Syntax.syntaxSPARQL ;
        
        if ( outSyntax.equals(Syntax.syntaxARQ) )
        {
            serializeARQ(query, writer) ;
            writer.flush() ;
            return ;
        }
        
        if (outSyntax.equals(Syntax.syntaxSPARQL_10))
        {
            serializeSPARQL_10(query, writer) ;
            writer.flush() ;
            return ;
        }

        if (outSyntax.equals(Syntax.syntaxSPARQL_11))
        {
            serializeSPARQL_11(query, writer) ;
            writer.flush() ;
            return ;
        }
        
//        if (outSyntax.equals(Syntax.syntaxSPARQL_X))
//        {
//            serializeSPARQL_X(query, writer) ;
//            writer.flush() ;
//            return ;
//        }
        
        Log.warn(Serializer.class,"Unknown syntax: "+outSyntax) ;
    }
     
    //changed to public
    static public void serializeARQ(Query query, 
                                     IndentedWriter writer, 
                                     FormatterElement eltFmt,
                                     FmtExprSPARQL    exprFmt,
                                     FormatterTemplate templateFmt)
    {
        QuerySerializer serilizer = new QuerySerializer(writer, eltFmt, exprFmt, templateFmt) ;
        query.visit(serilizer) ;
    }

    static public void serializeSPARQL_10(Query query, IndentedWriter writer)
    {
        // ARQ is a superset of SPARQL.
        serializeARQ(query, writer) ;
    }

    static public void serializeSPARQL_11(Query query, IndentedWriter writer)
    {
        // ARQ is a superset of SPARQL.
        serializeARQ(query, writer) ;
    }
}
