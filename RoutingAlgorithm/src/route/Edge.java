package route;
/**
 *
 * @author Jenifer Cochran
 *
 */
public class Edge
{
   private final Vertex start;
   private final Vertex end;
   private final double cost;
   /**
    * An Edge or a Path from the start Vertex to the end Vertex
    * @param start starting vertex
    * @param end   ending vertex
    */
   public Edge(final Vertex start, final Vertex end)
   {
	   this.start = start;
	   this.end   = end;
	   this.cost  = start.getDistance(end);
   }
   /**
    * The starting Vertex of this Edge
    * @return starting vertex
    */
   public Vertex getStart()
   {
   	return this.start;
   }
   /**
    * The ending Vertex of thisEdge
    * @return the ending vertex
    */
   public Vertex getEnd()
   {
   	return this.end;
   }
   /**
    * The cost or weight of this Edge
    * @return the cost (distance)
    */
   public double getCost()
   {
   	return this.cost;
   }

}
