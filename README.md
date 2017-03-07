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
azrael:polytech mosser$ mvn tomee:run
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

**Remark**: The cyclic marshalling problem is described here (and addressed) in terms of XML documents, but this is not an XML-based issue. Same solutions can be applied when dealing with other pivot representation (_e.g._, JSON).

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
azrael:clients mosser$ mvn exec:java -q -Dmain=mains.MainXmlTransient
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
  - Cons: The data model is now a tree on client side, instead of a graph on server side.

### Using IDs to model references (@XmlID, @XmlIDREF)

To remove the cons of the previous solution, we need to play with the mechanisms provided by XML to model graphs (considering that an XML document is intrinsically a tree). The XML Schema standard defined two concepts for this very purpose:

  - `ID`: _Identifiers defined using this datatype are global to a document and provide a way to uniquely identify their containing element, whatever its type and name is._ ([source](http://books.xmlschemata.org/relaxng/ch19-77151.html))
  - `IDREF`: _The `xsd:IDREF` datatype defines references to the identifiers defined by the ID datatype_ ([source](http://books.xmlschemata.org/relaxng/ch19-77159.html))

Using these two concepts, we can replace the containment relation that exists between a card and its owner by a reference. The `Customer` must be uniquely identified (in out case, by its `name`), and the `Card` will use a reference when marshalled.

```java
public class Customer {

    private  String name;

    @XmlID
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    // ...
}

public class Card {

    private Customer owner;

    @XmlIDREF
    public Customer getOwner() { return owner; }
    public void setOwner(Customer owner) { this.owner = owner; }
    
    // ...
}
```

Using these annotations, the data structure in the WSDL contract are modified as the following:

```xml
<xs:complexType name="customer">
  <xs:sequence>
    <xs:element minOccurs="0" name="card" type="tns:card"/>
    <xs:element minOccurs="0" name="name" type="xs:ID"/>
  </xs:sequence>
</xs:complexType>

<xs:complexType name="card">
  <xs:sequence>
    <xs:element minOccurs="0" name="identifier" type="xs:string"/>
    <xs:element minOccurs="0" name="owner" type="xs:IDREF"/>
  </xs:sequence>
</xs:complexType>
```

Using this contract, the stubs allows one to write the following code to address the service. One must notice that the _referenced_ relationship is not statically typed, and point to an `Object`. It is up to the consumer on the client side to know which concept is referenced and to perform the associated cast.

```java
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
```
We override the `main` property to start the associated Main method:

```
azrael:clients mosser$ mvn exec:java -q -Dmain=mains.MainXmlRef
** Retrieving Customers
  - Jacques - c3ea67e0-0717-4422-a50b-96475cc99225
    - c3ea67e0-0717-4422-a50b-96475cc99225 --> Jacques
  - Alison - 2bbcec19-5714-4d09-9466-44ee7d12e333
    - 2bbcec19-5714-4d09-9466-44ee7d12e333 --> Alison
  - Pierre - 3ac03f2c-d35c-40d8-8a24-a36789f23138
    - 3ac03f2c-d35c-40d8-8a24-a36789f23138 --> Pierre
  - Franck - f25096dc-85bb-40b9-b52f-b9ff67f60c33
    - f25096dc-85bb-40b9-b52f-b9ff67f60c33 --> Franck
  - Laura - ee5e40e5-270c-4850-997e-67df4f0d8b1f
    - ee5e40e5-270c-4850-997e-67df4f0d8b1f --> Laura
** done
```

Summary:

  - Pros: The object model is kept the same on the two side of the communication;
  - Cons: Need to expose a unique identifier (subtype of string) in the referenced class, and losing typing capabilities for the referencer.

### Using Data Transfer Objects (DTOs)

The second solution tackles a lot of issues triggered by the first one, but also adds tough constraints to the data model (global identifier uniqueness). The first solution was easy to implement, but does not allow one to really control the shape of the tree in complex cases.

Martin Fowler identified in _Patterns of Enterprise Application Architecture_ (2003) the [_Data Transfer Object_](https://martinfowler.com/eaaCatalog/dataTransferObject.html) pattern to tackle this challenge: _The solution is to create a Data Transfer Object that can hold all the data for the call. It needs to be serializable to go across the connection. Usually an assembler is used on the server side to transfer data between the DTO and any domain objects._

In order to control the way the object graph will be transformed into something serialisable (_i.e._, that can be marshalled), we simply build the associated data structure to support the _data transfer_).  In our case, we create a `CustomerWithCard` concept, to be returned by the service operation.

```java
public class CustomerWithCard {

    private String name;
    private String cardId;

    public CustomerWithCard() {}

    public CustomerWithCard(Customer cu) {
        this.name = cu.getName();
        this.cardId = cu.getCard().getIdentifier();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCardId() { return cardId; }
    public void setCardId(String cardId) { this.cardId = cardId; }
}
```

The XSD transformation is straightforward:

```xml
<xs:complexType name="customerWithCard">
  <xs:sequence>
    <xs:element minOccurs="0" name="cardId" type="xs:string"/>
    <xs:element minOccurs="0" name="name" type="xs:string"/>
  </xs:sequence>
</xs:complexType>
```

And the associated usage is also straightforward:

```java
public static void main(String[] args) {
  DemoService service = new DemoService();
  Demo port = service.getDemoPort();
  System.out.println("** Retrieving Customers");
  for (CustomerWithCard dto : port.listCustomers()) {
    System.out.println("  - " + dto.getName() + " - " + dto.getCardId());
  }
  System.out.println("** done");
}
```

Calling it from the command-line:

```
azrael:clients mosser$ mvn exec:java -q -Dmain=mains.MainDataTransferObject
** Retrieving Customers
  - Jacques - ef91624c-b4e4-4722-98b5-d41faf0e8bb1
  - Alison - bb564751-cda0-4a17-890e-8195f927de70
  - Pierre - 46e35f12-e6ce-417a-b95c-d1ac195168a0
  - Franck - 7f0649ea-6e47-43cf-86bd-56e44732c056
  - Laura - ff247833-57df-4db2-abb2-52bd4a3ea785
** done
```

Summary:

  - Pros: Allows one to control the way data are exchanged for a given operation; 
  - Cons: Need to create and maintain dedicated classes just for transferring data.

## Bonus: Automating stub generation with maven

One must not apply version control to generated sources. This is the case for the Java code associated to the WSDL contracts. To prevent git to track these files, we add a `.gitignore` descriptor in the `clients` module, containing the prefix to ignore.

```
src/main/java/stubs/
```

We can now erase these files from the disk, and instead asks Maven to call the `wsdl2java` code generator on our behalf (in the `pom.xmnl` descriptor). 

We use the `cxf-codegen-plugin` to support this task. The following configuration asks the plugin to process `DemoCycle.wsdl`, and to put the classes associated to the XML namespace `http://www.polytech.unice.fr/si/4a/isa/demo/cycle/` into a package named `stubs.cycle`. The configuration is hooked to the `generate-sources` step of the maven build process, which is triggered before the source compilation.

```xml
<plugin>
  <groupId>org.apache.cxf</groupId>
  <artifactId>cxf-codegen-plugin</artifactId>
  <version>${cxf.version}</version>
  <executions>
    <execution>
      <id>generate-sources</id>
      <phase>generate-sources</phase>
      <configuration>
        <sourceRoot>${basedir}/src/main/java/</sourceRoot>
        <wsdlOptions>
          <wsdlOption>
            <wsdl>${basedir}/src/main/resources/DemoCycle.wsdl</wsdl>
            <extraargs>
              <extraarg>-p</extraarg>
              <extraarg>http://www.polytech.unice.fr/si/4a/isa/demo/cycle/=stubs.cycle</extraarg>
            </extraargs>
          </wsdlOption>
        </wsdlOptions>
      </configuration>
      <goals>
        <goal>wsdl2java</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

With this configuration, one can notice that the plugin is called after the `default-clean` step and before the `default-resource` one. If one wants to add other WSDL contracts to process, simply add more `wsdlOption` nodes in the configuration file.

```
azrael:clients mosser$ mvn clean package
[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building ISA :: Cyclic References :: Remote Clients 1.0-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ ws-cycle-client ---
[INFO] Deleting /Users/mosser/work/polytech/4A_ISA_WebServices_Cycle/clients/target
[INFO] 
[INFO] --- cxf-codegen-plugin:3.1.10:wsdl2java (generate-sources) @ ws-cycle-client ---
[INFO] 
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ ws-cycle-client ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 4 resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ ws-cycle-client ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 35 source files to /Users/mosser/work/polytech/4A_ISA_WebServices_Cycle/clients/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ ws-cycle-client ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/mosser/work/polytech/4A_ISA_WebServices_Cycle/clients/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.1:testCompile (default-testCompile) @ ws-cycle-client ---
[INFO] No sources to compile
[INFO] 
[INFO] --- maven-surefire-plugin:2.12.4:test (default-test) @ ws-cycle-client ---
[INFO] No tests to run.
[INFO] 
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ ws-cycle-client ---
[INFO] Building jar: /Users/mosser/work/polytech/4A_ISA_WebServices_Cycle/clients/target/ws-cycle-client-1.0-SNAPSHOT.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 4.423 s
[INFO] Finished at: 2017-03-07T15:36:06+01:00
[INFO] Final Memory: 25M/298M
[INFO] ------------------------------------------------------------------------
```
