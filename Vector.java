import java.awt.Graphics;
import java.awt.Point;
import java.util.Arrays;

public class Vector{
	double[] v;
	int dimension;
	
	//CONSTRUCTORS
	public Vector(double x, double y){
		this.v = new double[]{x,y};
		this.dimension = 2;
	}
	
	public Vector(double x, double y, double z){
		this.v = new double[]{x,y,z};
		this.dimension = 3;
	}

	public Vector(double[] arr){
		this.v = arr;
		if(v.length==2) dimension = 2;
		else dimension = 3;
	}
	
	public Vector(Point p){
		this.v = new double[]{p.getX(),p.getY()};
		this.dimension = 2;
	}
	
	//spherical -> cartesian
	public Vector(String coordinateSystem, double ρ, double θ, double φ){ 
		this(ρ*Math.cos(θ)*Math.sin(φ),ρ*Math.sin(θ)*Math.sin(φ),ρ*Math.cos(φ));
	}
	
	//polar -> cartesian
	public Vector(String coordinateSystem, double r, double θ){ 
		this(r*Math.cos(θ),r*Math.sin(θ));
	}
	
	//BASIC VECTOR OPERATIONS		
	public Vector plus(Vector b){
		double[] p = new double[dimension];
		for(int i = 0; i < dimension; i++){
			p[i] = v[i] + b.v[i];
		}
		return new Vector(p);
	}
	
	public Vector minus(Vector b){
		double[] m = new double[dimension];
		for(int i = 0; i < dimension; i++){
			m[i] = v[i] - b.v[i];
		}
		return new Vector(m);
	}

	public Vector times(double a){
		double[] t = new double[dimension];
		for(int i = 0; i < dimension; i++){
			t[i] = v[i]*a;
		}
		return new Vector(t);
	}
	
	public Vector divide(double a){
		double[] d = new double[dimension];
		for(int i = 0; i < dimension; i++){
			d[i] = v[i]/a;
		}
		return new Vector(d);
	}
	
	public Vector unit(){
		double mag = magnitude();
		if(dimension==2) return new Vector(x()/mag,y()/mag);
		else return new Vector(x()/mag,y()/mag,z()/mag);
	}
	
	//BASIC SCALAR OPERATIONS
	public double dot(Vector b){
		double sum = 0;
		for(int i = 0; i < dimension; i++){
			sum+=v[i]*b.v[i];
		}
		return sum;
	}

	public double magnitude(){
		return Math.sqrt(magnitudeOptimized());
	}
	
	public double magnitudeOptimized(){
		return this.dot(this);
	}
	
	public double distanceOptimized(Vector v2){
		return this.minus(v2).magnitudeOptimized();
	}
	
	public double distance(Vector v2){
		return this.minus(v2).magnitude();
	}
	
	//2D METHODS
	public Vector vectorProjectionOn(Vector on){
		return on.times(this.dot(on)/on.dot(on));
	}
	
	//definition for dot product |A||B|cos(θ)=A.B -> θ = cos^-1(A.B/|A||B|)
	public double getAngle(Vector v2){
		return Math.acos(this.dot(v2)/(magnitude()*v2.magnitude()));
	}
	
	public void drawVector(Graphics g, Vector offset){
		g.drawLine((int)x(), (int)y(), (int)(x()+offset.x()), (int)(y()+offset.y()));
	}
	
	//3D METHODS
	public Vector crossProduct(Vector v2){
		double a = x(), b = y(), c = z(), d = v2.x(), e = v2.y(), f = v2.z();
		return new Vector(b*f-e*c,c*d-a*f,a*e-b*d);			
	}

	public Point getRender(double θ, double φ){ 
		return new Point((int)(x()*Math.cos(θ)-y()*Math.sin(θ)),(int)((x()*Math.sin(θ)+y()*Math.cos(θ))*Math.sin(φ)-z()*Math.cos(φ)));
	}

	public void drawVector(Graphics g, Vector offset, double θ, double φ){
		Point tail = this.getRender(θ, φ);
		Point tip = this.plus(offset).getRender(θ, φ);
		g.drawLine(tip.x, tip.y, tail.x, tail.y);
	}

	public void drawPoint(Graphics g, Vector offset, double θ, double φ, int width){
		Point tip = this.plus(offset).getRender(θ, φ);
		g.fillOval(tip.x, tip.y, width, width);
	}
	
	//GETTERS AND SETTERS
	public double x(){
		return v[0];
	}
	
	public double y(){
		return v[1];
	}
	
	public double z(){
		return v[2];
	}
	
	public void setX(double x){
		v[0] = x;
	}
	
	public void setY(double y){
		v[1] = y;
	}

	public void setZ(double z){
		v[2] = z;
	}
	
	public void setVector(double x, double y){
		setX(x);
		setY(y);
	}
	
	public void setVector(double x, double y, double z){
		setVector(x,y);
		setZ(z);
	}
	
	public double[] getArray(){
		if(dimension==2) return new double[]{x(),y()};
		else return new double[]{x(),y(),z()};
	}
	
	@Override public String toString(){ 
		return Arrays.toString(this.v);
	}
}