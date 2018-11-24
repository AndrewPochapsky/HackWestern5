package dreadloaf.com.hackwestern5

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.os.Environment.DIRECTORY_PICTURES
import android.support.v4.content.FileProvider
import android.util.Log
import android.widget.Button
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    val REQUEST_IMAGE_CAPTURE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.take_photo_button).setOnClickListener({
            val takePictureIntent : Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if(takePictureIntent.resolveActivity(packageManager) != null) {

                val photoFile = createImageFile()
                var imagePath = photoFile.absolutePath
                var photoURI = FileProvider.getUriForFile(this,
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
            val extras = data?.extras

            //val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, photoURI)
            //mImageView.setImageBitmap(bitmap)

            //mPresenter.uploadImageToStorage(photoURI)
        }
    }

}
