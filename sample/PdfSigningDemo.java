package ro.certsign.paperless.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.DocumentException;
import com.sun.xml.ws.fault.ServerSOAPFaultException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.InvalidParameterException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.certsign.paperless.signatureproperties.PDFSignatureRectangle;
import ro.certsign.paperless.signatureproperties.PDFSignatureRendering;
import ro.certsign.paperless.signatureproperties.PDFSigningContextSettings;
import ro.certsign.paperless.signatureproperties.PDFVisibilityOptions;
import ro.certsign.paperless.signatureproperties.SignatureTypeEnum;
import ro.certsign.paperless.signatureprovider.IntermediateSignature;
import ro.certsign.paperless.signatureprovider.NoLtvInformationFoundException;
import ro.certsign.paperless.signatureprovider.PdfSignatureProvider;
import ro.certsign.paperless.signatureprovider.SignatureProviderFactory;
import ro.certsign.paperlessenrollmentservice._2016._05.EnrollmentService;
import ro.certsign.paperlessenrollmentservice._2016._05.EnrollmentServiceContract;
import ro.certsign.paperlessenrollmentservice._2016._05.EnrollmentServiceContractEnrollEnvelopedPkcs7UserEnrolmentFaultFaultFaultMessage;
import ro.certsign.paperlessenrollmentservice._2016._05.EnrollmentServiceContractEnrollEnvelopedPkcs7UserEnrolmentServiceFaultFaultFaultMessage;
import ro.certsign.paperlessenrollmentservice._2016._05.EnrollmentServiceContractEnrollUserEnrolmentFaultFaultFaultMessage;
import ro.certsign.paperlessenrollmentservice._2016._05.EnrollmentServiceContractEnrollUserEnrolmentServiceFaultFaultFaultMessage;
import ro.certsign.paperlesssigningservice._2016._05.SigningService;
import ro.certsign.paperlesssigningservice._2016._05.SigningServiceContract;
import ro.certsign.paperlesssigningservice._2016._05.SigningServiceContractAuthorizeSigningAuthorizationFaultFaultFaultMessage;
import ro.certsign.paperlesssigningservice._2016._05.SigningServiceContractAuthorizeSigningCertificateFaultFaultFaultMessage;
import ro.certsign.paperlesssigningservice._2016._05.SigningServiceContractAuthorizeSigningEnrolmentFaultFaultFaultMessage;
import ro.certsign.paperlesssigningservice._2016._05.SigningServiceContractAuthorizeSigningOtpRetryLimitReachedFaultFaultFaultMessage;
import ro.certsign.paperlesssigningservice._2016._05.SigningServiceContractAuthorizeSigningOtpTimeoutFaultFaultFaultMessage;
import ro.certsign.paperlesssigningservice._2016._05.SigningServiceContractAuthorizeSigningSigningServiceFaultFaultFaultMessage;
import ro.certsign.paperlesssigningservice._2016._05.SigningServiceContractAuthorizeSigningSigningSessionFaultFaultFaultMessage;
import ro.certsign.paperlesssigningservice._2016._05.SigningServiceContractGetGeneralTermsAndConditionsSigningServiceFaultFaultFaultMessage;
import ro.certsign.paperlesssigningservice._2016._05.SigningServiceContractInitiateSigningCertificateFaultFaultFaultMessage;
import ro.certsign.paperlesssigningservice._2016._05.SigningServiceContractInitiateSigningSigningServiceFaultFaultFaultMessage;
import ro.certsign.paperlesssigningservice._2016._05.SigningServiceContractInitiateSigningSigningSessionFaultFaultFaultMessage;
import ro.certsign.schemas.paperlessenrollmentservice._2016._05.EnrolmentRequest;
import ro.certsign.schemas.paperlesssigningservice._2016._05.ArrayOfDocumentInfo;
import ro.certsign.schemas.paperlesssigningservice._2016._05.AuthorizationRequest;
import ro.certsign.schemas.paperlesssigningservice._2016._05.DocumentInfo;
import ro.certsign.schemas.paperlesssigningservice._2016._05.DocumentSignature;
import ro.certsign.schemas.paperlesssigningservice._2016._05.SigningRequest;
import ro.certsign.schemas.paperlesssigningservice._2016._05.SigningResponse;
import ro.certsign.schemas.paperlesssigningservice._2016._05.TermsAndConditions;

public class PdfSigningDemo {

    private static Logger logger = LoggerFactory.getLogger(PdfSigningDemo.class);

    private static String phoneNumber = null;
    private static String firstName = null;
    private static String lastName = null;
    private static String country = null;
    private static String address = null;
    private static String email = null;
    private static String cnp = null;
    private static String locality = null;
    private static String apAddress = null;
    private static String blAddress = null;
    private static String nrAddress = null;
    private static String scaraAddress = null;
    private static String stradaAddress = null;
    private static String urlAddress = null;
    private static String group = null;
    private static String profile = null;
    private static Integer certificateValidity = null;

    private static String signingSession = null;

    private static String wsdlSigningServiceUrl = null;
    private static String wsdlEnrollmentServiceUrl = null;
    private static String filePathToBeSigned = null;
    private static boolean isLocal = true;

    public static IntermediateSignature intermediateSignature = null;

    public static void main(String[] args) {

        System.setProperty("javax.net.ssl.trustStore", "src/main/resources/paperless_certs");
        System.setProperty("javax.net.ssl.trustStorePassword", "paperless");
        System.setProperty("javax.net.ssl.keyStore", "src/main/resources/keystore.p12");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");
        System.setProperty("javax.net.ssl.keyStoreType", "pkcs12");

        phoneNumber = "0700000000";
        firstName = "Paluna";
        lastName = "Vasile";
        country = "RO";
        address = "ADDRESS";
        email = "paluna.vasile@gmail.com";
        cnp = "1520323421585";
        locality = "Bucharest";
        apAddress = "A";
        blAddress = "B";
        nrAddress = "N";
        scaraAddress = "A";
        stradaAddress = "S";
        urlAddress = "tets";
        group = "Group 1";
        profile = "VProfile CA 1";
        certificateValidity = 2628001;
        wsdlSigningServiceUrl = "https://rssdemo.certsign.ro/RSS.SigningService_01/SigningService.svc?singleWsdl";
        wsdlEnrollmentServiceUrl = "https://rssdemo.certsign.ro/RSS.EnrollmentService_01/EnrollmentService.svc?singleWsdl";
        filePathToBeSigned = "/test2.pdf";

        String dummyQcrId;
        TermsAndConditions generalTermsAndConditions = null;
        String option;
        EnrollmentService enrollmentSerice = null;
        SigningService signingService = null;

        //===============initiliazing services===============
        try {
            signingService = new SigningService(new URL(wsdlSigningServiceUrl), new QName("http://www.certsign.ro/PaperlessSigningService/2016/05", "SigningService"));
            enrollmentSerice = new EnrollmentService(new URL(wsdlEnrollmentServiceUrl), new QName("http://www.certsign.ro/PaperlessEnrollmentService/2016/05", "EnrollmentService"));

            SigningServiceContract basicHttpBindingSigningServiceContract = signingService.getBasicHttpBindingSigningServiceContract();
            EnrollmentServiceContract basicHttpBindingEnrollmentSigningServiceContract = enrollmentSerice.getBasicHttpBindingEnrollmentServiceContract();

            //===============normal enrollment===============
            //dummyQcrId = java.util.UUID.randomUUID().toString();
            //            try {
            //                sendEnrolmentRequest(basicHttpBindingEnrollmentSigningServiceContract, dummyQcrId);
            //                System.out.println("Enrollment finished succesfully");
            //            } catch (IOException ex) {
            //                logger.error(ex.getMessage());
            //            } catch (EnrollmentServiceContractEnrollUserEnrolmentFaultFaultFaultMessage ex) {
            //                logger.error(ex.getFaultInfo().getMessage());
            //            } catch (EnrollmentServiceContractEnrollUserEnrolmentServiceFaultFaultFaultMessage ex) {
            //                logger.error(ex.getFaultInfo().getMessage());
            //            } catch (ServerSOAPFaultException ex) {
            //                logger.error(ex.getMessage());
            //            }
            //===============enrollment using pkcs7===============
            dummyQcrId = java.util.UUID.randomUUID().toString();
            try {
                sendEnrolmentRequestPKCS7(basicHttpBindingEnrollmentSigningServiceContract, dummyQcrId);
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            } catch (EnrollmentServiceContractEnrollUserEnrolmentFaultFaultFaultMessage ex) {
                logger.error(ex.getFaultInfo().getMessage());
            } catch (EnrollmentServiceContractEnrollUserEnrolmentServiceFaultFaultFaultMessage ex) {
                logger.error(ex.getFaultInfo().getMessage());
            } catch (ServerSOAPFaultException ex) {
                logger.error(ex.getMessage());
            }

            //===============retrieve general terms and conditions===============
            try {
                generalTermsAndConditions = basicHttpBindingSigningServiceContract.getGeneralTermsAndConditions(dummyQcrId);
                FileOutputStream fos = new FileOutputStream("TermsAndConditions.pdf");
                fos.write(generalTermsAndConditions.getFileTermsAndConditions());
            } catch (SigningServiceContractGetGeneralTermsAndConditionsSigningServiceFaultFaultFaultMessage ex) {
                logger.error(ex.getFaultInfo().getMessage());
            } catch (ServerSOAPFaultException ex) {
                logger.error(ex.getMessage());
            }
            if (generalTermsAndConditions == null) {
                logger.error("General terms and conditions not retrieved");
            }

            //===============sign a file===============
            try {
                singPdfInitialStep(basicHttpBindingSigningServiceContract, dummyQcrId, generalTermsAndConditions.getHashTermsAndConditions());
                signPdfFinalStep(basicHttpBindingSigningServiceContract);
            } catch (SigningServiceContractAuthorizeSigningEnrolmentFaultFaultFaultMessage ex) {
                logger.error(ex.getFaultInfo().getMessage());
            } catch (ServerSOAPFaultException ex) {
                logger.error(ex.getMessage());
            } catch (NullPointerException ex) {
                logger.error(ex.getMessage());
            }

            //===============apply ltv===============
            try {
                addLtv();
            } catch (NoLtvInformationFoundException e) {
                logger.error("Error: LTV not applied due to missing OCSP/CRL information!");
            } catch(Exception e) {
                logger.error(e.getMessage());
            }

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(PdfSigningDemo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void sendEnrolmentRequest(EnrollmentServiceContract basicHttpBindingEnrollmentSigningServiceContract, String qcrId) throws IOException, EnrollmentServiceContractEnrollUserEnrolmentFaultFaultFaultMessage, EnrollmentServiceContractEnrollUserEnrolmentServiceFaultFaultFaultMessage {

        String customerIdPhoto = "aVZCT1J3MEtHZ29BQUFBTlNVaEVVZ0FBQU5jQUFBQjVDQVlBQUFDcWE3UXJBQUFBQkhOQ1NWUUlDQWdJZkFoa2lBQUFCcFpKUkVGVQplSnp0M1UxdUZFY1loL0YvelhqQmp0eUFiSUlYRVRpY3dJNjVBRGNBYnNBUmNnUEVDVEFuU0M0UVk1OGdHQlJGVmpad0E5Z2hKVU5sCllYQ202aTNiODlIZDlmVThLMERJS3IxNmYvVFlUYys0dDd1SGJ4YWZGd2NQUHB4K0ZPbHM5K2Rmbk56NysrZkhSN25QVWtyc3lQKzkKMjMxNDRKMWVyL0ozWjNKdWIzNXJmdkxIbmYzdnhqNVlOVG4zOHUzdTRaUGN4eWdtZG1TalpwSVlYaXFBaGJFamF6ZTcvQlhEc3dFcwpqQjFacTFud080Wm5BMWdZTzdKeU0vTW5ETThHc0RCMlpLVXNMb25ocFFKWUdEdHlZMmxjRXNOTEJiQXdkdVRhcnNZbE1ieFVBQXRqClI2N3NlbHdTdzBzRnNEQjJKTm5OdUNTR2x3cGdZYzd0elcvdHZQL3poLzJmY2grbGxGYkRKUUVzRmNEQ25HNHZaanNuQUx0b2RWd1MKd0ZJQkxBeGdsNjJIU3dKWUtvQ0ZBVXpTSnJna2dLVUNXQmpBTnNRbEFTeVZjeS9mM2oxOGx2c1l4ZFE1c00xeFNRQkxOWFBQMyswKwpQTXA5akdMcUdOaDJ1Q1NBSmZKT2p3RzJWS2ZBdHNjbEFTd1J3S0k2QkRZTUx1a2JzRGM5RGUrbUFCYlZHYkRoY0VtU2MzZDZHdDRxCkFTeXFJMkRENHBLNkd0NnFBU3lxa3gwWkhwZlV6ZkRXQ1dCUkhleklPTGlrTG9hM2JnQ0xhbnhIeHNNbE5UKzhUUUpZVk1NN01pNHUKcWVuaGJSckFvaHJka2ZGeFNjME9iNXNBRnRYZ2preURTMnB5ZU5zR3NLakdkbVE2WEZKend4c2lnRVUxdENQVDRwS2FHdDVRQVN6cQo2NDY4dS92d1VlNmpiTlAwdUNTQUpRSllsTk50UDlPdk5UOGpsd2VYQkxCRUFFdFU4VU9vK1hCSkFFc0VzRVNWQXN1TFN3SllJb0FsCnFoQllmbHdTd0JKOUE4WXpja3RWQnF3TVhCTEFFbm1ueHp5RUdsVVJzSEp3U1FCTHhWUGV0a3FBbFlWTEFsZ3FnTmtxQUZZZUxxbVoKbTRpREJqQmI0Y0RLeENVMWNSTng4QUJtS3hoWXViaStWZkR3c2dRd1c2RTdVajR1cWRqaFpRdGd0Z0ozcEE1Y1VwSER5eHJBYklYdApTRDI0cE9LR2x6MkEyUXJha2Jwd1NVVU5yNGdBWml0a1IrckRKUlV6dkdJQ21LMkFIYWtUbDFURThJb0tZTGJNTzFJdkxpbjc4SW9MCllMYU1PMUkzTGdsZ2NRQ3paZHFSK25GSkFJc0RtTTI1bDFNL0k5Y0dMZ2xnY1FBelRmMFFhanU0SklERkFjdzBKYkMyY0VrQWl3T1kKYVNwZzdlR1NBQmIzRmRoZnUvdmY1ejVLS1UwQnJFMWNFc0Rpbk52N1J6dDhyTzVTWXdOckY1Y0VzRGllOGphTkNheHRYQkxBNGdCbQpHZ3RZKzdpa0MyQjNENS9sUGtZeEFjdzBCckErY0VuU3pEM25qVGFYQXBocGFHRDk0Qkx2WkdzQ21HbklIZWtLbDNReFBKWnBLYWZiCmk5bjh0OXpIS0NudjlIaUk3OU83d3lYdm4vNzQ5K21iM01jb0p1L1A1bDhXdklYZFVzN3IxZjN6NDZOdHY4N09BR2VwSisrZkRqRzAKWnZMK2JQRjVjWEQvdytuSDNFY3BKZWYxNnQ3NTcwK0crRnI5WExtQUZmWVYxZ05nWFRZa0xLa1hYTUFLQTVacGFGaFNEN2lBRlFZcwprL2RmWGd3TlMycjlleTVnaFFITDV2M1R2ZlBYUjJOODZYYXZYTUFLQTVadDVCMXBFeGV3d29CbG0yQkgyc01GckRCZzJTYmFrYlp3CkFTc01XTFlKZDZRZFhNQUtBNVp0NGgxcEF4ZXd3b0JseTdBajllTUNWaGl3YkpsMnBHNWN3QW9EbGkzamp0UjVFOW5yazVNZTNUcy8KUHNsOWxHSUNsaTN6UDc3MTRmTDZOUC95N3dHUGpTd0ZMRnNCcjJycWVsa0lMQnV3YkFYQWttckNCU3liOTZmQWlpb0VsbFRMeTBKZwptUzRla1RoK2t2c2NSVlVRTEttR0t4ZXdUR004ZTFSOWhjR1NTc2NGTEJPd0VoVUlTeW9aRjdCTXdFcFVLQ3lwMU8rNWdHVUNWbFFGCjl6ckx3d1VzRTdDaUt0bVJzbDRXVmpLMEtRTldWRVU3VWc2dWlvWTJWY0NLcW14SHlzQlYyZENtQ0ZoUkZlNUlmbHdWRG0zc2dCVlYKNlk3a3hWWHAwTVlNV0ZFVjcwZytYQlVQYmF5QUZWWDVqdVRCVmZuUXhnaFlVUTNzeVBTNHZEK3JmV2hEQjZ5b0JtQkpVOTlFNWlOcgpUTUNLYWdTV05PV1ZpNGY2VE1DS2FnaVdOQlV1WUpuRyttU05hbXNNbGpURnkwSmcyVWI4WkkwcWF4Q1dOUGFWQzFpMmdoK1J5RktqCnNLUXhjUUhMQnF5d2htRkpZK0VDbGcxWVlZM0Rrc2JBQlN3YnNNSTZ1ZGM1N0E4MGdHVURWbGhIOXpxSHUzSUJ5d2Fzc001MlpCaGMKblExdHBZQVYxdUdPYkkrcnc2SGRHTERDT3QyUjdYQjFPclJyQTFaWXh6dXlPYTZPaDNabHdBcnJmRWMydzlYNTBKSUJLNHdkMlFBWApRN01CSzR3ZGtiUXVMajZ5eGdhc01HQmR0dkpOWkQ2eUpoR3d3b0FWdE5LVmk0ZjZFZ0VyREZpbUczRUJLOHJyRTdDaWdKWHMycGVGCndJcnE0SDl5cngyd3J1ektLeGV3b29CbEE5YTFKWEVCS3dwWU5tRGRtTUVGckNoZzJZQzFVZ0V1WUVVQnl3YXNsYnY4Z1Fhd29vQmwKNGw3bmVzMGtZSm1BWldKSDFtK0hvWVU1N3o3T1BiQ1dZMGMyNnorOHZWcEZLTG5GUVFBQUFBQkpSVTVFcmtKZ2dnPT0=";

        EnrolmentRequest enrolmentRequest = new EnrolmentRequest();
        enrolmentRequest.setApartment(apAddress);
        enrolmentRequest.setAddress(address);
        enrolmentRequest.setBlock(blAddress);
        enrolmentRequest.setCnp(cnp);
        enrolmentRequest.setCountry(country);
        enrolmentRequest.setCustomerIdPhoto(Base64.getDecoder().decode(customerIdPhoto));
        enrolmentRequest.setEmail(email);
        enrolmentRequest.setFirstName(firstName);
        enrolmentRequest.setLastName(lastName);
        enrolmentRequest.setLocality(locality);
        enrolmentRequest.setNr(nrAddress);
        enrolmentRequest.setPhoneNumber(phoneNumber);
        enrolmentRequest.setExternalId(qcrId);
        enrolmentRequest.setSector(scaraAddress);
        enrolmentRequest.setStreet(stradaAddress);
        enrolmentRequest.setGroup(group);
        enrolmentRequest.setProfile(profile);
        enrolmentRequest.setCertificateValidity(certificateValidity);
        basicHttpBindingEnrollmentSigningServiceContract.enrollUser(enrolmentRequest);
    }

    private static void sendEnrolmentRequestPKCS7(EnrollmentServiceContract basicHttpBindingEnrollmentSigningServiceContract, String qcrId) throws IOException, EnrollmentServiceContractEnrollUserEnrolmentFaultFaultFaultMessage, EnrollmentServiceContractEnrollUserEnrolmentServiceFaultFaultFaultMessage {

        String customerIdPhoto = "aVZCT1J3MEtHZ29BQUFBTlNVaEVVZ0FBQU5jQUFBQjVDQVlBQUFDcWE3UXJBQUFBQkhOQ1NWUUlDQWdJZkFoa2lBQUFCcFpKUkVGVQplSnp0M1UxdUZFY1loL0YvelhqQmp0eUFiSUlYRVRpY3dJNjVBRGNBYnNBUmNnUEVDVEFuU0M0UVk1OGdHQlJGVmpad0E5Z2hKVU5sCllYQ202aTNiODlIZDlmVThLMERJS3IxNmYvVFlUYys0dDd1SGJ4YWZGd2NQUHB4K0ZPbHM5K2Rmbk56NysrZkhSN25QVWtyc3lQKzkKMjMxNDRKMWVyL0ozWjNKdWIzNXJmdkxIbmYzdnhqNVlOVG4zOHUzdTRaUGN4eWdtZG1TalpwSVlYaXFBaGJFamF6ZTcvQlhEc3dFcwpqQjFacTFud080Wm5BMWdZTzdKeU0vTW5ETThHc0RCMlpLVXNMb25ocFFKWUdEdHlZMmxjRXNOTEJiQXdkdVRhcnNZbE1ieFVBQXRqClI2N3NlbHdTdzBzRnNEQjJKTm5OdUNTR2x3cGdZYzd0elcvdHZQL3poLzJmY2grbGxGYkRKUUVzRmNEQ25HNHZaanNuQUx0b2RWd1MKd0ZJQkxBeGdsNjJIU3dKWUtvQ0ZBVXpTSnJna2dLVUNXQmpBTnNRbEFTeVZjeS9mM2oxOGx2c1l4ZFE1c00xeFNRQkxOWFBQMyswKwpQTXA5akdMcUdOaDJ1Q1NBSmZKT2p3RzJWS2ZBdHNjbEFTd1J3S0k2QkRZTUx1a2JzRGM5RGUrbUFCYlZHYkRoY0VtU2MzZDZHdDRxCkFTeXFJMkRENHBLNkd0NnFBU3lxa3gwWkhwZlV6ZkRXQ1dCUkhleklPTGlrTG9hM2JnQ0xhbnhIeHNNbE5UKzhUUUpZVk1NN01pNHUKcWVuaGJSckFvaHJka2ZGeFNjME9iNXNBRnRYZ2preURTMnB5ZU5zR3NLakdkbVE2WEZKend4c2lnRVUxdENQVDRwS2FHdDVRQVN6cQo2NDY4dS92d1VlNmpiTlAwdUNTQUpRSllsTk50UDlPdk5UOGpsd2VYQkxCRUFFdFU4VU9vK1hCSkFFc0VzRVNWQXN1TFN3SllJb0FsCnFoQllmbHdTd0JKOUE4WXpja3RWQnF3TVhCTEFFbm1ueHp5RUdsVVJzSEp3U1FCTHhWUGV0a3FBbFlWTEFsZ3FnTmtxQUZZZUxxbVoKbTRpREJqQmI0Y0RLeENVMWNSTng4QUJtS3hoWXViaStWZkR3c2dRd1c2RTdVajR1cWRqaFpRdGd0Z0ozcEE1Y1VwSER5eHJBYklYdApTRDI0cE9LR2x6MkEyUXJha2Jwd1NVVU5yNGdBWml0a1IrckRKUlV6dkdJQ21LMkFIYWtUbDFURThJb0tZTGJNTzFJdkxpbjc4SW9MCllMYU1PMUkzTGdsZ2NRQ3paZHFSK25GSkFJc0RtTTI1bDFNL0k5Y0dMZ2xnY1FBelRmMFFhanU0SklERkFjdzBKYkMyY0VrQWl3T1kKYVNwZzdlR1NBQmIzRmRoZnUvdmY1ejVLS1UwQnJFMWNFc0Rpbk52N1J6dDhyTzVTWXdOckY1Y0VzRGllOGphTkNheHRYQkxBNGdCbQpHZ3RZKzdpa0MyQjNENS9sUGtZeEFjdzBCckErY0VuU3pEM25qVGFYQXBocGFHRDk0Qkx2WkdzQ21HbklIZWtLbDNReFBKWnBLYWZiCmk5bjh0OXpIS0NudjlIaUk3OU83d3lYdm4vNzQ5K21iM01jb0p1L1A1bDhXdklYZFVzN3IxZjN6NDZOdHY4N09BR2VwSisrZkRqRzAKWnZMK2JQRjVjWEQvdytuSDNFY3BKZWYxNnQ3NTcwK0crRnI5WExtQUZmWVYxZ05nWFRZa0xLa1hYTUFLQTVacGFGaFNEN2lBRlFZcwprL2RmWGd3TlMycjlleTVnaFFITDV2M1R2ZlBYUjJOODZYYXZYTUFLQTVadDVCMXBFeGV3d29CbG0yQkgyc01GckRCZzJTYmFrYlp3CkFTc01XTFlKZDZRZFhNQUtBNVp0NGgxcEF4ZXd3b0JseTdBajllTUNWaGl3YkpsMnBHNWN3QW9EbGkzamp0UjVFOW5yazVNZTNUcy8KUHNsOWxHSUNsaTN6UDc3MTRmTDZOUC95N3dHUGpTd0ZMRnNCcjJycWVsa0lMQnV3YkFYQWttckNCU3liOTZmQWlpb0VsbFRMeTBKZwptUzRla1RoK2t2c2NSVlVRTEttR0t4ZXdUR004ZTFSOWhjR1NTc2NGTEJPd0VoVUlTeW9aRjdCTXdFcFVLQ3lwMU8rNWdHVUNWbFFGCjl6ckx3d1VzRTdDaUt0bVJzbDRXVmpLMEtRTldWRVU3VWc2dWlvWTJWY0NLcW14SHlzQlYyZENtQ0ZoUkZlNUlmbHdWRG0zc2dCVlYKNlk3a3hWWHAwTVlNV0ZFVjcwZytYQlVQYmF5QUZWWDVqdVRCVmZuUXhnaFlVUTNzeVBTNHZEK3JmV2hEQjZ5b0JtQkpVOTlFNWlOcgpUTUNLYWdTV05PV1ZpNGY2VE1DS2FnaVdOQlV1WUpuRyttU05hbXNNbGpURnkwSmcyVWI4WkkwcWF4Q1dOUGFWQzFpMmdoK1J5RktqCnNLUXhjUUhMQnF5d2htRkpZK0VDbGcxWVlZM0Rrc2JBQlN3YnNNSTZ1ZGM1N0E4MGdHVURWbGhIOXpxSHUzSUJ5d2Fzc001MlpCaGMKblExdHBZQVYxdUdPYkkrcnc2SGRHTERDT3QyUjdYQjFPclJyQTFaWXh6dXlPYTZPaDNabHdBcnJmRWMydzlYNTBKSUJLNHdkMlFBWApRN01CSzR3ZGtiUXVMajZ5eGdhc01HQmR0dkpOWkQ2eUpoR3d3b0FWdE5LVmk0ZjZFZ0VyREZpbUczRUJLOHJyRTdDaWdKWHMycGVGCndJcnE0SDl5cngyd3J1ektLeGV3b29CbEE5YTFKWEVCS3dwWU5tRGRtTUVGckNoZzJZQzFVZ0V1WUVVQnl3YXNsYnY4Z1Fhd29vQmwKNGw3bmVzMGtZSm1BWldKSDFtK0hvWVU1N3o3T1BiQ1dZMGMyNnorOHZWcEZLTG5GUVFBQUFBQkpSVTVFcmtKZ2dnPT0=";

        EnrolmentRequest enrolmentRequest = new EnrolmentRequest();
        enrolmentRequest.setApartment(apAddress);
        enrolmentRequest.setAddress(address);
        enrolmentRequest.setBlock(blAddress);
        enrolmentRequest.setCnp(cnp);
        enrolmentRequest.setCountry(country);
        enrolmentRequest.setCustomerIdPhoto(Base64.getDecoder().decode(customerIdPhoto));
        enrolmentRequest.setEmail(email);
        enrolmentRequest.setFirstName(firstName);
        enrolmentRequest.setLastName(lastName);
        enrolmentRequest.setLocality(locality);
        enrolmentRequest.setNr(nrAddress);
        enrolmentRequest.setPhoneNumber(phoneNumber);
        enrolmentRequest.setExternalId(qcrId);
        enrolmentRequest.setSector(scaraAddress);
        enrolmentRequest.setStreet(stradaAddress);
        enrolmentRequest.setGroup(group);
        enrolmentRequest.setProfile(profile);
        enrolmentRequest.setCertificateValidity(certificateValidity);
        createPKCS7EnrollmentRequest(enrolmentRequest);
        try {
            basicHttpBindingEnrollmentSigningServiceContract.enrollEnvelopedPkcs7User(createPKCS7EnrollmentRequest(enrolmentRequest));
        } catch (EnrollmentServiceContractEnrollEnvelopedPkcs7UserEnrolmentFaultFaultFaultMessage ex) {
            logger.error(ex.getFaultInfo().getMessage());
        } catch (EnrollmentServiceContractEnrollEnvelopedPkcs7UserEnrolmentServiceFaultFaultFaultMessage ex) {
            logger.error(ex.getFaultInfo().getMessage());
        }
    }

    private static byte[] createPKCS7EnrollmentRequest(EnrolmentRequest enrollmentRequest) {
        ObjectOutput out = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            String serializedEnrollment = mapper.writeValueAsString(enrollmentRequest);
            try {
                KeyStore keyStore = loadKeyStore();
                CMSSignedDataGenerator signatureGenerator = setUpProvider(keyStore);
                byte[] signedBytes = signPkcs7(serializedEnrollment.getBytes("UTF-8"), signatureGenerator);
                return signedBytes;
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(PdfSigningDemo.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (JsonProcessingException ex) {
            logger.error("Error during enrollment request serialization!", ex);
        }
        return null;
    }

    private static KeyStore loadKeyStore() throws Exception {

        KeyStore keystore = KeyStore.getInstance("PKCS12");
        InputStream is = PdfSigningDemo.class.getResourceAsStream("/certificate.p12");
        keystore.load(is, "1234".toCharArray());
        return keystore;
    }

    private static CMSSignedDataGenerator setUpProvider(final KeyStore keystore) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        Certificate[] certchain = (Certificate[]) keystore.getCertificateChain("1");
        final List<Certificate> certlist = new ArrayList<Certificate>();
        for (int i = 0, length = certchain == null ? 0 : certchain.length; i < length; i++) {
            certlist.add(certchain[i]);
        }
        Store certstore = new JcaCertStore(certlist);
        Certificate cert = keystore.getCertificate("1");
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").
                build((PrivateKey) (keystore.getKey("1", "1234".toCharArray())));
        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        generator.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider("BC").
                build()).build(signer, (X509Certificate) cert));
        generator.addCertificates(certstore);
        return generator;
    }

    private static byte[] signPkcs7(final byte[] content, final CMSSignedDataGenerator generator) throws Exception {

        CMSTypedData cmsdata = new CMSProcessableByteArray(content);
        CMSSignedData signeddata = generator.generate(cmsdata, true);
        return signeddata.getEncoded();
    }

    private static PDFSigningContextSettings setSignatureAppearance() {
        PDFSigningContextSettings signingContextSettings = new PDFSigningContextSettings();
        signingContextSettings.setSignatureType(SignatureTypeEnum.CUSTOM_SIGNATURE);
        signingContextSettings.setLocation("test");
        signingContextSettings.setReason("test");
        PDFVisibilityOptions visibilityOptions = new PDFVisibilityOptions();
        visibilityOptions.setSignatureFieldName("testFirstName");
        //visibilityOptions.setSignaturePageNumber(1);
        visibilityOptions.showSignatureOnAllPages(true);
        visibilityOptions.setImageUrl(new java.net.URL("https://www.iconfinder.com/icons/25482/download/png/128"));
        visibilityOptions.setSignatureRendering(PDFSignatureRendering.IMAGE);
        visibilityOptions.setFontSize(25f);
        visibilityOptions.setSignatureRectangle(new PDFSignatureRectangle(100, 100, 400, 400));
        signingContextSettings.setVisibilityOptions(visibilityOptions);
        return signingContextSettings;
    }

    private static void singPdfInitialStep(SigningServiceContract basicHttpBindingSigningServiceContract, String qcrId, byte[] hashTermsAndConditions) throws FileNotFoundException {
        PdfSignatureProvider signatureProvider = SignatureProviderFactory.getSignatureProvider();
        ArrayOfDocumentInfo arrayOfDocumentInfo = new ArrayOfDocumentInfo();

        DocumentInfo documentInfo = new DocumentInfo();
        String[] temp = filePathToBeSigned.split("/");
        documentInfo.setTitle(temp[temp.length - 1]);
        FileInputStream fis = new FileInputStream(new File(filePathToBeSigned));

        try {
            intermediateSignature = signatureProvider.getIntermediateSignature(fis, temp[temp.length - 1], setSignatureAppearance());
            documentInfo.setHash(intermediateSignature.getHash());
        } catch (InvalidParameterException ex) {
            logger.error(ex.getMessage());
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(PdfSigningDemo.class.getName()).log(Level.SEVERE, null, ex);
        }
        documentInfo.setHash(intermediateSignature.getHash());
        arrayOfDocumentInfo.getDocumentInfo().add(documentInfo);

        SigningRequest signingRequest = new SigningRequest();
        signingRequest.setExternalId(qcrId);
        signingRequest.setDocuments(arrayOfDocumentInfo);
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            for (DocumentInfo docInfo : arrayOfDocumentInfo.getDocumentInfo()) {
                digest.digest(docInfo.getHash());
            }
            signingRequest.setMasterHash(digest.digest());
            signingSession = basicHttpBindingSigningServiceContract.initiateSigning(signingRequest, hashTermsAndConditions);
        } catch (NoSuchAlgorithmException ex) {
            logger.error(ex.getMessage());
        } catch (SigningServiceContractInitiateSigningSigningServiceFaultFaultFaultMessage ex) {
            logger.error(ex.getFaultInfo().getMessage());
        } catch (SigningServiceContractInitiateSigningSigningSessionFaultFaultFaultMessage ex) {
            logger.error(ex.getFaultInfo().getMessage());
        } catch (SigningServiceContractInitiateSigningCertificateFaultFaultFaultMessage ex) {
            logger.error(ex.getFaultInfo().getMessage());
        }
    }

    private static void signPdfFinalStep(SigningServiceContract basicHttpBindingSigningServiceContract) throws SigningServiceContractAuthorizeSigningEnrolmentFaultFaultFaultMessage {

        PdfSignatureProvider signatureProvider = SignatureProviderFactory.getSignatureProvider();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println(" Please input authorization code received via SMS:");
        try {
            DataInputStream inStream = null;
            String pinCode = reader.readLine();
            System.out.println("Sending authorization request...");

            AuthorizationRequest authRequest = new AuthorizationRequest();
            authRequest.setSessionId(signingSession);
            authRequest.setCode(pinCode);

            SigningResponse authResponse = basicHttpBindingSigningServiceContract.authorizeSigning(authRequest);
            List<DocumentSignature> signatures = authResponse.getSignatures().getDocumentSignature();

            for (DocumentSignature signature : signatures) {
                OutputStream signedOutputStream = signatureProvider.embedSignature(new ByteArrayInputStream(((ByteArrayOutputStream) intermediateSignature.getBlankSignatureStream()).toByteArray()),
                        signature.getSignature(), intermediateSignature.getSignatureFieldName());
                FileOutputStream fos = new FileOutputStream(signature.getData().getTitle());
                ((ByteArrayOutputStream) signedOutputStream).writeTo(fos);
                System.out.println(String.format(" Signed file successfully saved in %1$s.", signature.getData().getTitle()));
            }

        } catch (IOException ex) {
            logger.error(ex.getMessage());
        } catch (SigningServiceContractAuthorizeSigningAuthorizationFaultFaultFaultMessage ex) {
            logger.error(ex.getFaultInfo().getMessage());
        } catch (SigningServiceContractAuthorizeSigningCertificateFaultFaultFaultMessage ex) {
            logger.error(ex.getFaultInfo().getMessage());
        } catch (SigningServiceContractAuthorizeSigningOtpRetryLimitReachedFaultFaultFaultMessage ex) {
            logger.error(ex.getFaultInfo().getMessage());
        } catch (SigningServiceContractAuthorizeSigningOtpTimeoutFaultFaultFaultMessage ex) {
            logger.error(ex.getFaultInfo().getMessage());
        } catch (SigningServiceContractAuthorizeSigningSigningServiceFaultFaultFaultMessage ex) {
            logger.error(ex.getFaultInfo().getMessage());
        } catch (SigningServiceContractAuthorizeSigningSigningSessionFaultFaultFaultMessage ex) {
            logger.error(ex.getFaultInfo().getMessage());
        }
    }

    private static void addLtv() throws IOException, DocumentException, OCSPException, GeneralSecurityException, OperatorCreationException, NoLtvInformationFoundException {
        Security.addProvider(new BouncyCastleProvider());

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Add LTV information to signed PDF document.");
        System.out.print("Enter PDF document location: ");
        String pdfLocation = reader.readLine();

        File pdfFile = new File(pdfLocation);
        byte[] pdfBytes = new byte[(int)pdfFile.length()];
        FileInputStream fileInputStream = new FileInputStream(pdfFile);
        fileInputStream.read(pdfBytes);
        fileInputStream.close();

        PdfSignatureProvider signatureProvider = SignatureProviderFactory.getSignatureProvider();
        ByteArrayOutputStream byteArrayOutputStream = signatureProvider.addLTVInformation(pdfBytes);

        String pdfOutput = pdfLocation.replace(".pdf", "-JavaWithLtvInformation.pdf");
        FileOutputStream fileOutputStream = new FileOutputStream(pdfOutput);
        fileOutputStream.write(byteArrayOutputStream.toByteArray());
        fileOutputStream.close();
    }
}
