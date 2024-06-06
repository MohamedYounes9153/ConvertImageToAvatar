import javax.imageio.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.lang.Boolean;

public class ImageToAvatar {
    //---------------------Constructor--------------------------------
    public ImageToAvatar() {}

    //-----------------Methods----------------------------------------
    public BufferedImage fileToBufferedImage(File filename) throws IOException {
        try{
            return ImageIO.read(filename);
        }catch(IOException e){
            System.out.println("IOException: " + e.toString());
            return null;
        }
    }

    public Boolean checkFormatAvatar(File filename, int circleRadius) throws IOException {
        BufferedImage image = this.fileToBufferedImage(filename);
        if(!(image.getHeight() == 512 && image.getWidth() == 512)){ //If the image is not of size 512x512
            System.out.println("Wrong image size");
            return false;
        }
        else{
            int pixel;
            int pixelsInCircle = 0;
            int averageRed = 0;
            int averageGreen = 0;
            int averageBlue = 0;
            for(int i=0; i<512; i++){
                for(int j=0; j<512; j++){
                    pixel = image.getRGB(i,j);
                    int alpha = (pixel >> 24) & 0xff;
                    if (alpha != (255 & 0xff) && Math.sqrt(Math.pow(i-255,2)+Math.pow(j-255,2)) <= circleRadius){ //If the pixel is within the circle (Norm of the vector from (0,0) to (i,j) <= circle radius) and is transparent
                        System.out.println("Transparent pixel within the circle");
                        return false;
                    }
                    else if (Math.sqrt(Math.pow(i-255,2)+Math.pow(j-255,2)) <= circleRadius){ // If pixel is within the circle
                        int red = (pixel >> 16) & 0xff;
                        int green = (pixel >> 8) & 0xff;
                        int blue = pixel & 0xff;
                        pixelsInCircle += 1;
                        averageRed += red;
                        averageGreen += green;
                        averageBlue += alpha;
                    }
                }
            }
            averageRed /= pixelsInCircle;
            averageGreen /= pixelsInCircle;
            averageBlue /= pixelsInCircle;
            if(0.299*averageRed+0.587*averageGreen+0.114*averageBlue <= 127.5){ // If the average brightness of the Avatar is considered dark (we will consider it as the avatar as not giving a "happy vibe")
                System.out.println("Avatar is too dark");
                return false;
            }
            return true;
        }
    }

    public File avatarInCircle(File filename, int circleRadius) throws IOException{
        if(checkFormatAvatar(filename, circleRadius)){
            File Output = new File("avatarincircle.png");
            int argb;
            int pixel;
            BufferedImage avatarincircle = new BufferedImage(512,512,BufferedImage.TYPE_INT_ARGB); // Create a new image
            BufferedImage avatar = fileToBufferedImage(filename);
            for(int i=0;i<512;i++){
                for(int j=0;j<512;j++){
                    pixel = avatar.getRGB(i,j);
                    int red = (pixel >> 16) & 0xff;
                    int green = (pixel >> 8) & 0xff;
                    int blue = pixel & 0xff;
                    int alpha = 255 & 0xff;
                    if (Math.sqrt(Math.pow(i-255,2)+Math.pow(j-255,2)) <= circleRadius){ // If pixel is within the circle

                        argb = (alpha << 24) | (red << 16) | (green << 8) | blue; // Copies the pixels argb data (alpha is 0 because the image is in the right format (non-transparent pixel))
                        avatarincircle.setRGB(i,j, argb);
                    }
                    else{
                        argb = ((0 & 0xff)<< 24) | (0 << 16) | (0 << 8) | 0; // fully transparent pixel
                        avatarincircle.setRGB(i,j, argb);
                    }
                }
            }
            try{
                ImageIO.write(avatarincircle, "png", Output);
            } catch (IOException e){
                System.out.println("Error while writing image data in file.");
            }
            return Output;
        }
        else{
            System.out.println("file is in wrong format");
            return null;
        }
    }

    public static void main(String args[]) throws IOException{
        ImageToAvatar incircle = new ImageToAvatar();
        File file = new File(args[0]);
        incircle.avatarInCircle(file, 256);
    }
}
