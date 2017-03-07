package dto;


public class CustomerWithCard {

    private String name;
    private String cardId;

    public CustomerWithCard() {}

    public CustomerWithCard(Customer cu) {
        this.name = cu.getName();
        this.cardId = cu.getCard().getIdentifier();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCardId() { return cardId; }
    public void setCardId(String cardId) { this.cardId = cardId; }
}
