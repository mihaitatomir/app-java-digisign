package RSSSDKSample.RSSSDKSample;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Scanner;

import javax.xml.namespace.QName;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import ro.certsign.paperless.signatureproperties.PDFSignatureRectangle;
import ro.certsign.paperless.signatureproperties.PDFSigningContextSettings;
import ro.certsign.paperless.signatureproperties.PDFVisibilityOptions;
import ro.certsign.paperless.signatureproperties.SignatureTypeEnum;
import ro.certsign.paperless.signatureprovider.IntermediateSignature;
import ro.certsign.paperless.signatureprovider.PdfSignatureProvider;
import ro.certsign.paperless.signatureprovider.SignatureProviderFactory;
import ro.certsign.paperlessenrollmentservice._2016._05.EnrollmentService;
import ro.certsign.paperlessenrollmentservice._2016._05.EnrollmentServiceContract;
import ro.certsign.paperlesssigningservice._2016._05.SigningService;
import ro.certsign.paperlesssigningservice._2016._05.SigningServiceContract;
import ro.certsign.schemas.paperlessenrollmentservice._2016._05.EnrolmentRequest;
import ro.certsign.schemas.paperlesssigningservice._2016._05.ArrayOfDocumentInfo;
import ro.certsign.schemas.paperlesssigningservice._2016._05.AuthorizationRequest;
import ro.certsign.schemas.paperlesssigningservice._2016._05.DocumentInfo;
import ro.certsign.schemas.paperlesssigningservice._2016._05.DocumentInfoDocumentDataType;
import ro.certsign.schemas.paperlesssigningservice._2016._05.DocumentSignature;
import ro.certsign.schemas.paperlesssigningservice._2016._05.SigningRequest;
import ro.certsign.schemas.paperlesssigningservice._2016._05.SigningResponse;
import ro.certsign.schemas.paperlesssigningservice._2016._05.TermsAndConditions;

public class Program {

	public static void main(String[] args) throws Exception {
		
		
	    String wsdlEnrollmentServiceUrl = "https://rssone-ci.rd.certsign.ro/RSS.EnrollmentService_01/EnrollmentService.svc?singleWsdl";
		EnrollmentService enrollmentSerice = new EnrollmentService(new URL(wsdlEnrollmentServiceUrl), new QName("http://www.certsign.ro/PaperlessEnrollmentService/2016/05", "EnrollmentService"));
        EnrollmentServiceContract basicHttpBindingEnrollmentSigningServiceContract = enrollmentSerice.getBasicHttpBindingEnrollmentServiceContract();

        
        EnrolmentRequest er = new EnrolmentRequest();
        er.setExternalId("123456");
        er.setFirstName("Paluna");
        er.setLastName("Vasile");
        er.setCnp("1520323421585");
        er.setEmail("paluna.vasile@gmail.com");
        er.setCountry("RO");
        er.setLocality("L");
        er.setPhoneNumber("0700000000");
        er.setAddress("A");
        
        byte[] signedEnrollmentRequest = createPKCS7EnrollmentRequest(er, "C:\\path\\to\\p12\\or\\pfx", "password");
        basicHttpBindingEnrollmentSigningServiceContract.enrollEnvelopedPkcs7User(signedEnrollmentRequest);
        
        String wsdlSigningServiceUrl = "https://rssone-ci.rd.certsign.ro/RSS.SigningService_01/SigningService.svc?singleWsdl";
        SigningService signingService = new SigningService(new URL(wsdlSigningServiceUrl), new QName("http://www.certsign.ro/PaperlessSigningService/2016/05", "SigningService"));
        SigningServiceContract basicHttpBindingSigningServiceContract = signingService.getBasicHttpBindingSigningServiceContract();
        
        
        TermsAndConditions tnc = basicHttpBindingSigningServiceContract.getGeneralTermsAndConditions("123456");
        
        
        FileInputStream pdfStream = new FileInputStream("C:\\path\\to\\pdf");
        PdfSignatureProvider sp = SignatureProviderFactory.getSignatureProvider();
        PDFSigningContextSettings signingContextSettings = new PDFSigningContextSettings();
        signingContextSettings.setSignatureType(SignatureTypeEnum.CUSTOM_SIGNATURE);
        signingContextSettings.setLocation("test");
        signingContextSettings.setReason("test");
        PDFVisibilityOptions visibilityOptions = new PDFVisibilityOptions();
        visibilityOptions.setSignatureFieldName("testFieldName");
        visibilityOptions.setFontSize(25f);
        visibilityOptions.setSignatureRectangle(new PDFSignatureRectangle(100, 100, 400, 400));
        signingContextSettings.setVisibilityOptions(visibilityOptions);
        
        IntermediateSignature is = sp.getIntermediateSignature(pdfStream, "SignerName", signingContextSettings);
                
        DocumentInfo di = new DocumentInfo();
        di.setDataType(DocumentInfoDocumentDataType.DOCUMENT_HASH);
        di.setHash(is.getHash());
        SigningRequest signingRequest = new SigningRequest();
        signingRequest.setExternalId("123456");
        signingRequest.setMasterHash(is.getHash());;
        ArrayOfDocumentInfo arrayOfDocumentInfo = new ArrayOfDocumentInfo();
        arrayOfDocumentInfo.getDocumentInfo().add(di);
        signingRequest.setDocuments(arrayOfDocumentInfo);
        String sessionId = basicHttpBindingSigningServiceContract.initiateSigning(signingRequest, tnc.getHashTermsAndConditions());
        
        System.out.println("Input authorization code");
        Scanner scanner = new Scanner(System.in);
        String code = scanner.next();
        scanner.close();
        
        AuthorizationRequest ar = new AuthorizationRequest();
        ar.setCode(code);
        ar.setSessionId(sessionId);
        SigningResponse response = basicHttpBindingSigningServiceContract.authorizeSigning(ar);
        
        byte[] blankSignature = ((ByteArrayOutputStream)is.getBlankSignatureStream()).toByteArray();
        DocumentSignature signature = response.getSignatures().getDocumentSignature().get(0);
        OutputStream signedPdf = sp.embedSignature(new ByteArrayInputStream(blankSignature),
		                			signature.getSignature(),
		                			is.getSignatureFieldName());
        FileOutputStream fos = new FileOutputStream("C:\\path\\to\\signed\\pdf");
        ((ByteArrayOutputStream) signedPdf).writeTo(fos);
        
	}

    private static byte[] createPKCS7EnrollmentRequest(EnrolmentRequest enrollmentRequest, String p12Path, String p12Password) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String serializedEnrollment = mapper.writeValueAsString(enrollmentRequest);

    	KeyStore keyStore = KeyStore.getInstance("PKCS12");
		FileInputStream f = new FileInputStream(p12Path);
		String password = p12Password;
		keyStore.load(f, password.toCharArray());
		
        Certificate cert = keyStore.getCertificate("1");
        PrivateKey privateKey = (PrivateKey) keyStore.getKey("1", password.toCharArray());
        
        Security.addProvider(new BouncyCastleProvider());
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC")
        																	.build(privateKey);
        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        generator.addSignerInfoGenerator(
        				new JcaSignerInfoGeneratorBuilder(
        						new JcaDigestCalculatorProviderBuilder()
        								.setProvider("BC")
        								.build())
        					.build(signer, (X509Certificate) cert));
        generator.addCertificate(new X509CertificateHolder(cert.getEncoded()));
        
        
        CMSTypedData cmsdata = new CMSProcessableByteArray(serializedEnrollment.getBytes("UTF-8"));
        CMSSignedData signeddata = generator.generate(cmsdata, true);
        return signeddata.getEncoded();

    }
}
