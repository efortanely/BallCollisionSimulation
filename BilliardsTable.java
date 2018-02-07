import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class BilliardsTable extends Engine {
	private ArrayList<CueBall> balls = new ArrayList<>();
	private Vector ballPoint;
	private CueBall selectedBall;

	// subdivide screen into grid and randomly select some of the subsections
	// to populate with a ball in a random position inside the subsection
	@Override
	public void first() {
		this.setSmooth(true);
		// partition size should be at least ball diameter+1 (but don't do this,
		// those poor balls)
		// ...you're going to do it, aren't you?
		int partitionSize = 70;
		Random rand = new Random();
		for (int i = -this.getWidth() / 2; i < this.getWidth() / 2 - partitionSize; i += partitionSize) {
			for (int j = -this.getHeight() / 2; j < this.getHeight() / 2 - partitionSize; j += partitionSize) {
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
				this.balls.add(ball);
			}
		}
	}

	@Override
	public void tick() {
		for (CueBall ball : this.balls)
			ball.tick();
	}

	@Override
	public void render(Graphics2D g) {
		g.setColor(new Color(0, 50, 0));
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		g.translate(this.getWidth() / 2, this.getHeight() / 2);
		g.setColor(Color.white);
		for (CueBall ball : this.balls)
			ball.render(g);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// shift mouse coordinate because of shifted graphics
		this.ballPoint = new Vector(e.getX() - this.getWidth() / 2, e.getY() - this.getHeight() / 2);

		// balls array is sorted by closest distance to mouse click
		CueBall[] ballsArray = this.balls.toArray(new CueBall[this.balls.size()]);
		Arrays.sort(ballsArray,
				(ballOne, ballTwo) -> (int) (ballOne.pos.distance(this.ballPoint) - ballTwo.pos.distance(this.ballPoint)));
		this.selectedBall = ballsArray[0];
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		Vector newPoint = new Vector(e.getX() - this.getWidth() / 2, e.getY() - this.getHeight() / 2);
		Vector newVel = newPoint.minus(this.ballPoint);
		this.selectedBall.vel = this.selectedBall.vel.plus(newVel);
	}

	public BilliardsTable(int width, int height, String title) {
		super(width, height, title);
	}

	public static void main(String[] s) {
		new BilliardsTable(600, 600, "Billiards Table").start();
	}

	private class CueBall {
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
		 * crucial things to remember about collisions!! 1. Object/object
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
			if (this.pos.x() + this.radius >= BilliardsTable.this.getWidth() / 2)
				this.vel.setX(-Math.abs(this.vel.x()));
			else if (this.pos.x() - this.radius <= -BilliardsTable.this.getWidth() / 2)
				this.vel.setX(Math.abs(this.vel.x()));

			if (this.pos.y() + this.radius >= BilliardsTable.this.getWidth() / 2)
				this.vel.setY(-Math.abs(this.vel.y()));
			else if (this.pos.y() - this.radius <= -BilliardsTable.this.getWidth() / 2)
				this.vel.setY(Math.abs(this.vel.y()));

			// friction
			this.vel = this.vel.times(0.98);

			// lower velocity before updating position if exceeds max value
			this.vel = this.vel.limit(25);

			// update position
			this.pos = this.pos.plus(this.vel);

			// ball collisions
			for (CueBall ball : BilliardsTable.this.balls) {
				if (!ball.equals(this) && this.intersecting(ball)) {
					// push back
					this.pos = this.pos.minus(this.vel);
					this.computeCollision(ball);
				}
			}
		}

		// spatial partitioning (quadtrees are more efficient, but it's better
		// than distance comparisons alone)
		private boolean intersecting(CueBall ballB) {
			double xDistance = Math.abs(this.pos.x() - ballB.pos.x());
			double yDistance = Math.abs(this.pos.y() - ballB.pos.y());
			double ballDistanceWithBuffer = 2 * (this.radius + ballB.radius);
			// ignore balls that are too far away to be colliding
			if (xDistance > ballDistanceWithBuffer || yDistance > ballDistanceWithBuffer)
				return false;
			else
				return this.pos.distanceOptimized(ballB.pos) <= Math.pow(this.radius + ballB.radius, 2);
		}

		// assume this is ball A
		private void computeCollision(CueBall ballB) {
			Vector distanceBtoA = this.pos.minus(ballB.pos);
			Vector negatedParallelVelA = this.vel.vectorProjectionOn2D(distanceBtoA);
			Vector perpendicularVelA = this.vel.minus(negatedParallelVelA);

			Vector distanceAtoB = ballB.pos.minus(this.pos);
			Vector negatedParallelVelB = ballB.vel.vectorProjectionOn2D(distanceAtoB);
			Vector perpendicularVelB = ballB.vel.minus(negatedParallelVelB);

			ballB.vel = perpendicularVelB.plus(negatedParallelVelA);
			this.vel = perpendicularVelA.plus(negatedParallelVelB);
		}

		public void render(Graphics2D g) {
			g.setColor(this.col);
			g.fillOval((int) (this.pos.x() - this.radius), (int) (this.pos.y() - this.radius), this.diameter, this.diameter);
		}
	}
}
