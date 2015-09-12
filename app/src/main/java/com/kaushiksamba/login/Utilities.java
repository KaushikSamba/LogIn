package com.kaushiksamba.login;

public class Utilities
{
    public static String url_raffle="https://a6f79872.ngrok.io/raffleapi.php";
    public static String f_id;
    public static String f_pass;
    public static String fb_name;
    public static String fb_id;
    public static int status;
        //Status 0 - Not logged into the app (Festember registration)
        //Status 1 - Logged into the app (Festember registration)
        //Status 2 - Logged into the app + Facebook registration
        //Status 3 - Logged into the app + Facebook registration + Facebook shared
        //Status 4 - Logged into the app + Logged out of FB + 1 coupon received.
        //Status 5 - Logged into the app + Logged out of FB + 6 coupons received.
    public static String[] coupons = new String[11];
}
