package com.izettle.java;

/**
 *  Helper methods to check input HTTPRequest values for use in HTTPResponse
 */
public class HttpRequestValidatingHelper {

    /**
     * Guard against HTTP Response Splitting attack - throw exception if /r or /n found in value to be used in a header
     * Info:
     * Ref: http://projects.webappsec.org/HTTP-Response-Splitting
     * Intro to response splitting: http://www.securiteam.com/securityreviews/5WP0E2KFGK.html
     * OWASP response splitting page: https://www.owasp.org/index.php/HTTP_Response_Splitting
     * How to Test: https://www.owasp.org/index.php/Testing_for_HTTP_Splitting/Smuggling_(OTG-INPVAL-016)
     * Wikipedia Response Splitting page: https://en.wikipedia.org/wiki/HTTP_response_splitting
     *
     * @param input String (found from a HTTPRequest)
     * @return the input String unchanged (for use in HTTPReponse)
     * @throws IllegalArgumentException if a CR/LF found in the string
     */
    public static String checkValueForNewline(String input) throws IllegalArgumentException {
        if (input == null || input.length() == 0) {
            return "";
        }
        if (input.indexOf('\r') > 0 || input.indexOf('\n') > 0) {
            throw new IllegalArgumentException("CR and/or LF found in param to be used in response header");
        }
        return input;
    }
}
