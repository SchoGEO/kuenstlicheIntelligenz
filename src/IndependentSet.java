import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import TwoSatTest.TwoSat;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.strtree.STRtree;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import TwoSatTest.Clause;
import TwoSatTest.Literal;


public class IndependentSet {
	public static void main (String[] args) throws NumberFormatException, IOException, ContradictionException, TimeoutException {
		
		int rectangleWidth = 30;
		int rectangleHeight = 10;
		double scale = 1.0; //change scale to enlarge or shrink rectangles

		//iterateSolvablePointCloud(2000,200,1200,rectangleWidth,rectangleHeight);

		/**
		 * Punkte einlesen und zeichnen
		 */
		/*LinkedList<Point> readPoints = Point.readPoints("C:\\Users\\Jannes\\IdeaProjects\\kuenstlicheIntelligenz\\1453672036918_iterPoints.txt");
		RectangleList<Rectangle> pointRects = Rectangle.threePositionModel(readPoints,rectangleWidth,rectangleHeight);

		writeToSVG(pointRects, readPoints, "iterPoints_Test3.svg", true);*/
		/**
		 * Punkte einlesen, lösen und zeichnen
		 */
		LinkedList<Point> readPoints = Point.readPoints("C:\\Users\\Jannes\\IdeaProjects\\kuenstlicheIntelligenz\\1453672036918_iterPoints.txt");
		RectangleList<Rectangle> pointRects = Rectangle.threePositionModel(readPoints,rectangleWidth,rectangleHeight);

		STRtree rectanglesTree = new STRtree(pointRects.size());
		for(Rectangle r : pointRects){
			rectanglesTree.insert(r.getEnvelope(),r);
		}
		List<int[]> intersectionClauses = Rectangle.getIntersectionClauses(pointRects,rectanglesTree);
		int[] result = new int[2];
		try{
			result = solve(pointRects,intersectionClauses,readPoints);
		}catch(Exception e){
			System.out.println(e);
		}
		boolean satisfiable = (result[0]==1);
		if(satisfiable){
			writeToSVG(pointRects, readPoints, "iterPoints_Test3_solve.svg", false);
		}

		/**
		 * Laufzeiten vergleichen
		 */
		//compareRuntimes(4000,4000,6000,6000,"threePositionModel",rectangleWidth,rectangleHeight);

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
	 * @return true wenn lösbar, sonst false
	 * @throws ContradictionException
	 * @throws TimeoutException
	 */
	public static int[] solve(RectangleList<Rectangle> rectangles, List<int[]> intersectionClauses,
							  LinkedList<Point> points) throws ContradictionException, TimeoutException {
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

		long currentTime = System.currentTimeMillis();
		//initialize solver
		ISolver solver = SolverFactory.newDefault();

		String model = rectangles.getModel();
		System.out.println("A " + model + " is used!");

		solver.newVar(rectangles.size()); //number of available rectangles
		//solver.setTimeout (3600); // 1 hour timeout

		if(model.equals("threePositionModel2CNF") || model.equals("twoPositionModel")){
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
				switch (rectangles.getModel()){
					case "twoPositionModel":
						int[] twoClause = {p1,p2};
						solver.addClause(new VecInt(twoClause));
						break;
					case "threePositionModel2CNF":
						p3 = point_rect.get(2).getID();
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
				}
			}
		}
		if(model.equals("threePositionModel") || model.equals("fourPositionModel")){
			//for every point
			for (Point p : points){
				//get rectangles of this point
				LinkedList<Rectangle> point_rect = p.getRectangles();
				//get the IDs of the rectangles
				int p1 = point_rect.get(0).getID();
				int p2 = point_rect.get(1).getID();
				int p3 = point_rect.get(2).getID();
				int p4;

				//build clauses depending the model used
				switch (model){
					case "threePositionModel":
						int[] threeClause = {p1,p2,p3};
						solver.addClause(new VecInt(threeClause));
						break;
					case "fourPositionModel":
						p3 = point_rect.get(2).getID();
						p4 = point_rect.get(3).getID();
						int[] fourClause = {p1,p2,p3,p4};
						solver.addClause(new VecInt(fourClause));
						break;
				}
			}
		}

		for(int[] intersectionClause : intersectionClauses){
			solver.addClause(new VecInt(intersectionClause));
		}
		/*//Rechtecke pro Punkt
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
		}*/

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
		int solvable = (problem.isSatisfiable()) ? 1 : 0;
		long afterTime = System.currentTimeMillis();
		System.out.println("sat4j needs: " + (afterTime-currentTime) + " milliseconds!");
		int[] result = {solvable,(int)(afterTime-currentTime)};

		//wenn Problem lösbar
		if (solvable == 1) {
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

		} else {
			System.out.println("The problem is unsatisfiable.");
		}
		return result;
	}

	/**
	 * solve-Methode, welche den TwoSatTest-Solver von Keith Schwarz zur Lösung des Labelproblems verwendet
	 * @param rectangles Rechtecke, welche mögliche Labelpositionen darstellen
	 * @param points Punkte, welche von den Rechtecken umgeben sind
	 * @return true wenn lösbar, sonst false
	 */
	public static int[] twoSATSolve(RectangleList<Rectangle> rectangles, List<int[]> intersectionClauses,
									LinkedList<Point> points) {

		long currentTime = System.currentTimeMillis();
		//Liste, welche alle zu berücksichtigenden Klauseln bekommt
		List<Clause<Integer>> formula = new LinkedList<Clause<Integer>>();
		String model = rectangles.getModel();

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
				case "twoPositionModel":
					formula.add(new Clause<Integer>(r1_lit, r2_lit));
					break;
				case "threePositionModel2CNF":
					r3 = point_rect.get(2).getID();
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

		long intersectTime = System.currentTimeMillis();
		for(int[] intersectionClause : intersectionClauses){
			//Literale mit den IDs der sich überschneidenden Rechtecke erstellen
			Literal<Integer> lit1 = new Literal<>(-intersectionClause[0], false);
			Literal<Integer> lit2 = new Literal<>(-intersectionClause[1], false);

			//Klausel für sich überschneidende Rechtecke mit den Literalen bilden (jeweils negiert)
			formula.add(new Clause<>(lit1, lit2));
		}
		long afterIntersectTime = System.currentTimeMillis();
		long buildIntersectClauses = afterIntersectTime-intersectTime;

		/*//für jedes Rechteck (außer die des letzten Punktes)
		long goThroughNeigh = 0;
		long queryTree = 0;
		long goThroughRects1 = System.currentTimeMillis();
		for (int i = 1 ; i < rectangles.size() ; i++){
			//Envelope besorgen (Rechteck an der Stelle ID -1, weil Rechtecke in einer Liste mit Indizes vorliegen)
			Rectangle r1 = rectangles.get(i - 1);
			Envelope r1Env = r1.getEnvelope();

			//intersecting Envelopes besorgen
			long queryTree1 = System.nanoTime();
			List<Rectangle> intersections = rectTree.query(r1Env);
			long queryTree2 = System.nanoTime();
			queryTree += (queryTree2-queryTree1);

			for(Rectangle intersectRect : intersections){
				int intersectID = intersectRect.getID();
				//Wenn das überschneidende Rechteck zu den Nachbarn gehört beim nächsten Rechteck weitermachen

				if(r1.getNeighbors().contains(intersectID) || i == intersectID){
					break;
				}
				else {
					long goThroughNeigh1 = System.nanoTime();
					//Literale mit den IDs der sich überschneidenden Rechtecke erstellen
					Literal<Integer> lit1 = new Literal<>(i, true);
					Literal<Integer> lit2 = new Literal<>(intersectID, true);

					//Klausel für sich überschneidende Rechtecke mit den Literalen bilden (jeweils negiert)
					formula.add(new Clause<>(lit1.negation(), lit2.negation()));
					long goThroughNeigh2 = System.nanoTime();
					goThroughNeigh += (goThroughNeigh2-goThroughNeigh1);
				}
			}
			rectTree.remove(r1Env,r1);
		}
		long goThroughRects2 = System.currentTimeMillis();
		System.out.println("Intersections rausfinden insgesamt: "+(goThroughRects2 -goThroughRects1));
		System.out.println("In den Nachbarrechtecken schauen: "+(goThroughNeigh/1000000));
		System.out.println("Query an den Tree: "+(queryTree/1000000));*/

		/*//Rechtecke pro Punkt
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
		}*/

		Map<Literal<Integer>, Boolean> truthAssignment = TwoSat.isSatisfiable(formula);
		long afterTime = System.currentTimeMillis();
		System.out.println("twoSatSolve needs: " + (afterTime-currentTime-buildIntersectClauses) + " milliseconds!");
		int solvable = (truthAssignment != null) ? 1 : 0;
		int[] result = {solvable,(int)(afterTime-currentTime)};

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

				if (!positive && !entry.getValue()) {
					rectangles.get(rect_id).setSelected(true);
				}
				if (!positive && entry.getValue()) {
					rectangles.get(rect_id).setSelected(false);
				}

				//System.out.println(entry.getKey() + " " + entry.getValue());
			}

		}
		else {
			System.out.println("The problem is unsatisfiable!");
		}
		return result;
	}


	/**
	 *
	 * @param maxPoints --> maximale Größe der Instanz, für die die Laufzeiten verglichen werden sollen
	 * @param model "threePositionModel" to compare sat4j(3CNF formula) with 2SAT (2CNF formula)
	 *              "threePositionModel2CNF" to compare sat4j(2CNF formula) with 2SAT (2CNF formula)
	 */
	public static void compareRuntimes(int minPoints, int maxPoints, int minFrameEdge, int maxFrameEdge, String model, int rectWidth, int rectHeight){
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


			for(int x = 1 ; x <= 1000 ; x++){
				for(int i = minPoints ; i <= maxPoints ; i = i+500){
					for(int y = minFrameEdge ; y <= maxFrameEdge ; y = y + 500){
						++id;
						LinkedList<Point> points = Point.randomPoints(i,y,y);
						LinkedList<Point> points2CNF = new LinkedList<>();
						RectangleList<Rectangle> rectangles;

						//Size für rectangles2CNF besorgen
						int size;
						switch (model){
							case "twoPositionModel":
								size = points.size() * 2;
								break;
							case "threePositionModel":
								size = points.size() * 4;
								break;
							default:
								size = 0;
						}
						RectangleList<Rectangle> rectangles2CNF = new RectangleList<>(size);
						//ArrayList<Rectangle> rectangles = new ArrayList<>(i*3);
						//Rectangle.resetList(rectangles);

						switch(model){
							case "twoPositionModel":
								rectangles = Rectangle.twoPositionModel(points, rectWidth, rectHeight);
								break;
							case "threePositionModel":
								points2CNF = Point.getPointsCopy(points);
								rectangles = Rectangle.threePositionModel(points, rectWidth, rectHeight);
								rectangles2CNF = Rectangle.threePositionModel2CNF(points2CNF, rectWidth, rectHeight);
								break;
							case "threePositionModel2CNF":
								rectangles = Rectangle.threePositionModel2CNF(points, rectWidth, rectHeight);
								break;
							case "fourPositionModel":
								rectangles = Rectangle.fourPositionModel(points, rectWidth, rectHeight);
								break;
							default:
								rectangles = new RectangleList<>(0);
								rectangles.setModel("default");
								System.out.println("There was no model found, RectanglesList could not be initialized");
						}

						//STRtree erstellen, über den überlappende Rechtecke bestimmt werden können
						STRtree rectanglesTree = new STRtree(rectangles.size());
						STRtree rectanglesTree2CNF = null;
						for(Rectangle r : rectangles){
							rectanglesTree.insert(r.getEnvelope(),r);
						}

						//Im Fall, dass rectangles2CNF instanziiert wurde
						if(rectangles2CNF.size() > 0){
							rectanglesTree2CNF = new STRtree(rectangles2CNF.size());
							for(Rectangle r : rectangles2CNF){
								rectanglesTree2CNF.insert(r.getEnvelope(),r);
							}
						}

						//Klauseln für überlappende Rechtecke erstellen
						List<int[]> intersectionClauses = Rectangle.getIntersectionClauses(rectangles,rectanglesTree);
						List<int[]> intersectionClauses2CNF = null;
						if(rectangles2CNF.size() > 0){
							intersectionClauses2CNF = Rectangle.getIntersectionClauses(rectangles2CNF, rectanglesTree2CNF);
						}

						//solve SAT instance and write solution to file, if it exists
						int[] sat4jResult = new int[2];
						try{
							sat4jResult = solve(rectangles, intersectionClauses, points);
						}catch(Exception e){
							System.out.println(e);
						}
						boolean satisfiable = (sat4jResult[0]==1);
						long timestamp = 0;
						if (satisfiable) {
							timestamp = System.currentTimeMillis();
							String distributionPath = newDirStr+timestamp+"_" +model+"_"+i+"_"+y+".svg";
							//write all points and rectangles to file
							writeToSVG(rectangles, points, distributionPath, true);
							String sat4jPath = newDirStr +timestamp+"_"+ model+"_"+i+"_"+y+"_sat4j.svg";
							writeToSVG(rectangles, points, sat4jPath, false);
						}

						writer.write(id+","+ "sat4j" + "," + i + "," + y + "," + satisfiable + "," + sat4jResult[1] + "\n");

						Rectangle.resetList(rectangles);

						//solve SAT instance and write solution to file, if it exists
						//boolean satisfiable = solve(rectangles, points, 3);
						int[] twoSatResult;
						if(rectangles2CNF.size() > 0){
							twoSatResult = twoSATSolve(rectangles2CNF, intersectionClauses2CNF, points2CNF);
						}
						else{
							twoSatResult = twoSATSolve(rectangles, intersectionClauses, points);
						}
						satisfiable = (twoSatResult[0]==1);
						if(satisfiable){
							String twoSatPath = newDirStr +timestamp+"_"+ model+"_"+i+"_"+y+"_2sat.svg";
							if(rectangles2CNF.size() > 0) {
								writeToSVG(rectangles2CNF, points2CNF, twoSatPath, false);
							}
							else{
								writeToSVG(rectangles, points, twoSatPath, false);
							}
						}

						writer.write(id+","+ "2Sat" + "," + i + "," + y + "," + satisfiable + "," + twoSatResult[1] + "\n");
					}
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

	public static void iterateSolvablePointCloud(int iterations, int startPoints,int frameWidth, int rectWidth, int rectHeight){
		LinkedList<Point> iterPoints = new LinkedList<>();
		boolean satisfiable = false;
		do{
			iterPoints = Point.randomPoints(startPoints,frameWidth,frameWidth);
			RectangleList<Rectangle> iterRectangles = Rectangle.threePositionModel(iterPoints,rectWidth,rectHeight);
			STRtree rectanglesTree = new STRtree(iterRectangles.size());
			for(Rectangle r : iterRectangles){
				rectanglesTree.insert(r.getEnvelope(),r);
			}
			List<int[]> intersectionClauses = Rectangle.getIntersectionClauses(iterRectangles,rectanglesTree);
			int[] result = new int[2];
			try{
				result = solve(iterRectangles,intersectionClauses,iterPoints);
			}catch(Exception e){
				System.out.println(e);
			}
			satisfiable = (result[0]==1);
		}while(!satisfiable);

		for(int i = 0 ; i <= iterations ; i++){
			LinkedList<Point> addPoint = Point.randomPoints(1,frameWidth,frameWidth);
			iterPoints.addLast(addPoint.getLast());
			RectangleList<Rectangle> iterRectangles = Rectangle.threePositionModel(iterPoints,rectWidth,rectHeight);
			STRtree rectanglesTree = new STRtree(iterRectangles.size());
			for(Rectangle r : iterRectangles){
				rectanglesTree.insert(r.getEnvelope(),r);
			}
			List<int[]> intersectionClauses = Rectangle.getIntersectionClauses(iterRectangles,rectanglesTree);
			int[] result = new int[2];
			try{
				result = solve(iterRectangles,intersectionClauses,iterPoints);
			}catch(Exception e){
				System.out.println(e);
			}
			satisfiable = (result[0]==1);
			if(satisfiable){
				System.out.println("New Point added to iterPoints!");
			}
			else{
				iterPoints.removeLast();
			}
		}
		String filename = "./"+System.currentTimeMillis() + "_iterPoints.txt";
		File newFile = new File(filename);

		try(BufferedWriter writer = new BufferedWriter(new FileWriter(newFile))) {
			for(Point p : iterPoints){
				writer.write(p.getColumn()+","+p.getRow()+"\n");
			}
		}catch(IOException io){
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
