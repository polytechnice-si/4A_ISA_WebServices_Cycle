package xmlTransient;


import javax.xml.bind.annotation.XmlTransient;

public class Card {


    protected String identifier;
    protected Customer owner;

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    @XmlTransient
    public Customer getOwner() { return owner; }
    public void setOwner(Customer owner) { this.owner = owner; }


}
