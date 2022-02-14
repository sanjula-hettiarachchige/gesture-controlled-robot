import uk.ac.warwick.dcs.maze.logic.IRobot;
import uk.ac.warwick.dcs.maze.logic.Maze;
import uk.ac.warwick.dcs.maze.logic.*;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.awt.Point;
import java.io.IOException;
import java.util.*;
import java.nio.file.*;
import java.io.File;



/** Gesture controlled robot
 * This class file is what is compiled and loaded into the maze environment. If the maze generated is a blank maze
 * then it will gamify the process. Walls will randomly appear in the top row and drop down (like tetris) and the
 * user will need to navigate the obstacle course using the hand controls. If the maze generated is not a blank maze, 
 * then the robot will simply be controlled by the position of the fist in the frame. 
 * The headings will be stored in a text file called giveheading.txt which is created by the Javacvv class. If there
 * is no file present, then it means that there is no valid heading to move, so a while loop will run within the controlrRobot
 * method to stop the robot from moving. 
 */
public class Roy{
    int pollRun = 0;
    ArrayList<ArrayList<Integer>> wallList = new ArrayList<ArrayList<Integer>>();
    int game = 1; //Sets game mode if the robot is run on a blank maze
    int pollrun = 0;


    public void controlRobot(IRobot robot){
        Path p = Paths.get("giveheading.txt"); //Defines path to the text file containing the heading
        boolean exists = Files.exists(p); //Checks whether the text fie#le exists
        boolean notexists = Files.notExists(p); //Checks whether the text file does not exist
        //As long as the file does not exist, it will be stuck in a loop so that the robot is stationary
        while (notexists){
            notexists = Files.notExists(p); //Checks whether the file is present again
        }
        wait(300); //Pauses the movement of the robot so that the latency of the program and the video source has time to catch up
        //Checks whether the robot is on a blank maze or not on the first move
        if (pollRun==0){
            game=1;
            //Checks if there is a wall around the robot, indicating whether the robot is in a blank maze or not
            for (int i=0;i<4;i++){
                if (robot.look(IRobot.AHEAD+i)==IRobot.WALL){
                    game = 0;//If not in a blank maze, set game variable to 0 so robot just follows hand commands on maze 
                }
            }

        }
        pollRun++;
        //If on a blank maze, then the maze area will be gamified.
        if (game==1){
            gamify(robot); //Gamifies the maze 
        }

        String heading="";
        //Attempts to read the heading from the text file
        try{
            for (String line : Files.readAllLines(Paths.get("giveheading.txt"))) {
                for (String part : line.split("\\s+")) {
                    heading = part; //Sets the first word in the text file to the heading
                    break;
            }
        }
        }catch(Exception e) {
            System.out.println("fail");
        }

        //If the text file existed
        if (exists){
            File file = new File("giveheading.txt");
            file.delete(); //Deletes the text file
        }

        //Translates the heading read from the text file into a robot heading
        if (heading.equals("north")){
            robot.setHeading(IRobot.NORTH);
        }else if (heading.equals("east")){
            robot.setHeading(IRobot.EAST);
        }else if (heading.equals("west")){
            robot.setHeading(IRobot.WEST);
        }else if (heading.equals("south")){
            robot.setHeading(IRobot.SOUTH);
        }

    }

    public void gamify(IRobot robot){
        Class<?> cls = robot.getClass(); //Stores the robot object
        try{
            Method meth = cls.getDeclaredMethod("getMaze");
            Maze maze = (Maze) meth.invoke(robot); //Retrieves the maze object 
            Random ran = new Random();
            int width = maze.getWidth(); //Gets the dimensions of the maze
            int height = maze.getHeight();
            Field targetField = maze.getClass().getDeclaredField("finish");
            targetField.setAccessible(true);
            targetField.set(maze, new Point(width-2, 1)); //Sets the target location
            maze.setFinish(width-2, 1);
            int a = ran.nextInt(4);
            //Randomly chance of a 1/2 that a blank line of walls is inserted 
            if (a!=2){
                nextWallRow(maze, 1); //Generate a blank row of squares at the top
            }else{
                nextWallRow(maze, 0); //Generate a row of random squares which are walls
            }
            generateWalls(maze);
            //Creates an event which is the same as collisions to get the maze to update the positions of the squares
            Event e = new Event(102, null); 
            EventBus.broadcast(e);
 
        }catch (Exception e){
            e.printStackTrace();
        }
        
    }

    
    
    public void nextWallRow(Maze maze, int blank){
        ArrayList<Integer> wallRow = new ArrayList<Integer>();
        int width = maze.getWidth();
        Random ran = new Random();
        int a = ran.nextInt(Math.round(width/3))+Math.round(width/3); //A random amount of walls to generate
        if (blank==1){
            for (int i=0; i<=a; i++){
                wallRow.add(0); //x coordinate of the wall
                wallRow.add(1); //y coordinate of the wall
            }
        }else{
            for (int i=0; i<=a; i++){
                //Randomly places wall coordinates on the top row of the maze
                int x = ran.nextInt(width-2)+1;
                wallRow.add(x+1); //x coordinate of the wall
                wallRow.add(1); //y coorindate of the wall
            }
        }
        wallList.add(0,wallRow);
    }

    public void generateWalls(Maze maze){
        clearMaze(maze); //Clears the maze so that the new walls can be displayed
        for(int i=0; i<=wallList.size()-1;i++){
            ArrayList<Integer> currentRow = (ArrayList) wallList.get(i); //Retrieves the current row
            for(int a=0; a<=currentRow.size()/2-1; a+=2){
                //Iterates through each coordinate pair of the array
                maze.setCellType(currentRow.get(a),currentRow.get(a+1),2);
                (wallList.get(i)).set(a+1, i+1);
            }
        }
        //Any rows which overflow to the bottom of the maze should be removed
        if (wallList.size()==maze.getHeight()-2){
            //Removes the bottom row from the arraylist
            wallList.remove(wallList.size()-1);
        }


    }

    public void clearMaze(Maze maze){
        int width = maze.getWidth();
        int height = maze.getHeight();
        //Iterates through every coordinate and sets it to a nonwall value 
        for(int a=1; a<=width-2; a++){
            for(int b=1; b<=height-2; b++){
                maze.setCellType(a,b,1);
            }
        }

    }

    public void reset(){
        //Resets the wallList and pollRun
        wallList.clear();
        pollRun = 0;
    }

    public static void wait(int ms){
        //A delay method so that the latency of the video feed does not impact the feedback the user receives when controlling the robot
        try{
            Thread.sleep(ms);
        }catch(InterruptedException ex){
            Thread.currentThread().interrupt();
        }
    }
}

