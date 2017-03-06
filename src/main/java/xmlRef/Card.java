package xmlRef;

import javax.xml.bind.annotation.XmlIDREF;


public class Card {


    protected String identifier;
    protected Customer owner;

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    @XmlIDREF
    public Customer getOwner() { return owner; }
    public void setOwner(Customer owner) { this.owner = owner; }


}
