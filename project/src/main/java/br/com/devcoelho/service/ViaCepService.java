package br.com.devcoelho.service;

import br.com.devcoelho.Address;
import br.com.devcoelho.BrazilianState;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

/**
 * Service class responsible for interacting with the ViaCEP API to validate and retrieve address
 * information based on a CEP (Brazilian postal code)
 */
public class ViaCepService {

  private static final String VIA_CEP_API_URL = "https://viacep.com.br/ws/%s/json/";

  /**
   * Validates and retrieves address information for a given CEP
   *
   * @param cep the CEP to validate and retrieve information for (format: 00000000 or 00000-000)
   * @return Address object with populated data if successful, null otherwise
   */
  public Address getAddressByCep(String cep) {
    // Remove any non-numeric characters from the CEP
    String numericCep = cep.replaceAll("\\D", "");

    // Validate CEP format (must be 8 digits)
    if (numericCep.length() != 8) {
      return null;
    }

    try {
      // Format the URL with the cleaned CEP
      String apiUrl = String.format(VIA_CEP_API_URL, numericCep);
      URL url = new URL(apiUrl);

      // Open connection
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(5000);

      // Check for successful response
      int responseCode = connection.getResponseCode();
      if (responseCode != 200) {
        return null;
      }

      // Read response
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(connection.getInputStream()));
      StringBuilder response = new StringBuilder();
      String line;

      while ((line = reader.readLine()) != null) {
        response.append(line);
      }
      reader.close();

      // Parse JSON response
      JSONObject jsonResponse = new JSONObject(response.toString());

      // Check if there's an error in the response
      if (jsonResponse.has("erro") && jsonResponse.getBoolean("erro")) {
        return null;
      }

      // Create and populate Address object
      return mapJsonToAddress(jsonResponse, formatCep(numericCep));

    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Maps the JSON response from ViaCEP to an Address object
   *
   * @param json the JSON response from ViaCEP
   * @param formattedCep the formatted CEP (with hyphen)
   * @return populated Address object
   */
  private Address mapJsonToAddress(JSONObject json, String formattedCep) {
    Address address = new Address();

    // Set address data from JSON response
    address.setAddress(json.getString("logradouro"));
    address.setNeighborhood(json.getString("bairro"));
    address.setCityName(json.getString("localidade"));
    address.setState(BrazilianState.fromAbbreviation(json.getString("uf")));
    address.setCepNumber(formattedCep);

    // Some fields aren't populated by the API
    address.setHouseNumber(""); // Number needs to be provided by the user
    address.setHouseComplement(json.optString("complemento", "")); // May be empty

    return address;
  }

  /**
   * Formats a numeric CEP by adding a hyphen (00000-000)
   *
   * @param numericCep the numeric CEP (8 digits)
   * @return formatted CEP with hyphen
   */
  private String formatCep(String numericCep) {
    return numericCep.substring(0, 5) + "-" + numericCep.substring(5);
  }
}
