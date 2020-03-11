public class CompletePaymentDTO {
   public PaymentDTO PM;
   public PaymentDTO PO;
   public byte[] signature;


    public CompletePaymentDTO(PaymentDTO o1, PaymentDTO o2){
        this.PM = o1;
        this.PO = o2;
    }

    public CompletePaymentDTO(PaymentDTO o1, byte[] o2){
        this.PM = o1;
        this.signature = o2;
    }
}
