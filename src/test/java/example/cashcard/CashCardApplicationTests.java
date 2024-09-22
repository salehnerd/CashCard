package example.cashcard;

import com.jayway.jsonpath.DocumentContext;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import static org.assertj.core.api.Assertions.assertThat;
import java.net.URI;

//This will start our Spring Boot application and make it available for our test to perform requests to it.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CashCardApplicationTests {
	
	//We've asked Spring to inject a test helper that’ll allow us to make HTTP requests to the locally running application.
//	/Note: Even though @Autowired is a form of Spring dependency injection, it’s best used only in tests. Don't worry, we'll discuss this in more detail later.
    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void shouldReturnACashCardWhenDataIsSaved()
    
    {
    	//Here we use restTemplate to make an HTTP GET request to our application endpoint /cashcards/99.
        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/99", String.class);
        //restTemplate will return a ResponseEntity, which we've captured in a variable we've named 
        //response. ResponseEntity is another helpful Spring object that provides valuable information 
        //about what happened with our request. We'll use this information throughout our tests in this course
        
        
        //We can inspect many aspects of the response, including the HTTP Response Status code, which we expect to be 200 OK.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        //This converts the response String into a JSON-aware object with lots of helper methods.
        DocumentContext documentContext = JsonPath.parse(response.getBody());
        
        //We expect that when we request a Cash Card with id of 99 a JSON object will be returned with 
        //something in the id field. For now, assert that the id is not null.
        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(99);
        
        Double amount = documentContext.read("$.amount");
        assertThat(amount).isEqualTo(123.45);
    }
    
    
    @Test
    void shouldNotReturnACashCardWithAnUnknownId() {
      ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/1000", String.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
      assertThat(response.getBody()).isBlank();
    }
    
    @Test
    void shouldCreateANewCashCard() {
    	//The database will create and manage all unique CashCard.id values for us. We shouldn't provide one.
        CashCard newCashCard = new CashCard(null, 250.00);
        //This is very similar to restTemplate.getForEntity, but we must also provide newCashCard data for the new CashCard.
        //In addition, and unlike restTemplate.getForEntity, we don't expect a CashCard to be returned to us, so we expect a Void response body.
        ResponseEntity<Void> createResponse = restTemplate.postForEntity("/cashcards", newCashCard, Void.class);
        //According to the official specification: "the origin server SHOULD send a 201 (Created) response ... We now expect the HTTP response status code to be 201 CREATED, which is semantically correct if our API creates a new CashCard from our request."
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        //The official spec continues to state the following:"send a 201 (Created) response containing a Location header field that provides an identifier for the primary resource created"
        //In other words, when a POST request results in the successful creation of a resource, such as a new CashCard, 
        //the response should include information for how to retrieve that resource. We'll do this by supplying a URI in a Response Header named "Location".
        //Note that URI is indeed the correct entity here and not a URL; a URL is a type of URI, while a URI is more generic.
        URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
        //Finally, we'll use the Location header's information to fetch the newly created CashCard.
        ResponseEntity<String> getResponse = restTemplate.getForEntity(locationOfNewCashCard, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        
     // Add assertions such as these
        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        Double amount = documentContext.read("$.amount");

        assertThat(id).isNotNull();
        assertThat(amount).isEqualTo(250.00);
     
}
    
    	
    
    
}