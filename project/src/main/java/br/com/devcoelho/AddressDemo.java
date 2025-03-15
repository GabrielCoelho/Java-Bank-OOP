package br.com.devcoelho;

/** Demonstration class showing how to use the ViaCEP integration */
public class AddressDemo {

  public static void main(String[] args) {
    // Create a new address
    Address address = new Address();

    // Validate and fill address information using a CEP
    boolean isValid = address.validateAndFillAddressByCep("13846-049");

    if (isValid) {
      System.out.println("CEP validation successful!");

      // Set the house number (not provided by ViaCEP)
      address.setHouseNumber("500");

      // Set the address type
      AddressType addressType = new AddressType();
      addressType.setAddressType("RESIDENTIAL");
      addressType.setAddressLocationType("HOME");
      address.setAddressLocationT(addressType);

      // Print the formatted address
      System.out.println("Formatted Address: " + address.getFormattedAddress());

      // Print detailed address information
      System.out.println("\nDetailed Address Information:");
      System.out.println("Street: " + address.getAddress());
      System.out.println("Number: " + address.getHouseNumber());
      System.out.println("Complement: " + address.getHouseComplement());
      System.out.println("Neighborhood: " + address.getNeighborhood());
      System.out.println("City: " + address.getCityName());
      System.out.println(
          "State: "
              + address.getState().getFullName()
              + " ("
              + address.getState().getAbbreviation()
              + ")");
      System.out.println("CEP: " + address.getCepNumber());

      // Create a person and associate the address
      Person person = new Person();
      person.setName("João Silva");
      person.setCpf("123.456.789-00");
      person.getAddress().add(address);

      System.out.println("\nPerson Information:");
      System.out.println("Name: " + person.getName());
      System.out.println("CPF: " + person.getCpf());
      System.out.println("Number of addresses: " + person.getAddress().size());
    } else {
      System.out.println("Invalid CEP! Please provide a valid Brazilian postal code.");
    }
  }
}
