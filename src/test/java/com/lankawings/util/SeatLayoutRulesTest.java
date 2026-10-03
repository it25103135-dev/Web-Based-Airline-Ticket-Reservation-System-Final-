package com.lankawings.util;
import org.junit.Test;import static org.junit.Assert.*;
public class SeatLayoutRulesTest{
 @Test public void validSeatWithinLayout(){Validator.seatInLayout("12F",72);assertEquals(12,Validator.seatRows(72));}
 @Test(expected=ValidationException.class) public void rejectsSeatOutsideLayout(){Validator.seatInLayout("13A",72);}
 @Test(expected=ValidationException.class) public void rejectsMalformedSeat(){Validator.seat("0A");}
}
