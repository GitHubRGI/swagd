package route;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
/**
 *
 * @author Jenifer Cochran
 *
 */
public class Graph
{
	private final Map<Vertex, List<Vertex>> graph;
	private int count = 0;
	/**
	 * Creates an adjacency list based on vertices and edges given.
	 *
	 * @param vertices the vertices or nodes that are in the graph
	 * @param edges     the edges or paths between each vertex in the graph
	 */
	public Graph(final List<Vertex> vertices, final List<Edge> edges)
	{
		//Creates an adjacency list based on the vertices and edges
		this.graph = vertices.stream()
						     .map(vertex -> {   //create a list of vertices connected to this current vertex
						     	 				final List<Vertex> connectedVertices = edges.stream()
						     	 													   .filter(edge -> edge.getStart().equals(vertex))
						     	 													   .map(edge-> edge.getEnd())
						     	 													   .collect(Collectors.toList());

						     	 				return new AbstractMap.SimpleEntry<>(vertex, connectedVertices);
						     				})
						     .collect(Collectors.toMap(entry -> entry.getKey(),
						     		                   entry -> entry.getValue()));

	}

	 /**based on https://code.google.com/p/a-star-java/source/browse/AStar/src/aStar/AStar.java?r=7
	  * Searches through the graph based on distance from the starting node and calculates the shortest
	  * path from starting vertex to the ending vertex
	  * @param start starting vertex of the path
	  * @param end   ending vertex of the path
	  */
	 public void aStar(final Vertex start, final Vertex end)
	 {
		 this.count = 0; //this is to see how many nodes we visit
		 this.clearVertices();
	     if((this.checkVertexInGraph(start) && this.checkVertexInGraph(end)) != true)
	     {
             return;
         }

		 final PriorityQueue<Vertex> openList   = new PriorityQueue<>(this.astarComparator);
		 final PriorityQueue<Vertex> closedList = new PriorityQueue<>(this.astarComparator);
		 //initialize starting Vertex
		 start.distanceFromStart = 0;
		 start.previous = start;
		 openList.add(start);

		 while(!openList.isEmpty())
		 {
			 final Vertex currentVertex = openList.poll();//get the Vertex with lowest cost
			 closedList.add(currentVertex); //put it in "done" pile
			 //if current vertex is the target then we are done
			 if(currentVertex.equals(end))
			 {
				 System.out.printf("\n\nNumber of nodes visited for Astar: %d", this.count);
				 return;
			 }
			 //for each reachable Vertex
			 for(final Vertex successor: this.graph.get(currentVertex))
			 {
				 //if the closed list already searched this vertex, skip it
				 if(closedList.contains(successor))
				 {
					 continue;
				 }
				 //calculate a tentative distance (see if this is better than what you already may have)
				 final double distanceFromStart = currentVertex.distanceFromStart + currentVertex.getDistance(successor);
				 final double distanceFromEnd   = successor.getDistance(end);

				 if(!openList.contains(successor))//if we dont have it, add it
				 {
					 successor.distanceFromStart = distanceFromStart;
					 successor.distanceFromEnd   = distanceFromEnd;
					 successor.previous = currentVertex;
					 openList.add(successor);
				 }
				 //if this is better then update the values and parent Vertex (previous)
				 else if(distanceFromStart < currentVertex.distanceFromStart)
				 {
					 successor.distanceFromStart = distanceFromStart;
					 successor.distanceFromEnd   = distanceFromEnd;
					 successor.previous          = currentVertex;
				 }
				 this.count++;
			 }
		 }
	 }

	/**Dijkstra's algorithm based on http://rosettacode.org/wiki/Dijkstra%27s_algorithm#Java
	 * This will calculate the shortest path from the start Vertex to every other Vertex in the graph
	 *
	 * @param start vertex that you want to find the shortest path for to the other vertices in the graph
	 */
	 public void dijkstra(final Vertex start)
	 {
		  this.count = 0;
		  this.clearVertices();
	      if(this.checkVertexInGraph(start) != true)
	      {
	          return;
	      }

	      final PriorityQueue<Vertex> vertexQueue = new PriorityQueue<>(this.dijkstrasComparator);
	      vertexQueue.add(start);
	      start.previous = start;

	      while(!vertexQueue.isEmpty())
	      {
	    	  final Vertex currentVertex = vertexQueue.poll();//gets the vertex with min cost/distance

	    	  //visit every edge exiting current Vertex and update costs
	    	  this.visitReachableVertices(currentVertex, vertexQueue);
	      }
	      System.out.printf("\nNumber of nodes visited for dijkstra without target: %d\n", this.count);
	 }

	 /**Based on http://en.wikipedia.org/wiki/Dijkstra%27s_algorithm
	  * This algorithm will find the shortest path from the starting Vertex to the ending Vertex.
	  * @param start starting vertex
	  * @param end   ending vertex
	  */
	 public void dijkstra(final Vertex start, final Vertex end)
	 {
		 this.count = 0;
		 this.clearVertices();
		 if((this.checkVertexInGraph(start) && this.checkVertexInGraph(end)) != true)
		 {
		     return;
		 }

	      final PriorityQueue<Vertex> vertexQueue = new PriorityQueue<>((o1, o2) -> Double.compare(o1.minDistance, o2.minDistance));
	      vertexQueue.add(start);
	      start.previous = start;

	      while(!vertexQueue.isEmpty())
	      {
	    	  final Vertex currentVertex = vertexQueue.poll();//gets the vertex with min cost/distance

	    	  if(currentVertex.equals(end))//if this is the target node stop the search
	    	  {
	    		  System.out.printf("\n\nNumber of nodes visited for dijkstra with target: %d", this.count);
	    		  return;
	    	  }
	    	  //visit every edge exiting current Vertex
	    	  this.visitReachableVertices(currentVertex, vertexQueue);
	      }
	 }

	 /**
	  * Visits the neighboring nodes and updates the cost if the new cost is less
	  *
	  * @param currentVertex  the vertex to which you search the neighboring nodes
	  * @param queue          the vertex queue that holds the vertices that need to be searched/ordered by lowest cost
	  */
	 public void visitReachableVertices(final Vertex currentVertex, final PriorityQueue<Vertex> queue)
	 {
		 for(final Vertex reachableVertex: this.graph.get(currentVertex))
   	     {
   		      final double weight = reachableVertex.getDistance(currentVertex);//get the distance between nodes

   		      if(weight < reachableVertex.minDistance) //if the cost is less, change the weight
   		      {
   			      queue.remove(reachableVertex);//remove it from the queue if it is there
   			      reachableVertex.minDistance = weight;//adjust to new values
   			      reachableVertex.previous = currentVertex;//adjust the path
   			      queue.add(reachableVertex); //add it to the queue in the correct order
   		      }
   		      this.count++;
   	     }
	 }

	 /**
	 * Clear out the data in the Vertices in this graph
	 */
	public void clearVertices()
	 {
		 //clear the nodes of previous values
		 this.graph.keySet().stream().forEach(vertex -> {
			                                          vertex.previous          = null;
			                                          vertex.minDistance       = Double.MAX_VALUE;
			                                          vertex.distanceFromEnd   = Double.MAX_VALUE;
			                                          vertex.distanceFromStart = Double.MAX_VALUE;
			                                       });
	 }
   /**
     * Prints an error message if the vertex is not in the graph
     *
	 * @param vertex the vertex that we wish to check if it is in the current graph
	 * @return True if the given vertex is in the graph, false otherwise
	 */
	// checks to see if the vertex is in this graph
	 public boolean checkVertexInGraph(final Vertex vertex)
	 {
		 if (!this.graph.containsKey(vertex))
	      {
	         System.err.printf("Graph doesn't contain the vertex \"%s\"\n", vertex.getName());
	         return false;
	      }
		 return true;
	 }
	 //prints out the last path found in graph starting with the end node
   /**
	 * prints out the last path found in graph starting with the end node
	 *
	 * @param vertex the target vertex (the end of the path)
	 */
	public void printDijkstraPath(final Vertex vertex) {
         if (vertex.equals(vertex.previous))
         {
            System.out.printf("%s", vertex.getName());
         }
         else if (vertex.previous == null)
         {
            System.out.printf("%s(unreached)", vertex.getName());
         }
         else
         {
        	this.printDijkstraPath(vertex.previous);
            System.out.printf(" -> %s(%f)", vertex.getName(), vertex.minDistance);
         }
      }

   /**
    * prints out the last path found in graph starting with the end node
    *
    * @param vertex the target vertex (the end of the path)
    */
	public void printAstarPath(final Vertex vertex)
	 {
		 if (vertex.equals(vertex.previous))
         {
            System.out.printf("%s", vertex.getName());
         }
         else if (vertex.previous == null)
         {
            System.out.printf("%s(unreached)", vertex.getName());
         }
         else
         {
        	this.printAstarPath(vertex.previous);
        	System.out.printf(" -> %s(%f)", vertex.getName(), vertex.distanceFromStart);

         }
	 }

	 Comparator<Vertex> astarComparator = (o1, o2) -> Double.compare(o1.distanceFromEnd, o2.distanceFromEnd);

	 Comparator<Vertex> dijkstrasComparator = (o1, o2) -> Double.compare(o1.minDistance, o2.minDistance);
}
