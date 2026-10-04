package com.lankawings.util;

import java.util.regex.Pattern;

/** Validation rules used by Booking Management and Travel-Agent booking. */
public final class Validator {
    private Validator() {}
    private static final Pattern NAME = Pattern.compile("^[\\p{L}\\p{M}][\\p{L}\\p{M} .'\\-]+$");
    private static final Pattern PASSPORT = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9\\-]{3,19}$");
    private static final Pattern SEAT = Pattern.compile("^(?:[1-9]|[1-9][0-9]|1[0-9]{2})[A-Fa-f]$");

    public static String text(String v,String label,int min,int max){
        if(v==null) v=""; v=v.strip().replaceAll("\\s{2,}"," ");
        if(v.length()<min) throw new ValidationException(label+" is required.");
        if(v.length()>max) throw new ValidationException(label+" is too long (maximum "+max+" characters).");
        return v;
    }
    public static String fullName(String v,String label){
        v=text(v,label,2,80);
        if(!NAME.matcher(v).matches()) throw new ValidationException(label+" may contain only letters, spaces and . ' -");
        return v;
    }
    public static String passport(String v){
        v=text(v,"Passport / ID",4,20).toUpperCase();
        if(!PASSPORT.matcher(v).matches()) throw new ValidationException("Passport / ID must contain only letters, numbers or dashes.");
        return v;
    }
    public static String seat(String v){
        v=text(v,"Seat",2,4).toUpperCase();
        if(!SEAT.matcher(v).matches()) throw new ValidationException("Seat must look like 1A, 12C or 105F.");
        return v;
    }
    public static int seatRows(int totalSeats){ return (int)Math.ceil(totalSeats/6.0); }
    public static void seatInLayout(String seat,int totalSeats){
        seat=seat(seat); int row=Integer.parseInt(seat.substring(0,seat.length()-1)); char col=seat.charAt(seat.length()-1);
        int pos=(row-1)*6+(col-'A')+1;
        if(pos<1||pos>totalSeats) throw new ValidationException("Seat "+seat+" is outside this aircraft's seat layout.");
    }
    public static int id(String raw,String label){
        try { int v=Integer.parseInt(raw); if(v<=0) throw new NumberFormatException(); return v; }
        catch(Exception e){ throw new ValidationException("Invalid "+label+"."); }
    }
}
