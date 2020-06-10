package br.com.utfpr.gerenciamento.server.util;

import java.io.File;

public class FileUtil {

    public static String getAbsolutePathRaiz() {
        String pathRaiz = new File(System.getProperty("user.dir")).getAbsolutePath();
        if (pathRaiz.contains("bin")) {
            pathRaiz = pathRaiz.replace("bin", "");
        }
        return pathRaiz;
    }

}
