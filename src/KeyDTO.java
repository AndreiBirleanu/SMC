import java.io.Serializable;

import javax.crypto.SealedObject;

import com.google.gson.Gson;

public class KeyDTO implements Serializable {
	private static final long serialVersionUID = 3417350360369776136L;
	//simetric key
	public String SK;
	//public key
    public SealedObject PK;
    //id semnatura
    public String id;
    //biti semnatura
    public byte[] signedId;

    public KeyDTO(){

    }

    public KeyDTO(SealedObject object, String key){
        this.SK= key;
        this.PK= object;
    }

    public KeyDTO(String id, byte[] signedId){
        this.id = id;
        this.signedId = signedId;
    }
    
    @Override
    public String toString() {
    
    	return new Gson().toJson(this);
    }
}

