import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SealedObject;

import com.google.gson.Gson;

public class Client {

	public Card card;
	public RSA rsa;
	public PublicKey publicKey;
	public PrivateKey privateKey;
	public AES aes;

	public Client(String cardNumber, String name, String expireDate, String cvv) {
		this.card = new Card(cardNumber, name, expireDate, cvv);
		try {
			rsa = new RSA();
			this.publicKey = rsa.publicKey;
			this.privateKey = rsa.getPrivateKey();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			Socket socket = new Socket(InetAddress.getByName("localhost"), Utils.SERVER_PORT);
			Client client = new Client("12345", "Jerry", "10/22", "123");

			FileInputStream fileIn1 = new FileInputStream("merchantPublicKey.txt");
			ObjectInputStream objectOut1 = new ObjectInputStream(fileIn1);
			PublicKey merchantPublicKey = (PublicKey) objectOut1.readObject();
			System.out.println(merchantPublicKey.toString());
			objectOut1.close();

			client.aes = new AES();
			SealedObject encryptedPublicKey = AES.encrypt(client.publicKey, client.aes.getKey());

			String symmetricKey = client.aes.getKey();
			String encryptedSymmetricKey = client.rsa.encrypt(merchantPublicKey, symmetricKey);

			KeyDTO keysObject = new KeyDTO(encryptedPublicKey, encryptedSymmetricKey);
			String keysJson = (new Gson().toJson(keysObject));
			System.out.println("[Client] Trimitem pe server JSON-ul:" + keysJson);

			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(keysJson);
			out.flush();
			System.out.println("[Client] Primim de la server JSON-ul criptat cu id ul si semnatura");
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			String encryptedIdJson = (String) in.readObject();
			String idJson = AES.decrypt(encryptedIdJson, client.aes.getKey());
			KeyDTO paymentId = (new Gson().fromJson(idJson, KeyDTO.class));
			System.out.println("[Client] Semnatura decriptata: " + paymentId);
			System.out.println("[Client] Verificam semnatura");
			if (Utils.verify(merchantPublicKey, paymentId.id.getBytes(), paymentId.signedId)) {
				System.out.println("[Client] Semnatura valida! Pregatim initierea platii!");
				PaymentDTO PI = new PaymentDTO(client.card, paymentId.id,
						"100", client.publicKey);
				System.out.println("[Client] Initiem plata");
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream out2 = new ObjectOutputStream(bos);
				out2.writeObject(PI);
				out2.flush();
				byte[] PIByte = bos.toByteArray();
				System.out.println("[Semnam] Semnam plata");
				byte[] signedPI = Utils.sign(client.privateKey, PIByte);
				PaymentDTO PM = new PaymentDTO(PIByte, signedPI);
				
				String orderDesc = "descriere comanda";
                PaymentDTO PO = new PaymentDTO(orderDesc, paymentId.id, "100" );

                bos = new ByteArrayOutputStream();
                ObjectOutputStream out3 = new ObjectOutputStream(bos);
				out3.writeObject(PO);
				out3.flush();
				byte[] POBytes = bos.toByteArray();

                //sign
                byte[] signedPO = Utils.sign(client.privateKey, POBytes);
                PaymentDTO BigPO = new PaymentDTO(POBytes, signedPO);

                //create object that must be sent
                CompletePaymentDTO paymentData = new CompletePaymentDTO(PM,BigPO);
                String paymentDataJson = (new Gson().toJson(paymentData));
                out.writeObject(paymentDataJson);
                out.flush();
			} else {
				System.out.println("[Client] Semnatura invalida!");
			}
		socket.close();
		} catch (IOException e) {
			System.out.println("[Client] Exceptie la crearea socketului client-server la portul " + Utils.SERVER_PORT);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
