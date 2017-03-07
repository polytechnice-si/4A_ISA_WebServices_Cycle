
package stubs.cycle;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the stubs.cycle package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ListCustomers_QNAME = new QName("http://www.polytech.unice.fr/si/4a/isa/demo/cycle/", "listCustomers");
    private final static QName _ListCustomersResponse_QNAME = new QName("http://www.polytech.unice.fr/si/4a/isa/demo/cycle/", "listCustomersResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: stubs.cycle
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ListCustomers }
     * 
     */
    public ListCustomers createListCustomers() {
        return new ListCustomers();
    }

    /**
     * Create an instance of {@link ListCustomersResponse }
     * 
     */
    public ListCustomersResponse createListCustomersResponse() {
        return new ListCustomersResponse();
    }

    /**
     * Create an instance of {@link Card }
     * 
     */
    public Card createCard() {
        return new Card();
    }

    /**
     * Create an instance of {@link Customer }
     * 
     */
    public Customer createCustomer() {
        return new Customer();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ListCustomers }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.polytech.unice.fr/si/4a/isa/demo/cycle/", name = "listCustomers")
    public JAXBElement<ListCustomers> createListCustomers(ListCustomers value) {
        return new JAXBElement<ListCustomers>(_ListCustomers_QNAME, ListCustomers.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ListCustomersResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.polytech.unice.fr/si/4a/isa/demo/cycle/", name = "listCustomersResponse")
    public JAXBElement<ListCustomersResponse> createListCustomersResponse(ListCustomersResponse value) {
        return new JAXBElement<ListCustomersResponse>(_ListCustomersResponse_QNAME, ListCustomersResponse.class, null, value);
    }

}
