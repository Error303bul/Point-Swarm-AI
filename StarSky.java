import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;

public class Main extends JFrame implements Runnable{
	
	private static final long serialVersionUID = 1L;
	
	public final int maxObjects=1000, infoPerObject=6, fps=100;
	public int[][] objects;
	/*
	slip: 0-100 (100 no slip, 0 full slip)
	distance: minimum distance between objects (any)
	objectsSpeed: the speed of moving objects
	visionDistance: if the distance to the nearest object is greater than this, the object will move randomly
	objectRadius: just a radius of objects
	gameSpeed: indicates how many times the code is played
	summonObjects: how much objects will spawn
	*/
	public static int slip=50, distance=50, objectSpeed=5,visionDistance=75, objectsRadius=10, gameSpeed=1000, summonObjects=100;
	//Colors
	public Color[] color= {
			new Color(255, 0, 229),
			new Color(252, 0, 0),
			new Color(202, 0, 252),
			new Color(0, 8, 252),
			new Color(0, 130, 252),
			new Color(0, 236, 252),
			new Color(0, 252, 130),
			new Color(4, 252, 0),
			new Color(248, 252, 0),
			new Color(252, 151, 0)
	};
	private BufferedImage buf;
	
	public final void randPos(int id) {
		objects[id][1]=rnd(0, this.getWidth());
		objects[id][2]=rnd(0, this.getHeight());
	}
	
	public final static int distance(int x1, int y1, int x2, int y2) {
		int dx=x2-x1;
		int dy=y2-y1;
		int result=(int) Math.sqrt(dx*dx+dy*dy);
		return result;
	}
	
	public double calculateSpeed() {
		double count=0;
		double total=0;
		for(int i=0;i<maxObjects;i++) {
			if(Math.abs(objects[i][3])+Math.abs(objects[i][4])!=0) {
				total+=(Math.abs(objects[i][3])+Math.abs(objects[i][4]))/2;
				count++;
			}
		}
		double result=(count>0)?(total/count):total;
		return result;
	}
	
	public final int nearestObject(int i) {
		int target=-1, targetDistance=99999999;
		for(int check=0;check<maxObjects;check++) {
			if(check==i) continue;
			if(objects[check][0]!=0) {
				int checkDistance=objectsDistance(i, check);
				if(checkDistance<targetDistance) {
					targetDistance=checkDistance;
					target=check;
				}
			}
		}
		return target;
	}
	
	
	public final static int direction(int x1, int y1, int x2, int y2) {
		int dx=x2-x1;
		int dy=y2-y1!=0?y2-y1:1;
		int result=(int) Math.atan(dx/dy);
		if(dy<0) {
			if(dx>0) {
				result+=135;
			}else {
				result-=135;
			}
		}
		return result;
	}
	
	public final int objectsDistance(int o1, int o2) {
		return distance(objects[o1][1], objects[o1][2], objects[o2][1], objects[o2][2]);
	}
	
	public final int objectsDirection(int o1, int o2) {
		return direction(objects[o1][1], objects[o1][2], objects[o2][1], objects[o2][2]);
	}


	
	public final static int rnd(int min,int max) {
		double gen = (Math.random() *((max - min) + 1)) + min;
		int result=(int)gen;
		return result;
	}
	
	public void createObjects(int count) {
		for(int i=0; i<count; i++) {
			objects[i][0]=1;
			objects[i][1]=0;
			objects[i][2]=0;
			objects[i][3]=0;
			objects[i][4]=0;
			objects[i][5]=rnd(0,color.length-1);
			randPos(i);
		}
	}
	
	public void resetObjects() {
		objects=new int[maxObjects][infoPerObject];
		for(int i=0;i<maxObjects;i++) {
			for(int j=0;j<infoPerObject;j++) {
				objects[i][j]=0;
			}
		}
		print("Objects reseted");
	}
	
	public static final void print(String text) {
		System.out.println(text);
	}
	
	public Main(String name) {
		this.setVisible(true);
		this.setTitle(name);
		this.setSize(750, 750);
		this.setAlwaysOnTop(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		print("Runner created");
	}
	
	
	@Override
	public void paint(Graphics g) {
		buf=new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
		drawAll();
		((Graphics2D)g).drawImage(buf, null, 8, 30);
		print("Paint function called");
	}
	
	private void drawAll() {
		Graphics2D gr=buf.createGraphics();
		gr.setColor(Color.BLACK);
		gr.fillRect(0, 0, this.getWidth(), this.getHeight());
		for(int i=0; i<maxObjects;i++) {
			if(objects[i][0]!=0) {
				gr.setColor(color[objects[i][5]]);
				gr.fillOval(objects[i][1]-objectsRadius/2, objects[i][2]-objectsRadius/2, objectsRadius, objectsRadius);
				int near=nearestObject(i);
				int dist=objectsDistance(i, near);
				if(dist<=visionDistance)	{
					gr.drawLine(objects[i][1], objects[i][2], objects[near][1], objects[near][2]);
				}
			}
		}
		gr.setColor(Color.white);
		gr.drawString(Double.toString(calculateSpeed()), 50, 50);
	}

	public static void main(String[] args) {
		print("Main function called");
		new Thread(new Main("Point Swarm AI")).start();
	}
	
	public final void runFrames(int count) {
		for(int i=0;i<count;i++){
			logic();
		}
	}

	@Override
	public void run() {
		resetObjects();
		if(summonObjects>maxObjects) summonObjects=maxObjects;
		createObjects(summonObjects);
		print("Loop started");
		while(true) {
			this.repaint();
			runFrames(gameSpeed);
			try {
				Thread.sleep(1000/fps);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			print("One tick completed");
		}
	}

	private void logic() {
		for(int i=0;i<maxObjects;i++) {
			if(objects[i][0]==1) {
				objects[i][1]+=objects[i][3]/fps;
				objects[i][2]+=objects[i][4]/fps;
				objects[i][3]=slip*objects[i][3]/100;
				objects[i][4]=slip*objects[i][4]/100;
				if(objects[i][1]<0) randPos(i);
				if(objects[i][2]<0) randPos(i);
				if(objects[i][1]>this.getWidth()) randPos(i);
				if(objects[i][2]>this.getHeight()) randPos(i);
				int target=nearestObject(i);
				int targetDistance=target>-1?objectsDistance(i,target):999999;
				if(target >-1) {
					int targetDir=objectsDirection(i, target);
					if(targetDistance>visionDistance) {
						targetDir=rnd(-180, 180);
						objects[i][3]+=(Math.sin(targetDir)*objectSpeed*fps);
						objects[i][4]-=(Math.cos(targetDir)*objectSpeed*fps);
					}else if(targetDistance<distance) {
						objects[i][3]-=(Math.sin(targetDir)*objectSpeed*fps);
						objects[i][4]+=(Math.cos(targetDir)*objectSpeed*fps);
					}
				}
				
			}
		}
	}
}
