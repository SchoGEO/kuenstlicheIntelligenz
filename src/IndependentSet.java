import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import TwoSatTest.TwoSat;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import TwoSatTest.Clause;
import TwoSatTest.Literal;

import javax.swing.*;


public class IndependentSet {
	public static void main (String[] args) throws NumberFormatException, IOException, ContradictionException, TimeoutException {
		
		int rectangleWidth = 30;
		int rectangleHeight = 10;
		double scale = 1.0; //change scale to enlarge or shrink rectangles

		compareRuntimes(10,6000,15000,15000,3,rectangleWidth,rectangleHeight);
		/*//read points from text file
		//LinkedList<Point> points = Point.readPoints("points_ext.csv");
		//create random points
		LinkedList<Point> points = Point.randomPoints(80,1200,1200);
		
		//create label candidates for the points
		ArrayList<Rectangle> rectangles = Rectangle.threePositionModel(points, (int) (rectangleWidth * scale), (int) (rectangleHeight * scale));
			
		//write all points and rectangles to file
		writeToSVG(rectangles, points, "3PM_distribution_random80_1200x1200_w60xh20.svg", true);
		
		//solve SAT instance and write solution to file, if it exists
		long currentTime = System.currentTimeMillis();
		//boolean satisfiable = solve(rectangles, points, 3);
		boolean satisfiable = solve(rectangles, points, 3);
		long afterTime = System.currentTimeMillis();

		System.out.println("Zeit in Millisekunden: " +(afterTime-currentTime));

		if (satisfiable) {
			writeToSVG(rectangles, points, "3PM_selection_random80_1200x1200_w60xh20_1.svg", false);
		}

		Rectangle.resetList(rectangles);

		//solve SAT instance and write solution to file, if it exists
		currentTime = System.currentTimeMillis();
		//boolean satisfiable = solve(rectangles, points, 3);
		satisfiable = twoSATSolve(rectangles, points, 3);
		afterTime = System.currentTimeMillis();

		System.out.println("Zeit in Millisekunden: " +(afterTime-currentTime));

		if (satisfiable) {
			writeToSVG(rectangles, points, "3PM_selection_random80_1200x1200_w60xh20_2.svg", false);
		}*/

	}

	/**
	 * solve-Methode, welche die sat4j-Bibliothek zur Lösung des Labelproblems verwendet
	 * @param rectangles Rechtecke, welche mögliche Labelpositionen darstellen
	 * @param points Punkte, welche von den Rechtecken umgeben sind
	 * @param model Angabe des Modells, welches vorliegt (2-Position, 3-Position, 4-Position Modell)
	 * @return true wenn lösbar, sonst false
	 * @throws ContradictionException
	 * @throws TimeoutException
	 */
	public static boolean solve(ArrayList<Rectangle> rectangles, LinkedList<Point> points, int model) throws ContradictionException, TimeoutException {
		/*
		 * TODO: --> Erledigt
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
		/*
		 * TODO:
		 * - solve Methode anpassen, sodass auch twoPositionModel funktioniert --> erledigt
		 * - solve Methode anpassen, sodass auch threePositionModel funktioniert --> erledigt
		 * 		- Rectangle Klasse muss angepasst werden mit Methode threePositionModel --> erledigt
		 * 		- komplizierter, weil jeder Punkt vier Rechtecke bekommt (a,b,c,d), aus denen sich drei Labelrechtecke
		 *		zusammensetzten lassen --> erledigt
		 * - Methode schreiben, mit der sich die Laufzeit automatisch testen lässt mit größer werdenden Instanzen
		 * - 2-Sat-Solver umsetzen für 2-Position- und 3-Position-Modell
		 */

		//initialize solver
		ISolver solver = SolverFactory.newDefault();

		if (model == 2 ^ model == 3 ^ model == 4){
			System.out.println("A " + model + "-PositionModel is used!");
		}
		//if not throw RuntimeException
		else{
			throw new RuntimeException("A Two-, Three- or Four-PositionModel has to be used!");
		}
		solver.newVar(rectangles.size()); //number of available rectangles
		//solver.setTimeout (3600); // 1 hour timeout

		//for every point
		for (Point p : points){
			//get rectangles of this point
			LinkedList<Rectangle> point_rect = p.getRectangles();
			//get the IDs of the rectangles
			int p1 = point_rect.get(0).getID();
			int p2 = point_rect.get(1).getID();
			int p3;
			int p4;

			//build clauses depending the model used
			switch (model){
				case 2:		int[] twoClause = {p1,p2};
							solver.addClause(new VecInt(twoClause));
							break;
				case 3:		p3 = point_rect.get(2).getID();
							p4 = point_rect.get(3).getID();
							int[] aOrC = {p1,p3};
							int[] dOrB = {p4,p2};
							int[] bOrC = {p2,p3};
							int[] notAOrB = {-p1,p2};
							int[] notDOrC = {-p4,p3};
							solver.addClause(new VecInt(aOrC));
							solver.addClause(new VecInt(dOrB));
							solver.addClause(new VecInt(bOrC));
							solver.addClause(new VecInt(notAOrB));
							solver.addClause(new VecInt(notDOrC));
							break;
				case 4:		p3 = point_rect.get(2).getID();
							p4 = point_rect.get(3).getID();
							int[] fourClause = {p1,p2,p3,p4};
							solver.addClause(new VecInt(fourClause));
							break;
			}
		}

		//Rechtecke pro Punkt
		int rectPerPoint = model;
		if (model == 3) rectPerPoint = 4;
		//für jedes Rechteck (außer die des letzten Punktes)
		for (int i = 1 ; i < rectangles.size() - rectPerPoint ; i++){
			//Rechteck besorgen (an der Stelle ID -1, weil Rechtecke in einer Liste mit Indizes vorliegen)
			Rectangle r1 = rectangles.get(i-1);
			//ID des nächsten Rechtecks zum Vergleichen bestimmen
			int nextPos = (i / rectPerPoint) * rectPerPoint + 1;
			if (i % rectPerPoint != 0) nextPos += rectPerPoint;
			//ausgewähltes Rechteck r1 mit allen verbleibenden Rechtecken auf Überschneidung vergleichen
			for (int j = nextPos ; j < rectangles.size() ; j++){
				Rectangle r2 = rectangles.get(j-1);
				//prüfen ob sich die Rechtecke überschneiden
				if (r1.intersects(r2)) {
					//Klausel für sich überschneidende Rechtecke mit den IDs bilden ("minus" für "not i or not j")
					int[] intersectClause = {-i, -j};
					//Klausel dem Solver übergeben
					solver.addClause(new VecInt(intersectClause));
				}
			}
		}

		/**
		 * --- ALTE METHODE ZUM VERGLEICH DER RECHTECK-ÜBERSCHNEIDUNGEN ---
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
		 */

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

	/**
	 * solve-Methode, welche den TwoSatTest-Solver von Keith Schwarz zur Lösung des Labelproblems verwendet
	 * @param rectangles Rechtecke, welche mögliche Labelpositionen darstellen
	 * @param points Punkte, welche von den Rechtecken umgeben sind
	 * @param model Angabe des Modells, welches vorliegt (2-Position, 3-Position)
	 * @return true wenn lösbar, sonst false
	 */
	public static boolean twoSATSolve(ArrayList<Rectangle> rectangles, LinkedList<Point> points, int model) {

		//Liste, welche alle zu berücksichtigenden Klauseln bekommt
		List<Clause<Integer>> formula = new LinkedList<Clause<Integer>>();

		//for every point
		for (Point p : points){
			//get rectangles of this point
			LinkedList<Rectangle> point_rect = p.getRectangles();
			//get the IDs of the rectangles
			int r1 = point_rect.get(0).getID();
			int r2 = point_rect.get(1).getID();
			int r3;
			int r4;

			Literal<Integer> r1_lit = new Literal<Integer>(r1, true);
			Literal<Integer> r2_lit = new Literal<Integer>(r2, true);

			//build clauses depending the model used
			switch (model){
				case 2:		formula.add(new Clause<Integer>(r1_lit, r2_lit));
							break;
				case 3:		r3 = point_rect.get(2).getID();
							r4 = point_rect.get(3).getID();
							Literal<Integer> r3_lit = new Literal<Integer>(r3, true);
							Literal<Integer> r4_lit = new Literal<Integer>(r4, true);
							formula.add(new Clause<>(r1_lit, r3_lit));
							formula.add(new Clause<>(r4_lit, r2_lit));
							formula.add(new Clause<>(r2_lit, r3_lit));
							formula.add(new Clause<>(r1_lit.negation(), r3_lit.negation()));
							formula.add(new Clause<>(r4_lit.negation(), r2_lit.negation()));
							formula.add(new Clause<>(r1_lit.negation(), r2_lit));
							formula.add(new Clause<>(r4_lit.negation(), r3_lit));
							break;
			}
		}

		//Rechtecke pro Punkt
		int rectPerPoint = model;
		if (model == 3) rectPerPoint = 4;
		//für jedes Rechteck (außer die des letzten Punktes)
		for (int i = 1 ; i < rectangles.size() - rectPerPoint ; i++){
			//Rechteck besorgen (an der Stelle ID -1, weil Rechtecke in einer Liste mit Indizes vorliegen)
			Rectangle r1 = rectangles.get(i-1);
			//ID des nächsten Rechtecks zum Vergleichen bestimmen
			int nextPos = (i / rectPerPoint) * rectPerPoint + 1;
			if (i % rectPerPoint != 0) nextPos += rectPerPoint;
			//ausgewähltes Rechteck r1 mit allen verbleibenden Rechtecken auf Überschneidung vergleichen
			for (int j = nextPos ; j < rectangles.size() ; j++){
				Rectangle r2 = rectangles.get(j-1);
				//prüfen ob sich die Rechtecke überschneiden
				if (r1.intersects(r2)) {
					//Literale mit den IDs der sich überschneidenden Rechtecke erstellen
					Literal<Integer> i_lit = new Literal<>(i, true);
					Literal<Integer> j_lit = new Literal<>(j, true);

					//Klausel für sich überschneidende Rechtecke mit den Literalen bilden (jeweils negiert)
					formula.add(new Clause<>(i_lit.negation(), j_lit.negation()));
				}
			}
		}
		long currentTime = System.currentTimeMillis();
		Map<Literal<Integer>, Boolean> truthAssignment = TwoSat.isSatisfiable(formula);
		long afterTime = System.currentTimeMillis();
		System.out.println("twoSatSolve needs: " + (afterTime-currentTime) + " milliseconds!");

		if (truthAssignment != null) {
			System.out.println("The problem is satisfiable.");
			//System.out.println("Truth-Assignment:");
			for (Entry<Literal<Integer>, Boolean> entry : truthAssignment.entrySet()) {
				//Rectangle-Index besorgen (1 kleiner als die ID)
				int rect_id = entry.getKey().value() - 1;
				//Schauen ob das Literal (positive / negative) ist -> z.B. 17 oder ~17
				boolean positive = entry.getKey().isPositive();
				//Wenn postitive den boolean-Wert aus truthAssignment übernehmen
				if (positive) {
					rectangles.get(rect_id).setSelected(entry.getValue());
				}
				//Wenn nicht positive und truthAssignment-Wert false ist, rectangle auf selected (true) setzen
				//weil "~17: false" gleichbedeutend mit "17: true" ist!
				else {
					rectangles.get(rect_id).setSelected(!entry.getValue());
				}

				/*if (!positive && !entry.getValue()) {
					rectangles.get(rect_id).setSelected(true);
				}
				if (!positive && entry.getValue()) {
					rectangles.get(rect_id).setSelected(false);
				}*/

				//System.out.println(entry.getKey() + " " + entry.getValue());
			}
			return true;
		}
		else {
			System.out.println("The problem is unsatisfiable!");
			return false;
		}

	}


	/**
	 *
	 * @param maxPoints --> maximale Größe der Instanz, für die die Laufzeiten verglichen werden sollen
	 */
	public static void compareRuntimes(int minPoints, int maxPoints, int minFrameEdge, int maxFrameEdge, int model, int rectWidth, int rectHeight){
		String newDirStr = "./" + String.valueOf(System.currentTimeMillis()) + "/";
		File newDir = new File(newDirStr);
		try{
			newDir.mkdir();
		}
		catch(Exception e){
			System.out.println(e);
		}
		String filename = newDirStr + System.currentTimeMillis() + ".txt";
		File newFile = new File(filename);
		int id = 0;
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(newFile)))
		{
			//Kopfzeile schreiben
			writer.write("ID" + "," + "Solver" + "," + "Points" + "," + "RasterSize" + "," + "solvable" + "," + "Runtime (ms)" + "\n");

			for(int i = minPoints ; i <= maxPoints ; i = i+100){
				for(int y = minFrameEdge ; y <= maxFrameEdge ; y = y + 50){
					++id;
					LinkedList<Point> points = Point.randomPoints(i,y,y);
					ArrayList<Rectangle> rectangles = new ArrayList<>(i*4);

					switch(model){
						case 2:
							rectangles = Rectangle.twoPositionModel(points, rectWidth, rectHeight);
							break;
						case 3:
							rectangles = Rectangle.threePositionModel(points, rectWidth, rectHeight);
							break;
						case 4:
							rectangles = Rectangle.fourPositionModel(points, rectWidth, rectHeight);
							break;
					}
					String distributionPath = newDirStr + model+"PM_"+i+"_"+y+".svg";
					//write all points and rectangles to file
					writeToSVG(rectangles, points, distributionPath, true);

					//solve SAT instance and write solution to file, if it exists
					long currentTime = System.currentTimeMillis();
					boolean satisfiable = false;
					try{
						satisfiable = solve(rectangles, points, 3);
					}catch(Exception e){
						System.out.println(e);
					}
					long afterTime = System.currentTimeMillis();
					String sat4jPath = newDirStr + model+"PM_"+i+"_"+y+"sat4j.svg";
					writeToSVG(rectangles, points, sat4jPath, false);

					writer.write(id+","+ "sat4j" + "," + i + "," + y + "," + satisfiable + "," + (afterTime-currentTime) + "\n");

					Rectangle.resetList(rectangles);

					//solve SAT instance and write solution to file, if it exists
					currentTime = System.currentTimeMillis();
					//boolean satisfiable = solve(rectangles, points, 3);
					satisfiable = twoSATSolve(rectangles, points, 3);
					afterTime = System.currentTimeMillis();

					String twoSatPath = newDirStr + model+"PM_"+i+"_"+y+"2sat.svg";

					writeToSVG(rectangles, points, twoSatPath, false);

					writer.write(id+","+ "2Sat" + "," + i + "," + y + "," + satisfiable + "," + (afterTime-currentTime) + "\n");
				}
			}
		}
		catch(IOException io)
		{
			System.out.println(io);
		}

		try {
			newFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
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
