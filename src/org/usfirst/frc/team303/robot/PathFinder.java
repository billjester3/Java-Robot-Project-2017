package org.usfirst.frc.team303.robot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;
import jaci.pathfinder.followers.EncoderFollower;
import jaci.pathfinder.modifiers.TankModifier;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;




public class PathFinder {

	double timeStep = 0.05;
	double maxVel = 4;
	double maxAccel = 7;
	double maxJerk = 5;
	double wheelBaseWidth = 2.5;
	int ticksPerRev = 2304; 
	double wheelDiameter = 0.3283333333333333 ;
	//done in feet for now

	double p = 0.3;
	double i = 0;
	double d = 0;
	double velocityRatio = 1/maxVel;
	double accelGain = 0;




	// The first argument is the proportional gain. Usually this will be quite high
	// The second argument is the integral gain. This is unused for motion profiling
	// The third argument is the derivative gain. Tweak this if you are unhappy with the tracking of the trajectory
	// The fourth argument is the velocity ratio. This is 1 over the maximum velocity you provided in the 
	//	      trajectory configuration (it translates m/s to a -1 to 1 scale that your motors can read)
	// The fifth argument is your acceleration gain. Tweak this if you want to get to a higher or lower speed quicker

	double l;
	double r;

	EncoderFollower testEncLeft;
	EncoderFollower testEncRight;

	public void pathFinderInit(){
		
		Waypoint[] testPoints = new Waypoint[]{
				new Waypoint(0,0,Pathfinder.d2r(0)),
				new Waypoint(8,0,Pathfinder.d2r(0)),
				//new Waypoint(10,4,Pathfinder.d2r(90)),
				//new Waypoint(10,10,Pathfinder.d2r(90))
				//new Waypoint(4,2,Pathfinder.d2r(90))
		};

		Trajectory.Config testConfig = new Trajectory.Config(Trajectory.FitMethod.HERMITE_CUBIC, Trajectory.Config.SAMPLES_HIGH, timeStep, maxVel, maxAccel, maxJerk);
		Trajectory testTrajectory = Pathfinder.generate(testPoints, testConfig);
		TankModifier testModifier = new TankModifier(testTrajectory).modify(wheelBaseWidth);

		Trajectory testLeftTrajectory = testModifier.getLeftTrajectory();
		Trajectory testRightTrajectory = testModifier.getRightTrajectory();
		
		testEncLeft = new EncoderFollower(testLeftTrajectory);
		testEncRight = new EncoderFollower(testRightTrajectory);

		testEncLeft.configureEncoder(Robot.drivebase.getLeftEncoder(), ticksPerRev, wheelDiameter);
		testEncRight.configureEncoder(Robot.drivebase.getRightEncoder(), ticksPerRev, wheelDiameter);
		testEncLeft.configurePIDVA(p, i, d, velocityRatio, accelGain);
		testEncRight.configurePIDVA(p, i, d, velocityRatio, accelGain);

	}

	public void followPath(){
		l = testEncLeft.calculate(Robot.drivebase.getLeftEncoder());
		r = testEncRight.calculate(Robot.drivebase.getRightEncoder());

		double theta = Robot.navX.getYaw();
		double desiredHeading = Pathfinder.r2d(testEncLeft.getHeading());
		double angleDifference = Pathfinder.boundHalfDegrees(desiredHeading-theta);
		double turn = -0.02*angleDifference;

		Robot.drivebase.drive(l+turn, r-turn);
		
	}

	public void IOThread() {
		Thread t = new Thread(()-> {

			File f;
			FileWriter fw;
			BufferedWriter bw;

			try {
				f = new File("/home/lvuser/Output.txt");
				fw = new FileWriter(f);
				bw = new BufferedWriter(fw);

				while(!OI.lBtn[4]) {try {Thread.sleep(1);} catch (Exception e) {}}

				while(OI.lBtn[4]) {
					
				}

				bw.close();
				fw.close();
			} catch (IOException e) {e.printStackTrace();}

		});
		t.start();
		t.setDaemon(true);
	}




}
