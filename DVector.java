import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

/**
 * Reads in a network file and performs the Distance Vector Routing Algorithm on the topology
 * @author magicantler
 *
 */
public class DVector {
	
	TreeMap<String, Node> node;
	
	public DVector(String filepath){
		
		File file=null;
		try{
			file = new File(filepath);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//all of the nodes in the topology
		node = new TreeMap<String, Node>();
		Scanner scan;
		try {
			scan = new Scanner(file);
		
			/*
			 * Reads in nodes, adds them to the topology if they aren't already in it, and sets the neighbors of said nodes
			 */
			while(scan.hasNextLine()){
				String line = scan.nextLine();
				String[] components = line.split("[ ]+");
				String host1 = components[0];
				String host2 = components[1];
				Double distance = Double.parseDouble(components[2]);
				
				
				if(!node.containsKey(host1)){
					node.put(host1,new Node(host1));
				}
				if(!node.containsKey(host2)){
					node.put(host2, new Node(host2));
				}
				//add to the list of neighbors of the node
				node.get(host1).setNeighbor(host2, distance);
				node.get(host2).setNeighbor(host1, distance);
				
			}
			scan.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		} catch (Exception e){
			e.printStackTrace();
			System.exit(0);
		}
		populate();		
	}
	
	/**
	 * Populate each nodes distance vectors for non-neighbors
	 */
	private void populate(){
		for(Object value:node.values()){
			((Node) value).setNonNeighbors(node.keySet());
		}

	}
	/**
	 * Prints the distance vectors of each node in the topology
	 */
	public void print(){
		
		for(Object value:node.values()){
			((Node)value).printDistanceVector();
		}
	}
	/**
	 * returns the data member node treemap
	 * @return
	 */
	public TreeMap<String, Node> getNodes(){
		return node;
	}
	
	
	/**
	 * Sends the updated dVector to the specified neighbors
	 * @param treeMap
	 * @param neighbors
	 */
	public void sendMessage(TreeMap<String, Double> treeMap, Set<String> neighbors, String fromNode){
		for(String s: neighbors){
			node.get(s).receiveVector(treeMap, fromNode);
		}
	}
	
	/**
	 * sends the initial distance vectors of each node to its neighbors
	 */
	public void init(){
		for(Node n:node.values()){
			sendMessage(n.getDVector(),n.getNeighbors(),n.getName());
		}
		
	}
	/**
	 * Constructs and prints the final table to stdout
	 */
	public void printTable(){
		
		System.out.println("\t\t\tDistance/First Hop from Source to Destination\n");
		System.out.println("\t\t\t       INF = 'not reachable'\n");
		System.out.println("\t\t\t         Destination\n");
		System.out.print("Source\t\t");
		
		/*
		 * prints a node to stdout for constructing a table as a new column entry
		 */
		for(String s: node.keySet()){
			System.out.print(s + "\t");
		}
		System.out.println();
		/*
		 * formats table with dashes
		 */
		for(int i = 0; i<node.keySet().size(); i++)
		{
			System.out.print("-----------");
		}
		
		/*
		 * appends node name to the table as a new row
		 */
		for(String s:node.keySet()){
			System.out.println();
			System.out.print("\t" + s + "|\t");
			//gets the distance vector of the specified node
			TreeMap<String, Double> tm = node.get(s).getDVector();
			
			/*
			 * Prints the distance from the given node to its destination node as well as the first hop node
			 */
			for(String j: tm.keySet()){
				String hop = node.get(s).getFirstHopToDest(j);
				double dist = tm.get(j);
				if(dist==-1.0){
					System.out.print("INF\t");
				}
				else{
					System.out.print(tm.get(j)+"/"+hop+"\t");
				}
			}
		}
		System.out.println();
	}

	public static void main(String[] args){
			//reads in file and constructs nodes
			if(args.length!=1){
				System.out.println("Error, please provide one argument of the correct filepath for the network");
				System.exit(0);
			}
			DVector vectorAlg = new DVector(args[0]);

			vectorAlg.init();
			//vectorAlg.print();
			TreeMap<String, Node> node = vectorAlg.getNodes();
			int nodeCount = node.keySet().size();
			/*
			 *exchanges routing tables b/w neighbors, recompute tables
			 *and sends distance routing vector to neighbors, until the algorithm
			 *converges (or no more changed can be made to nodes - i.e. updating halts)
			 */
			while(nodeCount>0){
				nodeCount = node.keySet().size();
				/*
				 * applies the distance vector algorithm to a node an if a change is made to its distance vecotr, then
				 * send off the new distance vector to its neighbors
				 */
				for(String s: node.keySet()){
					/*
					 * If the algorithm updated the distance vector of the node, then we send said distance vector off to that nodes neighbors
					 */
					if(node.get(s).runAlgorithm()){					
						TreeMap<String, Double> dVector = node.get(s).getDVector();
						Set<String> neighbors = node.get(s).getNeighbors();
						vectorAlg.sendMessage(dVector, neighbors, s);
					}
					else{
						//increment counter which is how we know when we're done!
						//when they have all stopped changing, we know we're done!
						nodeCount--;
					}
				}
			}
			vectorAlg.printTable();
	}

}
