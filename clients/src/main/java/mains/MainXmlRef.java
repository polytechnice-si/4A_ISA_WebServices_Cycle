package mains;

import stubs.xmlRef.*;

public class MainXmlRef {

    public static void main(String[] args) {
        DemoService service = new DemoService();
        Demo port = service.getDemoPort();
        System.out.println("** Retrieving Customers");
        for (Customer customer : port.listCustomers()) {
            Card card = customer.getCard();
            System.out.println("  - " + customer.getName() + " - " + customer.getCard().getIdentifier());
            Customer owner = (Customer) card.getOwner();
            System.out.println("    - " + card.getIdentifier()  + " --> " + owner.getName());
        }
        System.out.println("** done");
    }

}
