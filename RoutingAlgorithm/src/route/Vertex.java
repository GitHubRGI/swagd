package route;
/**
 *
 * @author Jenifer Cochran
 *
 */
public class Vertex
{
	private final double x;
	private final double y;
	private final String name;
	/**
	 * parent of this vertex (in accordance to the path)
	 */
	public Vertex previous;  //parent node
	/**
	 * the minimum distance to this vertex
	 */
	public double minDistance = Double.MAX_VALUE;

	/**
	 * Distance from source vertex
	 */
	public double distanceFromStart = Double.MAX_VALUE;
	/**
	 * Distance from target vertex
	 */
	public double distanceFromEnd   = Double.MAX_VALUE;

	/**
	 * @param name   Name of the Vertex ("A")
	 * @param xValue xValue (longitude)
	 * @param yValue yValue (latitude)
	 */
	public Vertex(final String name, final double xValue, final double yValue)
	{
		this.x = xValue;
		this.y = yValue;
		this.name = name;
	}
    /**
     * Calculates the distance between this Vertex and the parameter
     * @param end the vertex that needs to be calculated the distance to
     * @return distance from this vertex to end vertex
     */
	public double getDistance(final Vertex end)
	{
		//distance formula
		return Math.sqrt(Math.pow(this.y - end.y, 2) + Math.pow(this.x - end.x, 2));
	}
    /**
     * Returns the name associated with this Vertex
     * @return the name
     */
	public String getName() {
		return this.name;
	}

    @Override
	public boolean equals(final Object other)
	{
        if(this.getClass() ==  other.getClass())
        {
            return isEqual(this, (Vertex)other);
        }
        return false;
	}

	@Override
    public int hashCode()
    {
        return Double.valueOf(this.x).hashCode() ^ Double.valueOf(this.y).hashCode() ^ this.getName().hashCode();
    }
    /**
	 * Determines if the two Vertices are Equal depending on their
	 * x and y value and the name of the node
	 * @param first  a vertex to compare
	 * @param second a vertex to compare
	 * @return true if their x, y, and names are equal, false otherwise
	 */
	public static boolean isEqual(final Vertex first, final Vertex second)
	{
	    return first == null ? second == null : (first.x == second.x &&
	                                             first.y == second.y &&
	                                             first.name.equals(second.name));

	}

}
