package br.com.devcoelho;

import java.util.ArrayList;
import java.util.List;

/** Person */
public class Person {

  private String name;
  private List<Address> address = new ArrayList<>();
  private String cpf;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Address> getAddress() {
    return address;
  }

  public void setAddress(List<Address> address) {
    this.address = address;
  }

  public String getCpf() {
    return cpf;
  }

  public void setCpf(String cpf) {
    this.cpf = cpf;
  }
}
