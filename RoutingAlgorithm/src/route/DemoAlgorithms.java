package route;

import java.util.Arrays;
import java.util.List;
/**
 *
 * @author Jenifer Cochran
 *
 */
public class DemoAlgorithms
{
	private static final List<Edge> EDGES;
	private static final List<Vertex> VERTICIES;
	private static Vertex a;
	private static Vertex e;

	static
    {
	          a = new Vertex("a",  -90.0,  54.0); //start
	   Vertex b = new Vertex("b",   90.0, -18.0);
	   Vertex c = new Vertex("c",   90.0,  54.0);
	   Vertex d = new Vertex("d", -180.0, -72.0);
	          e = new Vertex("e",  -45.0,  18.0); //end
	   Vertex f = new Vertex("f",   45.0, -90.0);

	   VERTICIES = Arrays.asList(a, b, c, d, e, f);

	   EDGES = Arrays.asList( new Edge(a, b),
			   				  new Edge(a, c),
			   				  new Edge(a, f),
			   				  new Edge(b, c),
			   				  new Edge(b, d),
			   				  new Edge(c, d),
			   				  new Edge(c, f),
			   				  new Edge(d, e),
			   				  new Edge(e, f));
	}

	private static final Vertex START = a;
	private static final Vertex END = e;

	/**
	 * Just a demo of how it works
	 * @param args not needed
	 */
	public static void main(final String[] args)
	{
	      Graph graph = new Graph(VERTICIES,EDGES);
	      //dijkstra with out target node
	      graph.dijkstra(START);
	      System.out.println("Dijkstra with out target node: ");
	      graph.printDijkstraPath(END);
	      //dijkstra with target node
	      graph.dijkstra(START, END);
	      System.out.println("\nDijkstra with target node: ");
	      graph.printDijkstraPath(END);
	      //astar algorithm
	      graph.aStar(START, END);
	      System.out.println("\nAstar with target node: (comulative distance) ");
	      graph.printAstarPath(END);
	}

}
