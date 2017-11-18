package co.japo.fabric.storage;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by japodeveloper on 11/16/17.
 */

public class CloudStorageService {

    private static CloudStorageService instance;
    private StorageReference imagesChallenges;
    private StorageReference imagesUsers;

    private CloudStorageService(){
        imagesChallenges = FirebaseStorage.getInstance().getReference().child("images-challenges");
        imagesUsers = FirebaseStorage.getInstance().getReference().child("images-users");
    }

    public static CloudStorageService getInstance(){
        if(instance == null){
            instance = new CloudStorageService();
        }
        return instance;
    }

    public UploadTask uploadChallengeImage(Uri imageLocation) throws IOException{
        InputStream stream = new FileInputStream(new File(imageLocation.getPath()));
        return imagesChallenges.child(imageLocation.getLastPathSegment()).putStream(stream);
    }
}
