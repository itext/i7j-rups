package com.itextpdf.research.autoupdate;

import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.rups.model.PdfFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Encoder;
import org.bouncycastle.util.encoders.Base64Encoder;

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

    private void downloadUpdate() throws IOException {
        // TODO apply integrity check
        if (!"Incremental".equals(autoUpdateDict.getAsName(new PdfName("UpdateType")).getValue())) {
            throw new IllegalArgumentException("Only Incremental is supported in this PoC");
        }
        output.write(getFileContent());
        byte[] buf = new byte[2048];
        URLConnection conn = getUpdateURL().openConnection();
        InputStream is = conn.getInputStream();
        int bytesRead;
        while ((bytesRead = is.read(buf)) > 0) {
            output.write(buf, 0, bytesRead);
        }
    }

}
