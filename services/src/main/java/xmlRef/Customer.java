package xmlRef;


import javax.xml.bind.annotation.XmlID;

public class Customer {

    private  String name;
    private Card card;

    @XmlID
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Card getCard() { return card; }
    public void setCard(Card card) { this.card = card; }

}
