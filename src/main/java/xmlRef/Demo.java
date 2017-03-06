package xmlRef;


import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;
import java.util.List;

@WebService(targetNamespace = "http://www.polytech.unice.fr/si/4a/isa/demo/cycle/")
@Stateless(name="DemoReference")
public class Demo {

    @WebMethod
    public List<Customer> listCustomers() {
        return null;
    }

}
