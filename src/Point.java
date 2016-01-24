import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;
import java.util.StringTokenizer;

public class Point {
	//image coordinates of this point (column, row)
	private int c;
	private int r;
	
	//candidate labels for this point
	private LinkedList<Rectangle> rectangles;
	
	public Point(int c, int r) {
		this.c = c;
		this.r = r;
		rectangles = new LinkedList<Rectangle>();
	}
	
	public void addRectangle(Rectangle rect) {
		rectangles.add(rect);
	}
	
	public LinkedList<Rectangle> getRectangles() {
		return rectangles;
	}
	
	public int getColumn() {
		return c;
	}
	
	public int getRow() {
		return r;
	}

	public static LinkedList<Point> randomPoints(int anzahl, int breite, int hoehe){
		LinkedList<Point> points = new LinkedList<Point>();
		Random random = new Random();
		for(int i = 0 ; i < anzahl ; i++){
			int c = random.nextInt(breite);
			int r = random.nextInt(hoehe);
			points.add(new Point(c,r));
		}
		return points;
	}

	public static LinkedList<Point> getPointsCopy(LinkedList<Point> points){
		LinkedList<Point> pointsCopy = new LinkedList<>();
		for(Point p : points){
			pointsCopy.add(new Point(p.c,p.r));
		}
		return pointsCopy;
	}

	public static LinkedList<Point> readPoints(String filename) throws NumberFormatException, IOException {
		LinkedList<Point> points = new LinkedList<Point>();
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line;
		while ((line = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line, ",");
			int c = Integer.parseInt(st.nextToken());
			int r = Integer.parseInt(st.nextToken());
			points.add(new Point(c, r));
		}
		br.close();
		
		return points;
	}
}