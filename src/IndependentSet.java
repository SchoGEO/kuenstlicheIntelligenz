import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;


public class IndependentSet {
	public static void main (String[] args) throws NumberFormatException, IOException, ContradictionException, TimeoutException {
		
		int rectangleWidth = 60;
		int rectangleHeight = 50;
		double scale = 1.0; //change scale to enlarge or shrink rectangles
		
		//read points from text file
		//LinkedList<Point> points = Point.readPoints("points_ext.csv");
		//create random points
		LinkedList<Point> points = Point.randomPoints(10,500,500);
		
		//create label candidates for the points
		ArrayList<Rectangle> rectangles = Rectangle.fourPositionModel(points, (int) (rectangleWidth * scale), (int) (rectangleHeight * scale));
			
		//write all points and rectangles to file
		writeToSVG(rectangles, points, "rectangles_random_500x500_w60xh50.svg", true);
		
		//solve SAT instance and write solution to file, if it exists
		long currentTime = System.currentTimeMillis();
		boolean satisfiable = solve(rectangles, points);
		long afterTime = System.currentTimeMillis();

		System.out.println("Zeit in Millisekunden: " +(afterTime-currentTime));

		if (satisfiable) {
			writeToSVG(rectangles, points, "selection_random_500x500_w60xh50.svg", false);
		}

	}
	
	public static boolean solve(ArrayList<Rectangle> rectangles, LinkedList<Point> points) throws ContradictionException, TimeoutException {
		/*
		 * TODO: 
		 * Set up SAT formula to model the following problem:
		 * - For each point, one of its four rectangles has to be selected
		 * - For each two intersecting rectangles, at most one rectangle can be selected
		 * 
		 * Solve the SAT formula with sat4j
		 * 
		 * If the instance is satisfiable, 
		 * - use the method setSelected to indicate whether a rectangle is selected
		 * - return true  
		 */

		//initialize solver
		ISolver solver = SolverFactory.newDefault();
		solver.newVar(points.size()*4); //number of possible labels = point-count*4
		solver.setTimeout (3600); // 1 hour timeout

		//for every point
		for (Point p : points){
			//get 4 rectangles of this point
			LinkedList<Rectangle> point_rect = p.getRectangles();
			//get the IDs of the 4 rectangles
			int p1 = point_rect.get(0).getID();
			int p2 = point_rect.get(1).getID();
			int p3 = point_rect.get(2).getID();
			int p4 = point_rect.get(3).getID();
			//build newClause with IDs of 4 rectangles
			int[] newClause = {p1, p2, p3, p4};
			//add newClause to the solver
			solver.addClause(new VecInt(newClause));
		}

		//for every available rectangle
		for (Rectangle r1 : rectangles) {
			//and every other available rectangle
			for (Rectangle r2 : rectangles) {
				//get IDs of rectangles
				int r1_id = r1.getID();
				int r2_id = r2.getID();

				//check if rectangles are the same
				if (r1_id == r2_id) continue;

				//check if the rectangles intersect
				if (r1.intersects(r2)) {
					//build clause of intersecting rectangles "minus" for "not r1_id or not r2_id"
					int[] intersectClause = {-r1_id, -r2_id};
					//add clause to solver
					solver.addClause(new VecInt(intersectClause));
				}
			}
		}

		IProblem problem = solver;

		//wenn Problem lösbar
		if (problem.isSatisfiable()) {
			System.out.println("The problem is satisfiable.");
			//Lösung übergeben
			int[] solution = problem.model();
			//für jedes Element in der Lösung
			for (int i = 0; i < solution.length; i++) {
				//ID des rectangle besorgen
				int rec_id = rectangles.get(i).getID();
				//Wenn die Lösung an der Stelle i größer 0 ist, soll selected "true" sein
				boolean selected = solution[i] > 0;
				//ID des rectangle ausgeben und ob es in der Lösung enthalten ist
				System.out.println(rec_id + " = " + selected);
				//wenn das rectangle in der Lösung enthalten ist, die Instanz-Variable isSelected auf "true" setzen
				if (selected) {
					rectangles.get(i).setSelected(selected);
				}
			}
			return true;

		} else {
			System.out.println("The problem is unsatisfiable.");
			return false;
		}

	}
	
	
	public static void writeToSVG( ArrayList<Rectangle> rectangles, LinkedList<Point> points, String filename, boolean all) throws FileNotFoundException, UnsupportedEncodingException {
		
		//bounding box
		int c_min = Integer.MAX_VALUE;
		int r_min = Integer.MAX_VALUE;
		int c_max = Integer.MIN_VALUE;
		int r_max = Integer.MIN_VALUE;
		for (Rectangle r : rectangles) {
			c_min = Math.min(c_min, r.getX1());
			r_min = Math.min(r_min, r.getY1());
			c_max = Math.max(c_max, r.getX2());
			r_max = Math.max(r_max, r.getY2());
		}
		c_min -= 50;
		r_min -= 50;
		c_max += 50;
		r_max += 50;
				
		//create svg file
		PrintWriter writer = new PrintWriter(filename, "UTF-8");
		
		//write svg meta data
		writer.println("<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\""+ (c_max - c_min) + "\" height=\""+ (r_max - r_min) + "\" version=\"1.1\">");
				
		int i = 0;
		int j = 0;
				
		//write rectangles
		for (Rectangle r : rectangles) {
			if ( all || r.isSelected() ) {
				int r_width = r.getX2() - r.getX1();
				int r_height = r.getY2() - r.getY1();
				writer.println("<rect id=\""+ j++ + "\" x=\"" + (r.getX1() - c_min) + "\" y=\"" + (r.getY1() - r_min) + "\" width=\"" + r_width + "\" height=\"" + r_height + "\" style=\"fill:none;stroke-width:1;stroke:rgb(0,0,0)\" />");
			}
		}

		//write points
		for (Point p : points) {
			writer.println("<circle id=\""+ i++ + "\" cx=\"" + (p.getColumn() - c_min) + "\" cy=\"" + (p.getRow() - r_min) + "\" r=\"5\" stroke=\"black\" />");
		}
		
		//close file
		writer.println("</svg>");
		writer.close();
	}
	
	
	
}
