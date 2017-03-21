package org.usfirst.frc.team4152.robot;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.interfaces.Accelerometer;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * This is a demo program showing the use of the RobotDrive class. The
 * SampleRobot class is the base of a robot application that will automatically
 * call your Autonomous and OperatorControl methods at the right time as
 * controlled by the switches on the driver station or the field controls.
 *
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SampleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 *
 * WARNING: While it may look like a good choice to use for your code if you're
 * inexperienced, don't. Unless you know what you are doing, complex code will
 * be much more difficult under this system. Use IterativeRobot or Command-Based
 * instead if you're new.
 */
public class Robot extends SampleRobot {
	
	////RobotDrive System////
							//front left, back left, front right, back right.
	RobotDrive myRobot = new RobotDrive(0,1);
	Joystick stick = new Joystick(0);
	Joystick rightStick = new Joystick(1);
	//driving modes
	final boolean arcadeDrive = true;
	final boolean tankDrive = false;
	
	////DriveAccel Variables////
	double driveAccelMultiIncrease = 0.15;
	double driveAccelMultiInit = 0.4;
	double driveAccelMulti = driveAccelMultiInit;
	Timer driveTimeAccel = new Timer();
	
	////DumpLoad System////
	//DigitalInput SwitchDumpAxisExceed = new DigitalInput(5);
	//DigitalInput SwitchHoldAxisExceed = new DigitalInput(6);
	Spark dumpMotor = new Spark(4);
	Talon loadingMotor = new Talon(2);
	
	////CLIMB////
	Spark climbMotor = new Spark(5);
	
	//stores button values in memory, makes the code easier to understand
    final int buttonA = 1;
    final int buttonB = 2;
    final int buttonX = 3;
    final int buttonY = 4;
    final int leftBumper = 5;
    final int rightBumper = 6;
    final int buttonBack = 7;
    final int buttonStart = 8;
    final int lsPush = 9;
    final int rsPush = 10;
    
    ////Encoder Values////
	Encoder leftEncoder = new Encoder(1, 0, false);
	Encoder rightEncoder = new Encoder(4, 3 , false);
    final double wheelCircumference = 6 * Math.PI;
    
    ////Rio Vision////
    CameraServer rioVision = CameraServer.getInstance();
    
    ////Sensors////
    Accelerometer accel = new BuiltInAccelerometer();
    Gyro gyro = new AnalogGyro(0);
    int gyroOffset = -21;
    DigitalInput gearButton = new DigitalInput(6);
    
	public Robot() {
		myRobot.setExpiration(0.1);
	}

	@Override
	public void robotInit() {
	}
	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * You can add additional auto modes by adding additional comparisons to the
	 * if-else structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomous() {
		gyro.reset();
		SmartDashboard.putDouble("Gyro angle", gyro.getAngle());
		leftEncoder.setDistancePerPulse(wheelCircumference/2036);
		rightEncoder.setDistancePerPulse(wheelCircumference/2036);
		//turnRight(180);
		drive(57, 0.6);
		while(!gearButton.get())
		{
			Timer.delay(0.001);
		}
		drive(12, -0.6);
		turnRight(45);
		drive(48, 0.6);
		turnLeft(45);
		drive(120, 0.7);
		/*while(!gearButton.get())
		{
			Timer.delay(0.001);
		}*/
		//turnRight(90);
		//turnRightVariableSpeed(90);
		SmartDashboard.putString("Gyro", gyro.getAngle() + ", 9000");
	}

	/**
	 * Runs the motors with arcade steering.
	 */
	@Override
	public void operatorControl() {
		double speedController = 1;// stick.getRawAxis(2);
		leftEncoder.setDistancePerPulse(wheelCircumference/2036);
		rightEncoder.setDistancePerPulse(wheelCircumference/2036);
		myRobot.setSafetyEnabled(true);
		driveTimeAccel.start();
		leftEncoder.reset();
		rightEncoder.reset();
		rioVision.startAutomaticCapture();
		while (isOperatorControl() && isEnabled()) {
			speedController =1;// stick.getRawAxis(2);
			SmartDashboard.putBoolean("BoolTest", true);
			SmartDashboard.putInt("OpticEncoder", leftEncoder.get());
			SmartDashboard.putDouble("distance driven left wheels", leftEncoder.getDistance()*-1);
			SmartDashboard.putDouble("distance driven right wheels", rightEncoder.getDistance());
			SmartDashboard.putDouble("Speed Controller", speedController*100);
			//Move Value, Rotate value
			if(arcadeDrive) myRobot.arcadeDrive(stick.getRawAxis(1) * driveAccelMulti * speedController, stick.getRawAxis(0) * speedController * -driveAccelMulti);
			else if(tankDrive) myRobot.tankDrive(stick.getRawAxis(1), rightStick.getRawAxis(1));
			driveAccel();
			dumpBalls();
			loadingSystem();
			climbSystem();
			Timer.delay(0.005); // wait for a motor update time
		}
	}
	
	public void climbSystem()
	{
		if(stick.getRawButton(buttonStart) && stick.getRawButton(buttonBack))
		{
			climbMotor.set(-1);
		}
		else if(stick.getRawAxis(2) >= 0.33)
		{
			climbMotor.set(stick.getRawAxis(2));
		}
		else
		{
			climbMotor.set(0);
		}
		
	}
	
	public void dumpBalls()
	{
		if(stick.getRawButton(buttonY) /*&& SwitchDumpAxisExceed.get() == false*/)
		{
			dumpMotor.set(0.9);
		} 
		else if(stick.getRawButton(buttonA)/* && SwitchHoldAxisExceed.get() == false*/)
		{
			dumpMotor.set(-0.4);
		}
		else
		{
			dumpMotor.set(0);
		}
	}
	
	double loadingPower = 0;
	
	public void loadingSystem()
	{
		System.out.println("loadingSystem called");
		SmartDashboard.putDouble("LOAD POWAR", loadingPower);
		if(stick.getRawButton(rightBumper))
		{
			loadingMotor.set(-1);
			System.out.println("right Bumper pushed");
		}
		else if(stick.getRawButton(leftBumper))
		{
			loadingMotor.set(1);
		}
		else
		{
			loadingMotor.set(0);
		}
	}
	
	public void driveAccel()
	{
		SmartDashboard.putDouble("Axis0", stick.getRawAxis(0));
		SmartDashboard.putDouble("Axis1", stick.getRawAxis(1));
		SmartDashboard.putDouble("DriveMulti", driveAccelMulti);
		//checks if axis 0 exceeds range of 0.1 in either direction, then same for axis 1. After those checks it checks the timer to see if it exceeds 0.1 seconds. Axis 0 and Axis 1 exist as an OR due to one being able to equal 0 and one equal 1 or -1. Timer is an AND due to it being required during check to not jump up by a delay of 0.005.
		if((stick.getRawAxis(0) < -0.1 || stick.getRawAxis(0) > 0.1) || (stick.getRawAxis(1) < -0.1 || stick.getRawAxis(1) > 0.1))
		{
			if(driveAccelMulti < 0.9 && driveTimeAccel.get() > 0.075)
			{
				driveAccelMulti += driveAccelMultiIncrease;
				driveTimeAccel.reset();
			}
		}
		else
		{
			driveAccelMulti = driveAccelMultiInit;
		}
	}
	
	//Distance in inches
	public void drive(double distance){
		drive(distance, 1);
	}
	
	public void drive(double distance, double speed){
		leftEncoder.reset();
		gyro.reset();
		Timer.delay(0.2);
		while(Math.abs(leftEncoder.getDistance())<distance)
		{
			if(gyro.getAngle() > 0.5) myRobot.arcadeDrive(-speed, 0.3);
			else if(gyro.getAngle() < 0.5) myRobot.arcadeDrive(-speed, -0.3);
			else myRobot.arcadeDrive(-speed, 0);
		}
		SmartDashboard.putDouble("distance driven left wheels", leftEncoder.getDistance()*-1);
		SmartDashboard.putDouble("distance driven right wheels", rightEncoder.getDistance());
		
		while(true)
		{
			SmartDashboard.putDouble("accel x", accel.getX());
			SmartDashboard.putDouble("accel z", accel.getZ());
			SmartDashboard.putDouble("accel y", accel.getY());
			if(accel.getX() > 0.2) return;
			else myRobot.arcadeDrive(speed,0);
		}
		
	}
	
	final double pivotCircumference = 25.5*Math.PI;
	final double pivotDegree = pivotCircumference / 360;
	final double speed = 0.48;//was 0.4
	final double preStop = speed / 4;
	
	public void turnRight(double degrees){
		/*leftEncoder.reset();
		double pivotDegrees = pivotDegree * degrees;
		while(Math.abs(leftEncoder.getDistance()) < (pivotDegrees - (pivotDegrees * preStop))){
			myRobot.arcadeDrive(0, -speed);
			SmartDashboard.putString("Stopping turn", "false");
			SmartDashboard.putDouble("left Encoder", leftEncoder.getDistance());
			SmartDashboard.putDouble("Gyro angle", gyro.getAngle());
		}
		myRobot.stopMotor();*/
		gyroOffset = (int) -(Math.pow((degrees/29), 1.25)+4);
		boolean doneTurning = false;
		gyro.reset();
		Timer.delay(0.01);
		degrees = (degrees + gyroOffset) *100;
		int adjust = 0, swap = 1;
		
		while(!doneTurning){
			int gyroAngle = (int) (gyro.getAngle()*100);
			
			if(gyroAngle < degrees && adjust < 3){
				if(swap != 1) adjust++;
				if(adjust >=1){
					myRobot.arcadeDrive(0, -0.45);
				}else{
					myRobot.arcadeDrive(0, -speed);
				}				swap = 1;
			}else if(gyroAngle > degrees && adjust < 3){
				if(swap != 2) adjust++;
				if(adjust >=1) myRobot.arcadeDrive(0, 0.45);
				else myRobot.arcadeDrive(0, speed);
				swap = 2;
			}else{
				doneTurning = true;
			}
			
			SmartDashboard.putString("Gyro", gyroAngle + ", " + degrees);
			Timer.delay(0.001);
		}
		/*double lastDistance = Math.abs(leftEncoder.getDistance());
		SmartDashboard.putDouble("left Encoder", leftEncoder.getDistance());
		Timer.delay(0.01);
		while((Math.abs(leftEncoder.getDistance()) - lastDistance) >= 0.5)
		{
			SmartDashboard.putDouble("left Encoder", leftEncoder.getDistance());
			SmartDashboard.putString("Stopping turn", "Stopping");
			myRobot.arcadeDrive(0,1);
			lastDistance = Math.abs(leftEncoder.getDistance());
		}
		//myRobot.arcadeDrive(0, 1);*/
	}
	
	public void turnRightVariableSpeed(double degrees){
		leftEncoder.reset();
		double pivotDegrees = pivotDegree * degrees;
		double percentage = leftEncoder.getDistance() / pivotDegrees;
		while(Math.abs(leftEncoder.getDistance()) < pivotDegrees){
			if(percentage > 0.50)
			{
				myRobot.arcadeDrive(0,-1);
			}
			else if( percentage > 0.10)
			{
				myRobot.arcadeDrive(0,-0.75);
			}
			else
			{
				myRobot.arcadeDrive(0, -0.5);
			}
		}
		/*double lastDistance = leftEncoder.getDistance();
		while((Math.abs(leftEncoder.getDistance()) - lastDistance) >= 0.75)
		{
			myRobot.arcadeDrive(0,1);
			lastDistance = Math.abs(leftEncoder.getDistance());
		}*/
		//myRobot.arcadeDrive(0, 1);
	}
	
	public void turnLeft(double degrees){
		gyroOffset = (int) -(Math.pow((degrees/29), 1.25)+4);
		boolean doneTurning = false;
		gyro.reset();
		Timer.delay(0.01);
		degrees = (degrees + gyroOffset) *100;
		int adjust = 0, swap = 1;
		
		while(!doneTurning){
			int gyroAngle = (int) (gyro.getAngle()*100);
			
			if(gyroAngle > -degrees && adjust < 3){
				if(swap != 1) adjust++;
				if(adjust >=1){
					myRobot.arcadeDrive(0, 0.45);
				}else{
					myRobot.arcadeDrive(0, speed);
				}				
				swap = 1;
			}else if(gyroAngle < -degrees && adjust < 3){
				if(swap != 2) adjust++;
				if(adjust >=1) myRobot.arcadeDrive(0, -0.45);
				else myRobot.arcadeDrive(0, -speed);
				swap = 2;
			}else{
				doneTurning = true;
			}
			
			SmartDashboard.putString("Gyro", gyroAngle + ", " + degrees);
			Timer.delay(0.001);
		}
	}
	
	/*public void pivotRight(){
		leftEncoder.reset();
		while(Math.abs(leftEncoder.getDistance()) < 24.4){
			myRobot.tankDrive(1, 0);
		}
	}
	
	public void pivotLeft(){
		rightEncoder.reset();
		while(Math.abs(rightEncoder.getDistance()) < 24.4){
			myRobot.tankDrive(0, 1);
		}
	}*/
	
	/**
	 * Runs during test mode
	 */
	@Override
	public void test() {
	}
}