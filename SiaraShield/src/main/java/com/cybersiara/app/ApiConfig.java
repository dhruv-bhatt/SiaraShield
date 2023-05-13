package com.cybersiara.app;

public class ApiConfig {

    static String SERVICE_URL = "https://embed.mycybersiara.com/api/";
    public static String Login_URL = SERVICE_URL+"CyberSiara/GetCyberSiaraForAndroid";  // URL
    public static String GENERATE_CAPTCHA_URL = SERVICE_URL+"GenerateCaptcha/CaptchaForAndroid";
    public static String SUBMIT_CAPTCHA_URL =  SERVICE_URL+"SubmitCaptcha/VerifiedSubmitForAndroid";// URL
    public static String CAPTCHA_VERIFY_URL =  SERVICE_URL+"SubmitCaptcha/SubmitCaptchInfoForAndroid";// URL
    public static String VALIDATE_TOKEN_URL =  SERVICE_URL+"validate-token";// URL
//    public static String MASTER_URL =  "kNKLUO9OyUNd4y1azl7TFFH8hI0zyzYh";
//    public static String AUTH_KEY =  "jwqIM6PQ8YVfy9HOgQuRjW6OCyX0dAGS";

}
