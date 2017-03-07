package mains;

import stubs.cycle.*;

public class MainCycle {

    public static void main(String[] args) {
        DemoService service = new DemoService();
        Demo port = service.getDemoPort();
        System.out.println("** Retrieving Customers");
        for (Customer customer : port.listCustomers()) {
            Card card = customer.getCard();
            System.out.println("  - " + customer.getName() + " --> " + card.getIdentifier());
            System.out.println("    - " + card.getIdentifier()  + " --> " + card.getOwner().getName());
        }
        System.out.println("** done");
    }
}
