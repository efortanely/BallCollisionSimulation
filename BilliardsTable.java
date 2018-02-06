import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class BilliardsTable extends Engine {
	private ArrayList<CueBall> balls = new ArrayList<CueBall>();

	// subdivide screen into grid and randomly select some of the subsections
	// to populate with a ball in a random position inside the subsection
	@Override
	public void first() {
		// partition size should be at least ball diameter+1 (but don't do this,
		// those poor balls)
		// ...you're going to do it, aren't you?
		int partitionSize = 70;
		Random rand = new Random();
		for (int i = -getWidth() / 2; i < getWidth() / 2 - partitionSize; i += partitionSize) {
			for (int j = -getHeight() / 2; j < getHeight() / 2 - partitionSize; j += partitionSize) {
				boolean filterSubsection = rand.nextInt(2) == 0;
				if (filterSubsection)
					continue;

				int maxVel = 20;
				Vector velocity = new Vector(rand.nextInt(2 * maxVel) - maxVel, rand.nextInt(2 * maxVel) - maxVel);

				int diameter = 30;
				int radius = diameter / 2;
				int upperX = i + partitionSize - radius;
				int lowerX = i + radius;
				int upperY = j + partitionSize - radius;
				int lowerY = j + radius;
				Vector position = new Vector(rand.nextInt(upperX - lowerX) + lowerX,
						rand.nextInt(upperY - lowerY) + lowerY);

				CueBall ball = new CueBall(velocity, position, diameter);
				balls.add(ball);
			}
		}
	}

	@Override
	public void tick() {
		for (CueBall ball : balls)
			ball.tick();
	}

	@Override
	public void render(Graphics2D g) {
		g.setColor(new Color(0, 50, 0));
		g.fillRect(0, 0, getWidth(), getHeight());
		g.translate(getWidth() / 2, getHeight() / 2);
		setSmooth(true);
		g.setColor(Color.white);
		for (CueBall ball : balls)
			ball.render(g);
	}

	private static Vector ballPoint;
	private static CueBall selectedBall;

	@Override
	public void mousePressed(MouseEvent e) {
		// shift mouse coordinate because of shifted graphics
		ballPoint = new Vector(e.getX() - getWidth() / 2, e.getY() - getHeight() / 2);

		// balls array is sorted by closest distance to mouse click
		CueBall[] ballsArray = balls.toArray(new CueBall[balls.size()]);
		Arrays.sort(ballsArray);
		selectedBall = ballsArray[0];
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		Vector newPoint = new Vector(e.getX() - getWidth() / 2, e.getY() - getHeight() / 2);
		Vector newVel = newPoint.minus(ballPoint);
		selectedBall.vel = selectedBall.vel.plus(newVel);
	}

	public BilliardsTable(int width, int height, String title) {
		super(width, height, title);
	}

	public static void main(String[] s) {
		new BilliardsTable(600, 600, "Billiards Table").start();
	}

	private class CueBall implements Comparable<CueBall> {
		private Vector vel;
		private Vector pos;
		private Color col;
		private int diameter;
		private int radius;

		public CueBall(Vector vel, Vector pos, int diameter, Color col) {
			this.vel = vel;
			this.pos = pos;
			this.diameter = diameter;
			this.radius = diameter / 2;
			this.col = col;
		}

		public CueBall(Vector vel, Vector pos, int diameter) {
			this(vel, pos, diameter, Color.white);
		}

		/*
		 * crucial things to remember about collisions! 1. Object/object
		 * collisions-push back your objects before computing the collision to
		 * ensure they don't repeatedly render themselves as intersecting and
		 * get stuck together. 2. Wall/objects collisions-for the wall
		 * collisions, pushing back tends to push the ball back into neighboring
		 * balls, so to avoid them getting stuck inside other balls and
		 * vibrating against the wall, just update the velocity with the
		 * corresponding pos/neg (depending on which wall) ABSOLUTE VALUE of the
		 * respective velocity (this is to avoid the vibrating when it's found
		 * to be intersecting with the wall for multiple frames)
		 */
		public void tick() {
			// wall collisions
			if (pos.x() + radius >= getWidth() / 2)
				vel.setX(-Math.abs(vel.x()));
			else if (pos.x() - radius <= -getWidth() / 2)
				vel.setX(Math.abs(vel.x()));

			if (pos.y() + radius >= getWidth() / 2)
				vel.setY(-Math.abs(vel.y()));
			else if (pos.y() - radius <= -getWidth() / 2)
				vel.setY(Math.abs(vel.y()));

			// friction
			vel = vel.times(0.98);

			// lower velocity before updating position if exceeds max value
			double velocityCap = 25;
			this.vel.limit(velocityCap);
			
			// update position
			pos = pos.plus(vel);

			// ball collisions
			for (CueBall ball : balls) {
				if (!ball.equals(this) && this.intersecting(ball)) {
					// push back
					pos = pos.minus(vel);
					computeCollision(ball);
				}
			}
		}

		// spatial partitioning (quadtrees are more efficient, but it's better
		// than distance comparisons alone)
		private boolean intersecting(CueBall ballB) {
			double xDistance = Math.abs(pos.x() - ballB.pos.x());
			double yDistance = Math.abs(pos.y() - ballB.pos.y());
			double ballDistanceWithBuffer = 2 * (this.radius + ballB.radius);
			// ignore balls that are too far away to be colliding
			if (xDistance > ballDistanceWithBuffer || yDistance > ballDistanceWithBuffer)
				return false;
			else
				return pos.distanceOptimized(ballB.pos) <= Math.pow(radius + ballB.radius, 2);
		}

		// assume this is ball A
		private void computeCollision(CueBall ballB) {
			Vector distanceBtoA = pos.minus(ballB.pos);
			Vector negatedParallelVelA = vel.vectorProjectionOn2D(distanceBtoA);
			Vector perpendicularVelA = vel.minus(negatedParallelVelA);

			Vector distanceAtoB = ballB.pos.minus(pos);
			Vector negatedParallelVelB = ballB.vel.vectorProjectionOn2D(distanceAtoB);
			Vector perpendicularVelB = ballB.vel.minus(negatedParallelVelB);

			ballB.vel = perpendicularVelB.plus(negatedParallelVelA);
			vel = perpendicularVelA.plus(negatedParallelVelB);
		}

		public void render(Graphics g) {
			g.setColor(this.col);
			g.fillOval((int) (pos.x() - radius), (int) (pos.y() - radius), diameter, diameter);
		}

		@Override
		public int compareTo(CueBall ballB) {
			double distanceA = this.pos.distance(ballPoint);
			double distanceB = ballB.pos.distance(ballPoint);
			if (distanceA < distanceB)
				return -1;
			else if (distanceB > distanceA)
				return 1;
			else
				return 0;
		}
	}
}
