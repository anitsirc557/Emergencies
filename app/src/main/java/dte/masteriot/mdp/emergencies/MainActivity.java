package dte.masteriot.mdp.emergencies;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import dte.masteriot.mdp.emergencies.MapsActivity;
import dte.masteriot.mdp.emergencies.R;

public class MainActivity extends AppCompatActivity implements ListView.OnItemClickListener{
    private ListView lvCameras;
    private ArrayAdapter adapter;
    private TextView selectionURL;
    private TextView selectionCoordinates;
    private TextView modoURL;
    private ImageView ivCamera;
    private ArrayList<String> cameraListName = new ArrayList<>();
    private ArrayList<String> cameraListURLS = new ArrayList<>();
    private ArrayList<String> cameraListCoordinates = new ArrayList<>();
    private ArrayList<String> initalData = new ArrayList<>();
    private static final String URL_CAMERAS = "http://informo.madrid.es/informo/tmadrid/CCTV.kml";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //TextView URL
        //selectionURL = (TextView) findViewById(R.id.selectionURL);
        selectionCoordinates = (TextView)findViewById(R.id.selectionCoordinates);

        //modoURL = (TextView)findViewById(R.id.modoURL);
        ivCamera = (ImageView)findViewById(R.id.ivCamera);
        lvCameras = (ListView)findViewById(R.id.lvCameras);
        //Set choice mode
        lvCameras.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        //Set ListView item click listener
        lvCameras.setOnItemClickListener(this);

        //PARSE FILE BEFORE BUILDING ARRAYADAPTER
        //parseFileXMLFromAssets();
        //parseFileJson();
        //Build ArrayAdapter and set layout
        //adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, cameraListName);
        //Build ArrayAdapter and set layout. For step 4
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, initalData );
        //Set adapter for ListView
        lvCameras.setAdapter(adapter);

        DownloadFileTask task = new DownloadFileTask();
        task.execute(URL_CAMERAS);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        DownloadImageTask task = new DownloadImageTask();
        String items_URL = "";
        String items_coord = "";
        SparseBooleanArray checked= lvCameras.getCheckedItemPositions();
        for (int i = 0; i < checked.size(); i++) {
            if (checked.valueAt(i) ) {
                int pos = checked.keyAt(i);
                items_URL = items_URL + " " + cameraListURLS.get(pos);
                items_coord = items_coord + " " + cameraListCoordinates.get(pos);

            }
        }
        //selectionURL.setText(items_URL);
        selectionCoordinates.setText(items_coord);
        task.execute(items_URL);
    }

    //Método que ejecuta la pantalla de maps al clickar en la imagen
    //También muestra un marker en el lugar de la imagen
    public void cambiarPantalla(View view){
        String coordenadass;
        Intent intent = new Intent(this, MapsActivity.class);
        coordenadass = selectionCoordinates.getText().toString();
        intent.putExtra("coordinates", coordenadass);
        startActivity(intent);
    }

    public void parseFileXMLFromAssets(){

        try{
            //Open file from asset folder
            InputStream is = getAssets().open("CCTV.kml");
            parseFileXML(is);

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void parseFileXML(InputStream is){
        XmlPullParserFactory parserFactory;
        String cameraURL;
        String nameAttibute;
        try{
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is,null);
            int eventType = parser.getEventType();

            while(eventType != XmlPullParser.END_DOCUMENT){
                String elementName = null;
                elementName = parser.getName();

                switch(eventType){
                    case XmlPullParser.START_TAG:
                        if("description".equals(elementName)){
                            cameraURL = parser.nextText();
                            cameraURL = cameraURL.substring(cameraURL.indexOf("http:"));
                            cameraURL = cameraURL.substring(0, cameraURL.indexOf(".jpg") + 4);
                            cameraListURLS.add( cameraURL );
                        }
                        else if ("Data".equals(elementName)) {
                            // Get the "name" attribute value by means of: parser.getAttributeValue(...)
                            nameAttibute = parser.getAttributeValue(0);
                            if("Nombre".equals(nameAttibute)){
                                //Jump to next TAG
                                parser.nextTag();
                                //Get tag text
                                String cameraName = parser.nextText();
                                cameraListName.add(cameraName);
                            }

                        }else if("coordinates".equals(elementName)){
                            cameraURL = parser.nextText();
                            cameraListCoordinates.add(cameraURL);

                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parseFileJson(){
        CCTV cctv = new CCTV();
        Gson gson = new Gson();
        try{
            AssetManager am = getAssets(); // access to assets folder
            InputStream is = am.open("CCTV.json");
            cctv = gson.fromJson(new InputStreamReader(is), CCTV.class);
        } catch (IOException e) {
            // Exception handling
            e.printStackTrace();
        }
        int nCameras = cctv.kml.Document.Placemark.length;
        String cameraName;
        String URLCamera;
        String coordinatesCamera;
        for (int j=0;j<nCameras;j++){
            cameraName = cctv.kml.Document.Placemark[j].ExtendedData.Data[1].Value;
            cameraListName.add(cameraName);
            coordinatesCamera = cctv.kml.Document.Placemark[j].Point.coordinates;
            cameraListCoordinates.add(coordinatesCamera);
            //La URL hay que aislarla de la description
            URLCamera = cctv.kml.Document.Placemark[j].description;
            //Tratar el stream
            URLCamera = URLCamera.substring(URLCamera.indexOf("http:"));
            URLCamera = URLCamera.substring(0, URLCamera.indexOf(".jpg") + 4);
            cameraListURLS.add(URLCamera);

        }
    }

    class DATA{
        String Value;
        String _name;
    }
    class EXTENDEDDATA{
        DATA[] Data;
    }
    class PLACEMARK{
        String description;
        EXTENDEDDATA ExtendedData;
        POINT Point;
    }
    class DOCUMENT {
        String name;
        // other fields
        PLACEMARK[] Placemark;
    }
    class KML {
        DOCUMENT Document;
    }
    public class CCTV {
        KML kml;
    }
    class POINT{
        String altitudeMode;
        String coordinates;
    }

    //Step 2
    class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        private Bitmap loadedImage;

        public Bitmap getBitmap() {
            return this.loadedImage;
        }

        protected Bitmap doInBackground(String... urls) {
            // Worker thread
            //Bitmap loadedImage;
            String response = "";
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream is = urlConnection.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                loadedImage = BitmapFactory.decodeStream(bis);
                //ivCamera.setImageBitmap(loadedImage);
                bis.close();
                is.close();
                urlConnection.disconnect();

            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return loadedImage;
        }

        protected void onPostExecute(Bitmap bitmaps) {
            // Executed on UI thread
            ivCamera.setImageBitmap(bitmaps);
        }
    }

    //Step 4
    class DownloadFileTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            //Abrir URL
            String response = "";
            HttpURLConnection urlConnection = null;
            //Parse XML file
            XmlPullParserFactory parserFactory;
            String cameraURL;
            String nameAttibute;
            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream is = urlConnection.getInputStream();
                parseFileXML(is);

            }catch (MalformedURLException e) {
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            //Actualizar ArrayList
            adapter.clear();
            adapter.addAll( cameraListName );
            adapter.notifyDataSetChanged();
            //modoURL.setText("Modo URL");

        }
    }
}
