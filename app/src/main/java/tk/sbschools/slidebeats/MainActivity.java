package tk.sbschools.slidebeats;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SensorEventListener{
    //Code from this program has been used from "Beginning Android Games" by Mario Zechner
    //Review SurfaceView, Canvas, continue

    GameSurface gameSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        gameSurface = new GameSurface(this);
        setContentView(gameSurface);

        SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        Sensor accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelSensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause(){
        super.onPause();
        gameSurface.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        gameSurface.resume();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //x.setText("x: " + event.values[0]);
        //y.setText("y: " + event.values[1]);
        //z.setText("z: " + event.values[2]);
        gameSurface.deltaV = -event.values[0]*4;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    //----------------------------GameSurface Method--------------------------
    public class GameSurface extends SurfaceView implements Runnable {

        Thread gameThread;
        SurfaceHolder holder;
        volatile boolean running = false;
        Bitmap playerSlide,background,gameOver;
        Paint paintProperty,textPaint, scorePaint;
        MediaPlayer player,soundplayer;

        int screenWidth;
        int screenHeight;
        int value = 5;
        float deltaV = 0;

        private long mLastTime = 0;
        private int fps = 0, ifps = 0;

        int score = 0;
        boolean boost = false;
        long boostStart = 0;
        int multiplier = 3;
        int encouragementcountdown = 25;

        private long noteLastTime = 0;

        ArrayList<Note> noteList = new ArrayList<Note>();

        public GameSurface(Context context) {
            super(context);

            holder=getHolder();

            playerSlide = BitmapFactory.decodeResource(getResources(),R.drawable.slider);
            background = BitmapFactory.decodeResource(getResources(),R.drawable.background);
            gameOver = BitmapFactory.decodeResource(getResources(),R.drawable.gameover);

            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);
            screenWidth=sizeOfScreen.x;
            screenHeight=sizeOfScreen.y;

            background = Bitmap.createScaledBitmap(background, screenWidth, screenHeight, false);
            gameOver = Bitmap.createScaledBitmap(gameOver,screenWidth-50,gameOver.getHeight() * (gameOver.getWidth()/screenWidth),false);

            paintProperty= new Paint();
            paintProperty.setColor(Color.WHITE);
            textPaint = new Paint();
            textPaint.setColor(Color.BLUE);
            textPaint.setTextSize(32);
            scorePaint = new Paint();
            scorePaint.setColor(Color.RED);
            scorePaint.setTextSize(64);

            player = MediaPlayer.create(MainActivity.this,R.raw.this_game);
            player.start();

        }

        @Override
        public void run() {
            while (running == true){
                long now = System.currentTimeMillis();
                if (holder.getSurface().isValid() == false)
                    continue;
                Canvas canvas= holder.lockCanvas();
                //canvas.drawRGB(255,0,0);
                canvas.drawBitmap(background,0,0,null);
                canvas.drawText("FPS: " + fps,50,100,textPaint);
                canvas.drawText("Score: " + score,screenWidth-500,100,scorePaint);
                if(boost)
                    canvas.drawText("Multiplier: " + multiplier/1.5f,screenWidth-500,164,scorePaint);
                else
                    canvas.drawText("Multiplier: " + multiplier/3f,screenWidth-500,164,scorePaint);
                value += deltaV;
                if(value > screenWidth-325){
                    value = screenWidth-325;
                }
                if(value < 0){
                    value = 0;
                }
                canvas.drawBitmap( playerSlide,value,screenHeight-400,null);
                //System.out.println((double)player.getCurrentPosition()/player.getDuration()*1f);
                canvas.drawRect(0,0,(((float)player.getCurrentPosition())/player.getDuration())*screenWidth,30,paintProperty);

                canvas.drawLine((screenWidth/2)-128,550,256,screenHeight-100,paintProperty);
                canvas.drawLine((screenWidth/2),550,(screenWidth/2),screenHeight-100,paintProperty);
                canvas.drawLine((screenWidth/2)+128,550,screenWidth-256,screenHeight-100,paintProperty);

                Iterator<Note> iter = noteList.iterator();

                while (iter.hasNext()) {
                    Note n = iter.next();
                    n.render(canvas);
                    if(boost)
                        n.deltaY = 15;
                    else
                        n.deltaY = 10;
                    if(n.getRect().contains(value+(playerSlide.getWidth()/2),(screenHeight-400+(playerSlide.getHeight())))){
                        n.image = BitmapFactory.decodeResource(getResources(),R.drawable.notepuck_1_0);
                        n.lifetime--;
                    }
                    if(n.lifetime < 10){
                        n.lifetime--;
                        //System.out.println(n.lifetime);
                        scorePaint.setTextSize(75);
                        if(n.lifetime <= 0){
                            multiplier++;
                            encouragementcountdown--;
                            if(boost)
                                score += (10 * (multiplier/3f));
                            score += (10 * (multiplier/3f));
                            iter.remove();
                            scorePaint.setTextSize(64);
                        }
                    }
                    if (n.y>screenHeight-100) {
                        multiplier = 3;
                        try{
                            iter.remove();
                        }catch (IllegalStateException e){e.printStackTrace();}
                    }
                }

                if(now > (boostStart+10000) && boost){
                    boost = false;
                    scorePaint.setColor(Color.RED);
                }

                if(encouragementcountdown <= 0 && multiplier > 10){
                    encouragementcountdown=25;
                    switch((int)(Math.random()*4)+1){
                        case 1: soundplayer = MediaPlayer.create(MainActivity.this,R.raw.nice);
                            break;
                        case 2: soundplayer = MediaPlayer.create(MainActivity.this,R.raw.notbad);
                            break;
                        case 3: soundplayer = MediaPlayer.create(MainActivity.this,R.raw.solid);
                            break;
                        default: soundplayer = MediaPlayer.create(MainActivity.this,R.raw.perfect);
                            break;
                    }
                    //soundplayer = MediaPlayer.create(MainActivity.this,R.raw.nice);
                    soundplayer.start();
                }


                /*for(Note n : noteList){
                    n.render(canvas);
                    if(n.y>screenHeight-100){
                        n.remove(noteList);
                    }
                }*/

                /*if(Math.random() < 0.01){
                    int line = (int)(Math.random()*3)+1;
                    Bitmap image = BitmapFactory.decodeResource(getResources(),R.drawable.notepuck_0_0);
                    noteList.add(new Note(line,image,screenHeight,screenWidth));
                }*/

                if((((float)player.getCurrentPosition())/player.getDuration()) > 1 && noteList.size() == 0){
                    canvas.drawBitmap(gameOver,screenWidth/2-(gameOver.getWidth()/2),screenHeight/2-(gameOver.getHeight()/2),null);
                }

                holder.unlockCanvasAndPost(canvas);

                if(now > (noteLastTime + 816) && player.isPlaying() && !boost){
                    noteLastTime = now;
                    int line = (int)(Math.random()*3)+1;
                    Bitmap image = BitmapFactory.decodeResource(getResources(),R.drawable.notepuck_0_0);
                    noteList.add(new Note(line,image,screenHeight,screenWidth));
                }else  if(now > (noteLastTime + 408) && player.isPlaying() && boost){
                    noteLastTime = now;
                    int line = (int)(Math.random()*3)+1;
                    Bitmap image = BitmapFactory.decodeResource(getResources(),R.drawable.notepuck_0_0);
                    noteList.add(new Note(line,image,screenHeight,screenWidth));
                }

                ifps++;
                if(now > (mLastTime + 1000)) {
                    mLastTime = now;
                    fps = ifps;
                    ifps = 0;
                }
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            boost = true;
            long now = System.currentTimeMillis();
            boostStart = now;
            scorePaint.setColor(Color.MAGENTA);
            return super.onTouchEvent(event);
        }

        public void resume(){
            running=true;
            gameThread=new Thread(this);
            gameThread.start();
        }

        public void pause() {
            running = false;
            while (true) {
                try {
                    gameThread.join();
                } catch (InterruptedException e) {
                }
            }
        }

    }//End GameSurface
}//End Activity