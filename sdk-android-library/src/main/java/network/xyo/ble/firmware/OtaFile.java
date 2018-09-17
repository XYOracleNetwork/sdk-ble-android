package network.xyo.ble.firmware;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;


public class OtaFile {

    private static final int fileBlockSize = 240;
    private static final int fileChunkSize = 20;
    private byte[] bytes;
    private byte[][][] blocks;
    private int bytesAvailable;
    private int numberOfBlocks = 0;


    private OtaFile(InputStream inputStream) throws IOException {

        bytesAvailable = inputStream.available();

        bytes = new byte[bytesAvailable + 1];
        inputStream.read(bytes);
        byte crc = getCrc();
        bytes[bytesAvailable] = crc;
        initBlocks();
    }

    public static OtaFile fromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        return new OtaFile(is);
    }

    public static OtaFile fromInputStream(InputStream stream) throws IOException {
        return new OtaFile(stream);
    }

    public byte[][] getBlock(int index) {
        return blocks[index];
    }

    public int getNumberOfBlocks() {
        return numberOfBlocks;
    }

    public int getNumberOfBytes() {
        return bytes.length;
    }

    private void initBlocks() {
        blocks = new byte[numberOfBlocks][][];
        int byteOffset = 0;
        // Loop through all the bytes and split them into pieces the size of the default chunk size
        for (int i = 0; i < numberOfBlocks; i++) {

            int blockSize = fileBlockSize;
            if (i + 1 == numberOfBlocks ) {
                blockSize = bytes.length % fileBlockSize;
            }

            int chunksPerBlockCount = (int) Math.ceil((double) fileBlockSize / (double) fileChunkSize);
            numberOfBlocks = (int) Math.ceil((double) bytes.length / (double) fileBlockSize);
            int chunkNumber = 0;
            blocks[i] = new byte[chunksPerBlockCount][];
            for (int j = 0; j < blockSize; j += fileChunkSize) {
                // Default chunk size
                int chunkSize = fileChunkSize;
                // Last chunk of all
                if (byteOffset + fileChunkSize > bytes.length) {
                    chunkSize = bytes.length - byteOffset;
                }
                // Last chunk in block
                else if (j + fileChunkSize > blockSize) {
                    chunkSize = fileBlockSize % fileChunkSize;
                }

                byte[] chunk = Arrays.copyOfRange(bytes, byteOffset, byteOffset + chunkSize);
                blocks[i][chunkNumber] = chunk;
                byteOffset += chunkSize;
                chunkNumber++;
            }
        }

    }

    private byte getCrc() {
        byte crcCode = 0;
        for (int i = 0; i < bytesAvailable; i++) {
            Byte byteValue = bytes[i];
            int intVal = byteValue.intValue();
            crcCode ^= intVal;
        }

        return crcCode;
    }

}