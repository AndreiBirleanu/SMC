import java.io.Serializable;

public class Card implements Serializable{
	private static final long serialVersionUID = 1L;
	private String cardNumber;
	private String cardExp;
	private String name;
	private String ccv;

	public Card(String cardNumber, String name, String cardExp, String ccv) {
		super();
		this.cardNumber = cardNumber;
		this.name = name;
		this.cardExp = cardExp;
		this.ccv = ccv;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCardExp() {
		return cardExp;
	}

	public void setCardExp(String cardExp) {
		this.cardExp = cardExp;
	}

	public String getCcv() {
		return ccv;
	}

	public void setCcv(String ccv) {
		this.ccv = ccv;
	}
}
