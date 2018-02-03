package com.example.pjajoo.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private List<String> celebUrls = new ArrayList<>();
    private List<String> celebNames = new ArrayList<>();
    private int celebChosen;
    private int randomCorrectChoice;
    private List<String> answers = new ArrayList<>(4);
    private MediaPlayer yes;
    private MediaPlayer no;

    public void celebChosen(final View view) throws IOException {

        final Button button = (Button) view;

        int choice = Integer.parseInt(button.getTag().toString());

        if (choice == this.randomCorrectChoice) {
            if (no.isPlaying()) no.pause();
            yes.seekTo(0);
            yes.start();
            Log.i("Correct Answer Chosen", this.answers.get(choice));
            setImage();
        } else {
            if (yes.isPlaying()) yes.pause();
            no.seekTo(0);
            no.start();
            Log.i("Incorrect Answer Chosen", this.answers.get(choice));
        }
    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                final URL url = new URL(urls[0]);
                final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                final InputStream inputStream = urlConnection.getInputStream();
                final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            } catch (final Exception e) {
                e.printStackTrace();
                Log.e("Exception", "while downloading the image");
            }
            return null;
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            try {
                final URL url = new URL(urls[0]);
                final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                final InputStream in = urlConnection.getInputStream();
                final InputStreamReader inputStreamReader = new InputStreamReader(in);
                int data = inputStreamReader.read();
                String result = "";
                while (data != -1) {
                    char c = (char) data;
                    result += c;
                    data = inputStreamReader.read();
                }
                return result;
            } catch (final Exception e) {
                e.printStackTrace();
                Log.e("Exception", "while downloading the page source");
            }
            return null;
        }
    }

    private void setImage() {
        try {
            final Random random = new Random();
            this.celebChosen = random.nextInt(this.celebUrls.size());

            final ImageDownloader imageDownloader = new ImageDownloader();

            final Bitmap celebImage = imageDownloader.execute(celebUrls.get(celebChosen)).get();
            final ImageView imageView = findViewById(R.id.imageView);
            imageView.setImageBitmap(celebImage);

            this.randomCorrectChoice = random.nextInt(4);
            answers.clear();

            for (int i = 0; i < 4; i++) {
                if (i == this.randomCorrectChoice) {
                    answers.add(celebNames.get(celebChosen));
                } else {
                    int randomCelebChoice = random.nextInt(celebNames.size());
                    while (randomCelebChoice == this.randomCorrectChoice) {
                        randomCelebChoice = random.nextInt(celebNames.size());
                    }
                    answers.add(celebNames.get(randomCelebChoice));
                }
            }

            final Button choice0 = findViewById(R.id.choice0);
            final Button choice1 = findViewById(R.id.choice1);
            final Button choice2 = findViewById(R.id.choice2);
            final Button choice3 = findViewById(R.id.choice3);

            choice0.setText(answers.get(0));
            choice1.setText(answers.get(1));
            choice2.setText(answers.get(2));
            choice3.setText(answers.get(3));

        } catch (final Exception e) {
            e.printStackTrace();
            Log.e("Exception", "while setting the image");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final DownloadTask downloadTask = new DownloadTask();
        String result;

        try {
            result = downloadTask.execute("http://www.posh24.se/kandisar").get();
            Log.i("Contents of the URL", result);

            final String[] splitResult = result.split("<div class=\"sidebarContainer\">");

            Pattern p = Pattern.compile("<img src=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[0]);

            while (m.find()) {
                final String celebUrl = m.group(1);
                Log.i("Celeb URL", celebUrl);
                this.celebUrls.add(celebUrl);
            }


            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitResult[0]);

            while (m.find()) {
                final String celebName = m.group(1);
                Log.i("Celeb Name", celebName);
                this.celebNames.add(celebName);
            }

            yes = MediaPlayer.create(getApplicationContext(), R.raw.yes);
            no = MediaPlayer.create(getApplicationContext(), R.raw.no);

            setImage();

        } catch (final Exception e) {
            e.printStackTrace();
            Log.e("Exception", "while downloading the image");
        }
    }
}
