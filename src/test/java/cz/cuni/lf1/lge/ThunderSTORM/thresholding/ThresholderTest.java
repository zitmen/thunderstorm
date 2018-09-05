
package cz.cuni.lf1.lge.ThunderSTORM.thresholding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Vector;

import org.junit.Test;

import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;
import cz.cuni.lf1.lge.ThunderSTORM.filters.EmptyFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.BoxFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import ij.process.FloatProcessor;

public class ThresholderTest {

	@Test
	public void testSimpleNumber() {
		double thr = Thresholder.getThreshold("120.5");
		assertEquals(120.5, thr, 0.001);
	}

	@Test
	public void testFormula() {
		Vector<IFilterUI> filters = new Vector<IFilterUI>();
		IFilterUI filter = new EmptyFilter();
		filters.add(filter);
		Thresholder.loadFilters(filters);
		Thresholder.setActiveFilter(0);
		assertSame(filter.getThreadLocalImplementation(), Thresholder.getActiveFilter());

		FloatProcessor fp = new FloatProcessor(new float[][] { { 1, 2, 3, 4, 5 }, { 1, 2, 3, 4, 5 }, {
			1, 2, 3, 4, 5 }, { 1, 2, 3, 4, 5 }, { 1, 2, 3, 4, 5 } });
		Thresholder.setCurrentImage(fp);
		assertSame(fp, Thresholder.getCurrentImage());

		filter.getThreadLocalImplementation().filterImage(fp);
		float result = Thresholder.getThreshold("mean(I+F)+1");
		assertEquals(7, result, 1e-5);

		try {
			Thresholder.getThreshold("std(Wave.F)"); // unknown filter
			fail("should have thrown exception");
		}
		catch (FormulaParserException e) {}
	}

	@Test
	public void testStd() {
		setupThresholder();
		float calc = Thresholder.getThreshold("std(I)");
		float expected = 1.4434f;
		assertEquals(expected, calc, 1e-4);
	}

	@Test
	public void testMean() {
		setupThresholder();
		float calc = Thresholder.getThreshold("mean(I)");
		float expected = 3;
		assertEquals(expected, calc, 1e-5);
	}

	@Test
	public void testMedian() {
		setupThresholder();
		float calc = Thresholder.getThreshold("median(I)");
		float expected = 3;
		assertEquals(expected, calc, 1e-2);// imageJ calculates median from
																				// histogram and can be a little bit off
	}

	@Test
	public void testMax() {
		setupThresholder();
		float calc = Thresholder.getThreshold("max(I)");
		float expected = 5;
		assertEquals(expected, calc, 1e-5);
	}

	@Test
	public void testMin() {
		setupThresholder();
		float calc = Thresholder.getThreshold("min(I)");
		float expected = 1;
		assertEquals(expected, calc, 1e-5);
	}

	@Test
	public void testSum() {
		setupThresholder();
		float calc = Thresholder.getThreshold("sum(I)");
		float expected = 75;
		assertEquals(expected, calc, 1e-5);
	}

	@Test
	public void testAbs() {
		setupThresholder();
		float calc = Thresholder.getThreshold("sum(abs(-I))");
		float expected = 75;
		assertEquals(expected, calc, 1e-5);
	}

	@Test
	public void testOtherFilter() {
		setupThresholder();
		float calc = Thresholder.getThreshold("min(Box.F)");
		assertTrue(calc > 1);
	}

	@Test
	public void testMultiplication() {
		setupThresholder();
		float calc = Thresholder.getThreshold("sum(2*I)");
		float expected = 2 * 75;
		assertEquals(expected, calc, 1e-5);

		calc = Thresholder.getThreshold("2*sum(I)");
		expected = 2 * 75;
		assertEquals(expected, calc, 1e-5);
	}

	@Test
	public void testExponentiation() {
		setupThresholder();
		float calc = Thresholder.getThreshold("sum(I^2)");
		float expected = 275;
		assertEquals(expected, calc, 1e-5);

		calc = Thresholder.getThreshold("sum(I)^2");
		expected = 75 * 75;
		assertEquals(expected, calc, 1e-5);

		try {
			Thresholder.getThreshold("sum(2^I)");
			fail("should have thrown exception");
		}
		catch (FormulaParserException e) {}
		try {
			Thresholder.getThreshold("sum(I^I)");
			fail("should have thrown exception");
		}
		catch (FormulaParserException e) {}
	}

	@Test
	public void testDivision() {
		setupThresholder();
		float calc = Thresholder.getThreshold("sum(I/2)");
		float expected = 75f / 2;
		assertEquals(expected, calc, 1e-5);

		calc = Thresholder.getThreshold("sum(1/I)");
		expected = 11.416666666666666f;
		assertEquals(expected, calc, 1e-5);

		calc = Thresholder.getThreshold("sum(I)/2");
		expected = 75f / 2;
		assertEquals(expected, calc, 1e-5);

		calc = Thresholder.getThreshold("sum(I/I)");
		expected = 25;
		assertEquals(expected, calc, 1e-5);

		calc = Thresholder.getThreshold("sum(I-1)");
		expected = 50;
		assertEquals(expected, calc, 1e-5);
	}

	/**
	 * Setups the thresholder with two filters: EmptyFilter and BoxFilter.
	 * EmptyFilter is set as active. An image is set as current image and active
	 * filter is used to filter the image.
	 */
	private void setupThresholder() {
		// run from other thread to test behavior of thread locals
		Thread t = new Thread() {

			@Override
			public void run() {
				Vector<IFilterUI> filters = new Vector<IFilterUI>();
				IFilterUI filter = new EmptyFilter();
				IFilterUI filter2 = new BoxFilterUI();

				filters.add(filter);

				filters.add(filter2);

				Thresholder.loadFilters(filters);
			}
		};
		t.start();
		try {
			t.join();
		}
		catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		}

		Thresholder.setActiveFilter(0);

		FloatProcessor fp = new FloatProcessor(new float[][] { { 1, 2, 3, 4, 5 }, { 1, 2, 3, 4, 5 }, {
			1, 2, 3, 4, 5 }, { 1, 2, 3, 4, 5 }, { 1, 2, 3, 4, 5 } });
		Thresholder.setCurrentImage(fp);

		Thresholder.getActiveFilter().filterImage(fp);
	}
}
