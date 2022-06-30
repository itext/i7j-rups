package com.itextpdf.research.autoupdate;

import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.rups.model.PdfFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Base64.Encoder;

public class AutoUpdater {
    private final PdfFile input;
    private final OutputStream output;

    private final PdfDictionary autoUpdateDict;

    private byte[] fileContent;

    public AutoUpdater(PdfFile input, OutputStream output) {
        this.input = input;
        this.output = output;
        this.autoUpdateDict = input.getPdfDocument().getCatalog().getPdfObject()
                        .getAsDictionary(new PdfName("AutoUpdate"));
    }

    public boolean hasAutoUpdate() {
        return autoUpdateDict != null;
    }

    private byte[] getFileContent() {
        if (fileContent == null) {
            fileContent = input.getBytes();
        }
        return fileContent;
    }

    private URL getUpdateURL() throws MalformedURLException {
        PdfString repo = autoUpdateDict.getAsString(new PdfName("Repo"));
        PdfName addressMode = autoUpdateDict.getAsName(new PdfName("AddressMode"));
        StringBuilder urlStr = new StringBuilder(repo.toUnicodeString());
        Encoder enc = Base64.getUrlEncoder();
        if ("ContentDigest".equals(addressMode.getValue())) {
            byte[] hash;
            try {
                MessageDigest md = MessageDigest.getInstance("SHA384");
                md.update(getFileContent());
                hash = md.digest();
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
            urlStr.append("/hash/").append(new String(enc.encode(hash), StandardCharsets.UTF_8));
        } else if ("DocumentID".equals(addressMode.getValue())) {
            PdfArray arr = input.getPdfDocument().getTrailer().getAsArray(PdfName.ID);
            byte[] id1 = arr.getAsString(0).getValueBytes();
            byte[] id2 = arr.getAsString(1).getValueBytes();

            urlStr.append("/docId/")
                    .append(new String(enc.encode(id1), StandardCharsets.UTF_8))
                    .append('/')
                    .append(new String(enc.encode(id2), StandardCharsets.UTF_8));
        }
        return new URL(urlStr.toString());
    }

    private static boolean nameValueIs(PdfDictionary dict, String key, String expectedValue) {
        PdfName valueFound = dict.getAsName(new PdfName(key));
        return valueFound != null && expectedValue.equals(valueFound.getValue());
    }

    private void downloadUpdate() throws IOException, UpdateVerificationException {
        // this method only implements a small part of the draft spec, but was written to
        // somewhat realistically represent how one would ingest an update "for real", e.g.
        // with potentially large update payloads, only exposing update content to the
        // caller after verification passes, etc.

        if (!nameValueIs(autoUpdateDict, "UpdateType", "Incremental")) {
            throw new IOException("Only Incremental is supported in this PoC");
        }

        PdfDictionary integrity = autoUpdateDict.getAsDictionary(new PdfName("Integrity"));
        PasetoV4PublicVerifier verifier;
        MessageDigest implicitContentDigest;
        if (integrity != null) {
            boolean isPasetoV4Pub =
                    nameValueIs(integrity, "CertDataType", "PASETOV4Public");
            PdfString pskStr = integrity.getAsString(new PdfName("PreSharedKey"));
            if (!isPasetoV4Pub || pskStr == null) {
                throw new UpdateVerificationException("Only PASETOV4Public with pre-shared keys is supported");
            }
            try {
                verifier = new PasetoV4PublicVerifier(pskStr.getValueBytes());
            } catch (GeneralSecurityException e) {
                throw new UpdateVerificationException("Key deser error", e);
            }

            try {
                implicitContentDigest = MessageDigest.getInstance("SHA384");
            } catch (NoSuchAlgorithmException e) {
                throw new UpdateVerificationException("No SHA384", e);
            }
        } else {
            verifier = null;
            implicitContentDigest = null;
        }

        // stream the update content to disk (out of sight) while also feeding it to the verifier
        Path tempFile = Files.createTempFile("pdfupdate", ".bin");
        try (OutputStream tempOut = Files.newOutputStream(tempFile)) {
            byte[] buf = new byte[2048];
            URLConnection conn = getUpdateURL().openConnection();
            if (verifier != null) {
                try {
                    long contentLength = conn.getContentLengthLong();
                    if (contentLength == -1) {
                        throw new IOException("Content-Length must be present for this PoC");
                    }
                    verifier.init(conn.getHeaderField("X-PDF-Update-Token").getBytes(StandardCharsets.UTF_8), contentLength);
                } catch (GeneralSecurityException e) {
                    throw new UpdateVerificationException("Cryptographic failure", e);
                }
            }
            InputStream is = conn.getInputStream();
            int bytesRead;
            while ((bytesRead = is.read(buf)) > 0) {
                tempOut.write(buf, 0, bytesRead);
                if (verifier != null) {
                    try {
                        verifier.updateImplicit(buf, 0, bytesRead);
                        implicitContentDigest.update(buf, 0, bytesRead);
                    } catch (GeneralSecurityException e) {
                        throw new UpdateVerificationException("Cryptographic failure", e);
                    }
                }
            }

        }

        if (verifier != null) {
            // byte[] contentDigest = implicitContentDigest.digest();
            try {
                verifier.verifyAndGetPayload();
            } catch (SignatureException e) {
                throw new UpdateVerificationException("Cryptographic failure", e);
            }
        }


        // TODO implement hash comparison part of integrity check

        // all clear -> proceed to output
        output.write(getFileContent());
        try (InputStream tempIn = Files.newInputStream(tempFile)) {
            byte[] buf = new byte[2048];
            int bytesRead;
            while ((bytesRead = tempIn.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
        }
    }

}
