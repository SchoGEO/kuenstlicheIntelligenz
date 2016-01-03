//package twoSAT;
//
//import static org.junit.Assert.*;
//
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//
//import org.junit.Test;
//
//public class TwoSatTest {
//
//    @Test
//	public void test() {
//		Literal<String> a = new Literal<String>("a", true);
//		Literal<String> b = new Literal<String>("b", true);
//		Literal<String> c = new Literal<String>("c", true);
//		Literal<String> d = new Literal<String>("d", true);
//		Literal<String> e = new Literal<String>("e", true);
//		Literal<String> f = new Literal<String>("f", true);
//		Literal<String> g = new Literal<String>("g", true);
//		Literal<String> h = new Literal<String>("h", true);
//
//		Literal<String> i = new Literal<String>("i", true);
//
//		List<Clause<String>> formula = new LinkedList<Clause<String>>();
//		formula.add(new Clause<String>(a, b));
//		formula.add(new Clause<String>(c, d));
//		formula.add(new Clause<String>(e, f));
//		formula.add(new Clause<String>(g, h));
//		formula.add(new Clause<String>(b.negation(), c.negation()));
//		formula.add(new Clause<String>(d.negation(), e.negation()));
//		formula.add(new Clause<String>(f.negation(), g.negation()));
//		formula.add(new Clause<String>(h.negation(), a.negation()));
//
//		formula.add(new Clause<String>(i, i));
//		//formula.add(new Clause<String>(i.negation(), i.negation()));
//
//		Map<Literal<String>, Boolean> truthAssignment = TwoSatTest.isSatisfiable(formula);
//		assertNotNull(truthAssignment);
//
//		for (Entry<Literal<String>, Boolean> entry : truthAssignment.entrySet()) {
//			System.out.println(entry.getKey() + " " + entry.getValue());
//		}
//
//	}
//
//}
