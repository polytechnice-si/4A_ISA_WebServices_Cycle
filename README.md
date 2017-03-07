# Dealing with object graphs in Web Services

  - Author: Sebastien Mosser
  - Version: 03.17


## Build instruction

To build the project (Java code for the alternative web services and the associated clients) and start the container hosting the web services, simply clone the project, and use maven to build the artefacts and start TomEE.

```bash
azrael:polytech mosser$ https://github.com/polytechnice-si/4A_ISA_WebServices_Cycle.git 
azrael:polytech mosser$ cd 4A_ISA_WebServices_Cycle
azrael:polytech mosser$ mvn clean package
azrael:polytech mosser$ cd services
azrael:polytech mosser$ man tomee:run
```

## Problem description

We consider here a service named `Demo` that exposes an operation to return a list of `Customer`s. A customer is represented by a `name`, and  owns a loyalty `Card` that contains an `identifier`. From a given `Card`, one can retrieve the associated `Customer` by following the `owner` relationship.

<p align="center">
  <img src="https://raw.githubusercontent.com/polytechnice-si/4A_ISA_WebServices_Cycle/master/cd.png"/>
</p>

From the consumer point of view, we want to call the `listCustomers` operation, and display the results on the command line. Based on the contract exposed by the server ([DemoCycle.wsdl](https://github.com/polytechnice-si/4A_ISA_WebServices_Cycle/blob/master/clients/src/main/resources/DemoCycle.wsdl)), we generate the stubs, and the main code is straightforward:

```java
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
```

When invoked, this call triggers an exceptional behaviour, on the web service side. The automatic marshalling process does not work, as it detects a infinite cycle of marshalling from `Customer` to `Card` to `Customer` to `Card` to ... 

```
azrael:clients mosser$ mvn exec:java -q
* Retrieving Customers
[ERROR] Failed to execute goal org.codehaus.mojo:exec-maven-plugin:1.5.0:java (default-cli) on
project ws-cycle-client: An exception occured while executing the Java class. null:
InvocationTargetException: Client received SOAP Fault from server: Marshalling Error: A cycle is
detected in the object graph. This will cause infinitely deep XML: cycle.Customer@63046c09 ->
cycle.Card@94cf10 -> cycle.Customer@63046c09 Please see the server log to find more detail regarding
exact cause of the failure. -> [Help 1]
```

## Alternative Implementations

### Breaking up the cycle (@XmlTransient)

The immediate way to solve this problem is to make the cycle disappear. Considering that the entry point is the `Customer` business object, we can consider it as the root of our domain model, and transform the graph into a tree by cutting the `owner` relationship. This is done thanks to the `@XmlTransient` annotation, which tells the marshalling process to consider the annotated property (public attribute or get/set couple) as a volatile one.

```java
public class Card {

	// ...

    private Customer owner;
    
    @XmlTransient
    public Customer getOwner() { return owner; }
    public void setOwner(Customer owner) { this.owner = owner; }
}
```
As a consequence, in the WSDL contract, the `Card` data type does not include a reference to the `Customer` concept.

```xml
<xs:complexType name="customer">
  <xs:sequence>
    <xs:element minOccurs="0" name="card" type="tns:card"/>
    <xs:element minOccurs="0" name="name" type="xs:string"/>
  </xs:sequence>
</xs:complexType>

<xs:complexType name="card">
  <xs:sequence>
    <xs:element minOccurs="0" name="identifier" type="xs:string"/>
  </xs:sequence>
</xs:complexType>
```

As there is no more cycle, we can invoke the Web Service quite easily. But one must notice that it is now clearly impossible to come back from a given card to its owner.

```java
public static void main(String[] args) {
    DemoService service = new DemoService();
    Demo port = service.getDemoPort();
    System.out.println("** Retrieving Customers");
    for (Customer customer : port.listCustomers()) {
        System.out.println("  - " + customer.getName() + " - " + customer.getCard().getIdentifier());
    }
    System.out.println("** done");
}
```

We override the `main` property to start the associated Main method:

```
azrael:clients mosser$ mvn exec:java -q -Dmain="mains.MainXmlTransient"
** Retrieving Customers
  - Jacques - c79117c3-7620-4bcb-9963-cf1c859687a5
  - Alison - bf978bf5-3c19-4157-9aca-0ceb5adad76d
  - Pierre - 0159af7b-b192-4660-ae10-d5dcf0da7a30
  - Franck - 3d3a826c-b43c-49d8-801d-a82fc10b8360
  - Laura - d0a851c1-7a41-4af2-958d-8affad2fbf88
** done
```

Summary:

  - Pros: Easy to implement, straightforward to use;
  - Cons: The data model is now a tree instead of a graph.

### Using IDs to model references (@XmlID, @XmlIDREF)

To remove the cons of the previous solution, we need to play with the mechanisms provided by XML to model graphs (considering that an XML document is intrinsically a tree).

### Using Data Transfer Objects (DTOs)

## Bonus: Automating Stub generation with maven

