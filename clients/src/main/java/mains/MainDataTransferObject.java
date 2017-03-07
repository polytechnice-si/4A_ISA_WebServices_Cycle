package mains;

import stubs.dto.*;

public class MainDataTransferObject {

    public static void main(String[] args) {
        DemoService service = new DemoService();
        Demo port = service.getDemoPort();
        System.out.println("** Retrieving Customers");
        for (CustomerWithCard dto : port.listCustomers()) {
            System.out.println("  - " + dto.getName() + " - " + dto.getCardId());
        }
        System.out.println("** done");
    }
}
