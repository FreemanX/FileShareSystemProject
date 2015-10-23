package asg4;

import java.util.*;

public class Asg4 {

	private int[] dis;
	private int[] pre;
	private int[] disTime;
	private int[] finTime;

	public Asg4(int[] distance, int[] predecessor) {
		// TODO Auto-generated constructor stub
		this.dis = distance;
		this.pre = predecessor;
	}

	public Asg4(int[] discoverTime, int[] finishTime, int[] predecessor) {
		// TODO Auto-generated constructor stub
		this.disTime = discoverTime;
		this.finTime = finishTime;
		this.pre = predecessor;
	}

	public static int[][] getGraph(String fileName) {
		String[] input = tools.loadFile(fileName);
		int numOfVertices = tools.getArray(input[0])[0];
		int n = numOfVertices + 1;
		int[][] graph = new int[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++)
				graph[i][j] = 0;
		}

		for (int i = 1; i < n; i++) {
			int[] v = tools.getArray(input[i]);
			for (int j = 0; j < v.length; j++) {
				graph[i][v[j]] = 1;
			}
		}

		return graph;
	}

	// ////// Breath-First Search///////

	public static Asg4 BFS(int[][] graph) {
		Queue<Integer> q = new LinkedList<Integer>();
		int n = graph.length;
		char[] color = new char[n];
		int[] pred = new int[n];
		int[] d = new int[n];

		for (int i = 0; i < n; i++) // initialization
		{
			color[i] = 'w';
			pred[i] = 0;
			d[i] = Integer.MAX_VALUE;
		}
		// System.out.println("=======Begin========"); // to be removed
		for (int i = 1; i < n; i++) {
			// System.out.println("Start of iteration " + i); // to be removed
			if (color[i] == 'w') {
				// ////BFSVisit///////
				color[i] = 'g';
				d[i] = 0;
				q.add(i);
				while (!q.isEmpty()) {
					int u = q.remove();
					// System.out.println("u is " + u); // to be removed
					for (int v = 1; v < n; v++) {
						if (graph[u][v] == 0)
							continue;
						// System.out.println("v is " + v); // to be removed
						if (color[v] == 'w') {
							color[v] = 'g';
							d[v] = d[u] + 1;
							pred[v] = u;
							q.add(v);
						}
					}
					color[u] = 'b';
				}
				// //////////////////////////
			}
		}
		return new Asg4(d, pred);
	}

	// ////////////////Depth-First Search//////////////

	public static Asg4 DFS(int[][] graph) {
		int n = graph.length;
		char[] color = new char[n];
		int[] pred = new int[n];
		int[] d = new int[n];
		int[] f = new int[n];
		int time = 0;
		for (int i = 0; i < n; i++) // initialization
		{
			color[i] = 'w';
			pred[i] = 0;
			d[i] = 0;
			f[i] = 0;
		}

		for (int u = 1; u < n; u++) {
			if (color[u] == 'w') {
				// ///DFSVisit//////
				DFSVisit(u, graph, n, color, pred, d, f, time);
				// /////////////////
			}
		}

		return new Asg4(d, f, pred);
	}

	private static int DFSVisit(int u, int[][] graph, int n, char[] color,
			int[] pred, int[] d, int[] f, int time) {
		color[u] = 'g';
		d[u] = ++time;
		for (int v = 1; v < n; v++) {
			if (graph[u][v] == 0)
				continue; // v in Adj(u)
			if (color[v] == 'w') {
				pred[v] = u;
				time = DFSVisit(v, graph, n, color, pred, d, f, time);
			}
		}
		color[u] = 'b';
		f[u] = ++time;
		return time;
	}

	// Cycle Visit///////////////////////////////////
	public static boolean Cycle(int[][] graph) {
		int n = graph.length;
		char[] color = new char[n];
		int[] pred = new int[n];
		boolean result = false;
		for (int i = 0; i < n; i++) // initialization
		{
			color[i] = 'w';
			pred[i] = 0;
		}

		for (int u = 1; u < n; u++) {
			if (color[u] == 'w') {
				// ///Visit//////
				result = Visit(u, result, graph, n, color, pred);
				if(result == true) return result;
				// /////////////////
			}
		}
		return result;
	}

	private static boolean Visit(int u, boolean result, int[][] graph, int n,
			char[] color, int[] pred) {
		color[u] = 'g';
		for (int v = 1; v < n; v++) {
			if (graph[u][v] == 0)
				continue; // v in Adj(u)
			if (color[v] == 'w') {
				pred[v] = u;
				result = Visit(v, result, graph, n, color, pred);
			} else if (pred[u] != 0 && color[v] == 'g' && v != pred[u]) {
				//System.out.println("Result is true: pred[u] is " + pred[u] + ", u is " + u + ", v is " + v);
				result = true;
				return result;
			}
		}
		color[u] = 'b';
		//result = false;
		return result;
	}

	// /////////////////////////////////////////////////
	public static void main(String[] args) {
		int[][] g = Asg4.getGraph("data.txt");
		System.out.println("Input Graph is:");
		tools.print2DArray(g);

		System.out.println("\n==================BFS===================");
		Asg4 bfs = BFS(g);
		System.out.println("Distance array:");
		tools.printArray(bfs.dis);
		System.out.println("\nPredecessor array:");
		tools.printArray(bfs.pre);

		System.out.println("\n==================DFS===================");
		Asg4 dfs = DFS(g);
		System.out.println("Discover time:");
		tools.printArray(dfs.disTime);
		System.out.println("\nFinish time:");
		tools.printArray(dfs.finTime);
		System.out.println("\nPredecessor array:");
		tools.printArray(dfs.pre);
		System.out.println("\n==================Cycle===================");
		System.out.println(Cycle(g));
	}

}
