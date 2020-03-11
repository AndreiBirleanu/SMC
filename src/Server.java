import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.UUID;

import com.google.gson.Gson;

public class Server {

	private RSA rsa;
	private PublicKey publicKey;
	private PrivateKey privateKey;

	public Server() {
		try {
			rsa = new RSA();
			this.publicKey = rsa.publicKey;
			this.saveMerchantPublicKey(this.publicKey);
			this.privateKey = rsa.getPrivateKey();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveMerchantPublicKey(PublicKey publicKey) throws IOException {
		try {
			FileOutputStream fileOut = new FileOutputStream("merchantPublicKey.txt");
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(publicKey);
			objectOut.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {

		try {

			Server server = new Server();
			System.out.println("[Server] Cheia publica server " + server.publicKey);
			ServerSocket serverSocket = new ServerSocket(Utils.SERVER_PORT);
			System.out.println("[Server] Deschidem serverul vanzatorului la portul " + Utils.SERVER_PORT);
			Socket clientSocket = serverSocket.accept();
			System.out.println("[Server] Client conectat!" + clientSocket.toString());

			ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
			String json1 = (String) in.readObject();
			KeyDTO dataFromClient = (new Gson().fromJson(json1, KeyDTO.class));
			System.out.println("[Server] Am primit mesajul: " + dataFromClient.toString());
			// decriptez cu cheia privata a serverului cheia simetrica
			String simetricKey = server.rsa.decrypt(server.rsa.getPrivateKey(), dataFromClient.SK);
			System.out.println("[Server] Cheia simetrica decriptata " + simetricKey);
			PublicKey clientPublicKey = AES.decrypt(dataFromClient.PK, simetricKey);
			System.out.println("[Server] Cheia publica a clientului " + clientPublicKey.toString());

			String uniqueID = UUID.randomUUID().toString();
			byte[] signedUniqueID = Utils.sign(server.privateKey, uniqueID.getBytes());
			System.out.println("[SERVER] Am generat idul unic " + uniqueID + " si am semnat cheia privata");
			KeyDTO objectForClient = new KeyDTO(uniqueID, signedUniqueID);
			String json2 = (new Gson().toJson(objectForClient));

			String encryptedJson = AES.encrypt(json2, simetricKey);
			ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
			out.writeObject(encryptedJson);
			out.flush();
			System.out
					.println("[Server] Trimitem la client id ul si semnatura intr un json criptat cu cheia simetrica");

			String paymentDataJson = (String) in.readObject();
			CompletePaymentDTO paymentData = (new Gson().fromJson(paymentDataJson, CompletePaymentDTO.class));
			PaymentDTO PO = paymentData.PO;

			if (Utils.verify(clientPublicKey, PO.paymentDetails, PO.signedPaymentDetails)) {
				System.out.println("All good! Im going to tell the bank to prepare the payment!");
				ByteArrayInputStream in2 = new ByteArrayInputStream(PO.paymentDetails);
				ObjectInputStream is = new ObjectInputStream(in2);
				PaymentDTO paymentDetails = (PaymentDTO) is.readObject();
				if (paymentDetails.ammount.equals("100")) {
					PaymentDTO paymentForBank = new PaymentDTO(paymentDetails.paymentId,
							paymentDetails.ammount, clientPublicKey);
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ObjectOutputStream out2 = new ObjectOutputStream(bos);
					out2.writeObject(paymentForBank);
					out2.flush();
					byte[] signedPaymentForBank = Utils.sign(server.privateKey, bos.toByteArray());
					CompletePaymentDTO dataForBank = new CompletePaymentDTO(paymentData.PM, signedPaymentForBank);
					String dataForBankJson = (new Gson().toJson(dataForBank));
					
					InetAddress serverHost = InetAddress.getByName("localhost");
                    Socket bankSocket = new Socket(serverHost, Utils.BANK_PORT);

                    ObjectOutputStream bankOut = new ObjectOutputStream(bankSocket.getOutputStream());
                    bankOut.writeObject(dataForBankJson);
                    bankOut.flush();
                    bankSocket.close();
                    serverSocket.close();
				} else {
					System.out.println("Prea putini bani");
				}
			} else {
				System.out.println("[Server] OOPS");
			}

		} catch (IOException e) {
			System.out.println("[Server] Exceptie la crearea socketului SERVER-CLIENT la portul " + Utils.SERVER_PORT);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.out.println("[Server] Eroare la citirea mesajului de la Client");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("[Server] Eroare generica!");
			e.printStackTrace();
		}

	}
}
