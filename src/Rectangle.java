
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

	public static List<int[]> getIntersectionClauses(List<Rectangle> rectangles, STRtree rectTree){
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
		/*//Rechtecke pro Punkt
		int rectPerPoint = 4;
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
					intersectionClauses.add(intersectClause);
				}
			}
		}*/

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

	public static ArrayList<Rectangle> fourPositionModel(LinkedList<Point> points, int w, int h) {
		ArrayList<Rectangle> rectangles = new ArrayList<Rectangle>(4 * points.size());
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
	 * threePositionModel: drei mögliche Positionen durch vier nebeneinanderliegende Rechtecke
	 * @param points
	 * @param w
	 * @param h
	 * @return
	 */
	public static ArrayList<Rectangle> threePositionModel(LinkedList<Point> points, int w, int h) {
		maxVar = 0;
		ArrayList<Rectangle> rectangles = new ArrayList<Rectangle>(4 * points.size());
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

	public static ArrayList<Rectangle> twoPositionModel(LinkedList<Point> points, int w, int h) {
		ArrayList<Rectangle> rectangles = new ArrayList<Rectangle>(2 * points.size());
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
	 * If this.env is not set, a new Envelope is being created and returned, else this.env is returned
	 * @return Envelope object, which contains coordinates of this rectangle
	 */
	public Envelope getEnvelope(){
		if(this.env == null){
			return new Envelope((double)this.getX1(),(double)this.getX2(),(double)this.getY2(),(double)this.getY1());
		}
		else return this.env;
	}
	
	
}
