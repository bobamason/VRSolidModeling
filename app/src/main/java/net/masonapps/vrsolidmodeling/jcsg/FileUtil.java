/**
 * FileUtil.java
 * <p>
 * Copyright 2014-2014 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Michael Hoffer <info@michaelhoffer.de>.
 */

package net.masonapps.vrsolidmodeling.jcsg;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * File util class.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class FileUtil {

    private FileUtil() {
        throw new AssertionError("Don't instantiate me", null);
    }

    /**
     * Writes the specified string to a file.
     *
     * @param p file destination (existing files will be overwritten)
     * @param s string to save
     * @throws IOException if writing to file fails
     */
    public static void write(Path p, String s) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(p, Charset.forName("UTF-8"),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write(s, 0, s.length());
        }
    }

    /**
     * Reads the specified file to a string.
     *
     * @param p file to read
     * @return the content of the file
     * @throws IOException if reading from file failed
     */
    public static String read(Path p) throws IOException {
        return new String(Files.readAllBytes(p), Charset.forName("UTF-8"));
    }


    /**
     * Saves the specified csg using STL ASCII format.
     *
     * @param path destination path
     * @param csg  csg to save
     * @throws IOException
     */
    public static void toStlFile(Path path, CSG csg) throws IOException {
        try (BufferedWriter out = Files.newBufferedWriter(path, Charset.forName("UTF-8"),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            out.append("solid v3d.csg\n");
            csg.getPolygons().stream().forEach(
                    (Polygon p) -> {
                        try {
                            out.append(p.toStlString());
                        } catch (IOException ex) {
                            Logger.getLogger(CSG.class.getName()).log(Level.SEVERE, null, ex);
                            throw new RuntimeException(ex);
                        }
                    });
            out.append("endsolid v3d.csg\n");
        }
    }
}
