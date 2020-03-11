import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;

import com.google.gson.Gson;

public class Bank {

	static class BankAccount {
		public String ownerName;
		public String expireDate;
		public String cvv;
		public String cardNumber;
		public String stockMoney;

		public BankAccount(String ownerName, String expireDate, String cvv, String cardNumber, String stockMoney) {
			this.ownerName = ownerName;
			this.expireDate = expireDate;
			this.cvv = cvv;
			this.cardNumber = cardNumber;
			this.stockMoney = stockMoney;
		}
	}

	RSA rsa;
	PublicKey publicKey;
	public BankAccount bankAccount;

	public Bank() {
		try {
			rsa = new RSA();
			this.publicKey = rsa.publicKey;
			this.saveBankPublicKey(this.publicKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveBankPublicKey(PublicKey publicKey) throws IOException {
		try {
			FileOutputStream fileOut = new FileOutputStream("bankPublicKey.txt");
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(publicKey);
			objectOut.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String args[]) {
		BankAccount client = new BankAccount("Jerry", "10/22", "123", "12345", "1000");
		ServerSocket serverSocket = null;

		try {

			serverSocket = new ServerSocket(Utils.BANK_PORT);
			Socket socket = serverSocket.accept();

			FileInputStream fileIn1 = new FileInputStream("merchantPublicKey.txt");
			ObjectInputStream objectOut1 = new ObjectInputStream(fileIn1);
			objectOut1.close();

			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			String paymentDataJson = (String) in.readObject();
			CompletePaymentDTO paymentData = (new Gson().fromJson(paymentDataJson, CompletePaymentDTO.class));
			ByteArrayInputStream in2 = new ByteArrayInputStream(paymentData.PM.paymentDetails);
			ObjectInputStream is = new ObjectInputStream(in2);
			PaymentDTO PM = (PaymentDTO) is.readObject();

			if (PM.card.getName().equals(client.ownerName) && PM.card.getCardExp().equals(client.expireDate)
					&& PM.card.getCardNumber().equals(client.cardNumber) && (PM.card.getCcv().equals(client.cvv))) {
				if (Integer.parseInt(client.stockMoney) - Integer.parseInt(PM.ammount) > 0) {
					System.out.println("Realizat");
					client.stockMoney = Integer
							.toString(Integer.parseInt(client.stockMoney) - Integer.parseInt(PM.ammount));

				} else {
					System.out.println("Bani insuficienti");

				}
			}

			serverSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				serverSocket.close();
			} catch (Exception e) {
			}
		}
	}

}