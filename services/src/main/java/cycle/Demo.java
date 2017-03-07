package cycle;


import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@WebService(targetNamespace = "http://www.polytech.unice.fr/si/4a/isa/demo/cycle/")
@Stateless(name="DemoCycle")
public class Demo {

    @WebMethod
    public List<Customer> listCustomers() {
        List<Customer> result = new ArrayList<>();
        result.add(build("Jacques"));
        result.add(build("Alison"));
        result.add(build("Pierre"));
        result.add(build("Franck"));
        result.add(build("Laura"));
        return result;
    }

    private Customer build(String name) {
        Customer result = new Customer();
        result.setName(name);
        Card card = new Card();
        card.setIdentifier(UUID.randomUUID().toString());
        card.setOwner(result);
        result.setCard(card);
        return result;
    }

}
