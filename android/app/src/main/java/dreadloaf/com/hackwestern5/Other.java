package dreadloaf.com.hackwestern5;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseModelOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;
import com.google.firebase.ml.custom.model.FirebaseCloudModelSource;
import com.google.firebase.ml.custom.model.FirebaseModelDownloadConditions;

public class Other {

     static void makeClassification(Bitmap bitmap) {

        int batchNum = 0;
        float[][][][] input = new float[1][64][64][3];
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 64; y++) {
                int pixel = bitmap.getPixel(x, y);
                // Normalize channel values to [0.0, 1.0]. This requirement varies by
                // model. For example, some models might require values to be normalized
                // to the range [-1.0, 1.0] instead.
                input[batchNum][x][y][0] = Color.red(pixel) / 255.0f;
                input[batchNum][x][y][1] = Color.green(pixel) / 255.0f;
                input[batchNum][x][y][2] = Color.blue(pixel) / 255.0f;
            }
        }

         FirebaseModelDownloadConditions.Builder conditionsBuilder =
                 new FirebaseModelDownloadConditions.Builder().requireWifi();
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
             // Enable advanced conditions on Android Nougat and newer.
             conditionsBuilder = conditionsBuilder
                     .requireCharging()
                     .requireDeviceIdle();
         }
         FirebaseModelDownloadConditions conditions = conditionsBuilder.build();

        // Build a FirebaseCloudModelSource object by specifying the name you assigned the model
        // when you uploaded it in the Firebase console.
         FirebaseCloudModelSource cloudSource = new FirebaseCloudModelSource.Builder("flower-classifier")
                 .enableModelUpdates(true)
                 .setInitialDownloadConditions(conditions)
                 .setUpdatesDownloadConditions(conditions)
                 .build();
         FirebaseModelManager.getInstance().registerCloudModelSource(cloudSource);

         FirebaseModelOptions options = new FirebaseModelOptions.Builder()
                 .setCloudModelName("flower-classifier")
                 .build();
         try {
             FirebaseModelInterpreter firebaseInterpreter =
                     FirebaseModelInterpreter.getInstance(options);

             FirebaseModelInputOutputOptions inputOutputOptions =
                     new FirebaseModelInputOutputOptions.Builder()
                             .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 64, 64, 3})
                             .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 17})
                             .build();

             FirebaseModelInputs inputs = new FirebaseModelInputs.Builder()
                     .add(input)  // add() as many input arrays as your model requires
                     .build();
             firebaseInterpreter.run(inputs, inputOutputOptions)
                     .addOnSuccessListener(
                             new OnSuccessListener<FirebaseModelOutputs>() {
                                 @Override
                                 public void onSuccess(FirebaseModelOutputs result) {
                                     float[][] output = result.getOutput(0);
                                     float[] probabilities = output[0];
                                     for (float p : probabilities){
                                         Log.e("Prob", String.valueOf(p));
                                     }
                                 }
                             })
                     .addOnFailureListener(
                             new OnFailureListener() {
                                 @Override
                                 public void onFailure(@NonNull Exception e) {
                                     // Task failed with an exception
                                     Log.e("Error", e.toString());

                                 }
                             });

         } catch (FirebaseMLException e) {
             e.printStackTrace();
         }

     }
}
