import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class Node{
	
	/*
	 *Note: Each node maintains a vector of distances (and next hops) to all destinations
	 *Each node is also aware of its neighbors and the link cost to reach them!
	 */
	
	//set distances to neighbors
	HashMap<String, Double> neighbors;
	
	//distance vector which contains distances to every node in the network
	TreeMap<String,Double> dVector;
	
	//each node holds a temp dVector of all its neighbors. It's updated when a dVector is received via sendMessage
	HashMap<String,TreeMap<String, Double>> neighborDVector;
	
	//map which contains the first hop for a node to its given destination
	// <dest, first hop>
	HashMap<String, String> firstHop;
	
	
	//name of the node
	String name;
	
	/**
	 *initialize distance to self with 0 and infinity to other nodes within the vector
	 * @param name - name of the node
	 */
	public Node(String name){
		this.name = name;
		neighbors = new HashMap<String, Double>();
		dVector = new TreeMap<String,Double>();
		neighborDVector = new HashMap<String,TreeMap<String, Double>>();
		firstHop = new HashMap<String, String>();
		dVector.put(name, 0.0);
	}
	
	/**
	 * Sets the distances to neighbors and concurrently adds that distance to the dVector
	 * @param name - name of the neighboring node
	 * @param distance - distance to the neighboring node
	 */
	public void setNeighbor(String name, double distance){
		neighbors.put(name, distance);
		dVector.put(name, distance);
		firstHop.put(name, name);
	}
	
	/**
	 * Adds non-neighbors to the distance vector and sets their distance to -1 (INF).
	 * Also adds non-neighbors to the firstHop data structure.
	 * @param set - a set of all non-neighboring nodes
	 */
	public void setNonNeighbors(Set<String> set){
		/*
		 * Adds node to dVector and firstHop if it hasn't been added yet
		 */
		for(String s:set){
			if(!dVector.containsKey(s)){
				dVector.put(s, -1.0);
				firstHop.put(name, this.getName());
			}
		}
	}
	
	/**
	 * Display the distance vector
	 */
	public void printDistanceVector(){
		System.out.println("Node " + name + " distance vector:");
		/*
		 * prints a node's dVector
		 */
		for(Map.Entry<String,Double> e: dVector.entrySet()){
			String key = e.getKey();
			Double value = e.getValue();
			if(value!=-1)
				System.out.println(key + " | " + value);
			else
				System.out.println(key + " | " + "INF");
		}
	}
	
	/**
	 * Returns the neighbors
	 * @return
	 */
	public Set<String> getNeighbors(){
		return neighbors.keySet();
	}
	
	/**
	 * Returns all of the nodes in the topology
	 * @return
	 */
	public Set<String> getAllNodes(){
		return dVector.keySet();
	}
	
	/**
	 * Gets the current set cost to the destination node
	 * @param n - the destination node
	 * @return
	 */
	public double getCostToNode(String n){
		return dVector.get(n);
	}
	
	/**
	 * returns the first hop node of this object that is taken to get to the specified destination
	 * @param n - the destination node
	 * @return
	 */
	public String getFirstHopToDest(String n){
		return firstHop.get(n);
	}

	/**
	 * update the dVector
	 * @param key - name of the node which we are setting the summed distance to
	 * @param value - the distance to get to the key
	 */
	public void updateDV(String key, double value){
		this.dVector.put(key, value);
	}
	/**
	 * Updates the first hop node of the given node
	 * @param dest - the given destination node
	 * @param neighbor - the neighbor this node would have to hop to first in order to get to
	 * the destination node
	 */
	public void updateFirstHop(String dest, String neighbor){
		firstHop.put(dest, neighbor);
	}

	/**
	 * Returns the distance vector treemap
	 * @return
	 */
	public TreeMap<String,Double> getDVector(){
		return dVector;
	}
	
	/**
	 * Returns the nodes that will be receiving the distance vector/
	 * 
	 * dVector class will get the dVector of node x, loop through the set, and send it to each. they will receive it, set their hashmap/tree, and then run their own algorithm 
	 * @return
	 */
	public boolean runAlgorithm(){
		
		boolean change=false;
		/*
		 * For every node (other than 'this'), we peek at the distance vectors received from neighboring nodes, and compute the min - updating the current DV
		 */
				//and if the min is smaller than the current value of that dVector, set it, and then have that node send a message to it's neighbors.
			for(String neighbor:this.getNeighbors()){
					
				TreeMap<String, Double> wDVector = neighborDVector.get(neighbor);
					
				for(String n:this.getAllNodes()){
					
				if(!this.name.equals(n)){
				//cost of neighbor to N
				Double neighborToNCost = wDVector.get(n);
				//link distance to specified neighbor
				Double xToNeighborCost = this.neighbors.get(neighbor);
					
				//check if distance is infinity, in which case, leave the dVector alone.
				if(neighborToNCost!=-1){
					
					//find the min of {cost to current iteration neighbor + cost from that neighbor to the current destination node n}
					double xToNCost = this.getCostToNode(n);
					Double sumCost = neighborToNCost + xToNeighborCost;
					sumCost=Math.round((sumCost*100))/100.0;
					if(xToNCost==-1){
						//then we know that the sum is smaller than the current value of INF
						
						this.updateDV(n,sumCost);
						this.updateFirstHop(n, neighbor);
						change=true;
						}
						else{
							//double sumCost = neighborToNCost + xToNeighborCost;

							if(sumCost < xToNCost){
							//	System.out.println("Updating DV 2" +sumCost);
								//if the new cost is shorter, then update the distance vector!
								//and have said node, send a message to it's neighbors - containing its new dVector
								this.updateDV(n,sumCost);
								this.updateFirstHop(n, neighbor);
								change=true;
							}
						}
					}
				}
				}
			}
		return change;
	}
	
	/**
	 * @return - the name of this node
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * receives vector from neighbor, and replaces the old stored vector of that neighbor
	 * @param treeMap
	 * @return
	 */
	public void receiveVector(TreeMap<String, Double> treeMap, String fromNode){
		neighborDVector.put(fromNode, treeMap);
	}
}
