package com.itextpdf.research.autoupdate;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class PasetoV4PublicVerifier {

    public static final int SIGNATURE_LENGTH = 64;
    private final PublicKey publicKey;

    private long implicitLength;
    private long implicitLengthSoFar = 0;
    private Signature sig;

    byte[] decodedPayload;

    public PasetoV4PublicVerifier(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public PasetoV4PublicVerifier(byte[] pubKeyBytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
         this(KeyFactory.getInstance("Ed25519")
                 .generatePublic(new X509EncodedKeySpec(pubKeyBytes)));
    }

    public void init(byte[] token, long implicitLength)
            throws UpdateVerificationException, GeneralSecurityException {
        if (sig != null) {
            throw new IllegalStateException();
        }
        String tokenStr = new String(token, StandardCharsets.US_ASCII);
        String[] parts = tokenStr.split("\\.", 4);
        if (parts.length != 3) {
            throw new UpdateVerificationException("Expected 3-part no-footer token");
        }

        if (!"v4".equals(parts[0]) || !"public".equals(parts[1])) {
            throw new UpdateVerificationException("Expected v4.public token");
        }

        this.decodedPayload = Base64.getUrlDecoder().decode(parts[2]);
        int messageLength = decodedPayload.length - SIGNATURE_LENGTH;

        this.sig = Signature.getInstance("Ed25519");
        this.sig.initVerify(this.publicKey);
        // we need to feed the verifier 4 items of PAE data
        this.sig.update(le64(4));

        //header
        byte[] h = "v4.public.".getBytes(StandardCharsets.US_ASCII);
        this.sig.update(le64(h.length));
        this.sig.update(h);

        // message
        this.sig.update(le64(messageLength));
        this.sig.update(decodedPayload, 0, messageLength);

        // footer (empty)
        this.sig.update(le64(0));

        // implicit data
        sig.update(le64(implicitLength));
        // the rest is by streaming
        this.implicitLength = implicitLength;
    }

    public void updateImplicit(byte[] data, int off, int len) throws SignatureException {
        if (this.implicitLengthSoFar + len > this.implicitLength) {
            throw new IllegalStateException("Too much input");
        }
        sig.update(data, off, len);
        this.implicitLengthSoFar += len;
    }

    public byte[] verifyAndGetPayload() throws SignatureException, UpdateVerificationException {
        int messageLength = this.decodedPayload.length - SIGNATURE_LENGTH;
        if (this.implicitLengthSoFar != this.implicitLength) {
            String msg = String.format(
                    "Expected %d bytes of input, but got %d.",
                    this.implicitLength,
                    this.implicitLengthSoFar);
            throw new UpdateVerificationException(msg);
        }
        if (!sig.verify(this.decodedPayload, messageLength, SIGNATURE_LENGTH)) {
            throw new UpdateVerificationException("Invalid signature");
        }
        byte[] message = new byte[messageLength];
        System.arraycopy(this.decodedPayload, 0, message, 0, messageLength);
        return message;
    }

    private static byte[] le64(long l) {
        return new byte[] {
                (byte) l,
                (byte) (l >>> 8),
                (byte) (l >>> 16),
                (byte) (l >>> 24),
                (byte) (l >>> 32),
                (byte) (l >>> 40),
                (byte) (l >>> 48),
                (byte) ((l >>> 56) & 0x7f)  // clear msb
        };
    }
}
