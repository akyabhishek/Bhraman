package com.edorbit.bhraman;

import static java.io.File.createTempFile;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.edorbit.bhraman.ml.Model;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;



public class ScanFragment extends Fragment {
    ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;
    PreviewView previewView;
    Button scan,gallery,qrscanner;
    private ImageCapture imageCapture;
    File imageFile;
    int imageSize = 256;
    ProgressBar pbar;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("objects");;
    ObjectData objectData;
    FirebaseUser user;
    String en,hi,lang;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        scan=view.findViewById(R.id.scan);
        pbar=view.findViewById(R.id.pbar);
        previewView = view.findViewById(R.id.previewView);
        qrscanner=view.findViewById(R.id.qrScanner);
//        flash=view.findViewById(R.id.flash);

        user= FirebaseAuth.getInstance().getCurrentUser();
        gallery=view.findViewById(R.id.gallery);
        cameraProviderListenableFuture=ProcessCameraProvider.getInstance(getContext());
        cameraProviderListenableFuture.addListener(()->{
            try {
                ProcessCameraProvider cameraProvider=cameraProviderListenableFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },getExecutor());

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int MY_PERMISSIONS_REQUEST_CAMERA=0;
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA))
                    {

                    }
                    else
                    {
                        ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA );
                    }
                }else {
                    pbar.setVisibility(View.VISIBLE);
                    scanPhoto();
                }
            }
        });
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent cameraIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(cameraIntent, 1);
                pbar.setVisibility(View.VISIBLE);
            }
        });
        qrscanner.setOnClickListener(v->
        {
            scanCode();
        });

        return view;
    }

    private void scanCode() {

        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLaucher.launch(options);
    }
    ActivityResultLauncher<ScanOptions> barLaucher = registerForActivityResult(new ScanContract(), result->
    {
        if(result.getContents() !=null)
        {
            Toast.makeText(getContext(), "QR read - "+result.getContents(), Toast.LENGTH_SHORT).show();

            try{
            FirebaseDatabase.getInstance().getReference("objects").child(result.getContents().toLowerCase())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(snapshot.exists()){
                                objectData=snapshot.getValue(ObjectData.class);

                                databaseReference.child(result.getContents().toLowerCase()).child("views")
                                        .setValue(objectData.getViews()+1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(!task.isSuccessful()){
                                                    Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                if(user==null){
                                    lang=objectData.getEnVoice();
                                    startAr();
                                }
                                else{
                                    //language setting code
                                    FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("language")
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                    if(snapshot.exists()){
                                                        if(snapshot.getValue().equals("Hindi")){
                                                            lang= objectData.getHiVoice();
                                                        }
                                                        else{
                                                            lang= objectData.getEnVoice();
                                                        }
                                                    }
                                                    else{
                                                        lang= objectData.getEnVoice();
                                                    }
                                                    startAr();
                                                    pbar.setVisibility(View.GONE);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });

                                    //history code
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                                    long dateandtime = Long.parseLong(sdf.format(new Date()));
                                    HistoryData historyData=new HistoryData(objectData.getName(), dateandtime);

                                    FirebaseDatabase.getInstance().getReference("users").child(user.getUid())
                                            .child("history").child(objectData.getName().toLowerCase())
                                            .setValue(historyData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()) {
                                                        Toast.makeText(getContext(), "You are viewing " + objectData.getName(), Toast.LENGTH_SHORT).show();
                                                    }
                                                    else{
                                                        Toast.makeText(getContext(), "Your history is not updated", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }

                            }
                            else{
                                pbar.setVisibility(View.GONE);
                                Toast.makeText(getContext(), result.getContents()+" not found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
            } catch(Exception e){
            Toast.makeText(getContext(), "Scan a valid QR code. Error-"+e.getMessage() ,Toast.LENGTH_SHORT).show();
        }
//            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//            builder.setTitle("Result");
//            builder.setMessage(result.getContents());
//            builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
//            {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i)
//                {
//                    dialogInterface.dismiss();
//                }
//            }).show();
        }
    });


    private void scanPhoto() {

        try {
            imageFile= createTempFile("anImage", ".jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
//        File photo=new File("photo");
        imageCapture.takePicture(new ImageCapture.OutputFileOptions.Builder(imageFile).build(), getExecutor()
                , new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Bitmap picBitmap = BitmapFactory.decodeFile(String.valueOf(imageFile));
//                        Toast.makeText(Scanner.this, picBitmap.getWidth()+"Captured"+picBitmap.getHeight(), Toast.LENGTH_SHORT).show();
                        int dimension = Math.min(picBitmap.getWidth(), picBitmap.getHeight());
                        picBitmap = ThumbnailUtils.extractThumbnail(picBitmap, dimension, dimension);
//                        Toast.makeText(getActivity(), ""+dimension, Toast.LENGTH_SHORT).show();
                        picBitmap = Bitmap.createScaledBitmap(picBitmap, imageSize, imageSize, false);
                        classifyImage(picBitmap);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(getActivity(), "Not Captured!"+exception.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
        );
    }

    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        CameraSelector cameraSelector=new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        Preview preview=new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture=new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(getContext());
    }

    public void classifyImage(Bitmap image){
        try {
            Model model = Model.newInstance(getContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 256, 256, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
            for(int i = 0; i < imageSize; i ++){
                for(int j = 0; j < imageSize; j++){
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 1));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            // find the index of the class with the biggest confidence.
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            String[] classes = {"Buddha Statue","India Gate","Qutub Minar","Taj Mahal"};
            String objname=classes[maxPos];
//            Toast.makeText(getContext(), ""+(maxConfidence/sum)*100 , Toast.LENGTH_LONG).show();
            FirebaseDatabase.getInstance().getReference("objects").child(objname.toLowerCase())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if(snapshot.exists()){
                        objectData=snapshot.getValue(ObjectData.class);

                        databaseReference.child(objname.toLowerCase()).child("views")
                                .setValue(objectData.getViews()+1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(!task.isSuccessful()){
                                            Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                        if(user==null){
                            lang=objectData.getEnVoice();
                            startAr();
                        }
                        else{
                            //language setting code
                            FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("language")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                            if(snapshot.exists()){
                                                if(snapshot.getValue().equals("Hindi")){
                                                    lang= objectData.getHiVoice();
                                                }
                                                else{
                                                    lang= objectData.getEnVoice();
                                                }
                                            }
                                            else{
                                                lang= objectData.getEnVoice();
                                            }
                                            startAr();
                                            pbar.setVisibility(View.GONE);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                            //history code
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                            long dateandtime = Long.parseLong(sdf.format(new Date()));
                            HistoryData historyData=new HistoryData(objectData.getName(), dateandtime);

                            FirebaseDatabase.getInstance().getReference("users").child(user.getUid())
                                    .child("history").child(objectData.getName().toLowerCase())
                                    .setValue(historyData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                Toast.makeText(getContext(), "You are viewing " + objectData.getName(), Toast.LENGTH_SHORT).show();
                                            }
                                            else{
                                                Toast.makeText(getContext(), "Your history is not updated", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }

                    }
                    else{
                        pbar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), objname+" not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });



            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }
    private void startAr() {
        Intent sceneViewerIntent = new Intent(Intent.ACTION_VIEW);
        Uri intentUri =
                Uri.parse("https://arvr.google.com/scene-viewer/1.0").buildUpon()
                        .appendQueryParameter("file", objectData.getObjLink())
                        .appendQueryParameter("title", objectData.getName())
                        .appendQueryParameter("sound", lang)
                        .appendQueryParameter("mode", "ar_preferred")
                        .build();
        sceneViewerIntent.setData(intentUri);
        sceneViewerIntent.setPackage("com.google.android.googlequicksearchbox");
        sceneViewerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(sceneViewerIntent);

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==3){
            Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();

        }
        else{


            Uri dat = null;
            if (data != null) {
                dat = data.getData();
                Bitmap image = null;
                try {
                    image = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), dat);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
            }
            else{
                pbar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), "No image selected", Toast.LENGTH_SHORT).show();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }



}