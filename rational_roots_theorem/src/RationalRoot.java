// SWT
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;

import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.PaintEvent;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.custom.TableEditor;

import java.util.ArrayList;

public class RationalRoot {
	// Windows
	static Display display; // Object that talks to the OS
	static Shell shell; // Main window

	// Buttons
	static Button nextButton; // used in start screen
	static boolean bNextButtonPressed = false;
	static Button solveButton; // used in table screen
	static boolean bSolveButtonPressed = false;
	static Button newButton; // used in end screen
	static Button doneButton; // used in end screen
	
	// Combo (used in startScreen)
	static Combo combo;
	
	// Table for coefficients (used in tableScreen)
	static Table table;
	static int numCoefficients;
	static TableItem item;
	static ArrayList<TableColumn> columns = new ArrayList<TableColumn>();
	static ArrayList<TableEditor> editors = new ArrayList<TableEditor>();
	static ArrayList<Integer> coefficients = new ArrayList<Integer>();
	static boolean bTableError = false; // used if the user doesn't fill out the table correctly
	
	// Degree of polynomial
	static int degree; // the algorithm accepts degrees from 2-9
	
	// Rational Roots
	static ArrayList<Double> rationalRoots = new ArrayList<Double>();
	
	// Shell size
    static Point shellSize;
    
    // Comparison of doubles
	static double ZERO_THRESHOLD = Math.pow(10, -10);
	
	public static void main(String[] args) {
		// instantiating display and shell
		display = new Display();
		shell = new Shell(display, SWT.DIALOG_TRIM); // SWT.DIALOG_TRIM prevents user from resizing shell
		
		// instantiating buttons
		nextButton = new Button(shell, SWT.PUSH);
		nextButton.setVisible(false);
		solveButton = new Button(shell, SWT.PUSH);
		solveButton.setVisible(false);
		newButton = new Button(shell, SWT.PUSH);
		newButton.setVisible(false);
		doneButton = new Button(shell, SWT.PUSH);
		doneButton.setVisible(false);
	
		// instantiating combo in start screen
		combo = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setVisible(false);
		
		// instantiating table
		table = new Table(shell, SWT.BORDER | SWT.NO_SCROLL);
		table.setLinesVisible(true); 
		table.setHeaderVisible(true); 
		table.setFont(new Font(display, "Arial", 20, SWT.ITALIC)); // setting font of table
		table.setVisible(false);
		
		// fill the screen
		shell.setLayout(new FillLayout()); 
		
		// PaintListener class
		shell.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				//System.out.println("repaint......");
				// Place your redraw method here...
				repaintWindow(e);
			}
		});
		
		// SelectionListener class for Next Button
		nextButton.addSelectionListener(new SelectionListener() {
			// never called
			public void widgetDefaultSelected(SelectionEvent e) {
				//System.out.println("widget default selected");
			}
					
			public void widgetSelected(SelectionEvent e) {
				//System.out.println("widget selected");
				degree = Integer.parseInt(combo.getText());
				//System.out.println("degree: " + degree);
				bNextButtonPressed = true;
				nextButton.setVisible(false); // hiding button
				combo.setVisible(false); // hiding combo
				layoutTable();
				// asking OS for repaint message which then calls repaintWindow(e)
				shell.redraw();
			}
		});
		
		// SelectionListener class for Solve Button
		solveButton.addSelectionListener(new SelectionListener() {
			// never called
			public void widgetDefaultSelected(SelectionEvent e) {
				//System.out.println("widget default selected");
			}
							
			public void widgetSelected(SelectionEvent e) {
				//System.out.println("widget selected");
				removeSpaces(); // removes the spaces if accidentally pressed
				if (areCoefficients()) {
					getCoefficients();
					//System.out.println("coefficients: " + coefficients);
					findRoots();
					bSolveButtonPressed = true;
					newButton.setVisible(true);
					doneButton.setVisible(true);
					solveButton.setVisible(false);
					table.setVisible(false);
				} else {
					bTableError = true;
				}
				// asking OS for repaint message which then calls repaintWindow(e)
				shell.redraw();
			}
		});
		
		// SelectionListener class for New Button
		newButton.addSelectionListener(new SelectionListener() {
			// never called
			public void widgetDefaultSelected(SelectionEvent e) {
				//System.out.println("widget default selected");
			}
							
			public void widgetSelected(SelectionEvent e) {
				combo.select(2); // making combo show degree 3 
				coefficients.clear(); // removing coefficients from arraylist
				rationalRoots.clear(); // removing rational roots from arraylist
				clearTable(); // clearing the table for new data
				bTableError = false;
				columns.clear(); // removing columns from arraylist
				editors.clear(); // removing editors from arraylist
				bNextButtonPressed = false;
				bSolveButtonPressed = false;
				newButton.setVisible(false);
				doneButton.setVisible(false);
				// asking OS for repaint message which then calls repaintWindow(e)
				shell.redraw();
			}
		});
				
		// SelectionListener class for Done Button
		doneButton.addSelectionListener(new SelectionListener() {
			// never called
			public void widgetDefaultSelected(SelectionEvent e) {
				//System.out.println("widget default selected");
			}
									
			public void widgetSelected(SelectionEvent e) {
				shell.close(); // close and dispose
			}
		});
		
		// this shows the window
		shell.open();
		
		// shell size
		shellSize = shell.getSize();
		//System.out.println("Size of shell = " + shellSize);
		
		// arranging placement of child windows
		layoutChildWindows();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		// disposes all associated windows and their components
		display.dispose();
	}
	
	// Redraw the screen
	static void repaintWindow(PaintEvent e) {
		// called inside PaintControl when there is a PaintEvent
		if (!bNextButtonPressed) {
			drawStartScreen(e);
		} else if (bSolveButtonPressed) {
			drawSolutionScreen(e);
		} else {
			drawTableScreen(e);
		}
	}
	
	// filled the arraylist "coefficients" with the coefficients the user fills in
	static void getCoefficients() {
		for (int i = 0; i < editors.size(); i++) {
			Text text = (Text)editors.get(i).getEditor();
			coefficients.add(Integer.parseInt(text.getText()));	// adding coefficient
		}
	}
	
	// writing the polynomial based on the coefficients and degree
	static String writePolynomial() {
		String polynomial = "";
		int coefficient1 = coefficients.get(0);
		if (coefficient1 == 1) {
			polynomial = addSuperscript(degree);
		} else if (coefficient1 == -1) {
			polynomial = "-" + addSuperscript(degree);
		} else {
			polynomial = coefficient1 + addSuperscript(degree);
		}
		int deg = degree - 1;
		int i = 1;
		while (deg > -1) {
			int coefficient = coefficients.get(i);
			// checking integer coefficients
			if (coefficient != 0) {
				if (coefficient == 1 && deg != 0) {
					polynomial = polynomial + "+";
				} else if (coefficient == -1 && deg != 0) {
					polynomial = polynomial + "-";
				} else if (coefficient < -1) {
					polynomial = polynomial + coefficient;
				} else { // coefficient > 0
					polynomial = polynomial + "+" + coefficient;
				}
				polynomial = polynomial + addSuperscript(deg); // adding superscript exponent
			}
			i = i + 1;
			deg = deg - 1; 
		}
		return polynomial;
	}
	
	// used to write exponents with unicode superscript 
	static String addSuperscript(int degree) {
		String exponent = "";
		if (degree == 0) {
			exponent = ""; // no exponent
		} else if (degree == 1) {
			exponent = "x";
		} else if (degree == 2) {
			exponent = "x\u00B2";
		} else if (degree == 3) {
			exponent = "x\u00B3";
		} else if (degree == 4) {
			exponent = "x\u2074";
		} else if (degree == 5) {
			exponent = "x\u2075";
		} else if (degree == 6) {
			exponent = "x\u2076";
		} else if (degree == 7) {
			exponent = "x\u2077";
		} else if (degree == 8) {
			exponent = "x\u2078";
		} else { // degree == 9
			exponent = "x\u2079";
		}
		return exponent;
	}
	
	// used to write the column headers with unicode subscript
	static String addSubscript(int subscript) {
		String term = "";
		if (subscript == 0) {
			term = "a\u2080";
		} else if (subscript == 1) {
			term = "a\u2081";
		} else if (subscript == 2) {
			term = "a\u2082";
		} else if (subscript == 3) {
			term = "a\u2083";
		} else if (subscript == 4) {
			term = "a\u2084";
		} else if (subscript == 5) {
			term = "a\u2085";
		} else if (subscript == 6) {
			term = "a\u2086";
		} else if (subscript == 7) {
			term = "a\u2087";
		} else if (subscript == 8) {
			term = "a\u2088";
		} else { // index == 9
			term = "a\u2089";
		}
		return term;
	} 
	
	// used to remove the spaces (if accidentally pressed) within table cells
	static void removeSpaces() {
		for (int i = 0; i < editors.size(); i++) {
			Text text = (Text)editors.get(i).getEditor();
			text.setText(text.getText().replaceAll("\\s", "")); // removing whitespace characters (tabs, spaces, etc.) 
		}
	}
	
	// used to check if all table cells are filled out before moving on to the solution screen
	static boolean areCoefficients() {
		boolean bAreCoefficients = true;
		// loop through editors to get text from the cells of the table
		for (int i = 0; i < editors.size(); i++) {
			Text text = (Text)editors.get(i).getEditor();
			//System.out.println("text: " + text.getText());
			if (!isInteger(text.getText())) { // making sure the user did not enter letters
				bAreCoefficients = false;
				break;
			}
		}
		//System.out.println(areCoefficients);
		return bAreCoefficients;
	}
	
	// checking if the characters of a string are digits
	static boolean isInteger(String str) {
		if (str.length() == 0) return false; // empty cell
		boolean bIsInt = true;
		for (int i = 0; i < str.length(); i++) {
			char digit = str.charAt(i);
			if (digit != '-' || i > 0) {
				if (!Character.isDigit(digit)) {
					bIsInt = false;
					break;
				}
			}
		}
		return bIsInt;
	}
	
	// used to create a new table each time the user hits the New Button
	static void clearTable() {
		// disposing columns
		for (int i = 0; i < columns.size(); i++) {
			columns.get(i).dispose();
		}
		// disposing table item
		item.dispose();
		// disposing text boxes
		for (int i = 0; i < editors.size(); i++) {
			Text text = (Text)editors.get(i).getEditor();
			text.dispose();
		}
	}
	
	static void layoutChildWindows() {
		// setting dimensions and coordinates of combo
		int cWidth = 100;
		int cHeight = combo.getItemHeight(); // not set to a different height because the height of the combo cannot be altered
		Point cXY = shapeCenterXY(cWidth, cHeight);
		int cX = cXY.x;
		
		// combo for start screen
		combo.setBounds(cX, 280, cWidth, cHeight);
		combo.setFont(new Font(display, "Arial", 17, SWT.NORMAL));
		combo.setItems(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9"});
		combo.select(2); // making combo show degree 3
		
		// next button for start screen
		Point pt2 = shapeCenterXY(150, 70);
		nextButton.setBounds(pt2.x, 400, 150, 70);
		nextButton.setFont(new Font(display, "Arial", 25, SWT.BOLD));
		nextButton.setText("Next");
		
		// solve button for table screen
		solveButton.setBounds(pt2.x, 390, 150, 70);
		solveButton.setFont(new Font(display, "Arial", 25, SWT.BOLD));
		solveButton.setText("Solve");
		
		// setting x values for end screen buttons - setting them 15 pixels apart
		int newButtonx = shellSize.x / 2 - 150 - 15;
		int doneButtonx = newButtonx + 150 + 30;
		
		// new button for end screen
		newButton.setBounds(newButtonx, 530, 150, 70);
		newButton.setFont(new Font(display, "Arial", 25, SWT.BOLD));
		newButton.setText("New");
		
		// done button for end screen
		doneButton.setBounds(doneButtonx, 530, 150, 70);
		doneButton.setFont(new Font(display, "Arial", 25, SWT.BOLD));
		doneButton.setText("Done");
	}
	
	static void layoutTable() {
		// table for table screen
		numCoefficients = degree + 1;
		//System.out.println("coefficients num: " + numCoefficients);
		// create the columns.
		int deg = degree;
		for (int i = 0; i < numCoefficients; i++) {
			TableColumn column = new TableColumn(table, SWT.CENTER);
			columns.add(column);
			column.setText(addSubscript(deg));
			column.setWidth(100);
			column.setMoveable(false); // doesn't allow user to move headers
			column.setResizable(false); // doesn't allow user to resize headers
			deg = deg - 1;
		}
		// create 1 row.
		item = new TableItem(table, SWT.NONE);	
		// Loop through the columns.
		for (int i = 0; i < numCoefficients; i++) {
			// instantiate the control
			TableEditor editor = new TableEditor(table);
			editors.add(editor);
			Text text = new Text(table, SWT.NONE);
			text.setTextLimit(4);
			text.setFont(new Font(display, "Arial", 17, SWT.NORMAL));
			editor.grabHorizontal = true;
			editor.setEditor(text, item, i);
		}
		// setting table dimensions & coordinates
		int tableHeight = table.getHeaderHeight() + table.getItemHeight() + 3;
		int tableWidth = 100 * numCoefficients;
		Point tableXY = shapeCenterXY(tableWidth, tableHeight);
		// setting the bounds of the table
		table.setBounds(tableXY.x, tableXY.y, tableWidth, tableHeight);
		// showing the table
		table.setVisible(true);
		// setting the user's cursor to the first table cell
		table.setFocus();
	}
	
	// Starting screen (asking for degree of polynomial)
	static void drawStartScreen(PaintEvent e) {
		// title text
		e.gc.setFont(new Font(e.display, "Arial", 50, SWT.BOLD)); // setting font
		Point pt1 = textCenterXY(e.gc, "Rational Root Theorem");
		e.gc.drawText("Rational Root Theorem", pt1.x, 50, true); // true for transparency
		
		// asking for degree
		e.gc.setFont(new Font(e.display, "Arial", 32, SWT.BOLD)); // setting new font
		Point pt2 = textCenterXY(e.gc, "What is the degree of your polynomial?");
		e.gc.drawText("What is the degree of your polynomial?", pt2.x, 230, true);
		
		// show combo
		combo.setVisible(true);
		
		// show button
		nextButton.setVisible(true);
	}
	
	// Table screen where user enters the coefficients of the polynomial
	static void drawTableScreen(PaintEvent e) {
		// asking for coefficients
		e.gc.setFont(new Font(e.display, "Arial", 30, SWT.BOLD)); // setting font
		Point pt1 = textCenterXY(e.gc, "Enter the integer coefficients of your polynomial.");
		e.gc.drawText("Enter the integer coefficients of your polynomial.", pt1.x, 220, true); // true for transparency
		
		// show solve button
		solveButton.setVisible(true);
		
		// table error message
		if (bTableError) {
			e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_RED));
			Point pt2 = textCenterXY(e.gc, "Fill out all the cells of the table with integer coefficients.");
			e.gc.drawText("Fill out all the cells of the table with integer coefficients.", pt2.x, 480, true);
		}
	}
	
	// Solution screen that shows the user the rational roots (which are not always found) of the polynomial
	static void drawSolutionScreen(PaintEvent e) {
		// showing polynomial
		e.gc.setFont(new Font(e.display, "Arial", 45, SWT.BOLD)); 
		Point pt1 = textCenterXY(e.gc, writePolynomial());
		e.gc.drawText(writePolynomial(), pt1.x, 60, true); 
		
		// showing rational roots if there are any
		e.gc.setFont(new Font(e.display, "Arial", 40, SWT.BOLD));
		Point pt2 = textCenterXY(e.gc, "Rational Roots:");
		e.gc.drawText("Rational Roots:", pt2.x, 250, true); 
		e.gc.setFont(new Font(e.display, "Arial", 40, SWT.NORMAL));
		if (rationalRoots.size() != 0) {
			Point pt3 = textCenterXY(e.gc, "" + rationalRoots);
			e.gc.drawText("" + rationalRoots, pt3.x, 330, true);
		} else {
			Point pt4 = textCenterXY(e.gc, "No rational roots");
			e.gc.drawText("No rational roots", pt4.x, 330, true);
		}
	}
	
	public static void findRoots() {
		ArrayList<Integer> coeff = coefficients;
		int deg = degree;
		while (deg > 0) {
			int p = coeff.get(0); // first term
			int q = coeff.get(coeff.size() - 1); // last term
			ArrayList<Double> pFactors = findFactors(p); // finding factors of p
			ArrayList<Double> qFactors = findFactors(q); // finding factors of q
			ArrayList<Double> potentialRoots = findPotentialRoots(pFactors, qFactors); // finding possible solutions by dividing q by p
			double[] root = testRoots(coeff, deg, potentialRoots); // finding rational root
			if (root.length == 0) { // if no rational root is found, terminate while loop
				break;
			}
			rationalRoots.add(root[0]); // adding root to rationalRoots arraylist
			coeff = divide(coeff, root[0]); // using synthetic division to simplify polynomial
			deg = deg - 1;
		}
	}
	
	// used to find factors of p and q
	public static ArrayList<Double> findFactors(int num) {
		ArrayList<Double> factors = new ArrayList<Double>();
		// if num = 0, the only factor is 0
		if (num == 0) {
			factors.add(0.);
			return factors;
		}
		int number;
		if (num < 0) {
			number = -num;
		} else {
			number = num;
		}
		for (int i = 1; i <= number; i++) {
			if (number % i == 0) {
				factors.add((double)i);
			}
		}
		return factors;
	}
	
	// used to find possible solutions to the equation (factors of q / factors of p) 
	public static ArrayList<Double> findPotentialRoots(ArrayList<Double> pFactors, ArrayList<Double> qFactors) {
		ArrayList<Double> potentialRoots = new ArrayList<Double>();
		for (int q = 0; q < qFactors.size(); q++) {
			for (int p = 0; p < pFactors.size(); p++) {
				double qVal = qFactors.get(q);
				double pVal = pFactors.get(p);
				double potentialRoot = qVal / pVal;
				if (potentialRoots.indexOf(potentialRoot) == -1) { // making sure that root isn't already in the arraylist; no repeats
					potentialRoots.add(potentialRoot);
					potentialRoots.add(-potentialRoot);
				}
			}
		}
		return potentialRoots;
	}
	
	// finding one root 
	public static double[] testRoots(ArrayList<Integer> coefficients, int degree, ArrayList<Double> potentialRoots) {
		for (int i = 0; i < potentialRoots.size(); i++) {
			double root = potentialRoots.get(i);
			double remainder = findRemainder(coefficients, degree, root);
			//System.out.println("iteration " + i + ", root: " + root + ", remainder: " + remainder);
			if (isZero(Math.abs(remainder))) {
				double[] arr = {root};
				return arr;
			}
		}
		return new double[0]; // empty if no rational root is found
	}
	
	// used to find remainder
	public static double findRemainder(ArrayList<Integer> coefficients, int degree, double root) {
		int deg = degree;
		double remainder = 0;
		int i = 0;
		while (deg > -1) {
			remainder = remainder + coefficients.get(i) * Math.pow(root, deg);
			i = i + 1;
			deg = deg - 1;
		}
		return remainder;
	}
	
	// synthetic division 
	public static ArrayList<Integer> divide(ArrayList<Integer> coefficients, double root) {
		ArrayList<Integer> newCoefficients = new ArrayList<Integer>(); // arraylist for new coefficients after synthetic division
		int coefficient1 = coefficients.get(0);
		newCoefficients.add(coefficient1); // first new coefficient is same as original first coefficient
		double product = coefficient1 * root;
		for (int i = 1; i < coefficients.size() - 1; i++) {
			int newCoefficient = (int)(product + coefficients.get(i));
			newCoefficients.add(newCoefficient);
			product = newCoefficient * root;
		}
		return newCoefficients;
	}
	
	// Helper functions
	public static boolean isZero(double num) {
		return num < ZERO_THRESHOLD;
	}
	
	// used to check if rational roots work
	public static ArrayList<Boolean> checkRationalRoots() {
		ArrayList<Boolean> areRoots = new ArrayList<Boolean>();
		for (int i = 0; i < rationalRoots.size(); i++) {
			double remainder = findRemainder(coefficients, degree, rationalRoots.get(i));
			areRoots.add(isZero(remainder));
		}
		return areRoots;
	}
	
	// used to draw text exactly in the middle of the screen
	static Point textCenterXY(GC gc, String text) {
	    Point extent = gc.stringExtent(text);
	    int textWidth = extent.x;
	    int textHeight = extent.y;
	    int x = (shellSize.x - textWidth) / 2;
	    int y = (shellSize.y - textHeight) / 2;
	    return new Point(x, y);
	}
	
	// used to draw shapes exactly in the middle of the screen
	static Point shapeCenterXY(int width, int height) {
	    int x = (shellSize.x - width) / 2;
	    int y = (shellSize.y - height) / 2 - height / 2;
	    return new Point(x, y);
    }
}