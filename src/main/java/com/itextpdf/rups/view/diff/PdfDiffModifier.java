package com.itextpdf.rups.view.diff;

import com.itextpdf.io.source.ByteArrayOutputStream;

import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class PdfDiffModifier {

    private final byte[] data;

    private int position = 0;

    public PdfDiffModifier(byte[] data) {
        this.data = data;
    }

    public static byte[] decompress(byte[] input) throws DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(input);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        while (!inflater.finished()) {
            int decompressedSize = inflater.inflate(buffer);
            outputStream.write(buffer, 0, decompressedSize);
        }

        return outputStream.toByteArray();
    }


    public ByteArrayOutputStream next() throws DataFormatException, IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        while (position < data.length) {
            if (peekNextCharIsCharArray(0, "stream".toCharArray())) {
                position += "stream\n".length();
                int streamSize = findStartLocationOfNextOccurance("\nendstream\n".toCharArray());
                result.write("stream\n".getBytes());
                byte[] streamData = new byte[streamSize];
                for (int i = 0; i < streamSize; i++) {
                    streamData[i] = data[position + i];
                }
                byte[] decompressed = decompress(streamData);
                result.write(decompressed);
                position += streamSize;
                position += "\nendstream\n".length();
                result.write("\nendstream\n".getBytes());

            } else {
                result.write(data[position]);
                position++;
            }
        }
        return result;
    }

    private boolean peekNextCharIsCharArray(int offset, char[] needle) {
        for (int i = 0; i < needle.length; i++) {
            if (data[position + offset + i] != needle[i]) {
                return false;
            }
        }
        return true;
    }

    private int findStartLocationOfNextOccurance(char[] needle) {
        for (int i = position; i < data.length; i++) {
            if (data[i] == needle[0]) {
                boolean found = true;
                for (int j = 1; j < needle.length; j++) {
                    if (data[i + j] != needle[j]) {
                        found = false;
                        break;
                    }
                }
                if (found) {
                    return i - position;
                }
            }
        }
        return -1;

    }
}

