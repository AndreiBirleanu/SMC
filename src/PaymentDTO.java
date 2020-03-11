import java.io.Serializable;
import java.security.PublicKey;

public class PaymentDTO implements Serializable {
	private static final long serialVersionUID = 1L;
	public PublicKey publicKey;
    public Card card;
    public String ammount;
    public String paymentId;
    public String orderDesc;
    public byte[] paymentDetails;
    public byte[] signedPaymentDetails;

    public PaymentDTO (){

    }
    public PaymentDTO( Card card, String paymentId, String ammount, PublicKey publicKey) {
        this.card = card;
        this.paymentId = paymentId;
        this.ammount = ammount;
        this.publicKey = publicKey;
    }

    public PaymentDTO(String paymentId, String ammount, PublicKey publicKey) {
        this.paymentId = paymentId;
        this.ammount = ammount;
        this.publicKey = publicKey;
    }


    public PaymentDTO( byte[] paymentDetails, byte[] signedPaymentDetails){
        this.paymentDetails = paymentDetails;
        this.signedPaymentDetails = signedPaymentDetails;
    }

    public PaymentDTO( String orderDesc, String paymentId, String ammount ){
        this.orderDesc= orderDesc;
        this.ammount = ammount;
        this.paymentId = paymentId;
    }

}

