package asg4;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class tools {

	public tools() {
		// TODO Auto-generated constructor stub
	}

	public static String[] loadFile(String fileName) { // returns an array of
		// strings that each
		// elements is a line in
		// the file
		String result = "";
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				result = result + "/" + sCurrentLine;
				// System.out.println(sCurrentLine); //to be removed
			}

			result = result.substring(1, result.length());

		} catch (IOException e) {
			e.printStackTrace();
		}

		return result.split("/");
	}

	public static int[] getArray(String input) {
		String[] temp = input.split(" ");
		int[] a = new int[temp.length];
		for (int i = 0; i < a.length; i++) {
			a[i] = Integer.parseInt(temp[i]);
			// System.out.println(a[i]);//to be removed
		}
		return a;
	}

	public static int findMin(int[] x) {
		int min = x[0];

		for (int i = 1; i < x.length; i++)
			if (x[i] < min)
				min = x[i];

		return min;
	}

	public static void printArray(int[] X) {
		for (int i = 1; i < X.length; i++) {
			System.out.print(X[i] + " ");
		}
	}

	public static void printArray(char[] X) {
		for (int i = 1; i < X.length; i++) {
			System.out.print(X[i] + " ");
		}
	}

	public static void print2DArray(int[][] v) {

		for (int i = 1; i < v.length; i++) {
			for (int j = 1; j < v[i].length; j++) {
				System.out.print(v[i][j] + " ");
			}
			System.out.println();
		}
	}

	public static void print2DArray(double[][] v) {

		for (int i = 1; i < v.length; i++) {
			for (int j = 0; j < v[i].length; j++) {
				System.out.print(v[i][j] + " ");
			}
			System.out.println();
		}
	}

}
