import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics2D;
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

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public abstract class Engine extends Canvas implements Runnable, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener{
	private static final long serialVersionUID = 1L;
	private boolean running = false;
	private boolean smooth = false;
	private static double fps=60;
	
	public Engine(int width, int height, String title){
		Dimension dimension = new Dimension(width,height);
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
	}

	public synchronized void start(){
		running = true;
		new Thread(this).start();
	}
	
	public abstract void first();
	public abstract void tick();
	public abstract void render(Graphics2D g);

	//ticks update logic, rendering updates graphics.
	//uncomment code in method to see number of frames and ticks updated per second! :)
	@Override public void run() {
		double lastTimeSec = System.nanoTime()/1E9;
		double changeInTimeSec = 0;
		double secondsPerFrame = 1/fps;
		boolean renderToFrame = false;
		//double timerMillisec = System.currentTimeMillis();
		//int ticksSec = 0;
		//int framesSec = 0;
		
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		
		first();
		
		while(running){
			double currentTimeSec = System.nanoTime()/1E9;
			changeInTimeSec += currentTimeSec - lastTimeSec;
			lastTimeSec = currentTimeSec;
			
			while(changeInTimeSec >= secondsPerFrame){
				//ticksSec++;
				tick();
				changeInTimeSec -= secondsPerFrame;
				renderToFrame = true;
			}
			
			if(renderToFrame){
				//framesSec++;
				
				BufferStrategy bufferStrategy = this.getBufferStrategy();
				if(bufferStrategy == null){
					//triple buffering strategy is industry standard for balance between efficiency and avoiding tearing
					this.createBufferStrategy(3);
				}else{
					Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
					if(smooth)
						g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
					render(g);
					g.dispose();
					bufferStrategy.show();
				}
				
				renderToFrame = false;
			}else{
				//sleeping before attempting to update the screen again helps efficiency
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			/*
			int secondInMillisec = 1000;
			if(System.currentTimeMillis() - timerMillisec > secondInMillisec){
				timerMillisec += secondInMillisec;
				System.out.println("Fps: " + framesSec + " Ticks: " + ticksSec);
				framesSec = ticksSec = 0;
			}
			*/
		}
	}
	
	public void setSmooth(boolean smooth){
		this.smooth = smooth;
	}
	
	public static BufferedImage grabImage(BufferedImage img, int row, int col, int width, int height) {
		return img.getSubimage(col*width - width, row*height - height, width, height);
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
	
	@Override public void keyPressed(KeyEvent e){}
	@Override public void keyReleased(KeyEvent e){}
	@Override public void keyTyped(KeyEvent e){}
	
	@Override public void mouseClicked(MouseEvent arg0) {}
	@Override public void mouseEntered(MouseEvent arg0) {}
	@Override public void mouseExited(MouseEvent arg0) {}
	@Override public void mousePressed(MouseEvent arg0) {}
	@Override public void mouseReleased(MouseEvent arg0) {}
	
	@Override public void mouseDragged(MouseEvent arg0) {}
	@Override public void mouseMoved(MouseEvent arg0) {}
	
	@Override public void mouseWheelMoved(MouseWheelEvent arg0) {}
}