package com.izettle.java;

import static com.izettle.java.ValueChecks.empty;
import static java.lang.String.format;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class ResourceUtils {

    private ResourceUtils() {
    }

    /**
     * Will return a byte array of the resource
     * Will look in all classloaders
     *
     * @param resourceName The file name of the resource
     * @return byte array representation
     * @throws IOException On resource not found
     */
    public static byte[] getResourceAsBytes(String resourceName)
        throws IOException {
        ResourceLoader resourceLoader = new ResourceLoader();
        return resourceLoader.getBytesFromResource(resourceName);
    }

    /**
     * Will return a byte array of the resource
     * Will use {@code contextClass}'s classloader
     * @param contextClass The class to use for class loading
     * @param resourceName The file name of the resource
     * @return byte array representation
     * @throws IOException On resource not found
     */
    public static byte[] getResourceAsBytes(Class<?> contextClass, String resourceName)
        throws IOException {
        return getBytesFromStream(getResource(contextClass, resourceName).openStream());
    }

    /**
     * Get an input stream to a resource. Be sure to close it
     * Will look in all classloaders
     *
     * @param resourceName The file name of the resource
     * @return In input stream of the resource
     * @throws IOException On not found
     */
    public static InputStream getResourceAsStream(String resourceName) throws IOException {
        ResourceLoader resourceLoader = new ResourceLoader();
        return resourceLoader.getInputStream(resourceName);
    }

    /**
     * Get an input stream to a resource. Be sure to close it
     * Will use {@code contextClass}'s classloader
     * @param contextClass The class to use for class loading
     * @param resourceName The file name of the resource
     * @return In input stream of the resource
     * @throws IOException On not found
     */
    public static InputStream getResourceAsStream(Class<?> contextClass, String resourceName) throws IOException {
        return getResource(contextClass, resourceName).openStream();
    }

    private static class ResourceLoader {

        private byte[] getBytesFromResource(String resourceName) throws IOException {
            try (InputStream inputStream = getInputStream(resourceName)) {
                return getBytesFromStream(inputStream);
            }
        }

        public InputStream getInputStream(String resourceName) throws IOException {
            final ClassLoader[] classLoaders = new ClassLoader[]{
                getClass().getClassLoader(),
                ResourceUtils.class.getClassLoader(),
                Thread.currentThread().getContextClassLoader(),
                ClassLoader.getSystemClassLoader()
            };
            resourceName = removePreSlash(resourceName);
            URL resource = null;
            for (ClassLoader classLoader : classLoaders) {
                resource = classLoader.getResource(resourceName);

                if (resource != null) {
                    break;
                }
                resource = classLoader.getResource("/" + resourceName);
                if (resource != null) {
                    break;
                }
            }
            if (resource == null) {
                throw new IOException("Could not find resource: " + resourceName);
            }
            return resource.openStream();
        }

        private static String removePreSlash(String resourceName) {
            if (resourceName.charAt(0) == '/') {
                return resourceName.substring(1);
            }
            return resourceName;
        }
    }

    public static byte[] getBytesFromStream(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            return outputStream.toByteArray();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /**
     * Returns a {@code URL} pointing to {@code resourceName} if the resource is
     * found in the class path. {@code Resources.class.getClassLoader()} is used
     * to locate the resource.
     *
     * @throws NullPointerException if resource is not found
     */

    public static URL getResource(String resourceName) {
        URL url = ResourceUtils.class.getClassLoader().getResource(resourceName);
        requireNonNull(url, format("resource %s not found.", resourceName));
        return url;
    }

    /**
     * Returns a {@code URL} pointing to {@code resourceName} that is relative to
     * {@code contextClass}, if the resource is found in the class path.
     *
     * @throws NullPointerException if resource is not found
     */
    public static URL getResource(Class<?> contextClass, String resourceName) {
        URL url = contextClass.getResource(resourceName);

        requireNonNull(url, format("resource %s relative to %s not found.", resourceName, contextClass.getName()));
        return url;
    }

    /**
     * Checks that the specified object reference is not {@code null} and
     * throws a customized {@link NullPointerException} if it is. This method
     * is designed primarily for doing parameter validation in methods and
     * constructors with multiple parameters, as demonstrated below:
     * <blockquote><pre>
     * public Foo(Bar bar, Baz baz) {
     *     this.bar = Objects.requireNonNull(bar, "bar must not be null");
     *     this.baz = Objects.requireNonNull(baz, "baz must not be null");
     * }
     * </pre></blockquote>
     *
     * @param obj     the object reference to check for nullity
     * @param message detail message to be used in the event that a {@code
     *                NullPointerException} is thrown
     * @param <T> the type of the reference
     * @return {@code obj} if not {@code null}
     * @throws NullPointerException if {@code obj} is {@code null}
     */
    private static <T> T requireNonNull(T obj, String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }
        return obj;
    }

    /**
     * Lists all file names in a resource directory.
     *
     * @param clazz A class whose location is relative to where the resourceloader should
     *              look for resource files.
     * @param path The resource path (directory) where to look for files.
     * @return List of relative file names in the specified resource directory.
     * @throws URISyntaxException
     * @throws IOException
     */
    // Heavily influenced from http://stackoverflow.com/questions/6247144/how-to-load-a-folder-from-a-jar
    public static String[] getResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
        URL dirURL = clazz.getClassLoader().getResource(path);
        if (dirURL != null && "file".equals(dirURL.getProtocol())) {
            /* A file path: easy enough */
            return new File(dirURL.toURI()).list();
        }

        if (dirURL == null) {
            /*
             * In case of a jar file, we can't actually find a directory.
             * Have to assume the same jar as clazz.
             */
            String me = clazz.getName().replace(".", "/") + ".class";
            dirURL = clazz.getClassLoader().getResource(me);
        }

        if (dirURL != null && "jar".equals(dirURL.getProtocol())) {
            /* A JAR path */
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
            try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))) {
                Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
                Set<String> result = new HashSet<>(); //avoid duplicates in case it is a subdirectory
                while (entries.hasMoreElements()) {
                    String name = entries.nextElement().getName();
                    if (name.startsWith(path)) { //filter according to the path
                        String filename = name.substring(path.length());
                        while (filename.startsWith("/")) {
                            filename = filename.substring(1);
                        }
                        if (empty(filename)) {
                            continue;
                        }
                        result.add(filename);
                    }
                }
                return result.toArray(new String[result.size()]);
            }
        }

        throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
    }
}
