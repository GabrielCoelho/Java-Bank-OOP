package br.com.devcoelho;

/** Enum representing all Brazilian states (UFs - Unidades Federativas) */
public enum BrazilianState {
  AC("Acre"),
  AL("Alagoas"),
  AP("Amapá"),
  AM("Amazonas"),
  BA("Bahia"),
  CE("Ceará"),
  DF("Distrito Federal"),
  ES("Espírito Santo"),
  GO("Goiás"),
  MA("Maranhão"),
  MT("Mato Grosso"),
  MS("Mato Grosso do Sul"),
  MG("Minas Gerais"),
  PA("Pará"),
  PB("Paraíba"),
  PR("Paraná"),
  PE("Pernambuco"),
  PI("Piauí"),
  RJ("Rio de Janeiro"),
  RN("Rio Grande do Norte"),
  RS("Rio Grande do Sul"),
  RO("Rondônia"),
  RR("Roraima"),
  SC("Santa Catarina"),
  SP("São Paulo"),
  SE("Sergipe"),
  TO("Tocantins");

  private final String fullName;

  BrazilianState(String fullName) {
    this.fullName = fullName;
  }

  /**
   * Returns the full name of the state
   *
   * @return the full state name
   */
  public String getFullName() {
    return fullName;
  }

  /**
   * Returns the state abbreviation (UF)
   *
   * @return the state abbreviation
   */
  public String getAbbreviation() {
    return this.name();
  }

  /**
   * Returns a BrazilianState based on its abbreviation
   *
   * @param abbreviation state abbreviation (UF)
   * @return the corresponding BrazilianState or null if not found
   */
  public static BrazilianState fromAbbreviation(String abbreviation) {
    if (abbreviation == null || abbreviation.isEmpty()) {
      return null;
    }

    try {
      return BrazilianState.valueOf(abbreviation.toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Returns a BrazilianState based on its full name
   *
   * @param fullName state full name
   * @return the corresponding BrazilianState or null if not found
   */
  public static BrazilianState fromFullName(String fullName) {
    if (fullName == null || fullName.isEmpty()) {
      return null;
    }

    for (BrazilianState state : BrazilianState.values()) {
      if (state.getFullName().equalsIgnoreCase(fullName)) {
        return state;
      }
    }

    return null;
  }
}
