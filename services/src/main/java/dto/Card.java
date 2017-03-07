package dto;

public class Card {

    private String identifier;
    private Customer owner;

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public Customer getOwner() { return owner; }
    public void setOwner(Customer owner) { this.owner = owner; }

}
