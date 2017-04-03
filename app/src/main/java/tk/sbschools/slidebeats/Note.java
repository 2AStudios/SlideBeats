package tk.sbschools.slidebeats;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by 10018728 on 3/24/2017.
 */

public class Note {
    float x;
    float y;
    float deltaY;
    int line;
    Bitmap image;
    Rect rect;
    int lifetime = 10;

    int lineStartX = 0;
    int lineStartY = 0;
    int lineEndX = 0;
    int lineEndY = 0;
    public Note(int line, Bitmap image, int screenHeight, int screenWidth){
        this.line = line;
        switch (line){
            case 1:{
                lineStartX = (screenWidth/2)-128;
                lineStartY = 550;
                lineEndX = 256;
                lineEndY = screenHeight-100;
            }break;
            case 2:{
                lineStartX = (screenWidth/2);
                lineStartY = 550;
                lineEndX = (screenWidth/2);
                lineEndY = screenHeight-100;
            }break;
            case 3:{
                lineStartX = (screenWidth/2)+128;
                lineStartY = 550;
                lineEndX = screenWidth-256;
                lineEndY = screenHeight-100;
            }break;
            default:{
                Log.d("Testing","Could not switch to line");
            }
        }

        this.image = image;
        deltaY = 10;
        y = 550;
    }

    public Rect getRect(){
        return rect;
    }

    public void render(Canvas canvas){
        //System.out.println(lineStartX + " | "+ lineEndX + " | " + x);
        //x = lineStartX + ((lineEndX - lineStartX)/(lineEndY - lineStartY))*(y - lineStartY);
        x = lineStartX + (lineEndX - lineStartX)*((lineStartY-y)/(lineStartY - lineEndY));
        float imageWidth = (image.getWidth()/2)+((image.getWidth()/2)*((lineStartY-y)/(lineStartY - lineEndY)));
        float imageHeight = (image.getHeight()/2)+((image.getHeight()/2)*((lineStartY-y)/(lineStartY - lineEndY)));
        Bitmap scaledImage = Bitmap.createScaledBitmap(image, (int)imageWidth, (int)imageHeight, false);
        canvas.drawBitmap(scaledImage,x-(scaledImage.getWidth()/2), y-(scaledImage.getHeight()/2),null);
        rect = new Rect((int)(x-(scaledImage.getWidth()/2)),(int)(y-(scaledImage.getHeight()/2)),(int)(x+(scaledImage.getWidth()/2)), (int)(y+(scaledImage.getHeight()/2)));
        //canvas.drawBitmap(image, null, new RectF(x-(image.getWidth()/2), y-(image.getHeight()/2), imageWidth, imageHeight), null);
        y += deltaY;

    }

    public void remove(ArrayList<Note> list){list.remove(this);}

}
