package com;

import org.slf4j.Logger;

public class ByteClassLoader extends ClassLoader {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ByteClassLoader.class);

    private final byte[] classData;

    public ByteClassLoader(byte[] classData) {
        this.classData = classData;
        super(ClassLoader.getSystemClassLoader());
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> loadedClass = findLoadedClass(name);

        if (loadedClass == null) {
            try {
                return defineClass(name, classData, 0, classData.length);
            } catch (ClassFormatError error) {
                logger.error("ClassFormatError while defining class {}: {}", name, error.getMessage());
                return loadClass(name);
            }

        }
        return loadedClass;
    }
}
