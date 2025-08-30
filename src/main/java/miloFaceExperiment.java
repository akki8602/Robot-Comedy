/*
*	Milo Socket Connector
*	Version: 1.0
*
*	Description: The following code allows for a TCP connection to Milo
*		Allowing for commands to be run on Milo automatically. In order to run,
*		a text file with commands is needed and the actual commands need to be coded in.
*		Basic facial expression commands are already included.
*
*	Author: Alex Santiago-Anaya
*	Last Revised: October 31st, 2020 - AES
*
*	For questions about the code, contact alexesantiago@vt.edu
*/

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class miloFaceExperiment {
	
	static String IP = "100.64.235.149"; //TODO: SET THIS TO MILO IP
	static boolean running = true; //set to false to end threads

	public static void main(String[] args) throws UnknownHostException, IOException {
		System.out.println("Link Start!");		
		
		new miloFaceExperiment().runMain(args); //Avoiding static function for threads 
	}
	
	public void runMain(String[] args) throws UnknownHostException, IOException 
	{
		System.out.println("runMain entered");
		try {
			Socket sock = new Socket(IP, 4001); 
			System.out.println("sock created");
			OutputStream sockOut = sock.getOutputStream();
			System.out.println("sockOut created");
			InputStream sockIn = sock.getInputStream();
			System.out.println("sockIn created");
		
			Thread userOut = new Thread() {	
				public void run() {
					BufferedReader read = new BufferedReader(new InputStreamReader(sockIn));
					while(running) {
						try {
							String strIn = read.readLine();
							if(strIn != null && strIn.length()>0) {
								System.out.println(strIn);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			};
			userOut.start();
			
			//Initialization commands and parser.
			BufferedReader read = null;
			try {
				System.out.println("About to read file");
				read = new BufferedReader(new FileReader("Pigs.txt")); //TODO: CHANGE your file name as needed
				System.out.println("Opened script file: Pigs.txt");
			}catch(Exception ex) {
				ex.printStackTrace();
			}
			
			String fileInput = "";
			
			String command = "";
			Queue<String> commQueue = new LinkedList<>();
		
			//ABOUT CASENUM
			//at 1, caseNum is in WHITE SPACE MODE (space and tab)
			//at 2, caseNum is in COMMAND MODE (anything that isn't white space or #)
			//at 3, caseNum is in COMMENT MODE (Starts with a #)
			int caseNum = 1;
			
			
			//Big boy parser. puts commands in queue.
			fileInput = read.readLine();
			while(fileInput != null) {
				for(int i = 0; i < fileInput.length(); i++) {
					switch(caseNum) {
						case 1:
							if(fileInput.charAt(i) == '#') {
								caseNum = 3;
								i--;
							}
							else if(fileInput.charAt(i) != ' ' && fileInput.charAt(i) != '\t') {
								caseNum = 2;
								i--;
							}
							break;
						case 2:
							if(i == 0 || (fileInput.charAt(i-1) == ' ' || fileInput.charAt(i-1) == '\t')) {
								command += fileInput.charAt(i);
							}
							else if(fileInput.charAt(i) != ' ' && fileInput.charAt(i) != '\t') {
								if(fileInput.charAt(i) == '#') {
									i--;
									commQueue.add(command);
									command = "";
									caseNum = 3;
								}
								else {
									command += fileInput.charAt(i); 
								}
							}
							else {
								commQueue.add(command);
								command = "";
								caseNum = 1;
							}
							if(i+1 == fileInput.length()) {
								commQueue.add(command);
								command = "";
							}
							break;
						case 3:
							i = fileInput.length();
							break;
					}
				}
				fileInput = read.readLine();
			}
			
			Thread userIn = new Thread() {
				
				public void run() {
					try {
						
						String toDo = "";
						String toExecute;
						BufferedReader sysin = null;
						try {
							sysin = new BufferedReader(new InputStreamReader(System.in));
						} catch(Exception ex) {
							ex.printStackTrace();
						}
						String temp = "";
						
						//Sets the robot to neutral
						toExecute = "expression default\n";
						sockOut.write((toExecute + "\n").getBytes());
						
						for(int j = 0; j < 3; j++) {
							toExecute = "keyframe set 300:100|301:100|311:100|320:100|322:100\n";
							sockOut.write((toExecute + "\n").getBytes());
						}
						
						//Now lets do some commands!
						while(running) {
							System.out.println(commQueue);
							toDo = sysin.readLine();
							if(toDo != null) {
								try {
									//System.out.println("Input anything to continue");
									//temp = sysin.readLine(); //Indefinite Delay, Input when ready to move on.	
									//toDo = commQueue.peek();
									toExecute = "";
							
									if(toDo.equalsIgnoreCase("end")) { //Commands run until the file line says "end"
										running = false;
										break;
									}
									
									//General Commands - FOR TESTING PURPOSES
							
									if(toDo.equalsIgnoreCase("happy")) {
										toExecute = "keyframe set 320:200|322:150|300:125|311:100|301:150\n";
										sockOut.write((toExecute + "\n").getBytes());
										
										toExecute = "speak I am happy, you should be too!\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
							
									if(toDo.equalsIgnoreCase("sad")) {
										toExecute = "expression frown\n";
										sockOut.write((toExecute + "\n").getBytes());
										for(int i = 0; i < 4 ; i++) {
											toExecute = "keyframe set 300:200\n";
											sockOut.write((toExecute + "\n").getBytes());
										}
									}
							
									if(toDo.equalsIgnoreCase("fear")) {
										for(int i = 0; i < 2; i++) {
											toExecute = "keyframe set 320:0|322:125|300:200|302:200|311:100\n";
											sockOut.write((toExecute + "\n").getBytes());
										}
									}
							
									if(toDo.equalsIgnoreCase("disgust")) {
										toExecute = "keyframe set 320:0|322:125|300:200|301:50|311:100\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
							
									if(toDo.equalsIgnoreCase("anger")) {
										toExecute = "Expression frown\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
							
									if(toDo.equalsIgnoreCase("surprise")) {
										for(int i = 0; i < 2; i++) {
											toExecute = "keyframe set 322:300|301:300|300:300\n";
											sockOut.write((toExecute + "\n").getBytes());
										}
									}
									if(toDo.equalsIgnoreCase("test1")) {
										for(int i = 0; i < 2; i++) {
											toExecute = "keyframe set 300:300|322:200|301:300|311:100|320:300\n";
											sockOut.write((toExecute + "\n").getBytes());
										}
									}
									if(toDo.equalsIgnoreCase("test2")) {
										for(int i = 0; i < 2; i++) {
											toExecute = "keyframe set 322:100|301:100|300:100\n";
											sockOut.write((toExecute + "\n").getBytes());
										}
									}
									if(toDo.equalsIgnoreCase("test3")) {
										for(int i = 0; i < 2; i++) {
											toExecute = "keyframe set 322:200|301:200|300:175\n";
											sockOut.write((toExecute + "\n").getBytes());
										}
									}
							
									if(toDo.equalsIgnoreCase("anticipation")) {
										//nothing happens
									}
									
									//Three Little Pigs Commands - WITH FACIAL EXPRESSIONS
									
									if(toDo.equalsIgnoreCase("PigHappy")) {
										toExecute = "keyframe set 300:300|322:200|301:300|311:200|320:300\n";
										sockOut.write((toExecute + "\n").getBytes());
										
										TimeUnit.MILLISECONDS.sleep(500);
										toExecute = "speak That's good!\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
							
									if(toDo.equalsIgnoreCase("PigSad")) {
										toExecute = "expression frown\n";
										sockOut.write((toExecute + "\n").getBytes());
										for(int i = 0; i < 4 ; i++) {
											toExecute = "keyframe set 300:200\n";
											sockOut.write((toExecute + "\n").getBytes());
										}
										TimeUnit.MILLISECONDS.sleep(500);
										toExecute = "speak where will the pigs live now.\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									if(toDo.equalsIgnoreCase("PigFear")) {
										for(int i = 0; i < 2; i++) {
											toExecute = "keyframe set 320:0|322:125|300:200|302:200|311:100\n";
											sockOut.write((toExecute + "\n").getBytes());
										}
										TimeUnit.MILLISECONDS.sleep(500);
										toExecute = "speak Oh no!\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									if(toDo.equalsIgnoreCase("PigDisgust")) {
										toExecute = "keyframe set 320:0|322:125|300:200|301:50|311:100\n";
										sockOut.write((toExecute + "\n").getBytes());
										TimeUnit.MILLISECONDS.sleep(500);
										toExecute = "speak He can't want to eat them!\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									if(toDo.equalsIgnoreCase("PigAnger")) {
										toExecute = "Expression frown\n";
										sockOut.write((toExecute + "\n").getBytes());
										TimeUnit.MILLISECONDS.sleep(500);
										toExecute = "speak They shouldn't tease him like that!\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									if(toDo.equalsIgnoreCase("PigSurprise")) {
										for(int i = 0; i < 2; i++) {
											toExecute = "keyframe set 322:200|301:200|300:175\n";
											sockOut.write((toExecute + "\n").getBytes());
										}
										TimeUnit.MILLISECONDS.sleep(500);
										toExecute = "speak whoa, that's fast!\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									if(toDo.equalsIgnoreCase("PigAnticipation")) {
										toExecute = "speak I wonder what's going to happen!\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									//Three Little Pigs Commands - VOICE ONLY
									
									if(toDo.equalsIgnoreCase("PigHappyVO")) {
										toExecute = "speak That's good!\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
							
									if(toDo.equalsIgnoreCase("PigSadVO")) {
										toExecute = "speak where will the pigs live now.\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									if(toDo.equalsIgnoreCase("PigFearVO")) {
										toExecute = "speak Oh no!\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									if(toDo.equalsIgnoreCase("PigDisgustVO")) {
										toExecute = "speak He can't want to eat them!\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									if(toDo.equalsIgnoreCase("PigAngerVO")) {
										toExecute = "speak They shouldn't tease him like that!\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									if(toDo.equalsIgnoreCase("PigSurpriseVO")) {
										toExecute = "speak whoa, that's fast!\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									if(toDo.equalsIgnoreCase("PigAnticipationVO")) {
										toExecute = "speak I wonder what's going to happen!\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									//Boy Who Cried Wolf Commands - WITH FACIAL EXPRESSIONS
									
									if(toDo.equalsIgnoreCase("WolfHappy")) {
										toExecute = "keyframe set 320:200|322:150|300:125|311:100|301:150\n";
										sockOut.write((toExecute + "\n").getBytes());
										
										TimeUnit.MILLISECONDS.sleep(500);
										toExecute = "speak That's sounds nice!\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									if(toDo.equalsIgnoreCase("WolfSad")) {
										toExecute = "expression frown\n";
										sockOut.write((toExecute + "\n").getBytes());
										for(int i = 0; i < 4 ; i++) {
											toExecute = "keyframe set 300:200\n";
											sockOut.write((toExecute + "\n").getBytes());
										}
										TimeUnit.MILLISECONDS.sleep(500);
										toExecute = "speak All his sheep are gone...\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									if(toDo.equalsIgnoreCase("WolfFear")) {
										for(int i = 0; i < 2; i++) {
											toExecute = "keyframe set 320:0|322:125|300:200|302:200|311:100\n";
											sockOut.write((toExecute + "\n").getBytes());
										}
										TimeUnit.MILLISECONDS.sleep(500);
										toExecute = "speak He's going to eat the sheep. Oh no!\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									if(toDo.equalsIgnoreCase("WolfDisgust")) {
										toExecute = "keyframe set 320:0|322:125|300:200|301:50|311:100\n";
										sockOut.write((toExecute + "\n").getBytes());
										TimeUnit.MILLISECONDS.sleep(500);
										toExecute = "speak Gross!\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									if(toDo.equalsIgnoreCase("WolfAnger")) {
										toExecute = "Expression frown\n";
										sockOut.write((toExecute + "\n").getBytes());
										TimeUnit.MILLISECONDS.sleep(500);
										toExecute = "speak That's not nice!\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									if(toDo.equalsIgnoreCase("WolfSurprise")) {
										for(int i = 0; i < 2; i++) {
											toExecute = "keyframe set 322:200|301:200|300:175\n";
											sockOut.write((toExecute + "\n").getBytes());
										}
										TimeUnit.MILLISECONDS.sleep(500);
										toExecute = "speak Why didn't they help.\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									if(toDo.equalsIgnoreCase("WolfAnticipation")) {
										toExecute = "speak This should be good.\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									//Boy Who Cried Wolf Commands - VOICE ONLY
									
									if(toDo.equalsIgnoreCase("WolfHappyVO")) {
										toExecute = "speak That's sounds nice!\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									if(toDo.equalsIgnoreCase("WolfSadVO")) {
										toExecute = "speak All his sheep are gone...\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									if(toDo.equalsIgnoreCase("WolfFearVO")) {
										toExecute = "speak He's going to eat the sheep. Oh no!\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									if(toDo.equalsIgnoreCase("WolfDisgustVO")) {
										toExecute = "speak Gross!\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									if(toDo.equalsIgnoreCase("WolfAngerVO")) {
										toExecute = "speak That's not nice!\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									if(toDo.equalsIgnoreCase("WolfSurpriseVO")) {
										toExecute = "speak Why didn't they help.\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									if(toDo.equalsIgnoreCase("WolfAnticipationVO")) {
										toExecute = "speak This should be good.\n";
										sockOut.write((toExecute + "\n").getBytes());
									}
									
									System.out.println("Input anything to continue");
									temp = sysin.readLine(); //Indefinite Delay, Input when ready to move on.
									toExecute = "expression default\n";
									sockOut.write((toExecute + "\n").getBytes());
									for(int j = 0; j < 3; j++) {
										toExecute = "keyframe set 300:100|301:100|311:100|320:100|322:100\n" ;
										sockOut.write((toExecute + "\n").getBytes());
									}
							
									System.out.println("Command " + toDo + " Complete!");
									commQueue.remove();
									System.out.println("Temp Input: " + temp);
								} catch(Exception e) {
									e.printStackTrace();
								}
							}
						}
						System.out.println("Link end");
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
			};
			userIn.start();
			while(running) {
				try {
					//This section exists so that the socket is not closed
					//while the threads run
				}catch(Exception ex) {
					ex.getStackTrace();
				}
			}
			
			sock.close();
			}catch(Exception ex) {
				ex.getStackTrace();
			}
		
	}
}
