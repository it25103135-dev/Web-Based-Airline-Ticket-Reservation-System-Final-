package com.lankawings.util;

import java.time.YearMonth;
import java.util.Set;
import java.util.regex.Pattern;

/** Validation rules retained only for identifiers and Member 1 card/payment input. */
public final class Validator {
    private Validator() {}
    private static final Pattern NAME=Pattern.compile("^[\\p{L}\\p{M}][\\p{L}\\p{M} .'\\-]+$");
    private static final Pattern EXPIRY=Pattern.compile("^(0[1-9]|1[0-2])/(\\d{2})$");
    private static final Pattern CVV=Pattern.compile("^\\d{3}$");

    public static int id(String raw,String label){
        try{int n=Integer.parseInt(raw==null?"":raw.trim());if(n<=0)throw new NumberFormatException();return n;}
        catch(NumberFormatException e){throw new ValidationException("Invalid "+label+" ID.");}
    }
    public static String oneOf(String v,String label,String... allowed){
        for(String a:allowed) if(a.equals(v)) return v;
        throw new ValidationException("Choose a valid "+label+".");
    }
    private static String text(String v,String label,int min,int max){
        v=v==null?"":v.trim(); if(v.length()<min)throw new ValidationException(label+" is required."); if(v.length()>max)throw new ValidationException(label+" is too long."); return v;
    }
    public static String cardHolder(String v){
        v=text(v,"Cardholder name",2,60); if(!NAME.matcher(v).matches())throw new ValidationException("Cardholder name may contain only letters, spaces and . ' -"); return v;
    }
    public static String cardBrand(String d){
        if(d.startsWith("4"))return "VISA";
        if(d.length()>=2){int two=Integer.parseInt(d.substring(0,2));if(two>=51&&two<=55)return "MASTERCARD";}
        if(d.length()>=4){int four=Integer.parseInt(d.substring(0,4));if(four>=2221&&four<=2720)return "MASTERCARD";}
        return "UNKNOWN";
    }
    public static boolean luhn(String d){int sum=0;boolean dbl=false;for(int i=d.length()-1;i>=0;i--){int n=d.charAt(i)-'0';if(dbl){n*=2;if(n>9)n-=9;}sum+=n;dbl=!dbl;}return sum%10==0;}
    public static String cardNumber(String raw,String method){
        if(raw==null||raw.isBlank())throw new ValidationException("Card number is required.");
        String d=raw.replaceAll("[\\s\\-]",""); if(!d.matches("\\d+"))throw new ValidationException("Card number can contain digits only.");
        if(d.length()!=16)throw new ValidationException("Card number must be exactly 16 digits.");
        if(!luhn(d))throw new ValidationException("This card number is not valid. Please check it for typing mistakes.");
        String brand=cardBrand(d); if("Visa".equals(method)&&!"VISA".equals(brand))throw new ValidationException("This is not a Visa card number. Choose the correct payment method.");
        if("Mastercard".equals(method)&&!"MASTERCARD".equals(brand))throw new ValidationException("This is not a Mastercard number. Choose the correct payment method.");
        if("UNKNOWN".equals(brand))throw new ValidationException("Only Visa and Mastercard cards are accepted."); return d;
    }
    public static YearMonth expiry(String raw){
        var m=EXPIRY.matcher(raw==null?"":raw.trim()); if(!m.matches())throw new ValidationException("Enter the expiry date as MM/YY, e.g. 08/28.");
        YearMonth ym=YearMonth.of(2000+Integer.parseInt(m.group(2)),Integer.parseInt(m.group(1)));YearMonth now=YearMonth.now();
        if(ym.isBefore(now))throw new ValidationException("This card has expired."); if(ym.isAfter(now.plusYears(12)))throw new ValidationException("Expiry date is too far in the future."); return ym;
    }
    public static String cvv(String v){if(v==null||!CVV.matcher(v.trim()).matches())throw new ValidationException("CVV must be exactly 3 digits.");return v.trim();}
}
