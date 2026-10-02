/*
 * Copyright 2024-present original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.matrei.grailsplugin.csrf

import java.nio.charset.StandardCharsets
import java.security.SecureRandom

import groovy.transform.CompileStatic

/**
 * Masks CSRF tokens rendered in pages, to protect them against BREACH attacks.
 *
 * A masked token is the Base64 (URL-safe) encoding of random bytes followed by the token
 * XOR-ed with those bytes. Every masking uses new random bytes, so a token rendered in
 * a compressed response is different every time, and cannot be recovered by observing
 * the size of responses.
 *
 * @author Mattias Reichel
 * @since 3.0.0
 */
@CompileStatic
final class CsrfTokenMasking {

    private static final SecureRandom RANDOM = new SecureRandom()
    private static final Base64.Encoder ENCODER = Base64.urlEncoder.withoutPadding()
    private static final Base64.Decoder DECODER = Base64.urlDecoder

    private CsrfTokenMasking() {}

    /**
     * Masks a token with new random bytes.
     *
     * @param token The token to mask
     * @return the masked token
     */
    static String mask(String token) {
        var tokenBytes = token.getBytes(StandardCharsets.UTF_8)
        var randomBytes = new byte[tokenBytes.length]
        RANDOM.nextBytes(randomBytes)
        var maskedBytes = new byte[tokenBytes.length * 2]
        for (int i = 0; i < tokenBytes.length; i++) {
            maskedBytes[i] = randomBytes[i]
            maskedBytes[tokenBytes.length + i] = (byte) (randomBytes[i] ^ tokenBytes[i])
        }
        ENCODER.encodeToString(maskedBytes)
    }

    /**
     * Unmasks a masked token.
     *
     * @param maskedToken The masked token
     * @return the token, or null if the value is not a masked token
     */
    static String unmask(String maskedToken) {
        byte[] maskedBytes
        try {
            maskedBytes = DECODER.decode(maskedToken)
        } catch (IllegalArgumentException ignored) {
            return null
        }
        if (maskedBytes.length == 0 || maskedBytes.length % 2 != 0) {
            return null
        }
        var size = maskedBytes.length.intdiv(2) as int
        var tokenBytes = new byte[size]
        for (int i = 0; i < size; i++) {
            tokenBytes[i] = (byte) (maskedBytes[i] ^ maskedBytes[size + i])
        }
        return new String(tokenBytes, StandardCharsets.UTF_8)
    }
}
