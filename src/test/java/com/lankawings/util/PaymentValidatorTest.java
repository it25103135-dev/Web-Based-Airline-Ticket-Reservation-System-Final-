package com.lankawings.util;
import org.junit.Test;
import static org.junit.Assert.*;
public class PaymentValidatorTest {
 @Test public void approvedDemoCardsPassLuhn(){assertEquals("4242424242424242",Validator.cardNumber("4242 4242 4242 4242","Visa"));assertEquals("5555555555554444",Validator.cardNumber("5555 5555 5555 4444","Mastercard"));}
 @Test public void wrongBrandIsRejected(){assertThrows(ValidationException.class,()->Validator.cardNumber("4242424242424242","Mastercard"));}
 @Test public void cvvMustBeThreeDigits(){assertThrows(ValidationException.class,()->Validator.cvv("12"));assertEquals("123",Validator.cvv("123"));}
}
