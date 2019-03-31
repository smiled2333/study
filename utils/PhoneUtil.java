
package com.example.demo.phoneUtil;
 
import java.util.Locale;
 
import com.google.i18n.phonenumbers.PhoneNumberToCarrierMapper;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;
/**
 * 手机号归属地查询
 * 依赖jar包：com.googlecode.libphonenumber(Libphonenumber、Geocoder、Prefixmapper、Carrier)
 * 开发时间：2015-11-24
 *
 */
public class PhoneUtil {
 
    private static PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
 
    private static PhoneNumberToCarrierMapper carrierMapper = PhoneNumberToCarrierMapper.getInstance();
 
    private static PhoneNumberOfflineGeocoder geocoder = PhoneNumberOfflineGeocoder.getInstance();
 
 
    public static void main(String[] args) {
        boolean b = checkPhoneNumber("13500000001", "86");
        System.out.println(b);
        String carrier = getCarrier("13500000001", "86");
        System.out.println(carrier);
        String geo = getGeo("13500000001", "86");
        System.out.println(geo);
    }
 
 
    /**
     * 根据国家代码和手机号  判断手机号是否有效
     * @param phoneNumber
     * @param countryCode
     * @return
     */
    public static boolean checkPhoneNumber(String phoneNumber, String countryCode){
 
        int ccode = Integer.valueOf(countryCode);
        long phone = Long.valueOf(phoneNumber);
 
        PhoneNumber pn = new PhoneNumber();
        pn.setCountryCode(ccode);
        pn.setNationalNumber(phone);
 
        return phoneNumberUtil.isValidNumber(pn);
 
    }
 
    /**
     * 根据国家代码和手机号  判断手机运营商
     * @param phoneNumber
     * @param countryCode
     * @return
     */
    public static String getCarrier(String phoneNumber, String countryCode){
 
        int ccode = Integer.valueOf(countryCode);
        long phone = Long.valueOf(phoneNumber);
 
        PhoneNumber pn = new PhoneNumber();
        pn.setCountryCode(ccode);
        pn.setNationalNumber(phone);
        //返回结果只有英文，自己转成成中文
        String carrierEn = carrierMapper.getNameForNumber(pn, Locale.ENGLISH);
        String carrierZh = "";
        carrierZh += geocoder.getDescriptionForNumber(pn, Locale.CHINESE);
        switch (carrierEn) {
            case "China Mobile":
                carrierZh += "移动";
                break;
            case "China Unicom":
                carrierZh += "联通";
                break;
            case "China Telecom":
                carrierZh += "电信";
                break;
            default:
                break;
        }
        return carrierZh;
    }
 
 
    /**
     *
     * @Description: 根据国家代码和手机号  手机归属地
     * @date 2015-7-13 上午11:33:18
     * @param @param phoneNumber
     * @param @param countryCode
     * @param @return    参数
     * @throws
     */
    public static String getGeo(String phoneNumber, String countryCode){
 
        int ccode = Integer.valueOf(countryCode);
        long phone = Long.valueOf(phoneNumber);
 
        PhoneNumber pn = new PhoneNumber();
        pn.setCountryCode(ccode);
        pn.setNationalNumber(phone);
        return geocoder.getDescriptionForNumber(pn, Locale.CHINESE);
    }
	/*
	<!-- https://mvnrepository.com/artifact/com.googlecode.libphonenumber/libphonenumber -->
<dependency>
    <groupId>com.googlecode.libphonenumber</groupId>
    <artifactId>libphonenumber</artifactId>
    <version>8.9.10</version>
</dependency>
<!-- https://mvnrepository.com/artifact/com.googlecode.libphonenumber/geocoder -->
<dependency>
    <groupId>com.googlecode.libphonenumber</groupId>
    <artifactId>geocoder</artifactId>
    <version>2.99</version>
</dependency>
<!-- https://mvnrepository.com/artifact/com.googlecode.libphonenumber/prefixmapper -->
<dependency>
    <groupId>com.googlecode.libphonenumber</groupId>
    <artifactId>prefixmapper</artifactId>
    <version>2.99</version>
</dependency>
<!-- https://mvnrepository.com/artifact/com.googlecode.libphonenumber/carrier -->
<dependency>
    <groupId>com.googlecode.libphonenumber</groupId>
    <artifactId>carrier</artifactId>
    <version>1.89</version>
</dependency>
	*/
}