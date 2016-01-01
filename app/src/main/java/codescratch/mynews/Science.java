package codescratch.mynews;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import api.AlchemyAPI;

public class Science extends AppCompatActivity {


    public TextView urlText;
    public TextView textview;
    public ImageView imageview;
    /****
     *
     * Put your API Key into the variable below.  Can get key from http://www.alchemyapi.com/api/register.html
     */
    public String AlchemyAPI_Key = "0a510e50e2bb60a41c839e4c8d6d7ced2abf6bdc";

    /** Called when the activity is first created. */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_science);

        urlText = (TextView) findViewById(R.id.entry);
        textview = (TextView) findViewById(R.id.TextView01);
        urlText.setText("http://www.engadget.com");

        textview.setText("");
        textview.setMovementMethod(new ScrollingMovementMethod());

        final Button text_button = (Button) findViewById(R.id.text_button);
        text_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SendAlchemyCall("text");
            }
        });

        // Handle Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle(R.string.drawer_item_multi_drawer);

        //set the back arrow in the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(false);

    }

    private void SendAlchemyCall(final String call)
    {
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    SendAlchemyCallInBackground(call);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }
    private void SendAlchemyCallInBackground(final String call)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textview.setText("Making call: "+call);
            }
        });

        Document doc = null;
        AlchemyAPI api = null;
        try
        {
            api = AlchemyAPI.GetInstanceFromString(AlchemyAPI_Key);
        }
        catch( IllegalArgumentException ex )
        {
            textview.setText("Error loading AlchemyAPI.  Check that you have a valid AlchemyAPI key set in the AlchemyAPI_Key variable.  Keys available at alchemyapi.com.");
            return;
        }

        String someString = urlText.getText().toString();
        try{
            if( "text".equals(call))
            {
                doc = api.URLGetText(someString);
                ShowDocInTextView(doc, false);
            }

//            else if( "image".equals(call))
//            {
//                doc = api.URLGetImage(someString);
//                ShowTagInTextView(doc, "image");
//            }

//            else if( "imageClassify".equals(call))
//            {
//                Bitmap bitmap = ((BitmapDrawable)imageview.getDrawable()).getBitmap();
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//                byte[] imageByteArray = stream.toByteArray();
//
//                AlchemyAPI_ImageParams imageParams = new AlchemyAPI_ImageParams();
//                imageParams.setImage(imageByteArray);
//                imageParams.setImagePostMode(AlchemyAPI_ImageParams.RAW);
//                doc = api.ImageGetRankedImageKeywords(imageParams);
//                ShowTagInTextView(doc, "text");
//            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            textview.setText("Error: " + e.getMessage());
        }
    }
    private void ShowTagInTextView(final Document doc, final String tag)
    {
        Log.d(getString(R.string.app_name), doc.toString());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textview.setText("Tags: \n");
                Element root = doc.getDocumentElement();
                NodeList items = root.getElementsByTagName(tag);
                for (int i=0;i<items.getLength();i++) {
                    Node concept = items.item(i);
                    String astring = concept.getNodeValue();
                    astring = concept.getChildNodes().item(0).getNodeValue();
                    textview.append("\n" + astring);
                }
            }
        });
    }

    private void ShowDocInTextView(final Document doc, final boolean showSentiment)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textview.setText("");
                if( doc == null )
                {
                    return;
                }

                Element root = doc.getDocumentElement();
                NodeList items = root.getElementsByTagName("text");
                if( showSentiment )
                {
                    NodeList sentiments = root.getElementsByTagName("sentiment");
                    for (int i=0;i<items.getLength();i++){
                        Node concept = items.item(i);
                        String astring = concept.getNodeValue();
                        astring = concept.getChildNodes().item(0).getNodeValue();
                        textview.append("\n" + astring);
                        if( i < sentiments.getLength() )
                        {
                            Node sentiment = sentiments.item(i);
                            Node aNode = sentiment.getChildNodes().item(1);
                            Node bNode = aNode.getChildNodes().item(0);
                            textview.append(" (" + bNode.getNodeValue()+")");
                        }
                    }
                }
                else
                {
                    for (int i=0;i<items.getLength();i++) {
                        Node concept = items.item(i);
                        String astring = concept.getNodeValue();
                        astring = concept.getChildNodes().item(0).getNodeValue();
                        textview.append("\n" + astring);
                    }
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if(resultCode == RESULT_OK){
            Uri selectedImage = imageReturnedIntent.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            try
            {
                Bitmap imgBitmap = getScaledBitmap(picturePath, 400, 400);

                // Images from the filesystem might be rotated...
                ExifInterface exif = new ExifInterface(picturePath);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

                Log.d(getString(R.string.app_name), "Orientation: "+orientation);

                switch (orientation) {
                    case 3:
                    {
                        Matrix matrix = new Matrix();

                        matrix.postRotate(90);
                        imgBitmap = Bitmap.createBitmap(
                                imgBitmap, 0, 0, imgBitmap.getWidth(), imgBitmap.getHeight(), matrix, true);
                    }
                    break;
                    case 6:
                    {
                        Matrix matrix = new Matrix();

                        matrix.postRotate(90);
                        imgBitmap = Bitmap.createBitmap(
                                imgBitmap, 0, 0, imgBitmap.getWidth(), imgBitmap.getHeight(), matrix, true);
                    }
                    break;

                }

                imageview.setImageBitmap(imgBitmap);
            }
            catch (Exception e)
            {
                textview.setText("Error loading image: " + e.getMessage());
            }
        }
    }

    private Bitmap getScaledBitmap(String picturePath, int width, int height) {
        BitmapFactory.Options sizeOptions = new BitmapFactory.Options();
        sizeOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picturePath, sizeOptions);

        int inSampleSize = calculateInSampleSize(sizeOptions, width, height);

        sizeOptions.inJustDecodeBounds = false;
        sizeOptions.inSampleSize = inSampleSize;

        return BitmapFactory.decodeFile(picturePath, sizeOptions);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handle the click on the back arrow click
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

}
