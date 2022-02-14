package javacv3;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.List;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import java.lang.Math;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;



/**The javacvv class is incharge of retrieving the frame from the webcam and determining which heading 
 * the robot should move based off the position of the persons fist within the frame. Four boxes are arranged
 * in a cross formation, one for each heading. The haarcascade classifier for a fist is used to determine 
 * whether there are fists in the frame. The classfier has already been trained on a wide range of images of fists, 
 * however there are still instances when it can go wrong. If there are instances of a fist in the frame, then 
 * each of these are iterated through. The coordinates of each one are checked to see whether they are in one of
 * the heading boxes or not. This is done by checking the midpoint of the fists coordinates. If the fist is determined to 
 * be inside one of the heading boxes, the corresponding heading is saved in a text file. This is then opened by 
 * the controller and the heading is read. It is then deleted again so that javacvv knows to re-create a file
 * with a new heading. 
 */
public class Javacvv{
 
	static JFrame frame;
	static JLabel label;
	static ImageIcon icon;
    static PrintWriter writer;  
 
	public static void main(String[] args) throws FileNotFoundException{
            
        System.load("C:/opencv/build/java/x64/opencv_java453.dll");

        //Loads the haarcascade classifier
        CascadeClassifier cascadeHandClassifier = new CascadeClassifier(
		"C:/Users/Ktm Sam/Documents/NetBeansProjects/opencv-object-detection-master/data/haarcascade files/fist.xml");
		
		//Defines and opens the webcam feed
		VideoCapture videoDevice = new VideoCapture(1);
		videoDevice.open(1);
        Mat frameCapture = new Mat(); 
		videoDevice.read(frameCapture); //Retrieves the frame data as a matrix 

		//Determines the margin and size of the video source
        int width = frameCapture.width()-200;
        int height = frameCapture.height()-100;
        int margin = 200;
        int size = 50; //Size of each target square

        //Defines each point to create each of the 4 target squares
        //Coordinates of the target square for the west box
        int W1 = Math.round(width/4)-size+margin;
        int W2 = Math.round(height/2)-size;
        int W3 = Math.round(width/4)+size+margin;
        int W4 = Math.round(height/2)+size;

        //Coordinates of the target square for the north box
        int N1 = Math.round(width/2)-size+margin;
        int N2 = Math.round(height/4)-size;
        int N3 = Math.round(width/2)+size+margin;
        int N4 =  Math.round(height/4)+size;

        //Coordinates of the target square for the south box
        int S1 = Math.round(width/2)-size+margin;
        int S2 = Math.round(height/4*3)-size;
        int S3 = Math.round(width/2)+size+margin;
        int S4 = Math.round(height/4*3)+size;

        //Coordinates of the target square for the east box
        int E1 = Math.round(width/4*3)-size+margin;
        int E2 = Math.round(height/2)-size;
        int E3 = Math.round(width/4*3)+size+margin;
        int E4 = Math.round(height/2)+size;
                                        
		if (videoDevice.isOpened()) {
			while (true) {		
				frameCapture = new Mat();
				videoDevice.read(frameCapture); //Retrieves new frame from the webcam
                Core.flip(frameCapture,frameCapture,1); //Reflects the image along a mirror line
				
				MatOfRect fists = new MatOfRect();
				cascadeHandClassifier.detectMultiScale(frameCapture, fists); //Detects all instances of fists in the frame
                    
                //Plots each of the bounding rectangles for the heading boxes
                Imgproc.rectangle(frameCapture, new Point(E1,E2), new Point(E3,E4), new Scalar(0, 100, 0),3);
                Imgproc.rectangle(frameCapture, new Point(N1, N2), new Point(N3, N4), new Scalar(0, 100, 0),3);
                Imgproc.rectangle(frameCapture, new Point(W1, W2), new Point(W3, W4), new Scalar(0, 100, 0),3);
                Imgproc.rectangle(frameCapture, new Point(S1, S2), new Point(S3, S4),new Scalar(0, 100, 0),3);                          
                
                String heading="";
                //Iterates through each 'fist' detected
                for (Rect rect : fists.toArray()) {
                	//Calculates the midpoint of each fists detected
                    int mp_x = Math.round((rect.x+rect.x+rect.width)/2);
                    int mp_y = Math.round((rect.y+rect.y+rect.height)/2);

                    //If inside the west box
                    if ((mp_x>W1)&&(mp_y>W2)&&(mp_x<W3)&&(mp_y<W4)){;
                        heading = "west";
                    //If fist inside the east box
                    }else if ((mp_x>E1)&&(mp_y>E2)&&(mp_x<E3)&&(mp_y<E4)){
                        heading = "east";
                    //If first is inside the south box
                    }else if ((mp_x>S1)&&(mp_y>S2)&&(mp_x<S3)&&(mp_y<S4)){
                        heading = "south";
                    //If fist is inside the north box
                    }else if ((mp_x>N1)&&(mp_y>N2)&&(mp_x<N3)&&(mp_y<N4)){
                        heading = "north";
                    }
                	//Plots text near each instance of a fist on the frame
                    Imgproc.putText(frameCapture, "Fist", new Point(rect.x,rect.y-5), 1, 2, new Scalar(0,0,255));
                    //Plots a rectangle around each instance of a fist						
                    Imgproc.rectangle(frameCapture, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 100, 0),3);
                }
                
                //If the fist is not inside any heading box
                if (heading.equals("")){ 
                }else{
                	//If there is a valid heading retrieved from the video source
                    Path p = Paths.get("giveheading.txt"); //Creates a new text file 
                    boolean notexists = Files.notExists(p); //Checks whether the file was not created properly
                    if (notexists){
                        try {
                            writeHeading(heading); //Writes heading to the text file
                        } catch (UnsupportedEncodingException ex) {
                            Logger.getLogger(Javacvv.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                 PushImage(ConvertMat2Image(frameCapture));      
		}}else{
			System.out.println("Video source not connected"); //If the video source does not exist
			return;
		}
	}

    /**Writes the heading retrieved from the gesture control into a text file*/    
    public static void writeHeading(String heading) throws FileNotFoundException, UnsupportedEncodingException{
        writer = new PrintWriter("giveheading.txt", "UTF-8"); //Creates a text file to store the heading
        writer.println(heading); //Insteads the heading into the text file
        writer.close(); //Closes the text file
    
    }

    /**Converts the matrix image to an Image object*/
	private static BufferedImage ConvertMat2Image(Mat cameraImage) {
		MatOfByte byteMatImage = new MatOfByte();
		Imgcodecs.imencode(".jpg", cameraImage, byteMatImage); 
		byte[] byteArray = byteMatImage.toArray();
		BufferedImage bufferedImage = null;
		try {
			//Converts the matrix image to a BufferedImage type
			InputStream in = new ByteArrayInputStream(byteArray);
			bufferedImage = ImageIO.read(in);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return bufferedImage;
	}
  	
    /**Generates the gui to display the video frame
     */
	public static void guiFrame() {
		//Creates frame to display the video feed
		frame = new JFrame();
		frame.setLayout(new FlowLayout());
		frame.setSize(700, 600);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
        
    /**Loads the image onto the frame*/
	public static void PushImage(Image img2) {
		//Updates the image on the frame with the next frame from the webcam
		if (frame == null)
			guiFrame();
		if (label != null)
			frame.remove(label);
		icon = new ImageIcon(img2);
		label = new JLabel();
		label.setIcon(icon); //Inserts new frame into the gui
		frame.add(label);
		frame.revalidate();
	}
}