package dto;

import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@WebService(targetNamespace = "http://www.polytech.unice.fr/si/4a/isa/demo/ref/")
@Stateless(name="DemoDataTransferObject")
public class Demo {

    @WebMethod
    public List<CustomerWithCard> listCustomers() {
        List<CustomerWithCard> result = new ArrayList<>();
        result.add(build("Jacques"));
        result.add(build("Alison"));
        result.add(build("Pierre"));
        result.add(build("Franck"));
        result.add(build("Laura"));
        return result;
    }

    private CustomerWithCard build(String name) {
        Customer result = new Customer();
        result.setName(name);
        Card card = new Card();
        card.setIdentifier(UUID.randomUUID().toString());
        card.setOwner(result);
        result.setCard(card);
        return new CustomerWithCard(result);
    }

}
