/*
 * =============
 * Original code
 * =============
 * FreeSec: libcrypt for NetBSD
 * Copyright (c) 1994 David Burren
 * All rights reserved.
 *
 * Adapted for FreeBSD-2.0 by Geoffrey M. Rehmet
 * Adapted for FreeBSD-4.0 by Mark R V Murray
 *
 * =========
 * Java port
 * =========
 * Copyright (c) 2009 Tim Vernum
 * All rights reserved.
 *
 * =======
 * License
 * =======
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the author nor the names of other contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * This is an original implementation (in C) of the DES and the crypt(3) interfaces
 * by David Burren <davidb@werj.com.au>, ported to Java by Tim Vernum
 * 
 * All the hard work was done by David. The bugs are Tim's fault.
 */

package us.terebi.plugins.crypt.internal;

/**
 * An implementation of Unix Crypt in Java.
 * Derived from "FreeSec: libcrypt for NetBSD", and ported into Java.
 * 
 * This class is <strong>not</strong> thread safe within a single instance
 * (but multiple instances can be accessed, each in their own thread)
 * 
 * This code is released under a BSD style license - see the source code for full details.
 * 
 * @author David Burren (original implementation in C)
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a> (conversion to Java)
 */
public class Crypt
{
    /*
     * This code started life a direct port of "FreeSec: libcrypt for NetBSD" into Java.
     * The original code was taken from the FreeBSD svn repository in December 2009.
     * The C code was then modified to convert the use of pointer arithmetic into array access,
     *  and to reduce the number of places where an integer was treated as an array of 4 bytes.
     * This code was then ported over to Java with as few modifications as necessary.
     * Once the code was working successfully in Java, it was refactored into more idiomatic Java. 
     */
    final static byte IP[] = { 58, 50, 42, 34, 26, 18, 10, 2, 60, 52, 44, 36, 28, 20, 12, 4, 62, 54, 46, 38, 30, 22, 14, 6, 64, 56, 48, 40, 32, 24,
            16, 8, 57, 49, 41, 33, 25, 17, 9, 1, 59, 51, 43, 35, 27, 19, 11, 3, 61, 53, 45, 37, 29, 21, 13, 5, 63, 55, 47, 39, 31, 23, 15, 7 };

    final static int IP_MASK_LEFT[][] = new int[8][256], IP_MASK_RIGHT[][] = new int[8][256];

    final static byte KEY_PERM[] = { 57, 49, 41, 33, 25, 17, 9, 1, 58, 50, 42, 34, 26, 18, 10, 2, 59, 51, 43, 35, 27, 19, 11, 3, 60, 52, 44, 36, 63,
            55, 47, 39, 31, 23, 15, 7, 62, 54, 46, 38, 30, 22, 14, 6, 61, 53, 45, 37, 29, 21, 13, 5, 28, 20, 12, 4 };

    final static byte INVERSE_KEY_PERM[] = new byte[64];
    final static int KEY_PERM_MASK_LEFT[][] = new int[8][128];
    final static int KEY_PERM_MASK_RIGHT[][] = new int[8][128];

    final static byte KEY_SHIFTS[] = { 1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1 };

    final static byte COMP_PERM[] = { 14, 17, 11, 24, 1, 5, 3, 28, 15, 6, 21, 10, 23, 19, 12, 4, 26, 8, 16, 7, 27, 20, 13, 2, 41, 52, 31, 37, 47, 55,
            30, 40, 51, 45, 33, 48, 44, 49, 39, 56, 34, 53, 46, 42, 50, 36, 29, 32 };
    final static byte INVERSE_COMP_PERM[] = new byte[56];

    final static int COMP_MASK_LEFT[][] = new int[8][128];
    final static int COMP_MASK_RIGHT[][] = new int[8][128];

    final static byte SBOX[][] = {
            { 14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7, 0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8, 4, 1, 14, 8, 13, 6, 2, 11,
                    15, 12, 9, 7, 3, 10, 5, 0, 15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13 },
            { 15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10, 3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5, 0, 14, 7, 11, 10, 4, 13, 1,
                    5, 8, 12, 6, 9, 3, 2, 15, 13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9 },
            { 10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8, 13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1, 13, 6, 4, 9, 8, 15, 3, 0,
                    11, 1, 2, 12, 5, 10, 14, 7, 1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12 },
            { 7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15, 13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9, 10, 6, 9, 0, 12, 11, 7, 13,
                    15, 1, 3, 14, 5, 2, 8, 4, 3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14 },
            { 2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9, 14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6, 4, 2, 1, 11, 10, 13, 7, 8,
                    15, 9, 12, 5, 6, 3, 0, 14, 11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3 },
            { 12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11, 10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8, 9, 14, 15, 5, 2, 8, 12, 3,
                    7, 0, 4, 10, 1, 13, 11, 6, 4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13 },
            { 4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1, 13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6, 1, 4, 11, 13, 12, 3, 7, 14,
                    10, 15, 6, 8, 0, 5, 9, 2, 6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12 },
            { 13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7, 1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2, 7, 11, 4, 1, 9, 12, 14, 2,
                    0, 6, 10, 13, 15, 3, 5, 8, 2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11 } };
    final static byte INVERSE_SBOX[][] = new byte[8][64];

    final static byte PBOX[] = { 16, 7, 20, 21, 29, 12, 28, 17, 1, 15, 23, 26, 5, 18, 31, 10, 2, 8, 24, 14, 32, 27, 3, 9, 19, 13, 30, 6, 22, 11, 4,
            25 };
    final static byte INVERSE_PBOX[] = new byte[32];

    final static int PSBOX[][] = new int[4][256];
    final static short M_SBOX[][] = new short[4][4096];

    final static int BITS32[] = { 0x80000000, 0x40000000, 0x20000000, 0x10000000, 0x08000000, 0x04000000, 0x02000000, 0x01000000, 0x00800000,
            0x00400000, 0x00200000, 0x00100000, 0x00080000, 0x00040000, 0x00020000, 0x00010000, 0x00008000, 0x00004000, 0x00002000, 0x00001000,
            0x00000800, 0x00000400, 0x00000200, 0x00000100, 0x00000080, 0x00000040, 0x00000020, 0x00000010, 0x00000008, 0x00000004, 0x00000002,
            0x00000001 };
    final static int BITS28[] = new int[28];
    final static int BITS24[] = new int[24];
    final static byte BITS8[] = { (byte) 0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01 };

    final static byte INITIAL_PERM[] = new byte[64];
    final static byte FINAL_PERM[] = new byte[64];
    final static int FP_MASK_LEFT[][] = new int[8][256], FP_MASK_RIGHT[][] = new int[8][256];

    final static byte[] ASCII64 = "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".getBytes();

    static
    {
        initialiseDES();
    }

    int _saltbits;
    long _oldSalt;
    int _oldRawkey0, _oldRawkey1;

    final int _encryptionKeysLeft[] = new int[16];
    final int _encryptionKeysRight[] = new int[16];

    static int asciiToBinary(byte ch)
    {
        if (ch > 'z')
        {
            return 0;
        }
        if (ch >= 'a')
        {
            return (ch - 'a' + 38);
        }
        if (ch > 'Z')
        {
            return 0;
        }
        if (ch >= 'A')
        {
            return (ch - 'A' + 12);
        }
        if (ch > '9')
        {
            return 0;
        }
        if (ch >= '.')
        {
            return (ch - '.');
        }
        return 0;
    }

    static void initialiseDES()
    {
        // Setup the bit arrays
        System.arraycopy(BITS32, 4, BITS28, 0, 28);
        System.arraycopy(BITS32, 8, BITS24, 0, 24);

        /*
         * Invert the S-boxes, reordering the input bits.
         */
        for (int i = 0; i < 8; i++)
        {
            for (int j = 0; j < 64; j++)
            {
                int b = (j & 0x20) | ((j & 1) << 4) | ((j >> 1) & 0xf);
                INVERSE_SBOX[i][j] = SBOX[i][b];
            }
        }

        /*
         * Convert the inverted S-boxes into 4 arrays of 8 bits.
         * Each will handle 12 bits of the S-box input.
         */
        for (int b = 0; b < 4; b++)
        {
            for (int i = 0; i < 64; i++)
            {
                for (int j = 0; j < 64; j++)
                {
                    int a = INVERSE_SBOX[(b << 1)][i] << 4;
                    int c = INVERSE_SBOX[(b << 1) + 1][j];
                    M_SBOX[b][(i << 6) | j] = (short) (a | c);
                }
            }
        }

        /*
         * Set up the initial & final permutations into a useful form, and
         * initialise the inverted key permutation.
         */
        for (int i = 0; i < 64; i++)
        {
            INITIAL_PERM[FINAL_PERM[i] = (byte) (IP[i] - 1)] = (byte) i;
            INVERSE_KEY_PERM[i] = (byte) 255;
        }

        /*
         * Invert the key permutation and initialise the inverted key
         * compression permutation.
         */
        for (int i = 0; i < 56; i++)
        {
            //            u_key_perm[i] = (byte) (KEY_PERM[i] - 1);
            INVERSE_KEY_PERM[KEY_PERM[i] - 1] = (byte) i;
            INVERSE_COMP_PERM[i] = (byte) 255;
        }

        /*
         * Invert the key compression permutation.
         */
        for (int i = 0; i < 48; i++)
        {
            INVERSE_COMP_PERM[COMP_PERM[i] - 1] = (byte) i;
        }

        /*
         * Set up the OR-mask arrays for the initial and final permutations,
         * and for the key initial and compression permutations.
         */
        for (int k = 0; k < 8; k++)
        {
            int inbit = 0;
            int obit = 0;
            for (int i = 0; i < 256; i++)
            {
                IP_MASK_LEFT[k][i] = 0;
                IP_MASK_RIGHT[k][i] = 0;
                FP_MASK_LEFT[k][i] = 0;
                FP_MASK_RIGHT[k][i] = 0;

                for (int j = 0; j < 8; j++)
                {
                    inbit = 8 * k + j;
                    if ((i & BITS8[j]) != 0)
                    {
                        if ((obit = INITIAL_PERM[inbit]) < 32)
                        {
                            IP_MASK_LEFT[k][i] |= BITS32[obit];
                        }
                        else
                        {
                            IP_MASK_RIGHT[k][i] |= BITS32[obit - 32];
                        }
                        if ((obit = FINAL_PERM[inbit]) < 32)
                        {
                            FP_MASK_LEFT[k][i] |= BITS32[obit];
                        }
                        else
                        {
                            FP_MASK_RIGHT[k][i] |= BITS32[obit - 32];
                        }
                    }
                }
            }

            for (int i = 0; i < 128; i++)
            {
                KEY_PERM_MASK_LEFT[k][i] = 0;
                KEY_PERM_MASK_RIGHT[k][i] = 0;

                for (int j = 0; j < 7; j++)
                {
                    inbit = 8 * k + j;
                    if ((i & BITS8[j + 1]) != 0)
                    {
                        if ((obit = INVERSE_KEY_PERM[inbit]) == 255)
                        {
                            continue;
                        }
                        if (obit < 28)
                        {
                            KEY_PERM_MASK_LEFT[k][i] |= BITS28[obit];
                        }
                        else
                        {
                            KEY_PERM_MASK_RIGHT[k][i] |= BITS28[obit - 28];
                        }
                    }
                }

                COMP_MASK_LEFT[k][i] = 0;
                COMP_MASK_RIGHT[k][i] = 0;
                for (int j = 0; j < 7; j++)
                {
                    inbit = 7 * k + j;
                    if ((i & BITS8[j + 1]) != 0)
                    {
                        obit = INVERSE_COMP_PERM[inbit];
                        if (obit == -1)
                        {
                            continue;
                        }
                        if (obit < 24)
                        {
                            COMP_MASK_LEFT[k][i] |= BITS24[obit];
                        }
                        else
                        {
                            COMP_MASK_RIGHT[k][i] |= BITS24[obit - 24];
                        }
                    }
                }
            }
        }

        /*
         * Invert the P-box permutation, and convert into OR-masks for
         * handling the output of the S-box arrays setup above.
         */
        for (int i = 0; i < 32; i++)
        {
            INVERSE_PBOX[PBOX[i] - 1] = (byte) i;
        }

        for (int b = 0; b < 4; b++)
        {
            for (int i = 0; i < 256; i++)
            {
                PSBOX[b][i] = 0;
                for (int j = 0; j < 8; j++)
                {
                    if ((i & BITS8[j]) != 0)
                        PSBOX[b][i] |= BITS32[INVERSE_PBOX[8 * b + j]];
                }
            }
        }

    }

    void setupSalt(long salt)
    {
        if (salt == _oldSalt)
        {
            return;
        }
        _oldSalt = salt;

        _saltbits = 0;
        int saltbit = 1;
        int obit = 0x800000;
        for (int i = 0; i < 24; i++)
        {
            if ((salt & saltbit) != 0)
            {
                _saltbits |= obit;
            }
            saltbit <<= 1;
            obit >>= 1;
        }
    }

    int desSetKey(short[] key)
    {
        int rawkey0 = get32Bits(key, 0);
        int rawkey1 = get32Bits(key, 4);

        if ((rawkey0 | rawkey1) > 0 && rawkey0 == _oldRawkey0 && rawkey1 == _oldRawkey1)
        {
            /*
             * Already setup for this key.
             * This optimisation fails on a zero key (which is weak and
             * has bad parity anyway) in order to simplify the starting
             * conditions.
             */
            return 0;
        }
        _oldRawkey0 = rawkey0;
        _oldRawkey1 = rawkey1;

        /*
         *  Do key permutation and split into two 28-bit subkeys.
         */
        int k0 = KEY_PERM_MASK_LEFT[0][rawkey0 >>> 25]
                | KEY_PERM_MASK_LEFT[1][(rawkey0 >>> 17) & 0x7f]
                | KEY_PERM_MASK_LEFT[2][(rawkey0 >>> 9) & 0x7f]
                | KEY_PERM_MASK_LEFT[3][(rawkey0 >>> 1) & 0x7f]
                | KEY_PERM_MASK_LEFT[4][rawkey1 >>> 25]
                | KEY_PERM_MASK_LEFT[5][(rawkey1 >>> 17) & 0x7f]
                | KEY_PERM_MASK_LEFT[6][(rawkey1 >>> 9) & 0x7f]
                | KEY_PERM_MASK_LEFT[7][(rawkey1 >>> 1) & 0x7f];
        int k1 = KEY_PERM_MASK_RIGHT[0][rawkey0 >>> 25]
                | KEY_PERM_MASK_RIGHT[1][(rawkey0 >>> 17) & 0x7f]
                | KEY_PERM_MASK_RIGHT[2][(rawkey0 >>> 9) & 0x7f]
                | KEY_PERM_MASK_RIGHT[3][(rawkey0 >>> 1) & 0x7f]
                | KEY_PERM_MASK_RIGHT[4][rawkey1 >>> 25]
                | KEY_PERM_MASK_RIGHT[5][(rawkey1 >>> 17) & 0x7f]
                | KEY_PERM_MASK_RIGHT[6][(rawkey1 >>> 9) & 0x7f]
                | KEY_PERM_MASK_RIGHT[7][(rawkey1 >>> 1) & 0x7f];
        /*
         *  Rotate subkeys and do compression permutation.
         */
        int shifts = 0;
        for (int round = 0; round < 16; round++)
        {
            int t0, t1;

            shifts += KEY_SHIFTS[round];

            t0 = (k0 << shifts) | (k0 >> (28 - shifts));
            t1 = (k1 << shifts) | (k1 >> (28 - shifts));

            int i0l = (t0 >> 21) & 0x7f;
            int i1l = (t0 >> 14) & 0x7f;
            int i2l = (t0 >> 7) & 0x7f;
            int i3l = t0 & 0x7f;
            int i4l = (t1 >> 21) & 0x7f;
            int i5l = (t1 >> 14) & 0x7f;
            int i6l = (t1 >> 7) & 0x7f;
            int i7l = t1 & 0x7f;
            int left = COMP_MASK_LEFT[0][i0l]
                    | COMP_MASK_LEFT[1][i1l]
                    | COMP_MASK_LEFT[2][i2l]
                    | COMP_MASK_LEFT[3][i3l]
                    | COMP_MASK_LEFT[4][i4l]
                    | COMP_MASK_LEFT[5][i5l]
                    | COMP_MASK_LEFT[6][i6l]
                    | COMP_MASK_LEFT[7][i7l];

            int right = COMP_MASK_RIGHT[0][i0l]
                    | COMP_MASK_RIGHT[1][i1l]
                    | COMP_MASK_RIGHT[2][i2l]
                    | COMP_MASK_RIGHT[3][i3l]
                    | COMP_MASK_RIGHT[4][i4l]
                    | COMP_MASK_RIGHT[5][i5l]
                    | COMP_MASK_RIGHT[6][i6l]
                    | COMP_MASK_RIGHT[7][i7l];

            _encryptionKeysLeft[round] = left;
            _encryptionKeysRight[round] = right;
        }
        return 0;
    }

    private int get32Bits(short[] key, int index)
    {
        int byte1 = key[index + 0] << 24;
        int byte2 = key[index + 1] << 16;
        int byte3 = key[index + 2] << 8;
        int byte4 = key[index + 3];
        return byte1 | byte2 | byte3 | byte4;
    }

    int doDes(int leftIn, int rightIn, int out[], int count)
    {
        if (count <= 0)
        {
            return 1;
        }

        int l = IP_MASK_LEFT[0][leftIn >> 24]
                | IP_MASK_LEFT[1][(leftIn >> 16) & 0xff]
                | IP_MASK_LEFT[2][(leftIn >> 8) & 0xff]
                | IP_MASK_LEFT[3][leftIn & 0xff]
                | IP_MASK_LEFT[4][rightIn >> 24]
                | IP_MASK_LEFT[5][(rightIn >> 16) & 0xff]
                | IP_MASK_LEFT[6][(rightIn >> 8) & 0xff]
                | IP_MASK_LEFT[7][rightIn & 0xff];

        int r = IP_MASK_RIGHT[0][leftIn >> 24]
                | IP_MASK_RIGHT[1][(leftIn >> 16) & 0xff]
                | IP_MASK_RIGHT[2][(leftIn >> 8) & 0xff]
                | IP_MASK_RIGHT[3][leftIn & 0xff]
                | IP_MASK_RIGHT[4][rightIn >> 24]
                | IP_MASK_RIGHT[5][(rightIn >> 16) & 0xff]
                | IP_MASK_RIGHT[6][(rightIn >> 8) & 0xff]
                | IP_MASK_RIGHT[7][rightIn & 0xff];

        for (int i = 0; i < count; i++)
        {
            int leftIndex = 0;
            int rightIndex = 0;
            int f = 0;
            for (int round = 0; round < 16; round++)
            {
                /*
                 * Expand R to 48 bits (simulate the E-box).
                 */
                int r48l = ((r & 0x00000001) << 23)
                        | ((r & 0xf8000000) >>> 9)
                        | ((r & 0x1f800000) >>> 11)
                        | ((r & 0x01f80000) >>> 13)
                        | ((r & 0x001f8000) >>> 15);

                int r48r = ((r & 0x0001f800) << 7)
                        | ((r & 0x00001f80) << 5)
                        | ((r & 0x000001f8) << 3)
                        | ((r & 0x0000001f) << 1)
                        | ((r & 0x80000000) >>> 31);
                /*
                 * Do salting for crypt() and friends, and
                 * XOR with the permuted key.
                 */

                f = (r48l ^ r48r) & _saltbits;
                r48l ^= f ^ _encryptionKeysLeft[leftIndex++];
                r48r ^= f ^ _encryptionKeysRight[rightIndex++];

                /*
                 * Do sbox lookups (which shrink it back to 32 bits)
                 * and do the pbox permutation at the same time.
                 */
                int s0 = r48l >>> 12;
                int s1 = r48l & 0xfff;
                int s2 = r48r >>> 12;
                int s3 = r48r & 0xfff;

                short i1 = M_SBOX[0][s0];
                short i2 = M_SBOX[1][s1];
                short i3 = M_SBOX[2][s2];
                short i4 = M_SBOX[3][s3];
                f = PSBOX[0][i1] | PSBOX[1][i2] | PSBOX[2][i3] | PSBOX[3][i4];
                /*
                 * Now that we've permuted things, complete f().
                 */
                f ^= l;
                l = r;
                r = f;
            }
            r = l;
            l = f;
        }
        /*
         * Do final permutation (inverse of IP).
         */
        out[0] = FP_MASK_LEFT[0][l >>> 24]
                | FP_MASK_LEFT[1][(l >>> 16) & 0xff]
                | FP_MASK_LEFT[2][(l >>> 8) & 0xff]
                | FP_MASK_LEFT[3][l & 0xff]
                | FP_MASK_LEFT[4][r >>> 24]
                | FP_MASK_LEFT[5][(r >>> 16) & 0xff]
                | FP_MASK_LEFT[6][(r >>> 8) & 0xff]
                | FP_MASK_LEFT[7][r & 0xff];
        out[1] = FP_MASK_RIGHT[0][l >>> 24]
                | FP_MASK_RIGHT[1][(l >>> 16) & 0xff]
                | FP_MASK_RIGHT[2][(l >>> 8) & 0xff]
                | FP_MASK_RIGHT[3][l & 0xff]
                | FP_MASK_RIGHT[4][r >>> 24]
                | FP_MASK_RIGHT[5][(r >>> 16) & 0xff]
                | FP_MASK_RIGHT[6][(r >>> 8) & 0xff]
                | FP_MASK_RIGHT[7][r & 0xff];
        return 0;
    }

    private String cryptDES(byte key[], byte setting[])
    {
        short keybuf[] = new short[8];
        for (int i = 0; i < 8 && i < key.length; i++)
        {
            keybuf[i] = (short) (key[i] << 1);
        }

        if (desSetKey(keybuf) != 0)
        {
            return null;
        }

        int salt = (asciiToBinary(setting[1]) << 6) | asciiToBinary(setting[0]);
        setupSalt(salt);

        int count = 25;
        int r[] = new int[2];
        if (doDes(0, 0, r, count) != 0)
        {
            return null;
        }

        byte output[] = new byte[16];
        output[0] = setting[0];
        output[1] = setting[1] == 0 ? setting[0] : setting[1];
        int p = 2;

        int l = (r[0] >> 8);
        output[p++] = ASCII64[(l >> 18) & 0x3f];
        output[p++] = ASCII64[(l >> 12) & 0x3f];
        output[p++] = ASCII64[(l >> 6) & 0x3f];
        output[p++] = ASCII64[l & 0x3f];

        l = (r[0] << 16) | ((r[1] >> 16) & 0xffff);
        output[p++] = ASCII64[(l >> 18) & 0x3f];
        output[p++] = ASCII64[(l >> 12) & 0x3f];
        output[p++] = ASCII64[(l >> 6) & 0x3f];
        output[p++] = ASCII64[l & 0x3f];

        l = r[1] << 2;
        output[p++] = ASCII64[(l >> 12) & 0x3f];
        output[p++] = ASCII64[(l >> 6) & 0x3f];
        output[p++] = ASCII64[l & 0x3f];
        output[p] = 0;

        return new String(output, 0, p);
    }

    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.err.println("Usage: " + Crypt.class.getName() + " <password> <salt>");
            return;
        }

        String p = args[0];
        String s = args[1];
        CharSequence c = new Crypt().crypt(p, s);
        System.out.println("crypt( " + p + " , " + s + " ) = " + c + "\n");
        return;
    }

    public String crypt(String password, String salt)
    {
        return cryptDES(password.getBytes(), salt.getBytes());
    }

}
