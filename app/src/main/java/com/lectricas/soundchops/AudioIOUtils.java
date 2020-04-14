package com.lectricas.soundchops;

/**
 * @author Alexey Grigorev 1
 */
public class AudioIOUtils {

    public static int[] decodeBytes(byte[] bytes, int numberOfSamples, int sampleSizeInBytes, boolean bigEndian) {
        int[] audioSamples = new int[numberOfSamples];
        byte[] temporaryBuffer = new byte[sampleSizeInBytes];

        int k = 0;
        for (int i = 0; i < audioSamples.length; i++) {
            // collect sample byte in big-endian order
            if (bigEndian) {
                // bytes start with MSB
                for (int j = 0; j < sampleSizeInBytes; j++) {
                    temporaryBuffer[j] = bytes[k++];
                }
            } else {
                // bytes start with LSB
                for (int j = sampleSizeInBytes - 1; j >= 0; j--) {
                    temporaryBuffer[j] = bytes[k++];
                }
            }
            // get integer value from bytes
            int ival = 0;

            for (int j = 0; j < sampleSizeInBytes; j++) {
                ival += temporaryBuffer[j];
                if (j < sampleSizeInBytes - 1) {
                    ival <<= 8;
                }
            }
            // decode value

            audioSamples[i] = ival;
        }
        return audioSamples;
    }
}