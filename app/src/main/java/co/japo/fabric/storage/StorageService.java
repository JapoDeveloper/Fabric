package co.japo.fabric.storage;

/**
 * Created by japodeveloper on 11/16/17.
 */

public class StorageService {

    private static StorageService instance;

    private StorageService(){}

    public static StorageService getInstance(){
        if(instance == null){
            instance = new StorageService();
        }
        return instance;
    }
}
