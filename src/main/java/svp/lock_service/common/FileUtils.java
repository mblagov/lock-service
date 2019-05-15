package svp.lock_service.common;

import svp.lock_service.models.BaseResponse;

import java.io.File;

public class FileUtils {

    public static boolean isFileExists(String pathFile) {
        return new File(pathFile).exists();
    }
}
