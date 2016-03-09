
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.strtree.STRtree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class Rectangle {
	private static int maxVar = 0;
	private int c1;
	private int c2;
	private int r1;
	private int r2;
	private int id; //IDs are automatically generated with an incrementing counter
	private Envelope env;	//vividsolutions Envelope for using STRtree (easier testing of intersections)
	private LinkedList<Integer> neighbors;
	private boolean isSelected;
	
	public Rectangle(int c1, int r1, int c2, int r2) {
		this.c1 = c1;
		this.c2 = c2;
		this.r1 = r1;
		this.r2 = r2;
		this.env = getEnvelope(); //Envelope is being created while instantiating the Rectangle object
		id = ++maxVar;
	}
	
	public boolean intersects(Rectangle other) {
		if ( other.c1 > this.c2 ) return false; // other rectangle lies to the right of this rectangle
		if ( other.c2 < this.c1 ) return false; // other rectangle lies to the left of this rectangle
		if ( other.r1 > this.r2 ) return false; // other rectangle lies blow this rectangle
		if ( other.r2 < this.r1 ) return false; // other rectangle lies above this rectangle
		return true;
	}
	
	public boolean equals(Rectangle other){
		return this.id == other.id;
	}

	public int getX1() {
		return c1;
	} 
	
	public int getX2() {
		return c2;
	}
	
	public int getY1() {
		return r1;
	} 
	
	public int getY2() {
		return r2;
	}
	
	public int getID() {
		return id;
	}

	public LinkedList<Integer> getNeighbors() {return neighbors; }

	/**
	 * Erstellt für eine gegebene Menge von Rechtecken alle Klauseln für Überlappungen von Rechtecken.
	 * Für benachbarte Rechtecke welche zum selben Punkt gehören werden keine Klauseln erstellt
	 * @param rectangles Rechtecke für die die Klauseln erstellt werden
	 * @param rectTree STRtree welcher die Envelopes der Rechtecke enthält. Wird benötigt um Anfragen von
	 *                 Rechtecküberlappungen zu stellen.
	 * @return Liste aus int-Arrays welche die einzelnen Klauseln darstellen
	 */
	public static List<int[]> getIntersectionClauses(RectangleList<Rectangle> rectangles, STRtree rectTree){
		//Liste mit den Klauseln für die Rückgabe
		List<int[]> intersectionClauses = new LinkedList<>() ;

		for(Rectangle rectangle : rectangles){
			Envelope rectEnv = rectangle.env;
			List<Rectangle> intersections = rectTree.query(rectEnv);

			for(Rectangle intersectRect : intersections){
				int intersectID = intersectRect.getID();

				//Wenn das überschneidende Rechteck zu den Nachbarn gehört beim nächsten Rechteck weitermachen
				if(rectangle.getNeighbors().contains(intersectID) || rectangle.getID() == intersectID){
					continue;
				}
				else {
					//Arrays mit den IDs der sich überschneidenden Rechtecke erstellen
					int[] intersectionClause = {-rectangle.getID(),-intersectID};

					//in die Liste für die Ausgabe tun
					intersectionClauses.add(intersectionClause);
				}
			}
			rectTree.remove(rectEnv,rectangle);
		}
		return intersectionClauses;
	}
	
	public static LinkedList<Rectangle> readRectangles(String filename) throws NumberFormatException, IOException {
		LinkedList<Rectangle> rectangles = new LinkedList<Rectangle>();
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line;
		while ((line = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line, ",");
			int c1 = Integer.parseInt(st.nextToken());
			int r1 = Integer.parseInt(st.nextToken());
			int c2 = Integer.parseInt(st.nextToken());
			int r2 = Integer.parseInt(st.nextToken());
			rectangles.add(new Rectangle(c1, r1, c2, r2));
		}
		br.close();
		
		return rectangles;
	}
	
	public String toString() {
		return "" + c1 + "," + r1 + "," + c2 + "," + r2;
	}

	public static RectangleList<Rectangle> fourPositionModel(LinkedList<Point> points, int w, int h) {
		RectangleList<Rectangle> rectangles = new RectangleList<>(4 * points.size());
		rectangles.setModel("fourPositionModel");
		for (Point p : points) {
			
			//lower left
			Rectangle r1 = new Rectangle(p.getColumn() - w, p.getRow(), p.getColumn(), p.getRow() + h);
			
			//lower right
			Rectangle r2 = new Rectangle(p.getColumn(), p.getRow(), p.getColumn() + w, p.getRow() + h);
			
			//upper left
			Rectangle r3 = new Rectangle(p.getColumn() - w, p.getRow() - h, p.getColumn(), p.getRow());
			
			//upper right
			Rectangle r4 = new Rectangle(p.getColumn(), p.getRow() - h, p.getColumn() + w, p.getRow());

			//add rectangles to set of possible labels for p
			p.addRectangle(r1);
			p.addRectangle(r2);
			p.addRectangle(r3);
			p.addRectangle(r4);
			
			//add rectangles to set of all labels
			rectangles.add(r1); 
			rectangles.add(r2); 
			rectangles.add(r3); 
			rectangles.add(r4); 
		}
		return rectangles;
	}

	/**
	 * threePositionModel2CNF: drei mögliche Positionen durch vier nebeneinanderliegende Rechtecke
	 * @param points Punkte für die die Beschriftungsrechtecke erstellt werden sollen
	 * @param w Breite der Rechtecke
	 * @param h Höhe der Rechtecke
	 * @return Liste mit den erstellten Rechtecken
	 */
	public static RectangleList<Rectangle> threePositionModel2CNF(LinkedList<Point> points, int w, int h) {
		maxVar = 0;
		RectangleList<Rectangle> rectangles = new RectangleList<Rectangle>(4 * points.size());
		rectangles.setModel("threePositionModel2CNF");
		for (Point p : points) {

			//upper left left
			Rectangle r1 = new Rectangle(p.getColumn() - w, p.getRow() - h, p.getColumn() - (w/2), p.getRow());

			//upper left right
			Rectangle r2 = new Rectangle(p.getColumn() - (w/2), p.getRow() - h, p.getColumn(), p.getRow());

			//upper right left
			Rectangle r3 = new Rectangle(p.getColumn(), p.getRow() - h, p.getColumn() + (w/2), p.getRow());

			//upper right right
			Rectangle r4 = new Rectangle(p.getColumn() + (w/2), p.getRow() - h, p.getColumn() + w, p.getRow());

			//set neighbor rectangles of new rectangles
			r1.neighbors = new LinkedList<>();
			r1.neighbors.add(r2.getID());

			r2.neighbors = new LinkedList<>();
			r2.neighbors.add(r1.getID());
			r2.neighbors.add(r3.getID());

			r3.neighbors = new LinkedList<>();
			r3.neighbors.add(r2.getID());
			r3.neighbors.add(r4.getID());

			r4.neighbors = new LinkedList<>();
			r4.neighbors.add(r3.getID());

			//add rectangles to set of possible labels for p
			p.addRectangle(r1);
			p.addRectangle(r2);
			p.addRectangle(r3);
			p.addRectangle(r4);

			//add rectangles to set of all labels
			rectangles.add(r1);
			rectangles.add(r2);
			rectangles.add(r3);
			rectangles.add(r4);
		}
		return rectangles;
	}

	public static RectangleList<Rectangle> threePositionModel(LinkedList<Point> points, int w, int h) {
		maxVar = 0;
		RectangleList<Rectangle> rectangles = new RectangleList<>(3 * points.size());
		rectangles.setModel("threePositionModel");
		for (Point p : points) {

			//upper left
			Rectangle r1 = new Rectangle(p.getColumn() - w, p.getRow() - h,p.getColumn(), p.getRow());

			//upper middle
			Rectangle r2 = new Rectangle(p.getColumn() - (w/2), p.getRow() - h,p.getColumn() + (w/2), p.getRow());

			//upper right
			Rectangle r3 = new Rectangle(p.getColumn(), p.getRow() - h, p.getColumn() + w, p.getRow());

			//set neighbor rectangles of new rectangles
			r1.neighbors = new LinkedList<>();
			r1.neighbors.add(r2.getID());
			r1.neighbors.add(r3.getID());

			r2.neighbors = new LinkedList<>();
			r2.neighbors.add(r1.getID());
			r2.neighbors.add(r3.getID());

			r3.neighbors = new LinkedList<>();
			r3.neighbors.add(r1.getID());
			r3.neighbors.add(r2.getID());


			//add rectangles to set of possible labels for p
			p.addRectangle(r1);
			p.addRectangle(r2);
			p.addRectangle(r3);

			//add rectangles to set of all labels
			rectangles.add(r1);
			rectangles.add(r2);
			rectangles.add(r3);
		}
		return rectangles;
	}

	public static RectangleList<Rectangle> twoPositionModel(LinkedList<Point> points, int w, int h) {
		RectangleList<Rectangle> rectangles = new RectangleList<>(2 * points.size());
		rectangles.setModel("twoPositionModel");
		for (Point p : points) {

			//upper left
			Rectangle r3 = new Rectangle(p.getColumn() - w, p.getRow() - h, p.getColumn(), p.getRow());

			//upper right
			Rectangle r4 = new Rectangle(p.getColumn(), p.getRow() - h, p.getColumn() + w, p.getRow());

			//add rectangles to set of possible labels for p
			p.addRectangle(r3);
			p.addRectangle(r4);

			//add rectangles to set of all labels
			rectangles.add(r3);
			rectangles.add(r4);
		}
		return rectangles;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	/**
	 * Methode resetList setzt die selected-Variable jedes Rechtecks in der Liste auf false
	 * @param rectangles Eine Liste mit Rechtecken
	 */
	public static void resetList(List<Rectangle> rectangles) {
		for (Rectangle rectangle: rectangles) {
			rectangle.setSelected(false);
		}
	}

	/**
	 * Erstellt Envelope wenn noch nicht vorhanden und gibt Envelope zurück
	 * @return Envelope Objekt, welches Koordinaten des Rechtecks enthält
	 */
	public Envelope getEnvelope(){
		if(this.env == null){
			return new Envelope((double)this.getX1(),(double)this.getX2(),(double)this.getY2(),(double)this.getY1());
		}
		else return this.env;
	}

	/**
	 * Entfernt Referenzen zu Nachbarrechtecken und Envelope-Objekten.
	 * Wird benötigt wenn sehr viele Probleminstanzen nacheinander geschaffen werden.
	 * Werden die Referenzen nicht entfernt wird der Platz im Arbeitsspeicher trotz Garbage Collector nicht geräumt.
	 * @param rectangles Liste mit Rechtecken, deren Referenzen entfernt werden sollen
	 */
	public static void removeReferences(RectangleList<Rectangle> rectangles){
		for (Rectangle r : rectangles){
			r.env.setToNull();
			r.neighbors = null;
			//rectangles.remove(r);
		}
	}
	
	
}
