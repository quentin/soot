/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrice Pominville, Raja Vallee-Rai, Patrick Lam
 * Copyright (C) 2007 Richard L. Halpert
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.  
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */


package soot.toolkits.graph;


import java.util.*;

import soot.*;
import soot.util.*;




/**
 *   HashMap based implementation of a MutableEdgeLabelledDirectedGraph.
 */
class DGEdge<N>
{
	final N from;
	final N to;
	
	public DGEdge(N from, N to)
	{
		this.from = from;
		this.to = to;
	}
	
	public N from()
	{
		return from;
	}
	
	public N to()
	{
		return to;
	}
	
	public boolean equals(Object o)
	{
		if(o instanceof DGEdge)
		{
			DGEdge<N> other = (DGEdge<N>) o;
			return from.equals(other.from) && to.equals(other.to);
		}
		return false;
	}
	
	public int hashCode()
	{
		return from.hashCode() + to.hashCode();
	}
}

public class HashMutableEdgeLabelledDirectedGraph<N,L> implements MutableEdgeLabelledDirectedGraph<N,L>
{
    protected HashMap<N,ArrayList<N>> nodeToPreds;
    protected HashMap<N,ArrayList<N>> nodeToSuccs;
    
    protected HashMap<DGEdge<N>,ArrayList<L>> edgeToLabels;
    protected HashMap<L,ArrayList<DGEdge<N>>> labelToEdges;

    protected Chain<N> heads;
    protected Chain<N> tails;

    public HashMutableEdgeLabelledDirectedGraph()
    {
      clearAll();
    }

    /** Removes all nodes and edges. */
    public void clearAll() {
      nodeToPreds = new HashMap<N,ArrayList<N>>();
      nodeToSuccs = new HashMap<N,ArrayList<N>>();
    
      edgeToLabels = new HashMap<DGEdge<N>,ArrayList<L>>();
      labelToEdges = new HashMap<L,ArrayList<DGEdge<N>>>();

      heads = new HashChain<N>();
      tails = new HashChain<N>();
    }

    public Object clone() {
        HashMutableEdgeLabelledDirectedGraph<N,L> g = new HashMutableEdgeLabelledDirectedGraph<N,L>();
        g.nodeToPreds = (HashMap<N,ArrayList<N>>)nodeToPreds.clone();
        g.nodeToSuccs = (HashMap<N,ArrayList<N>>)nodeToSuccs.clone();
        g.edgeToLabels = (HashMap<DGEdge<N>,ArrayList<L>>)edgeToLabels.clone();
        g.labelToEdges = (HashMap<L,ArrayList<DGEdge<N>>>)labelToEdges.clone();
        g.heads = HashChain.listToHashChain(HashChain.toList(heads));
        g.tails = HashChain.listToHashChain(HashChain.toList(tails));
        return g;
    }

    /* Returns an unbacked list of heads for this graph. */
    public List<N> getHeads()
    {
        ArrayList<N> l = new ArrayList<N>(); l.addAll(heads);
        return Collections.<N>unmodifiableList(l);
    }

    /* Returns an unbacked list of tails for this graph. */
    public List<N> getTails()
    {
        ArrayList<N> l = new ArrayList<N>(); l.addAll(tails);
        return Collections.<N>unmodifiableList(l);
    }

    public List<N> getPredsOf(N s)
    {
        List<N> l = nodeToPreds.get(s);
        if (l != null)
            return Collections.<N>unmodifiableList(l);
        else
            throw new RuntimeException(s+"not in graph!");
    }

    public List<N> getSuccsOf(N s)
    {
        List<N> l = nodeToSuccs.get(s);
        if (l != null)
            return Collections.<N>unmodifiableList(l);
        else
            throw new RuntimeException(s+"not in graph!");
    }

    public int size()
    {
        return nodeToPreds.keySet().size();
    }

    public Iterator<N> iterator()
    {
        return nodeToPreds.keySet().iterator();
    }

    public void addEdge(N from, N to, L label)
    {
        if (from == null || to == null)
						throw new RuntimeException("edge from or to null");
						
		    if (label == null)
						throw new RuntimeException("edge with null label");

        if (containsEdge(from, to, label))
            return;

        List<N> succsList = nodeToSuccs.get(from);
        if (succsList == null)
            throw new RuntimeException(from + " not in graph!");

        List<N> predsList = nodeToPreds.get(to);
        if (predsList == null)
            throw new RuntimeException(to + " not in graph!");

        if (heads.contains(to))
            heads.remove(to);

        if (tails.contains(from))
            tails.remove(from);
		
		if(!succsList.contains(to))
	        succsList.add(to);
	    if(!predsList.contains(from))
	        predsList.add(from);
	    
	    DGEdge<N> edge = new DGEdge<N>(from, to);
		if(!edgeToLabels.containsKey(edge))
			edgeToLabels.put(edge, new ArrayList<L>());
	    List<L> labels = edgeToLabels.get(edge);
	    
	    if(!labelToEdges.containsKey(label))
	    	labelToEdges.put(label, new ArrayList<DGEdge<N>>());
	    List<DGEdge<N>> edges = labelToEdges.get(label);
		
//		if(!labels.contains(label))
			labels.add(label);
//		if(!edges.contains(edge))
			edges.add(edge);
    }

	public List<L> getLabelsForEdges(N from, N to)
	{
		DGEdge<N> edge = new DGEdge<N>(from, to);
		return edgeToLabels.get(edge);
	}
	
	public MutableDirectedGraph<N> getEdgesForLabel(L label)
	{
		List<DGEdge<N>> edges = labelToEdges.get(label);
		MutableDirectedGraph<N> ret = new HashMutableDirectedGraph<N>();
		if(edges == null)
			return ret;
		for(DGEdge<N> edge : edges)
		{
			if(!ret.containsNode(edge.from()))
				ret.addNode(edge.from());
			if(!ret.containsNode(edge.to()))
				ret.addNode(edge.to());
			ret.addEdge(edge.from(), edge.to());
		}
		return ret;
	}

    public void removeEdge(N from, N to, L label)
    {
        if (!containsEdge(from, to, label))
            return;
            
        DGEdge<N> edge = new DGEdge<N>(from, to);
        List<L> labels = edgeToLabels.get(edge);
        if( labels == null )
        	throw new RuntimeException("edge " + edge + " not in graph!");
        
        List<DGEdge<N>> edges = labelToEdges.get(label);
        if( edges == null )
        	throw new RuntimeException("label " + label + " not in graph!");

		labels.remove(label);
		edges.remove(edge);
		
		// if this edge has no more labels, then it's gone!
		if(labels.isEmpty())
		{
			edgeToLabels.remove(edge);

	        List<N> succsList = nodeToSuccs.get(from);
	        if (succsList == null)
	            throw new RuntimeException(from + " not in graph!");

	        List<N> predsList = nodeToPreds.get(to);
	        if (predsList == null)
	            throw new RuntimeException(to + " not in graph!");
			
	        succsList.remove(to);
	        predsList.remove(from);

	        if (succsList.isEmpty())
	            tails.add(from);

	        if (predsList.isEmpty())
	            heads.add(to);
	    }
	    
	    // if this label has no more edges, then who cares?
	    if(edges.isEmpty())
	    	labelToEdges.remove(label);
    }

    public void removeAllEdges(N from, N to)
	{
        if (!containsAnyEdge(from, to))
            return;
            
        DGEdge<N> edge = new DGEdge<N>(from, to);
        List<L> labels = edgeToLabels.get(edge);
        if( labels == null )
        	throw new RuntimeException("edge " + edge + " not in graph!");
        
        for(L label : labels)
        {        
	        removeEdge(from, to, label);
	    }
	}

    public void removeAllEdges(L label)
    {
		if( !containsAnyEdge(label) )
			return;
		
		List<DGEdge<N>> edges = labelToEdges.get(label);
		if( edges == null )
			throw new RuntimeException("label " + label + " not in graph!");
			
		for(DGEdge<N> edge : edges)
		{
			removeEdge(edge.from(), edge.to(), label);
		}
	}

    public boolean containsEdge(N from, N to, L label)
    {
		DGEdge<N> edge = new DGEdge<N>(from, to);
		if(edgeToLabels.get(edge) != null && edgeToLabels.get(edge).contains(label))
			return true;
		return false;
    }

    public boolean containsAnyEdge(N from, N to)
    {
		DGEdge<N> edge = new DGEdge<N>(from, to);
		if(edgeToLabels.get(edge) != null && edgeToLabels.get(edge).isEmpty())
			return false;
		return true;
    }

    public boolean containsAnyEdge(L label)
    {
		if(labelToEdges.get(label) != null && labelToEdges.get(label).isEmpty())
			return false;
		return true;
    }

    public boolean containsNode(N node)
    {
        return nodeToPreds.keySet().contains(node);
    }

    public List<N> getNodes()
    {
        return (List<N>) Arrays.asList(nodeToPreds.keySet().toArray());
    }

    public void addNode(N node)
    {
				if (containsNode(node))
						throw new RuntimeException("Node already in graph");
				
				nodeToSuccs.put(node, new ArrayList<N>());
        nodeToPreds.put(node, new ArrayList<N>());
        heads.add(node); 
				tails.add(node);
    }

    public void removeNode(N node)
    {
        List<N> succs = (List<N>)nodeToSuccs.get(node).clone();
        for (Iterator<N> succsIt = succs.iterator(); succsIt.hasNext(); )
            removeAllEdges(node, succsIt.next());
        nodeToSuccs.remove(node);

        List<N> preds = (List<N>)nodeToPreds.get(node).clone();
        for (Iterator<N> predsIt = preds.iterator(); predsIt.hasNext(); )
            removeAllEdges(predsIt.next(), node);
        nodeToPreds.remove(node);

        if (heads.contains(node))
            heads.remove(node); 

        if (tails.contains(node))
            tails.remove(node);
    }

    public void printGraph()
    {

		for (Iterator<N> it = iterator(); it.hasNext(); )
		{
		    N node = it.next();
		    G.v().out.println("Node = "+node);
		    G.v().out.println("Preds:");
		    for (Iterator<N> predsIt = getPredsOf(node).iterator(); predsIt.hasNext(); )
		    {
		    	N pred = predsIt.next();
		        DGEdge<N> edge = new DGEdge<N>(pred, node);
		        List<L> labels = edgeToLabels.get(edge);
				G.v().out.println("     " + pred + " [" + labels + "]");
		    }
		    G.v().out.println("Succs:");
		    for (Iterator<N> succsIt = getSuccsOf(node).iterator(); succsIt.hasNext(); )
		    {
		    	N succ = succsIt.next();
		        DGEdge<N> edge = new DGEdge<N>(node, succ);
		        List<L> labels = edgeToLabels.get(edge);
				G.v().out.println("     " + succ + " [" + labels + "]");
		    }
		}
    }

}

 
