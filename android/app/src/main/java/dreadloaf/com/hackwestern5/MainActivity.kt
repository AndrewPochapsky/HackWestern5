package dreadloaf.com.hackwestern5

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.os.Environment.DIRECTORY_PICTURES
import android.support.v4.content.FileProvider
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.custom.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.ml.custom.model.FirebaseCloudModelSource
import com.google.firebase.ml.custom.model.FirebaseModelDownloadConditions
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    val REQUEST_IMAGE_CAPTURE = 1
    lateinit var photoURI : Uri

    lateinit var mImageView : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)

        mImageView = findViewById(R.id.image_view)


        findViewById<Button>(R.id.take_photo_button).setOnClickListener({
            val takePictureIntent : Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if(takePictureIntent.resolveActivity(packageManager) != null) {

                val photoFile = createImageFile()
                var imagePath = photoFile.absolutePath
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile)

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                Log.e("InteractActivity", "started activity")
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)

            }


        })
    }


    lateinit var mCurrentPhotoPath: String

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath()
        return image
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        //Log.e("InteractActivity", "on activity result")
        if(requestCode == 1 && resultCode == RESULT_OK){
            Log.e("MAIN", "WE GOOD!!!!")
            //val extras = data?.extras

            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, photoURI)
            //makeClassification(bitmap)
            Other.makeClassification(bitmap)
            //mImageView.setImageBitmap(bitmap)

            //mPresenter.uploadImageToStorage(photoURI)
        }
    }

    fun makeClassification(bitmap : Bitmap){

        var batchNum = 0

        val input = Array(1) { Array(64) { Array(64) { FloatArray(3) } } }

        for(x in 0..63){
            for(y in 0..63){
                val pixel = bitmap.getPixel(x, y)

                input[batchNum][x][y][0] = Color.red(pixel) / 255.0f
                input[batchNum][x][y][1] = Color.green(pixel) / 255.0f
                input[batchNum][x][y][2] = Color.blue(pixel) / 255.0f

            }
        }

        var conditionsBuilder = FirebaseModelDownloadConditions.Builder().requireWifi()


        conditionsBuilder = conditionsBuilder.requireCharging().requireDeviceIdle()

        var conditions = conditionsBuilder.build();

        val cloudSource = FirebaseCloudModelSource.Builder("flower-classifier")
                .enableModelUpdates(true)
                .setInitialDownloadConditions(conditions)
                .setUpdatesDownloadConditions(conditions)
                .build()
        FirebaseModelManager.getInstance().registerCloudModelSource(cloudSource)

        val options = FirebaseModelOptions.Builder()
                .setCloudModelName("flower-classifier")
                .build()

        val firebaseInterpreter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            FirebaseModelInterpreter.getInstance(options)
        } else {
            TODO("VERSION.SDK_INT < JELLY_BEAN")
        }
        val inputOutputOptions = FirebaseModelInputOutputOptions.Builder()
                .setInputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 64, 64, 3))
                .setOutputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 17))
                .build()



        var inputs = FirebaseModelInputs.Builder().add(input).build()
        firebaseInterpreter?.run(inputs, inputOutputOptions)?.addOnSuccessListener(object : OnSuccessListener<FirebaseModelOutputs>{
            override fun onSuccess(p0: FirebaseModelOutputs?) {
                val output = p0!!.getOutput<FloatArray>(0)
                val probabilities = output[0]

            }
        })?.addOnFailureListener(object : OnFailureListener{
            override fun onFailure(p0: Exception) {
                Log.e("WHat", p0.toString())
            }
        })

    }

}
