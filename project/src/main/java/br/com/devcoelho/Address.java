package br.com.devcoelho;

/** Address */
public class Address {

  private String address;
  private String houseNumber;
  private String houseComplement;
  private String neighborhood;
  private String cityName;
  private BrazilianState state;
  private String cepNumber;
  private AddressType addressLocationT;

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getHouseNumber() {
    return houseNumber;
  }

  public void setHouseNumber(String houseNumber) {
    this.houseNumber = houseNumber;
  }

  public String getHouseComplement() {
    return houseComplement;
  }

  public void setHouseComplement(String houseComplement) {
    this.houseComplement = houseComplement;
  }

  public String getNeighborhood() {
    return neighborhood;
  }

  public void setNeighborhood(String neighborhood) {
    this.neighborhood = neighborhood;
  }

  public String getCityName() {
    return cityName;
  }

  public void setCityName(String cityName) {
    this.cityName = cityName;
  }

  public AddressType getAddressLocationT() {
    return addressLocationT;
  }

  public void setAddressLocationT(AddressType addressLocationT) {
    this.addressLocationT = addressLocationT;
  }

  public BrazilianState getState() {
    return state;
  }

  public void setState(BrazilianState state) {
    this.state = state;
  }

  public String getCepNumber() {
    return cepNumber;
  }

  public void setCepNumber(String cepNumber) {
    this.cepNumber = cepNumber;
  }

  /**
   * Returns a formatted address string
   *
   * @return the complete formatted address
   */
  public String getFormattedAddress() {
    StringBuilder formattedAddress = new StringBuilder();

    formattedAddress.append(address);

    if (houseNumber != null && !houseNumber.isEmpty()) {
      formattedAddress.append(", ").append(houseNumber);
    }

    if (houseComplement != null && !houseComplement.isEmpty()) {
      formattedAddress.append(", ").append(houseComplement);
    }

    if (neighborhood != null && !neighborhood.isEmpty()) {
      formattedAddress.append(" - ").append(neighborhood);
    }

    if (cityName != null && !cityName.isEmpty()) {
      formattedAddress.append(", ").append(cityName);
    }

    if (state != null) {
      formattedAddress.append("/").append(state.getAbbreviation());
    }

    if (cepNumber != null && !cepNumber.isEmpty()) {
      formattedAddress.append(" - CEP: ").append(cepNumber);
    }

    return formattedAddress.toString();
  }
}
