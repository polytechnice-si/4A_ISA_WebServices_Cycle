package cycle;


public class Card {


    protected String identifier;
    protected Customer owner;

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public Customer getOwner() { return owner; }
    public void setOwner(Customer owner) { this.owner = owner; }


}
