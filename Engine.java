
//// WIP ////
//@author Elizabeth "Rosemary" Fortanely

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public abstract class Engine extends Canvas
		implements Runnable, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
	private static final long serialVersionUID = 1L;
	private boolean running;
	private boolean smooth;
	private static double fps;

	public Engine(int width, int height, String title) {
		Dimension dimension = new Dimension(width, height);
		setPreferredSize(dimension);
		setMaximumSize(dimension);
		setMinimumSize(dimension);

		JFrame frame = new JFrame(title);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(this);
		frame.pack();
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		requestFocusInWindow();

		running = false;
		smooth = false;
		fps = 60;
	}

	public synchronized void start() {
		running = true;
		new Thread(this).start();
	}

	public abstract void first();

	public abstract void tick();

	public abstract void render(Graphics2D g);

	// ticks update logic, rendering updates graphics.
	// uncomment code in method to see number of frames and ticks updated per
	// second! :)
	@Override
	public void run() {
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);

		first();

		double lastTimeSec = System.nanoTime() / 1E9;
		double changeInTimeSec = 0;
		double secondsPerFrame = 1 / fps;
		boolean renderToFrame = false;
		// double timerMillisec = System.currentTimeMillis();
		// int ticksSec = 0;
		// int framesSec = 0;

		while (running) {
			double currentTimeSec = System.nanoTime() / 1E9;
			changeInTimeSec += currentTimeSec - lastTimeSec;
			lastTimeSec = currentTimeSec;

			while (changeInTimeSec >= secondsPerFrame) {
				// ticksSec++;
				tick();
				changeInTimeSec -= secondsPerFrame;
				renderToFrame = true;
			}

			if (renderToFrame) {
				// framesSec++;

				BufferStrategy bufferStrategy = this.getBufferStrategy();
				if (bufferStrategy == null) {
					this.createBufferStrategy(3);
				} else {
					Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
					if (smooth)
						g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					render(g);
					g.dispose();
					bufferStrategy.show();
				}

				renderToFrame = false;
			} else {
				// sleeping before attempting to update the screen again helps
				// efficiency
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			/*
			 * int secondInMillisec = 1000; if(System.currentTimeMillis() -
			 * timerMillisec > secondInMillisec){ timerMillisec +=
			 * secondInMillisec; System.out.println("Fps: " + framesSec +
			 * " Ticks: " + ticksSec); framesSec = ticksSec = 0; }
			 */
		}
	}

	//// SPRITE METHODS ////

	public static BufferedImage grabImage(BufferedImage img, int row, int col, int width, int height) {
		return img.getSubimage(col * width - width, row * height - height, width, height);
	}

	public static BufferedImage loadImage(String path) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return img;
	}

	//// IO ////

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
	}

	//// GETTERS AND SETTERS ////

	public static double getSecondsPerFrame() {
		return 1 / fps;
	}

	public void setSmooth(boolean smooth) {
		this.smooth = smooth;
	}

	public void setFps(int fps) {
		Engine.fps = fps;
	}
}

class Vector {
	double[] v;
	int dimension;

	//// CONSTRUCTORS ////

	public Vector(double x, double y) {
		this.v = new double[] { x, y };
		this.dimension = 2;
	}

	public Vector(double x, double y, double z) {
		this.v = new double[] { x, y, z };
		this.dimension = 3;
	}

	public Vector(double[] arr) {
		this.v = arr;
		if (v.length == 2)
			dimension = 2;
		else
			dimension = 3;
	}

	public Vector(Point p) {
		this.v = new double[] { p.getX(), p.getY() };
		this.dimension = 2;
	}

	public Vector(Vector vector) {
		if (vector.dimension == 2) {
			this.v = new double[] { vector.x(), vector.y() };
			this.dimension = 2;
		} else {
			this.v = new double[] { vector.x(), vector.y(), vector.z() };
			this.dimension = 3;
		}
	}

	// spherical -> cartesian
	public Vector(String coordinateSystem, double ρ, double θ, double φ) {
		this(ρ * Math.cos(θ) * Math.sin(φ), ρ * Math.sin(θ) * Math.sin(φ), ρ * Math.cos(φ));
	}

	// polar -> cartesian
	public Vector(String coordinateSystem, double r, double θ) {
		this(r * Math.cos(θ), r * Math.sin(θ));
	}

	//// VECTOR OPERATIONS ////

	public Vector plus(Vector b) {
		double[] p = new double[dimension];
		for (int i = 0; i < dimension; i++) {
			p[i] = v[i] + b.v[i];
		}
		return new Vector(p);
	}

	public Vector minus(Vector b) {
		double[] m = new double[dimension];
		for (int i = 0; i < dimension; i++) {
			m[i] = v[i] - b.v[i];
		}
		return new Vector(m);
	}

	public Vector times(double a) {
		double[] t = new double[dimension];
		for (int i = 0; i < dimension; i++) {
			t[i] = v[i] * a;
		}
		return new Vector(t);
	}

	public Vector componentProduct(Vector v2) {
		double[] cp = new double[dimension];
		for (int i = 0; i < dimension; i++) {
			cp[i] = this.v[i] * v2.v[i];
		}
		return new Vector(cp);
	}

	public Vector divide(double a) {
		double[] d = new double[dimension];
		for (int i = 0; i < dimension; i++) {
			d[i] = v[i] / a;
		}
		return new Vector(d);
	}

	public Vector unit() {
		double mag = magnitude();
		if (dimension == 2)
			return new Vector(x() / mag, y() / mag);
		else
			return new Vector(x() / mag, y() / mag, z() / mag);
	}

	public Vector setMagnitude(double magnitude) {
		return this.unit().times(magnitude);
	}

	public Vector limit(double magnitude) {
		Vector limitedMagnitude = new Vector(this.v);
		if (magnitudeOptimized() > magnitude * magnitude)
			limitedMagnitude = limitedMagnitude.setMagnitude(magnitude);
		return limitedMagnitude;
	}

	//// SCALAR OPERATIONS ////

	public double dot(Vector b) {
		double sum = 0;
		for (int i = 0; i < dimension; i++) {
			sum += v[i] * b.v[i];
		}
		return sum;
	}

	public double magnitude() {
		return Math.sqrt(magnitudeOptimized());
	}

	public double magnitudeOptimized() {
		return this.dot(this);
	}

	public double distanceOptimized(Vector v2) {
		return this.minus(v2).magnitudeOptimized();
	}

	public double distance(Vector v2) {
		return this.minus(v2).magnitude();
	}

	//// 2D METHODS ////

	public double scalarProjectionOn2D(Vector on) {
		return this.dot(on.unit());
	}

	public Vector vectorProjectionOn2D(Vector on) {
		return on.times(this.dot(on) / on.dot(on));
	}

	// counter-clockwise rotation
	// https://en.wikipedia.org/wiki/Rotation_matrix#In_two_dimensions
	public Vector rotateBy2D(double θ) {
		Matrix rotationMatrix = new Matrix(
				new double[][] { { Math.cos(θ), -Math.sin(θ) }, { Math.sin(θ), Math.cos(θ) } });
		Vector rotated = rotationMatrix.times(this);
		return rotated;
	}

	// definition for dot product |A||B|cos(θ)=A.B -> θ = cos^-1(A.B/|A||B|)
	public double angleBetween2D(Vector v2) {
		return Math.acos(this.dot(v2) / (magnitude() * v2.magnitude()));
	}

	public void drawVector2D(Graphics g, Vector magnitude) {
		g.drawLine((int) x(), (int) y(), (int) (x() + magnitude.x()), (int) (y() + magnitude.y()));
		g.drawOval((int) (x() + magnitude.x()), (int) (y() + magnitude.y()), 2, 2);
	}

	//// 3D METHODS ////

	public Vector crossProduct(Vector v2) {
		double a = x(), b = y(), c = z(), d = v2.x(), e = v2.y(), f = v2.z();
		return new Vector(b * f - c * e, c * d - a * f, a * e - b * d);
	}

	// rotation by an angle around a given axis
	// https://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations
	public Vector rotateBy3D(String axis, double θ) {
		Vector rotated = null;

		Matrix rotationMatrixX = new Matrix(
				new double[][] { { 1, 0, 0 }, { 0, Math.cos(θ), -Math.sin(θ) }, { 0, Math.sin(θ), Math.cos(θ) } });
		Matrix rotationMatrixY = new Matrix(
				new double[][] { { Math.cos(θ), 0, Math.sin(θ) }, { 0, 1, 0 }, { -Math.sin(θ), 0, Math.cos(θ) } });
		Matrix rotationMatrixZ = new Matrix(
				new double[][] { { Math.cos(θ), -Math.sin(θ), 0 }, { Math.sin(θ), Math.cos(θ), 0 }, { 0, 0, 1 } });

		if (axis.equals("x"))
			rotated = rotationMatrixX.times(this);
		if (axis.equals("y"))
			rotated = rotationMatrixY.times(this);
		if (axis.equals("z"))
			rotated = rotationMatrixZ.times(this);

		return rotated;
	}

	// rotation given Tait–Bryan angles
	// yaw, pitch, and roll about z, y, x (respectively)
	// https://en.wikipedia.org/wiki/Rotation_matrix#General_rotations
	public Vector rotateBy3D(double α, double β, double γ) {
		Matrix rotationMatrixX = new Matrix(
				new double[][] { { 1, 0, 0 }, { 0, Math.cos(γ), -Math.sin(γ) }, { 0, Math.sin(γ), Math.cos(γ) } });
		Matrix rotationMatrixY = new Matrix(
				new double[][] { { Math.cos(β), 0, Math.sin(β) }, { 0, 1, 0 }, { -Math.sin(β), 0, Math.cos(β) } });
		Matrix rotationMatrixZ = new Matrix(
				new double[][] { { Math.cos(α), -Math.sin(α), 0 }, { Math.sin(α), Math.cos(α), 0 }, { 0, 0, 1 } });

		Vector rotated = rotationMatrixZ.times(rotationMatrixY).times(rotationMatrixX).times(this);
		return rotated;
	}

	// rotation given a vector and an angle
	// https://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle
	public Vector rotateBy3D(Vector vector, double θ) {
		vector = vector.unit();

		Matrix rotationMatrix = new Matrix(new double[][] {
				{ Math.cos(θ) + vector.x() * vector.x() * (1 - Math.cos(θ)),
						vector.x() * vector.y() * (1 - Math.cos(θ) - vector.z() * Math.sin(θ)),
						vector.x() * vector.z() * (1 - Math.cos(θ)) + vector.y() * Math.sin(θ) },
				{ vector.y() * vector.x() * (1 - Math.cos(θ) + vector.z() * Math.sin(θ)),
						Math.cos(θ) + vector.y() * vector.y() * (1 - Math.cos(θ)),
						vector.y() * vector.z() * (1 - Math.cos(θ)) - vector.x() * Math.sin(θ) },
				{ vector.z() * vector.x() * (1 - Math.cos(θ) - vector.y() * Math.sin(θ)),
						vector.z() * vector.y() * (1 - Math.cos(θ) + vector.x() * Math.sin(θ)),
						Math.cos(θ) + vector.z() * vector.z() * (1 - Math.cos(θ)) } });

		Vector rotated = rotationMatrix.times(this);
		return rotated;
	}

	// returns the perspective projection based on the position of the
	// viewing camera and the Tait–Bryan angles of the camera:
	// yaw, pitch, and roll about z, y, x (respectively)
	// https://en.wikipedia.org/wiki/3D_projection#Perspective_projection
	public Vector perspectiveProjection(Vector camera, double α, double β, double γ) {
		Matrix rotationMatrixX = new Matrix(
				new double[][] { { 1, 0, 0 }, { 0, Math.cos(γ), Math.sin(γ) }, { 0, -Math.sin(γ), Math.cos(γ) } });
		Matrix rotationMatrixY = new Matrix(
				new double[][] { { Math.cos(β), 0, -Math.sin(β) }, { 0, 1, 0 }, { Math.sin(β), 0, Math.cos(β) } });
		Matrix rotationMatrixZ = new Matrix(
				new double[][] { { Math.cos(α), Math.sin(α), 0 }, { -Math.sin(α), Math.cos(α), 0 }, { 0, 0, 1 } });

		Vector pointInCameraCoordinateSystem = this.minus(camera);
		return rotationMatrixX.times(rotationMatrixY).times(rotationMatrixZ).times(pointInCameraCoordinateSystem);
	}

	public void drawVectorPerspective(Graphics2D g, Vector camera, Vector magnitude, double α, double β, double γ) {
		Vector tail = this.perspectiveProjection(camera, α, β, γ);
		Vector tip = this.plus(magnitude).perspectiveProjection(camera, α, β, γ);
		g.drawLine((int) tip.x(), (int) tip.y(), (int) tail.x(), (int) tail.y());
	}

	public void drawPointPerspective(Graphics2D g, Vector camera, Vector magnitude, double α, double β, double γ,
			int width) {
		Vector tip = this.plus(magnitude).perspectiveProjection(camera, α, β, γ);
		g.fillOval((int) tip.x(), (int) tip.y(), width, width);
	}

	// https://en.wikipedia.org/wiki/Isometric_projection#Mathematics
	public Vector isometricProjection() {
		double γ = Math.asin(Math.tan(Math.PI / 6)); // ~35.264 degrees
		double β = Math.PI / 4; // 45 degrees
		Matrix rotationMatrixX = new Matrix(
				new double[][] { { 1, 0, 0 }, { 0, Math.cos(γ), Math.sin(γ) }, { 0, -Math.sin(γ), Math.cos(γ) } });
		Matrix rotationMatrixY = new Matrix(
				new double[][] { { Math.cos(β), 0, -Math.sin(β) }, { 0, 1, 0 }, { Math.sin(β), 0, Math.cos(β) } });

		return rotationMatrixX.times(rotationMatrixY).times(this).orthographicProjection("xy");
	}

	public void drawVectorIsometric(Graphics2D g, Vector magnitude) {
		Vector tail = this.isometricProjection();
		Vector tip = this.plus(magnitude).isometricProjection();
		g.drawLine((int) tip.x(), (int) tip.y(), (int) tail.x(), (int) tail.y());
	}

	public void drawPointIsometric(Graphics2D g, Vector magnitude, int width) {
		Vector tip = this.plus(magnitude).isometricProjection();
		g.fillOval((int) tip.x(), (int) tip.y(), width, width);
	}

	// parallel projection of the vector onto a given plane: "xy", "xz", "yz"
	public Vector orthographicProjection(String plane) {
		Matrix xyPlaneBasis = new Matrix(new double[][] { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 0 } });
		Matrix xzPlaneBasis = new Matrix(new double[][] { { 1, 0, 0 }, { 0, 0, 0 }, { 0, 0, 1 } });
		Matrix yzPlaneBasis = new Matrix(new double[][] { { 0, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } });

		switch (plane) {
		case "xy":
			Vector projected = xyPlaneBasis.times(this);
			return new Vector(projected.x(), projected.y());
		case "xz":
			return xzPlaneBasis.times(this);
		case "yz":
			return yzPlaneBasis.times(this);
		default:
			throw new IllegalArgumentException("Define an existing plane: \"xy\", \"xz\", or \"yz\"");
		}
	}

	public Vector angleProjection(double θ, double φ) {
		double x = x() * Math.cos(θ) - y() * Math.sin(θ);
		double y = (x() * Math.sin(θ) + y() * Math.cos(θ)) * Math.sin(φ) - z() * Math.cos(φ);
		return new Vector(x, y);
	}

	public void drawVectorAngle(Graphics2D g, Vector magnitude, double θ, double φ) {
		Vector tail = this.angleProjection(θ, φ);
		Vector tip = this.plus(magnitude).angleProjection(θ, φ);
		g.drawLine((int) tip.x(), (int) tip.y(), (int) tail.x(), (int) tail.y());
	}

	public void drawPointAngle(Graphics2D g, Vector magnitude, double θ, double φ, int width) {
		Vector tip = this.plus(magnitude).angleProjection(θ, φ);
		g.fillOval((int) tip.x(), (int) tip.y(), width, width);
	}

	//// GETTERS AND SETTERS ////
	public double x() {
		return v[0];
	}

	public double y() {
		return v[1];
	}

	public double z() {
		return v[2];
	}

	public void setX(double x) {
		v[0] = x;
	}

	public void setY(double y) {
		v[1] = y;
	}

	public void setZ(double z) {
		v[2] = z;
	}

	public void setVector(double x, double y) {
		setX(x);
		setY(y);
	}

	public void setVector(double x, double y, double z) {
		setVector(x, y);
		setZ(z);
	}

	public double[] getArray() {
		if (dimension == 2)
			return new double[] { x(), y() };
		else
			return new double[] { x(), y(), z() };
	}

	public int getDimension() {
		return this.dimension;
	}

	@Override
	public String toString() {
		return Arrays.toString(this.v);
	}
}

class Matrix {
	private double[][] matrix;

	public Matrix(double[][] matrix) {
		this.matrix = matrix;
	}

	//// BASIC MATRIX OPERATIONS ////

	public Matrix plus(Matrix matrixB) {
		if (this.rows() != matrixB.rows() || this.columns() != matrixB.columns())
			throw new IllegalArgumentException("Mismatched dimensions for matrix addition");

		double[][] newMatrix = this.matrix.clone();
		for (int i = 0; i < rows(); i++) {
			for (int j = 0; j < columns(); j++) {
				newMatrix[i][j] += matrixB.matrix[i][j];
			}
		}
		return new Matrix(newMatrix);
	}

	public Matrix minus(Matrix matrixB) {
		if (this.rows() != matrixB.rows() || this.columns() != matrixB.columns())
			throw new IllegalArgumentException("Mismatched dimensions for matrix subtraction");

		double[][] newMatrix = this.matrix.clone();
		for (int i = 0; i < rows(); i++) {
			for (int j = 0; j < columns(); j++) {
				newMatrix[i][j] -= matrixB.matrix[i][j];
			}
		}
		return new Matrix(newMatrix);
	}

	public Matrix times(Matrix matrixB) {
		if (this.columns() != matrixB.rows())
			throw new IllegalArgumentException("Mismatched dimensions for matrix multiplication");

		double[][] newMatrix = new double[rows()][matrixB.columns()];
		for (int i = 0; i < rows(); i++) {
			for (int j = 0; j < matrixB.columns(); j++) {
				double indexVal = 0; // found the bug, i initially made this an
										// int :/
				// each new matrix element is the dot product of row(A) . col(B)
				for (int k = 0; k < columns(); k++) {
					indexVal += matrix[i][k] * matrixB.matrix[k][j];
				}
				newMatrix[i][j] = indexVal;
			}
		}
		return new Matrix(newMatrix);
	}

	public Vector times(Vector vector) {
		Matrix matrixB = null;
		if (vector.dimension == 2)
			matrixB = new Matrix(new double[][] { { vector.x() }, { vector.y() } });
		else
			matrixB = new Matrix(new double[][] { { vector.x() }, { vector.y() }, { vector.z() } });
		Matrix rotatedMatrix = this.times(matrixB);
		Vector rotatedVector = (vector.dimension == 2)
				? new Vector(rotatedMatrix.matrix[0][0], rotatedMatrix.matrix[1][0])
				: new Vector(rotatedMatrix.matrix[0][0], rotatedMatrix.matrix[1][0], rotatedMatrix.matrix[2][0]);
		return rotatedVector;
	}

	public Matrix times(double scalar) {
		double[][] newMatrix = this.matrix.clone();
		for (int i = 0; i < rows(); i++) {
			for (int j = 0; j < columns(); j++) {
				newMatrix[i][j] *= scalar;
			}
		}
		return new Matrix(newMatrix);
	}

	//// ELEMENTARY ROW OPERATIONS ////

	public Matrix rowSwap(int rowOne, int rowTwo) {
		double[][] newMatrix = this.matrix.clone();
		double[] tempRow = newMatrix[rowOne];
		newMatrix[rowOne] = newMatrix[rowTwo];
		newMatrix[rowTwo] = tempRow;
		return new Matrix(newMatrix);
	}

	public Matrix rowMultiplication(int row, double scalar) {
		double[][] newMatrix = this.matrix.clone();
		for (int i = 0; i < columns(); i++) {
			newMatrix[row][i] *= scalar;
		}
		return new Matrix(newMatrix);
	}

	public Matrix rowAddition(int rowOne, int rowTwo, double scalar) {
		double[][] newMatrix = this.matrix.clone();
		for (int i = 0; i < columns(); i++) {
			newMatrix[rowOne][i] += scalar * newMatrix[rowTwo][i];
		}
		return new Matrix(newMatrix);
	}

	//// DETERMINANT METHODS ////

	// computes determinant by cofactor expansion
	public double determinant() {
		int[] zeroes = getMostZeroes();
		boolean newRowExpansion = zeroes[0] == 0;
		int newIndex = zeroes[1];
		return determinantHelper(newRowExpansion, newIndex);
	}

	private double determinantHelper(boolean rowExpansion, int index) {
		double determinant = 0;

		if (rows() == 2 && columns() == 2) {
			determinant += matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
		} else {
			if (rowExpansion) {
				for (int col = 0; col < columns(); col++) {
					Matrix cofactor = this.cofactor(index, col);
					int[] zeroes = cofactor.getMostZeroes();
					boolean newRowExpansion = zeroes[0] == 0;
					int newIndex = zeroes[1];

					determinant += Math.pow(-1, index + col) * matrix[index][col]
							* cofactor.determinantHelper(newRowExpansion, newIndex);
				}
			} else { // column expansion
				for (int row = 0; row < rows(); row++) {
					Matrix cofactor = this.cofactor(row, index);
					int[] zeroes = cofactor.getMostZeroes();
					boolean newRowExpansion = zeroes[0] == 0;
					int newIndex = zeroes[1];

					determinant += Math.pow(-1, row + index) * matrix[row][index]
							* cofactor.determinantHelper(newRowExpansion, newIndex);
				}
			}
		}

		return determinant;
	}

	private int[] getMostZeroes() {
		int numZeroes = 0;
		int index = 0;
		int rowOrCol = 0;

		for (int r = 0; r < rows(); r++) {
			int foundZeroes = 0;
			for (int c = 0; c < columns(); c++) {
				if (matrix[r][c] == 0)
					foundZeroes++;
			}
			if (foundZeroes > numZeroes) {
				numZeroes = foundZeroes;
				index = r;
				rowOrCol = 0;
			}
		}

		for (int c = 0; c < columns(); c++) {
			int foundZeroes = 0;
			for (int r = 0; r < rows(); r++) {
				if (matrix[r][c] == 0)
					foundZeroes++;
			}
			if (foundZeroes > numZeroes) {
				numZeroes = foundZeroes;
				index = c;
				rowOrCol = 1;
			}
		}

		return new int[] { rowOrCol, index };
	}

	public Matrix cofactor(int row, int col) {
		double[][] cofactorMatrix = new double[rows() - 1][columns() - 1];

		for (int i = 0; i < rows(); i++) {
			for (int j = 0; j < columns(); j++) {
				if (i == row || j == col) {
					continue;
				} else if (j < col) {
					if (i < row)
						cofactorMatrix[i][j] = matrix[i][j];
					else // i > row
						cofactorMatrix[i - 1][j] = matrix[i][j];
				} else { // j > col
					if (i < row)
						cofactorMatrix[i][j - 1] = matrix[i][j];
					else // i > row
						cofactorMatrix[i - 1][j - 1] = matrix[i][j];
				}
			}
		}

		return new Matrix(cofactorMatrix);
	}

	// the world's most inefficient inverse finding method
	public Matrix inverseCofactorMethod() {
		if (isLinearlyDependent()) {
			throw new IllegalArgumentException("This matrix is not invertible. The columns must be independent.");
		} else {
			double[][] matrixOfCofactors = new double[rows()][columns()];

			for (int i = 0; i < rows(); i++) {
				for (int j = 0; j < columns(); j++) {
					matrixOfCofactors[i][j] = Math.pow(-1, i + j) * cofactor(i, j).determinant();
				}
			}

			Matrix adjugate = new Matrix(matrixOfCofactors).getTranspose();

			return adjugate.times(1 / determinant());
		}
	}

	public Matrix getTranspose() {
		double[][] transpose = new double[columns()][rows()];
		for (int i = 0; i < columns(); i++) {
			for (int j = 0; j < rows(); j++) {
				transpose[i][j] = this.matrix[j][i];
			}
		}
		return new Matrix(transpose);
	}

	//// MISC METHODS ////

	public boolean isLinearlyIndependent() {
		return rows() == columns() && this.determinant() != 0;
	}

	public boolean isLinearlyDependent() {
		return rows() > columns() || this.determinant() == 0;
	}

	public int rows() {
		return this.matrix.length;
	}

	public int columns() {
		return this.matrix[0].length;
	}

	@Override
	public String toString() {
		String matrixOutput = "";
		for (int i = 0; i < rows(); i++) {
			for (int j = 0; j < columns(); j++) {
				matrixOutput += matrix[i][j] + " ";
			}
			matrixOutput += "\n";
		}
		return matrixOutput;
	}
}

class Particle {
	private Vector position;
	private Vector velocity;
	private Vector acceleration;
	private double damping; // simulates drag on velocity
	private double inverseMass; // set to 0 for immovable objects with
								// "infinite" mass
	private Vector gravity;
	private double dimension;
	private double deltaTime;

	public Particle(Vector position, Vector velocity, Vector acceleration, double damping, double mass) {
		this.position = new Vector(position.x(), -position.y());
		this.velocity = velocity;
		this.acceleration = acceleration;
		this.damping = damping;
		this.inverseMass = 1 / mass;
		deltaTime = Engine.getSecondsPerFrame();
		if (position.getDimension() == 2)
			dimension = 2;
		else
			dimension = 3;
	}

	public void tick() {
		integrate(deltaTime);
	}

	public void render(Graphics2D g) {
		g.setColor(Color.black);
		g.drawOval((int) position.x(), (int) -position.y(), 1, 1);
	}

	private void integrate(double deltaTime) {
		if (inverseMass <= 0 || deltaTime <= 0)
			return;

		position = position.plus(velocity.times(deltaTime));

		// TODO update with sum of a = 1/m * f, for gravity it's just a = g
		Vector summedAcceleration = acceleration;

		velocity = velocity.plus(summedAcceleration.times(deltaTime));
		velocity = velocity.times(Math.pow(damping, deltaTime));
	}

	//// GETTERS AND SETTERS ////
	public double getMass() {
		return 1 / inverseMass;
	}

	public double getKineticEnergy() {
		return 0.5 * getMass() * velocity.magnitudeOptimized();
	}

	public void setInverseMass(double inverseMass) {
		this.inverseMass = inverseMass;
	}

	public void setMass(double mass) {
		this.inverseMass = 1 / mass;
	}

	public void setGravity(double gravity) {
		if (dimension == 2)
			this.gravity = new Vector(0, -gravity);
		else if (dimension == 3)
			this.gravity = new Vector(0, -gravity, 0);
		else
			throw new IllegalArgumentException("Define a dimension.");
	}

	public void setAcceleration(Vector acceleration) {
		this.acceleration = acceleration;
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}
}

// https://github.com/processing/processing/blob/master/core/src/processing/core/PApplet.java#L5037
// because my noise class wasn't properly interpolating between cycles...
// added mapping function and edited comments for clarity

/**
 * Perlin noise is a random sequence generator producing a more natural ordered,
 * harmonic succession of numbers compared to the standard random function. The
 * main difference to the random function is that Perlin noise is defined in an
 * infinite n-dimensional space where each pair of coordinates corresponds to a
 * fixed semi-random value (fixed only for the lifespan of the program). The
 * resulting value will always be between 0.0 and 1.0. The actual noise is
 * structured similar to an audio signal, in respect to the function's use of
 * frequencies. Similar to the concept of harmonics in physics, perlin noise is
 * computed over several octaves which are added together for the final result.
 * Another way to adjust the character of the resulting sequence is the scale of
 * the input coordinates. As the function works within an infinite space the
 * value of the coordinates doesn't matter as such, only the distance between
 * successive coordinates does. As a general rule the smaller the difference
 * between coordinates, the smoother the resulting noise sequence will be. Steps
 * of 0.005-0.03 work best for most applications.
 */
class Noise {
	private static final int PERLIN_YWRAPB = 4;
	private static final int PERLIN_YWRAP = 1 << PERLIN_YWRAPB;
	private static final int PERLIN_ZWRAPB = 8;
	private static final int PERLIN_ZWRAP = 1 << PERLIN_ZWRAPB;
	private static final int PERLIN_SIZE = 4095;
	private static int perlin_octaves = 4;
	private static float perlin_amp_falloff = 0.5f;
	private static int perlin_TWOPI, perlin_PI;
	private static float[] perlin_cosTable;
	private static float[] perlin;
	private static Random perlinRandom;
	private static final float sinLUT[];
	private static final float cosLUT[];
	private static final float SINCOS_PRECISION = 0.5f;
	private static final int SINCOS_LENGTH = (int) (360f / SINCOS_PRECISION);
	private static final float DEG_TO_RAD = (float) Math.PI / 180.0f;
	static {
		sinLUT = new float[SINCOS_LENGTH];
		cosLUT = new float[SINCOS_LENGTH];
		for (int i = 0; i < SINCOS_LENGTH; i++) {
			sinLUT[i] = (float) Math.sin(i * DEG_TO_RAD * SINCOS_PRECISION);
			cosLUT[i] = (float) Math.cos(i * DEG_TO_RAD * SINCOS_PRECISION);
		}
	}

	/**
	 * Adjusts the character and level of detail produced by the Perlin noise
	 * function. Similar to harmonics in physics, noise is computed over several
	 * octaves. Lower octaves contribute more to the output signal and as such
	 * define the overal intensity of the noise, whereas higher octaves create
	 * finer grained details in the noise sequence. By default, noise is
	 * computed over 4 octaves with each octave contributing exactly half than
	 * its predecessor, starting at 50% strength for the 1st octave. This
	 * falloff amount can be changed by adding an additional function parameter.
	 * Eg. a falloff factor of 0.25 means each octave will now have 25% impact
	 * (75% less) of the previous lower octave. Any value between 0.0 and 0.5 is
	 * valid. Lower values will produce smoother results as the higher octaves
	 * are surpressed.
	 */

	public static void noiseDetail(int octaves) {
		if (octaves > 0)
			perlin_octaves = octaves;
	}

	public static void noiseDetail(int octaves, float falloff) {
		noiseDetail(octaves);
		if (falloff > 0)
			perlin_amp_falloff = falloff;
	}

	/**
	 * Sets the seed value for noise. By default, noise produces different
	 * results each time the program is run. Set the parameter to a constant to
	 * return the same pseudo-random numbers each time the software is run.
	 */
	public static void noiseSeed(long seed) {
		if (perlinRandom == null)
			perlinRandom = new Random();
		perlinRandom.setSeed(seed);
		perlin = null;
	}

	public static float mapNoise(float x, float y, float min, float max) {
		return map(noise(x, y), min, max);
	}

	public static float mapNoise(float x, float y, float z, float min, float max) {
		return map(noise(x, y, z), min, max);
	}

	public static float mapNoise(float x, float min, float max) {
		return map(noise(x, 0f, 0f), min, max);
	}

	private static float map(float x, float min, float max) {
		return x * (max - min) + min;
	}

	public static float noise(float x) {
		return noise(x, 0f, 0f);
	}

	public static float noise(float x, float y) {
		return noise(x, y, 0f);
	}

	public static float noise(float x, float y, float z) {
		if (perlin == null) {
			if (perlinRandom == null) {
				perlinRandom = new Random();
			}
			perlin = new float[PERLIN_SIZE + 1];
			for (int i = 0; i < PERLIN_SIZE + 1; i++) {
				perlin[i] = perlinRandom.nextFloat(); // (float)Math.random();
			}

			perlin_cosTable = cosLUT;
			perlin_TWOPI = perlin_PI = SINCOS_LENGTH;
			perlin_PI >>= 1;
		}

		if (x < 0)
			x = -x;
		if (y < 0)
			y = -y;
		if (z < 0)
			z = -z;

		int xi = (int) x, yi = (int) y, zi = (int) z;
		float xf = x - xi;
		float yf = y - yi;
		float zf = z - zi;
		float rxf, ryf;

		float r = 0;
		float ampl = 0.5f;

		float n1, n2, n3;

		for (int i = 0; i < perlin_octaves; i++) {
			int of = xi + (yi << PERLIN_YWRAPB) + (zi << PERLIN_ZWRAPB);

			rxf = noise_fsc(xf);
			ryf = noise_fsc(yf);

			n1 = perlin[of & PERLIN_SIZE];
			n1 += rxf * (perlin[(of + 1) & PERLIN_SIZE] - n1);
			n2 = perlin[(of + PERLIN_YWRAP) & PERLIN_SIZE];
			n2 += rxf * (perlin[(of + PERLIN_YWRAP + 1) & PERLIN_SIZE] - n2);
			n1 += ryf * (n2 - n1);

			of += PERLIN_ZWRAP;
			n2 = perlin[of & PERLIN_SIZE];
			n2 += rxf * (perlin[(of + 1) & PERLIN_SIZE] - n2);
			n3 = perlin[(of + PERLIN_YWRAP) & PERLIN_SIZE];
			n3 += rxf * (perlin[(of + PERLIN_YWRAP + 1) & PERLIN_SIZE] - n3);
			n2 += ryf * (n3 - n2);

			n1 += noise_fsc(zf) * (n2 - n1);

			r += n1 * ampl;
			ampl *= perlin_amp_falloff;
			xi <<= 1;
			xf *= 2;
			yi <<= 1;
			yf *= 2;
			zi <<= 1;
			zf *= 2;

			if (xf >= 1.0f) {
				xi++;
				xf--;
			}
			if (yf >= 1.0f) {
				yi++;
				yf--;
			}
			if (zf >= 1.0f) {
				zi++;
				zf--;
			}
		}
		return r;
	}

	private static float noise_fsc(float i) {
		// using bagel's cosine table instead
		return 0.5f * (1.0f - perlin_cosTable[(int) (i * perlin_PI) % perlin_TWOPI]);
	}
}
